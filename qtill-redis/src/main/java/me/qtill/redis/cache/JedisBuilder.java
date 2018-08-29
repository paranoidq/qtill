package me.qtill.redis.cache;

import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.List;
import java.util.Set;

/**
 * Jedis builder构造器
 *
 * @author paranoidq
 * @since 1.0.0
 */
class JedisBuilder {

    // 主机名
    private String       host                          = "localhost";
    private int          port                          = 6379;
    private String       password;
    private int          maxActive                     = 8;
    private int          maxIdle                       = 8;
    private long         maxWaitMillis                 = 5000;
    private boolean      testWhileIdle                 = true;
    private long         timeBetweenEvictionRunsMillis = 5000;
    private long         minEvictableIdleTimeMillis    = 10000;
    private int          timeout                       = 5;
    private int          soTimeout                     = 5;
    private int          maxAttempts                   = 10;
    private List<String> clusterNodes;


    private JedisBuilder() {
    }

    public static JedisBuilder newInstance() {
        return new JedisBuilder();
    }


    /**
     * 构建{@link JedisCluster}实例
     *
     * @return
     */
    public JedisCluster buildJedisCluster() {
        JedisPoolConfig jedisPoolConfig = configJedisPool();
        Set<HostAndPort> hostAndPorts = resolveClusterNodes(clusterNodes);
        JedisCluster jedisCluster;
        if (StringUtils.isEmpty(password)) {
            jedisCluster = new JedisCluster(hostAndPorts, timeout, soTimeout, maxAttempts, jedisPoolConfig);
        } else {
            jedisCluster = new JedisCluster(hostAndPorts, timeout, soTimeout, maxAttempts, password, jedisPoolConfig);
        }
        return jedisCluster;
    }

    /**
     * 构造{@link JedisPool}实例
     *
     * @return
     */
    public JedisPool buildJedisPool() {
        JedisPoolConfig jedisPoolConfig = configJedisPool();
        JedisPool jedisPool;
        if (StringUtils.isEmpty(password)) {
            jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout);
        } else {
            jedisPool = new JedisPool(jedisPoolConfig, host, port, timeout, password);
        }
        return jedisPool;
    }


    /**
     * 是否开启cluster模式
     *
     * @return
     */
    public boolean isClusterMode() {
        return clusterNodes != null && !clusterNodes.isEmpty();
    }


    /**
     * 配置jedisPool
     *
     * @return
     */
    private JedisPoolConfig configJedisPool() {
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

        // 配置jedis线程池数量
        jedisPoolConfig.setMaxTotal(maxActive);
        jedisPoolConfig.setMaxIdle(maxIdle);
        jedisPoolConfig.setMaxWaitMillis(maxWaitMillis);

        // 配置jedis线程池idle eviction
        jedisPoolConfig.setTestWhileIdle(testWhileIdle);
        jedisPoolConfig.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        jedisPoolConfig.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        return jedisPoolConfig;
    }


    /**
     * 解析clusterNodes字符串
     * 格式：host:port
     *
     * @param clusterNodes
     * @return
     */
    private Set<HostAndPort> resolveClusterNodes(List<String> clusterNodes) {
        Set<HostAndPort> hostAndPorts = Sets.newHashSet();
        for (String nodeString : clusterNodes) {
            String[] entry = nodeString.split(":");
            hostAndPorts.add(new HostAndPort(entry[0], Integer.parseInt(entry[1])));
        }
        return hostAndPorts;
    }

    public JedisBuilder clusterNodes(List<String> clusterNodes) {
        this.clusterNodes = clusterNodes;
        return this;
    }

    public JedisBuilder host(String host) {
        this.host = host;
        return this;
    }

    public JedisBuilder port(int port) {
        this.port = port;
        return this;
    }

    public JedisBuilder maxActive(int maxActive) {
        if (maxActive > 0) {
            this.maxActive = maxActive;
        }
        return this;
    }

    public JedisBuilder maxIdle(int maxIdle) {
        if (maxIdle > 0 && maxIdle <= maxActive) {
            this.maxIdle = maxIdle;
        }
        return this;
    }

    public JedisBuilder maxWaitMillis(long maxWaitMillis) {
        if (maxWaitMillis > 0) {
            this.maxWaitMillis = maxWaitMillis;
        }
        return this;
    }

    public JedisBuilder timeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        if (timeBetweenEvictionRunsMillis > 0) {
            this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
        }
        return this;
    }

    public JedisBuilder minEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        if (minEvictableIdleTimeMillis > 0) {
            this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
        }
        return this;
    }

    public JedisBuilder testWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
        return this;
    }

    public JedisBuilder soTimeout(int soTimeout) {
        this.soTimeout = soTimeout;
        return this;
    }

    public JedisBuilder maxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        return this;
    }

}

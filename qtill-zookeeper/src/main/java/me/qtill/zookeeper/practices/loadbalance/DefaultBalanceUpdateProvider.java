package me.qtill.zookeeper.practices.loadbalance;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkBadVersionException;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * 尤其要注意更新balance时的一致性问题
 *
 * TODO: 这里是否意味着，多个请求到达zk，能否按照顺序执行？还是多线程并行执行？
 *
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class DefaultBalanceUpdateProvider implements BalanceUpdateProvider {

    private String   serverPath;
    private ZkClient zkClient;

    public DefaultBalanceUpdateProvider(String serverPath, ZkClient zkClient) {
        this.serverPath = serverPath;
        this.zkClient = zkClient;
    }

    @Override
    public boolean addBalance(Integer step) {
        Stat stat = new Stat();
        ServerData sd;

        while (true) {
            try {
                sd = zkClient.readData(this.serverPath, stat);
                sd.setBalance(sd.getBalance() + step);

                // 这里存在一致性问题，需要带上版本号，利用CAS类似的操作确保成功
                // !!! 带上版本，因为可能有其他客户端连接到服务器修改了负载
                zkClient.writeData(this.serverPath, sd, stat.getVersion());
                return true;
            } catch (ZkBadVersionException e) {
                System.out.println("concurrent modify balance, retry");
            } catch (Exception e) {
                return false;
            }

            ZooKeeper zooKeeper;
        }
    }

    @Override
    public boolean reduceBalance(Integer step) {
        Stat stat = new Stat();
        ServerData sd;

        while (true) {
            try {
                sd = zkClient.readData(serverPath, stat);

                // [优化]
                // 这里可以直接判断sd小于等于0就return了，不会导致一致性问题
                // 1. 在这期间没有client连接，已经小于等于0了，没必要再次执行writeData操作
                // 2. 在这期间有client连接，version改变了，writeData执行不会成功
                // 综上，这里可以判断小于等于0，就直接return，没必要执行多余的writeData
                if (sd.getBalance() <= 0) {
                    return true;
                }

                final Integer curBalance = sd.getBalance();
                // 考虑边界情况
                sd.setBalance(curBalance > step ? curBalance - step : 0);

                // 同样，带上版本号
                zkClient.writeData(this.serverPath, sd, stat.getVersion());

                return true;
            } catch (ZkBadVersionException e) {
                System.out.println("concurrent modify balance, retry");
            } catch (Exception e) {
                return false;
            }
        }
    }
}

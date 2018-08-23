package me.qtill.ehcache.tryit;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.EvictionAdvisor;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.EntryUnit;
import org.ehcache.config.units.MemoryUnit;

/**
 * @author paranoidq
 * @since 1.0.0
 */
public class Try {


    public static void main(String[] args) {

        // 构造
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
            .withCache("preConfigured",
                CacheConfigurationBuilder.newCacheConfigurationBuilder(Long.class, String.class, ResourcePoolsBuilder.heap(10))
                    .withEvictionAdvisor(new EvictionAdvisor<Long, String>() {
                        // no eviction
                        @Override
                        public boolean adviseAgainstEviction(Long key, String value) {
                            return true;
                        }
                    }))
            .build();
        cacheManager.init();

        Cache<Long, String> cache = cacheManager.getCache("preConfigured", Long.class, String.class);
        cache.put(1L, "aa");
        String v = cache.get(1L);
        System.out.println(v);

        cacheManager.removeCache("preConfigured");
        cacheManager.close();


        // 持久化
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
            .with(CacheManagerBuilder.persistence("/Users/paranoidq/git-repo/qtill/qtill-ehcache/data"))
            .withCache("persistence", CacheConfigurationBuilder.newCacheConfigurationBuilder(
                Long.class,
                String.class,
                ResourcePoolsBuilder.newResourcePoolsBuilder()
                    .heap(10, EntryUnit.ENTRIES)
                    .disk(10, MemoryUnit.MB, true)))
            .build();


    }
}

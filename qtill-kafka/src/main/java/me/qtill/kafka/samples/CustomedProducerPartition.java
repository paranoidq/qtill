package me.qtill.kafka.samples;

import org.apache.kafka.clients.producer.Partitioner;
import org.apache.kafka.common.Cluster;
import org.apache.kafka.common.PartitionInfo;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * 自定义的根据key确定producer将数据写入哪个分区
 *
 * @author paranoidq
 * @since 1.0.0
 */
public class CustomedProducerPartition implements Partitioner {

    @Override
    public int partition(String topic, Object key, byte[] keyBytes, Object value, byte[] valueBytes, Cluster cluster) {
        // 通过cluster获取partition相关信息
        List<PartitionInfo> partitionInfoList = cluster.partitionsForTopic(topic);
        int partitionNum = partitionInfoList.size();


        Random random = new Random();
        int selectedPartition = random.nextInt(partitionNum);
        return selectedPartition;
    }

    @Override
    public void close() {

    }

    @Override
    public void configure(Map<String, ?> configs) {

    }
}

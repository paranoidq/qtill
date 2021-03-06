package me.qtill.commons.collection;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;

import com.google.common.collect.EvictingQueue;

/**
 * 特殊类型Queue:LIFO的Stack, LRU的Queue
 */
public class MoreQueues {

	//////////////// 特殊类型Queue：Stack ///////////

	/**
	 * 支持后进先出的栈，用ArrayDeque实现, 经过Collections#asLifoQueue()转换顺序
	 * 
	 * 需设置初始长度，默认为16，数组满时成倍扩容
     * 返回Collections内部实现的{@code AsLIFOQueue}实例
	 * 
	 * @see Collections#asLifoQueue()
	 */
	public static <E> Queue<E> createStack(int initSize) {
		return Collections.asLifoQueue(new ArrayDeque<E>(initSize));
	}

	/**
	 * 支持后进先出的无阻塞的并发栈，用ConcurrentLinkedDeque实现，经过Collections#asLifoQueue()转换顺序
	 * 
	 * 另对于BlockingQueue接口， JDK暂无Lifo倒转实现，因此只能直接使用未调转顺序的LinkedBlockingDeque
	 * 
	 * @see Collections#asLifoQueue()
	 */
	public static <E> Queue<E> createConcurrentStack() {
		return (Queue<E>) Collections.asLifoQueue(QueueUtil.newConcurrentNonBlockingDequeUnlimit());
	}

	//////////////// 特殊类型Queue：LRUQueue ///////////

	/**
	 * LRUQueue, 如果Queue已满，则删除最旧的元素（head）.
     * LRUQueue<tt>没有</tt>以下特性：最近访问元素移到队尾（最新）
     *
	 * 内部实现是ArrayDeque
     * Queue符合FIFO特性，所以LRU移除的方式就是删除head元素。所以Queue本质上并不涉及LRU元素根据访问排序的问题
	 */
	public static <E> EvictingQueue<E> createLRUQueue(int maxSize) {
		return EvictingQueue.create(maxSize);
	}

}
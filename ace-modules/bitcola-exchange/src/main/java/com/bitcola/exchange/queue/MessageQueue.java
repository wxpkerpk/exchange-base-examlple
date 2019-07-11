package com.bitcola.exchange.queue;



import com.bitcola.exchange.common.RunnableResource;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Wrapper of BlockingQueue to send message.
 * 
 * @author liaoxuefeng
 * 
 * @param <T>
 *            Type of message bean.
 */
public class MessageQueue<T> implements RunnableResource {

	// counter:
	private final AtomicLong count = new AtomicLong(0);
	private final int maxSize;
	private BlockingQueue<T> queue;

	public MessageQueue(int maxSize) {
		this.maxSize = maxSize;
		this.queue = new ArrayBlockingQueue<>(maxSize);
	}

	public void put(T t) {
		try {
			queue.put(t);
			count.incrementAndGet();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Retrieves and removes the message of this queue, waiting if necessary
	 * until an element becomes available.
	 * 
	 * @return the message of this queue.
	 * @throws InterruptedException
	 *             if interrupted while waiting
	 */
	public T get() throws InterruptedException {
		return queue.take();
	}

	/**
	 * Retrieves and removes the message of this queue, waiting up to the
	 * specified wait time if necessary for an element to become available.
	 * 
	 * @param timeoutInMillis
	 * @return the message of this queue, or null if the specified waiting time
	 *         elapses before an element is available
	 * @throws InterruptedException
	 *             if interrupted while waiting
	 */
	public T getMessage(long timeoutInMillis) throws InterruptedException {
		return queue.poll(timeoutInMillis, TimeUnit.MILLISECONDS);
	}

	public int getMaxSize() {
		return this.maxSize;
	}

	public int size() {
		return this.queue.size();
	}

	public long totalMessages() {
		return count.get();
	}

	@Override
	public synchronized void start() {
	}

	@Override
	public synchronized void shutdown() {
		for (int i = 0; i < 100; i++) {
			if (queue.size() == 0) {
				queue = null;
				return;
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
			}
		}
		throw new IllegalStateException("Queue still holds " + queue.size() + " message(s).");
	}
}

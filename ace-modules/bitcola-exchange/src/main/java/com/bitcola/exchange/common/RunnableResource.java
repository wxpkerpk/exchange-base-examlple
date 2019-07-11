package com.bitcola.exchange.common;

public interface RunnableResource {

	/**
	 * Start this resource.
	 */
	void start();

	/**
	 * Safely close resource.
	 */
	void shutdown();
}

package com.bitcola.exchange.bitcolapush.util.sendcloud.sdk.model;


import com.bitcola.exchange.bitcolapush.util.sendcloud.sdk.exception.ReceiverException;

public interface Receiver {
	public boolean useAddressList();
	
	public boolean validate() throws ReceiverException;
	
	public String toString();
}
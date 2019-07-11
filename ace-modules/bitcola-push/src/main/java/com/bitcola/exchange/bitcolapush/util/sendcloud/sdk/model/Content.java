package com.bitcola.exchange.bitcolapush.util.sendcloud.sdk.model;


import com.bitcola.exchange.bitcolapush.util.sendcloud.sdk.exception.ContentException;

public interface Content {
	public boolean useTemplate();

	public boolean validate() throws ContentException;
}
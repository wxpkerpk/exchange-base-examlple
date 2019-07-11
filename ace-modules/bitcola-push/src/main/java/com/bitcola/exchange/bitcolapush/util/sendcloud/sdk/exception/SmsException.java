package com.bitcola.exchange.bitcolapush.util.sendcloud.sdk.exception;

public class SmsException extends Throwable implements SCException {
	private static final long serialVersionUID = 1L;
	private static final int errorCode = 401;

	public SmsException(String message) {
		super(message);
	}

	public String getMessage() {
		return String.format("code:%d,message:%s", errorCode, super.getMessage());
	}
}

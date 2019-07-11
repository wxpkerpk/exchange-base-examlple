package com.bitcola.exchange.bitcolapush.util.sendcloud.sdk.model;


import com.bitcola.exchange.bitcolapush.util.sendcloud.sdk.config.Config;
import com.bitcola.exchange.bitcolapush.util.sendcloud.sdk.exception.ReceiverException;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 邮件列表收件人
 * 
 * @author SendCloud
 *
 */
public class AddressListReceiver implements Receiver {

	public boolean useAddressList() {
		return true;
	}

	/**
	 * 地址列表
	 */
	private List<String> invokeNames = new ArrayList<String>();

	public List<String> getInvokeNames() {
		return invokeNames;
	}

	/**
	 * 增加地址列表的调用名称
	 * 
	 * @param to
	 */
	public void addTo(String to) {
		invokeNames.addAll(Arrays.asList(to.split(";")));
	}

	public boolean validate() throws ReceiverException {
		if (CollectionUtils.isEmpty(invokeNames))
			throw new ReceiverException("地址列表为空");
		if (invokeNames.size() > Config.MAX_MAILLIST)
			throw new ReceiverException("地址列表超过上限");
		return true;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String address : invokeNames) {
			if (sb.length() > 0)
				sb.append(";");
			sb.append(address);
		}
		return sb.toString();
	}
}
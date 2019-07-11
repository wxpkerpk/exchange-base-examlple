package com.bitcola.exchange.bitcolapush.util.sendcloud.sdk.demo.mail;


import com.bitcola.exchange.bitcolapush.util.sendcloud.sdk.builder.SendCloudBuilder;
import com.bitcola.exchange.bitcolapush.util.sendcloud.sdk.core.SendCloud;
import com.bitcola.exchange.bitcolapush.util.sendcloud.sdk.exception.VoiceException;
import com.bitcola.exchange.bitcolapush.util.sendcloud.sdk.model.SendCloudVoice;
import com.bitcola.exchange.bitcolapush.util.sendcloud.sdk.util.ResponseData;

import java.io.IOException;
import java.text.ParseException;

public class SendVoice {

	public static void send() throws ParseException, IOException, VoiceException {
		SendCloudVoice voice = new SendCloudVoice();
		voice.setCode("123456");
		voice.setPhone("12345678910;12345678911");

		SendCloud sc = SendCloudBuilder.build();
		ResponseData res = sc.sendVoice(voice);

		System.out.println(res.getResult());
		System.out.println(res.getStatusCode());
		System.out.println(res.getMessage());
		System.out.println(res.getInfo());
	}

	public static void main(String[] args) throws Throwable {
		send();
	}
}

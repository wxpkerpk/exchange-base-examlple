package com.bitcola.chain.util;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.net.NetworkInterface;
import java.util.Enumeration;


public class NetUtil {
    private static ArrayList<String> getLocalIpList() {
        ArrayList<String> ipList = new ArrayList<String>();
        try {
            Enumeration interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) interfaces.nextElement();
                Enumeration ipAddrEnum = ni.getInetAddresses();
                while (ipAddrEnum.hasMoreElements()) {
                    InetAddress addr = (InetAddress) ipAddrEnum.nextElement();
                    if (addr.isLoopbackAddress() == true) {
                        continue;
                    }
                    String ip = addr.getHostAddress();
                    if (ip.indexOf(":") != -1) {
                        continue;
                    }
                    ipList.add(ip);
                }
            }
            Collections.sort(ipList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ipList;
    }

    public static String getLocalIpAddress() {
        ArrayList<String> addressList = getLocalIpList();
        for (String ip : addressList) {
            if (ip.startsWith("192")){
                return ip;
            }
        }
        return "127.0.0.1";
    }


}
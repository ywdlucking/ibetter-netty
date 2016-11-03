package com.ywd.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LocalIp {

	public static Log log = LogFactory.getLog(LocalIp.class);
	public static String ip;

	static {
		try {
			List<String> sip = new ArrayList<String>();
			try {
				for (Enumeration<NetworkInterface> en = NetworkInterface
						.getNetworkInterfaces(); en.hasMoreElements();) {
					NetworkInterface intf = en.nextElement();
					for (Enumeration<InetAddress> enumIpAddr = intf
							.getInetAddresses(); enumIpAddr.hasMoreElements();) {
						InetAddress inetAddress = enumIpAddr.nextElement();
						if (!inetAddress.isLoopbackAddress()
								&& !inetAddress.isLinkLocalAddress()
								&& inetAddress.isSiteLocalAddress()) {
							sip.add(inetAddress.getHostAddress().toString());
						}
					}
				}
			} catch (SocketException ex) {
				log.error("获取本机ip失败",ex);
			}
			if (sip.size() > 0) {
				ip = sip.get(0);
			}
			log.info("本机Ip: " + ip);
		} catch (Exception e) {
			log.error("获取本机ip失败",e);
		}
	}
}

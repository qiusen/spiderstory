package com.dihaitech.spiderstory;

import java.io.BufferedReader;
import java.io.IOException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.DefaultProxyRoutePlanner;
import org.apache.http.util.EntityUtils;

public class Vote {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

//		String[] ips = new String[] { "120.202.249.233", "120.202.249.232",
//				"221.180.147.30", "111.13.109.26", "111.13.2.130",
//				"111.13.109.51", "111.1.36.26", "111.1.36.22", "183.224.1.56",
//				"119.147.115.30", "221.180.147.30", "221.180.147.30",
//				"221.180.147.30", "115.236.59.194", "116.236.203.238",
//				"111.1.32.28", "117.25.129.238", "211.138.121.36",
//				"211.167.105.74", "221.180.147.30", "111.1.36.23",
//				"111.1.36.26", "111.13.87.173", "111.13.109.26",
//				"111.1.36.164", "60.55.43.74", "61.234.123.64", "111.1.36.23",
//				"211.138.121.38", "218.240.131.12", "111.1.36.25",
//				"115.29.76.112", "115.29.76.112", "60.194.100.51",
//				"42.121.105.155", "222.216.109.114", "117.79.148.73",
//				"223.4.21.184", "115.29.28.137", "121.196.141.249",
//				"180.153.32.9", "112.124.5.121", "121.199.30.110",
//				"115.28.11.165" };
//		int[] ports = new int[] { 8080, 80, 81, 80, 80, 80, 84, 80, 80, 8088,
//				82, 86, 83, 3128, 8080, 81, 8888, 86, 80, 80, 83, 85, 8081, 82,
//				86, 80, 8080, 82, 85, 80, 81, 8088, 8081, 80, 8888, 3128, 80,
//				80, 8090, 80, 8080, 80, 82, 8888 };
		
		String[] ips = new String[] { "180.96.62.21"};
		int[] ports = new int[] { 3390};

		for (int i = 0; i < ips.length; i++) {
			vote(ips[i], ports[i]);
			
			try {
				Thread.sleep(30000);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private static void vote(String ip, int port) {
		System.out.println(ip + " " + port);
		String url = "http://www.naturalizer.com.cn/ch/bltp2/Vote.asp?id=72";

		HttpHost proxy = new HttpHost(ip, port);
		DefaultProxyRoutePlanner routePlanner = new DefaultProxyRoutePlanner(
				proxy);

		HttpGet httpget = new HttpGet(url);
		CloseableHttpClient httpclient = HttpClients
				.custom()
				.setRoutePlanner(routePlanner)
				.setUserAgent(
						"Mozilla/5.0 (X11; U; Linux i686; zh-CN; rv:1.9.1.2) Gecko/20090803 Fedora/3.5.2-2.fc11 Firefox/3.5.2")
				.setProxy(proxy).build();

		httpget.setHeader("Referer",
				"http://www.naturalizer.com.cn/ch/bltp2/index.asp");
		// httpget.setHeader("ip", "116.213.222.57");

		CloseableHttpResponse response = null;
		BufferedReader br = null;
		try {
			response = httpclient.execute(httpget);

			HttpEntity entity = response.getEntity();
			if (entity != null) {
				String html = EntityUtils.toString(entity);// 获得html源代码

				System.out
						.println(new String(html.getBytes("ISO8859-1"), "GBK"));
			}

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
				if (response != null)
					response.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}

package com.dihaitech.spiderstory;

import java.io.BufferedReader;

import java.io.BufferedWriter;

import java.io.FileWriter;

import java.io.IOException;

import java.util.ArrayList;

import java.util.List;

import org.apache.http.HttpEntity;

import org.apache.http.client.methods.CloseableHttpResponse;

import org.apache.http.client.methods.HttpGet;

import org.apache.http.impl.client.CloseableHttpClient;

import org.apache.http.impl.client.HttpClients;

import org.apache.http.util.EntityUtils;

import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;

import org.jsoup.nodes.Element;

import org.jsoup.select.Elements;

public class SpiderStory {

	public static void main(String[] args) {

		// TODO Auto-generated method stub

		// String url = "http://www.513gp.org/zhongguoshipianjudaquan/";

		// String fileName = "D:/zhongguoshipianjudaquan.txt";

		String url = "http://www.wddsnxn.org/";

		String fileName = "/myfile/wddsnxn.txt";

		BufferedWriter bufferedWriter = null;

		try {

			bufferedWriter = new BufferedWriter(new FileWriter(fileName));

			List<String> urlList = getUrlList(url);

			if (urlList != null && urlList.size() > 0) {

				String urlTemp = null;

				for (int i = 0; i < urlList.size(); i++) {

					urlTemp = urlList.get(i);

					String text = getContent(urlTemp);

					System.out.println(text);

					bufferedWriter.write(text);

					bufferedWriter.flush();

				}

			} else {

				System.out.println(url + " 未分析到链接地址");

			}

		} catch (Exception e) {

			// TODO Auto-generated catch block

			e.printStackTrace();

		} finally {

			// Close the BufferedWriter

			try {

				if (bufferedWriter != null) {

					bufferedWriter.flush();

					bufferedWriter.close();

				}

			} catch (Exception ex) {

				ex.printStackTrace();

			}

		}

	}

	/**
	 * 
	 * 根据链接地址获取内容
	 * 
	 * @param url
	 * 
	 * @return
	 */

	public static String getContent(String url) {

		String content = "";

		HttpGet httpget = new HttpGet(url);

		CloseableHttpClient httpclient = HttpClients
				.custom()

				// .setRoutePlanner(routePlanner)

				.setUserAgent(
						"Mozilla/5.0 (X11; U; Linux i686; zh-CN; rv:1.9.1.2) Gecko/20090803 Fedora/3.5.2-2.fc11 Firefox/3.5.2")

				.build();

		CloseableHttpResponse response = null;

		BufferedReader br = null;

		try {

			response = httpclient.execute(httpget);

			HttpEntity entity = response.getEntity();

			if (entity != null) {

				String html = EntityUtils.toString(entity);// 获得html源代码

				if (html != null && !"".equals(html)) {

					Document doc = Jsoup
							.parse(html.replaceAll("<br/>", "\r\n"));

					// Elements linksElements =
					// doc.select("div#page>div#content>div#main>div.left>div#recommend>ul>li>a");

					// //以上代码的意思是 找id为“page”的div里面 id为“content”的div里面
					// id为“main”的div里面 class为“left”的div里面
					// id为“recommend”的div里面ul里面li里面a标签

					Elements titleElements = doc.select(".chaptertitle");

					for (Element ele : titleElements) {

						String title = new String(ele.text().getBytes(
								"ISO8859-1"), "UTF-8");

						content += title;

					}

					content += "\r\n\r\n";

					Elements contentElements = doc.select("div#BookText");

					for (Element ele : contentElements) {

						String text = new String(ele.text().getBytes(
								"ISO8859-1"), "UTF-8");

						content += text;

					}

					content += "\r\n\r\n\r\n\r\n";

				}

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

		return content;

	}

	/**
	 * 
	 * 根据URL地址获取页面中的链接地址列表
	 * 
	 * @param url
	 * 
	 * @return
	 */

	public static List<String> getUrlList(String url) {

		List<String> urlList = null;

		HttpGet httpget = new HttpGet(url);

		CloseableHttpClient httpclient = HttpClients
				.custom()

				// .setRoutePlanner(routePlanner)

				.setUserAgent(
						"Mozilla/5.0 (X11; U; Linux i686; zh-CN; rv:1.9.1.2) Gecko/20090803 Fedora/3.5.2-2.fc11 Firefox/3.5.2")

				.build();

		CloseableHttpResponse response = null;

		BufferedReader br = null;

		try {

			response = httpclient.execute(httpget);

			HttpEntity entity = response.getEntity();

			if (entity != null) {

				urlList = new ArrayList<String>();

				String html = EntityUtils.toString(entity);// 获得html源代码

				if (html != null && !"".equals(html)) {

					Document doc = Jsoup.parse(html);

					// Elements linksElements =
					// doc.select("div#page>div#content>div#main>div.left>div#recommend>ul>li>a");

					// //以上代码的意思是 找id为“page”的div里面 id为“content”的div里面
					// id为“main”的div里面 class为“left”的div里面
					// id为“recommend”的div里面ul里面li里面a标签

					Elements linksElements = doc.select(".booklist")
							.select(".clearfix").select("a");

					for (Element ele : linksElements) {

						String href = ele.attr("href");

						String title = ele.text();

						System.out.println(href + "	" + title);

						urlList.add(href);

					}

				}

				// InputStream istream = entity.getContent();

				// try {

				// // do something useful

				// br = new BufferedReader(new InputStreamReader(istream,"UTF-8"
				// ));

				// String temp = null;

				// while((temp = br.readLine())!=null){

				// System. out.println(temp);

				// }

				// } finally {

				// istream.close();

				// }

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

		return urlList;

	}

}

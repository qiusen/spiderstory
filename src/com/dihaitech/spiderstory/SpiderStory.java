package com.dihaitech.spiderstory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.dihaitech.spiderstory.common.Property;
import com.dihaitech.spiderstory.model.Article;
import com.dihaitech.spiderstory.model.ArticleColumn;
import com.dihaitech.spiderstory.model.Channel;
import com.dihaitech.spiderstory.model.Templete;
import com.dihaitech.spiderstory.service.IArticleColumnService;
import com.dihaitech.spiderstory.service.IArticleService;
import com.dihaitech.spiderstory.service.IChannelService;
import com.dihaitech.spiderstory.service.ITempleteService;

import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;

public class SpiderStory {

	private static ApplicationContext factory = 
			new FileSystemXmlApplicationContext(new String[] { 
					"bin/applicationContext.xml"});
	
	private static IArticleService articleService = (IArticleService)factory.getBean("articleService");
	private static IArticleColumnService articleColumnService = (IArticleColumnService)factory.getBean("articleColumnService");
	private static ITempleteService templeteService = (ITempleteService)factory.getBean("templeteService");
	private static IChannelService channelService = (IChannelService)factory.getBean("channelService");
	
	public static void main(String[] args) {

		// TODO Auto-generated method stub

		String url = "http://www.513gp.org/zhongguoshipianjudaquan/";
		String fileName = "/myfile/zhongguoshipianjudaquan.txt";
		String columnCode = "zgspjdqdyj";
		String categoryCode = "xs";
		String auth = "我是骗子他祖宗";
		
//		String url = "http://www.wddsnxn.org/";
//		String fileName = "/myfile/wddsnxn.txt";
//		String columnCode = "ssjh";
//		String categoryCode = "xs";
//		String auth = "仐三";
		
		
		List<Integer> idList = spider(url, fileName, columnCode, categoryCode, auth);
		if(idList!=null && idList.size()>0){
			for(int i=0;i<idList.size();i++){
				System.out.println(idList.get(i).intValue());
				if(i==0){	//第一篇文章发布时，判断是不是前面已有文章，若有，则更新前面文章
					publishPrevArticle(idList.get(i), columnCode);
				}
				publishArticle(idList.get(i), columnCode);
				
				if(i==idList.size()-1){
					publishChannel(columnCode);
				}
			}
		}

	}

	/**
	 * 爬取
	 * @param url
	 * @param fileName
	 * @param columnCode
	 * @param categoryCode
	 * @param auth
	 * @return
	 */
	public static List<Integer> spider(String url, String fileName, String columnCode, String categoryCode, String auth) {
		String urlFileName = fileName + "_url";	//记录已爬取的URL地址
		File urlFile = new File(urlFileName);
		
		Map<String, Integer> urlMap = null;
		if(urlFile.exists()){
			urlMap = new HashMap<String, Integer>();
			BufferedReader br = null;
			try{
				br = new BufferedReader(new InputStreamReader(new FileInputStream(urlFile),"UTF-8"));
				String temp = null;
				while((temp=br.readLine())!=null){
					urlMap.put(temp.trim(), 1);
				}
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				try {
					if (br != null) {
						br.close();
					}
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
		}
		
		
		BufferedWriter bufferedWriter = null;
		BufferedWriter urlBufferedWriter = null;
		List<Integer> idList = null;

		IArticleService articleService = (IArticleService)factory.getBean("articleService");
		
		try {

			bufferedWriter = new BufferedWriter(new FileWriter(fileName, true));
			urlBufferedWriter = new BufferedWriter(new FileWriter(urlFileName, true));
			
			List<String> urlList = getUrlList(url);
			
			if (urlList != null && urlList.size() > 0) {
				idList = new ArrayList<Integer>();
				String urlTemp = null;
				Article article = null;
				for (int i = 0; i < urlList.size(); i++) {
					urlTemp = urlList.get(i);
					
					if(urlMap!=null && urlMap.containsKey(urlTemp)){
						continue;
					}
					
					String[] reStrs = getContent(urlTemp);
					System.out.println(reStrs[0]);
					bufferedWriter.write(reStrs[0] + "\r\n" + reStrs[1]);
					bufferedWriter.flush();
					
					
					urlBufferedWriter.write(urlTemp + "\r\n");
					urlBufferedWriter.flush();
					
					
					//存库
					article = new Article();
					article.setColumnCode(columnCode);
					article.setCategoryCode(categoryCode);
					article.setTitle(reStrs[0]);
					article.setContent(reStrs[1]);
					article.setAuth(auth);
					article.setShortTitle(reStrs[0]);
					article.setBrief(reStrs[0]);
					article.setStatus(1);
					article.setTempleteId(1);
					article.setCreateuser(auth);
					article.setCreatetime(new Date());
					article.setUpdateuser(auth);
					article.setUpdatetime(new Date());
					
					articleService.addSave(article);
					idList.add(article.getId());
					
					
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
				if (urlBufferedWriter != null) {
					urlBufferedWriter.flush();
					urlBufferedWriter.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}

		}
		
		return idList;
	}
	
	
	/**
	 * 发布频道首页
	 * @param channel
	 * @return
	 */
	private static boolean publishChannel(String columnCode){
		boolean success = false;
		
		ArticleColumn ac = new ArticleColumn();
		ac.setCode(columnCode);
		ArticleColumn articleColumnVO = articleColumnService.selectArticleColumnByCode(ac);
		
		Channel channel = new Channel();
		channel.setId(articleColumnVO.getChannelId());
		Channel channelVO = channelService.selectChannelById(channel);
//		Channel channelVO = channel;
		if(channelVO!=null){
			//模板
			Templete templete = new Templete();
			templete.setId(channelVO.getTempleteId());
			Templete templeteVO = templeteService.selectTempleteById(templete);
			String templeteContent = templeteVO.getContent();
			
			//数据
			Map<String, Object> rootMap=new HashMap<String, Object>();
			rootMap.put("channel", channelVO);
			
			ArticleColumn articleColumn = new ArticleColumn();
			articleColumn.setChannelId(channelVO.getId());
			List<ArticleColumn> articleColumnList = articleColumnService.selectArticleColumnByChannelId(articleColumn);
			rootMap.put("articleColumnList", articleColumnList);
			
			if(articleColumnList!=null && articleColumnList.size()>0){
				ArticleColumn articleColumnTemp = null;
				StringBuffer strbuf = new StringBuffer();
				for(int i=0;i<articleColumnList.size();i++){
					articleColumnTemp = articleColumnList.get(i);
					if(i==0){
						strbuf.append("'" + articleColumnTemp.getCode() + "'");
					}else{
						strbuf.append(",'" + articleColumnTemp.getCode() + "'");
					}
				}
				Article article = new Article();
				article.setIdStr(strbuf.toString());
				List<Article> articleList = articleService.selectArticleByColumnCodes(article);
				rootMap.put("articleList", articleList);
			}
			
			//路径
			String fileFolder = Property.FILE_PUBLISH_PATH + channelVO.getCode();
			File file = new File(fileFolder);
			if(!file.exists()){
				file.mkdirs();
			}
			String filePath = fileFolder + "/index.html";

			//写文件
			PrintWriter printWriter = null;
			
			try{
				printWriter = new PrintWriter(
					new BufferedWriter(
							new OutputStreamWriter(
									new FileOutputStream(filePath),"utf-8")));
				
				Configuration cfg = new Configuration();
				StringTemplateLoader stringLoader = new StringTemplateLoader();

				stringLoader.putTemplate("templete", templeteContent);
				cfg.setTemplateLoader(stringLoader);
				freemarker.template.Template t = cfg.getTemplate("templete","utf-8");
				t.process(rootMap, printWriter);
				printWriter.flush();
				
				success = true;
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				if(printWriter!=null){
					printWriter.close();
					printWriter = null;
				}
			}
		}
		
		return success;
	}
	
	/**
	 * 发布前面的文章
	 * 
	 * @param id
	 * @param columnCode
	 * @return
	 */
	private static boolean publishPrevArticle(Integer id, String columnCode){
		boolean success = false;
		
		//上一篇
		Article article = new Article();
		article.setId(id);
		article.setColumnCode(columnCode);
		Article prevArticle = articleService.selectPreviousArticle(article);
		
		if(prevArticle!=null){
			success = publishArticle(prevArticle.getId(), columnCode);
		}
		
		return success;
	}
	
	/**
	 * 发布文章
	 * 
	 * @param id
	 * @param columnCode
	 * @return
	 */
	private static boolean publishArticle(Integer id, String columnCode){
		boolean success = false;

		Article article = new Article();
		article.setId(id);
		
		//重新获取文章所有内容
		Article articleVO = articleService.selectArticleById(article);
		
		article.setColumnCode(columnCode);
		
		Article prevArticle = articleService.selectPreviousArticle(article);
		if(prevArticle!=null){
			articleVO.setPrevId(prevArticle.getId());
		}
		
		Article nextArticle = articleService.selectNextArticle(article);
		if(nextArticle!=null){
			articleVO.setNextId(nextArticle.getId());
		}
		
		
		
		ArticleColumn articleColumn = new ArticleColumn();
		articleColumn.setCode(articleVO.getColumnCode());
		ArticleColumn articleColumnVO = articleColumnService.selectArticleColumnByCode(articleColumn);
		if(articleColumnVO!=null && articleColumnVO.getChannelId()!=null){
			Channel channel = new Channel();
			channel.setId(articleColumnVO.getChannelId());
			Channel channelVO = channelService.selectChannelById(channel);
			String fileFolder = Property.FILE_PUBLISH_PATH + channelVO.getCode();
			File file = new File(fileFolder);
			if(!file.exists()){
				file.mkdirs();
			}
			
			String filePath = fileFolder + "/" + articleVO.getId() + ".html";
			System.out.println(filePath);
			
			//模板
			Templete templete = new Templete();
			templete.setId(articleVO.getTempleteId());
			Templete templeteVO = templeteService.selectTempleteById(templete);
			String templeteContent = templeteVO.getContent();
			
			//数据
			Map<String, Object> rootMap=new HashMap<String, Object>();
			rootMap.put("article", articleVO);
			rootMap.put("articleColumn", articleColumnVO);
			rootMap.put("channel", channelVO);
			
			//写文件
			PrintWriter printWriter = null;
			
			try{
				printWriter = new PrintWriter(
					new BufferedWriter(
							new OutputStreamWriter(
									new FileOutputStream(filePath),"utf-8")));
				
				Configuration cfg = new Configuration();
				StringTemplateLoader stringLoader = new StringTemplateLoader();

				stringLoader.putTemplate("templete", templeteContent);
				cfg.setTemplateLoader(stringLoader);
				freemarker.template.Template t = cfg.getTemplate("templete","utf-8");
				t.process(rootMap, printWriter);
				printWriter.flush();
				
				success = true;
			}catch(Exception e){
				e.printStackTrace();
			}finally{
				if(printWriter!=null){
					printWriter.close();
					printWriter = null;
				}
			}
			
		}
		
		return success;
	}

	/**
	 * 
	 * 根据链接地址获取内容
	 * 
	 * @param url
	 * 
	 * @return
	 */

	public static String[] getContent(String url) {

		String[] reStrs = new String[2];
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
//					Document doc = Jsoup
//							.parse(html.replaceAll("<br/>", "\r\n"));
					Document doc = Jsoup
							.parse(html.replaceAll("<br/>", "dihaitech"));

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
						reStrs[0] = title;
					}
					content += "\r\n\r\n";
					Elements contentElements = doc.select("div#BookText");
					for (Element ele : contentElements) {
						String text = new String(ele.text().getBytes(
								"ISO8859-1"), "UTF-8");
						
						content += text.replaceAll("dihaitech", "\r\n");
//						System.out.println(content);
						reStrs[1] = text.replaceAll("dihaitech", "<br/>");
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

		return reStrs;

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

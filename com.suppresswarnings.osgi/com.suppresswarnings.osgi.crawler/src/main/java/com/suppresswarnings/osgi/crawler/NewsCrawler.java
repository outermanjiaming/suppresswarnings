/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.suppresswarnings.osgi.crawler;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;

/**
 * crawl some news title
 * @author lijiaming
 *
 */
public class NewsCrawler extends BreadthCrawler {
	ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
	ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<String, Long>();
	Jobs jobs;
	AtomicLong lastAtomicLong = new AtomicLong(0);
	Path path = Paths.get("/Users/lijiaming/data/append.crawler.zhidao.201901301833.log");
	
    public NewsCrawler(Jobs jobs) {
		super(jobs.getName(), true);
		this.jobs = jobs;
		setResumable(true);
		addSeed(jobs.getSeed());
        addRegex(jobs.getRegex());
        getConf().setExecuteInterval(1000);
        setThreads(5);
        try {
			if(!path.toFile().exists()) Files.write(path, ("").getBytes(), StandardOpenOption.CREATE_NEW);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
        
	}

	public static void main( String[] args ) throws Exception 
    {
        System.out.println( "Hello Crawler!" );
//        Jobs jobs = new Jobs("jianshu-crawler", "https://www.jianshu.com/p/82d81319ad69", "https:\\/\\/www\\.jianshu\\.com\\/p\\/.+", "h1[class=title]");
//        Jobs jobs = new Jobs("meizu-crawler", "https://bbs.meizu.cn/forum.php?mod=forumdisplay&fid=103&orderby=heats&filter=dateline&dateline=7948800&orderby=heats%20(URL:%20https://bbs.meizu.cn/forum.php?mod=forumdisplay&fid=103&orderby=heats&filter=dateline&dateline=7948800&orderby=heats)", "https:\\/\\/bbs\\.meizu\\.cn\\/thread.+", "td[class=t_f]");
//        Jobs jobs = new Jobs("AA-roadcode", "https://www.aa.co.nz/RoadCodeQuizController/getSet", "https://www.aa.co.nz/RoadCodeQuizController/getSet", "a[class=xst gv]");
        Jobs jobs = new Jobs("wx-crawler", "https://mp.weixin.qq.com/s/tc_oUewAPvaArU1UAbJy7A", "https:\\/\\/mp\\.weixin\\.qq\\.com\\/.*", "html");
        
//        Jobs jobs = new Jobs("10why-crawler", "http://www.10why.net/", "http:\\/\\/www\\.10why\\.net.+", "a");
//        Jobs jobs = new Jobs("zhidao-crawler", "https://zhidao.baidu.com", "https:\\/\\/zhidao\\.baidu\\.com\\/question.+", "span");
        NewsCrawler crawler = new NewsCrawler(jobs);
        crawler.start(1);
    } 

	@Override
	public void visit(Page page, CrawlDatums arg1) {
		System.out.println(page.toString());
		boolean match = page.matchUrl(jobs.getRegex());
		System.out.println(page.url() + " == " + jobs.getRegex() + " ? " + match);
		if (true) {
			Elements e = page.select(jobs.getSelect());
			if(e == null) {
				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Empty");
				return;
			}
			Iterator<Element> itr = e.iterator();
			while(itr.hasNext()) {
				Element one = itr.next();
	            String title = one.text();
	            if(title.contains("百度") || title.endsWith("...") || title.length() > 50) {
		        	 System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Ignore");
		        	 continue;
		        }
	            if(title.contains("什么") || title.contains("怎样")) {
	            	map.computeIfAbsent(title, (s)-> {
	            		try {
							Files.write(path, (title+ "\n").getBytes(), StandardOpenOption.APPEND);
						} catch (IOException e1) {
							e1.printStackTrace();
						}
	            		return 1L;
	            	});
	            	map.putIfAbsent(title, 1L);
	            	map.computeIfPresent(title, (k, v) -> v+1);
	          
	            	System.err.println(map.size() + "\t[================================LIJIAMING================================]: " + title);
	            }
	            
			}
        }
	}
}

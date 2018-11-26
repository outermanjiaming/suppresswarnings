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

import java.util.Iterator;

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
	Jobs jobs;
    public NewsCrawler(Jobs jobs) {
		super(jobs.getName(), true);
		this.jobs = jobs;
		setResumable(true);
		addSeed(jobs.getSeed());
        addRegex(jobs.getRegex());
        getConf().setExecuteInterval(1000);
        setThreads(16);
	}

	public static void main( String[] args ) throws Exception 
    {
        System.out.println( "Hello Crawler!" );
//        Jobs jobs = new Jobs("jianshu-crawler", "https://www.jianshu.com/p/82d81319ad69", "https:\\/\\/www\\.jianshu\\.com\\/p\\/.+", "h1[class=title]");
//        Jobs jobs = new Jobs("meizu-crawler", "https://bbs.meizu.cn/forum.php?mod=forumdisplay&fid=103&orderby=heats&filter=dateline&dateline=7948800&orderby=heats%20(URL:%20https://bbs.meizu.cn/forum.php?mod=forumdisplay&fid=103&orderby=heats&filter=dateline&dateline=7948800&orderby=heats)", "https:\\/\\/bbs\\.meizu\\.cn\\/thread.+", "td[class=t_f]");
        Jobs jobs = new Jobs("meizu-forum", "https://www.aa.co.nz/RoadCodeQuizController/getSet", "https://www.aa.co.nz/RoadCodeQuizController/getSet", "a[class=xst gv]");
//        Jobs jobs = new Jobs("csdn-crawler", "https://blog.csdn.net/dQCFKyQDXYm3F8rB0/article/details/79990871", "https:\\/\\/blog\\.csdn\\.net\\/.*\\/article\\/details\\/.*", "h1[class=csdn_top]");
        NewsCrawler crawler = new NewsCrawler(jobs);
        crawler.start(3);
    } 

	@Override
	public void visit(Page page, CrawlDatums arg1) {
		if (page.matchUrl(jobs.getRegex())) {
			Elements e = page.select(jobs.getSelect());
			if(e == null) {
				System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Empty");
				return;
			}
			Iterator<Element> itr = e.iterator();
			while(itr.hasNext()) {
				Element one = itr.next();
	            String title = one.text();
	            System.err.println("[================================LIJIAMING================================]: " + title);
			}
        }
	}
}

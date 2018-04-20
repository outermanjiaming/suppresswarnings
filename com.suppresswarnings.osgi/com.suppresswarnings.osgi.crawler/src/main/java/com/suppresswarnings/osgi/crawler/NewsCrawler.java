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

import org.jsoup.nodes.Element;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;

/**
 * crawl some news title
 * @author lijiaming
 *
 */
public class NewsCrawler extends BreadthCrawler {
    public NewsCrawler(String crawlPath, boolean autoParse) {
		super(crawlPath, autoParse);
		setResumable(true);
		addSeed("https://www.newscientist.com/article/2166741-watch-robots-assemble-a-flat-pack-ikea-chair-in-just-9-minutes/");
        addRegex("https:\\/\\/www\\.newscientist\\.com\\/article\\/.+");
        getConf().setExecuteInterval(1000);
        setThreads(16);
	}

	public static void main( String[] args ) throws Exception 
    {
        System.out.println( "Hello Crawler!" );
        NewsCrawler crawler = new NewsCrawler("crawler", true);
        crawler.start(3);
    }

	@Override
	public void visit(Page page, CrawlDatums arg1) {
		if (page.matchUrl("https:\\/\\/www\\.newscientist\\.com\\/article\\/.+")) {
			Element e = page.select("h1[class=article-title]").first();
			if(e == null) return;
            String title = e.text();
            System.err.println("[================================LIJIAMING================================]: " + title);
        }
	}
}

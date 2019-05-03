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

import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.osgi.leveldb.LevelDB;
import com.suppresswarnings.osgi.leveldb.LevelDBImpl;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import cn.edu.hfut.dmic.webcollector.plugin.net.OkHttpRequester;

/**
 * crawl some news title
 * @author lijiaming
 *
 */
public class NewsCrawler extends BreadthCrawler {
	Jobs jobs;
	LevelDB leveldb = new LevelDBImpl("texts");
	HashSet<KeyValue> sets = new HashSet<>();
    public NewsCrawler(Jobs jobs) {
		super(jobs.getName(), true);
		this.jobs = jobs;
		setResumable(false);
		addSeed(jobs.getSeed());
        addRegex(jobs.getRegex());
        getConf().setExecuteInterval(1000);
        setThreads(1);
	}

	public static void main( String[] args ) throws Exception 
    {
        System.out.println( "Hello Crawler!" );
//        Jobs jobs = new Jobs("jianshu-crawler", "https://www.jianshu.com/p/82d81319ad69", "https:\\/\\/www\\.jianshu\\.com\\/p\\/.+", "h1[class=title]");
//        Jobs jobs = new Jobs("meizu-crawler", "https://bbs.meizu.cn/forum.php?mod=forumdisplay&fid=103&orderby=heats&filter=dateline&dateline=7948800&orderby=heats%20(URL:%20https://bbs.meizu.cn/forum.php?mod=forumdisplay&fid=103&orderby=heats&filter=dateline&dateline=7948800&orderby=heats)", "https:\\/\\/bbs\\.meizu\\.cn\\/thread.+", "td[class=t_f]");
//        Jobs jobs = new Jobs("AA-roadcode", "https://www.aa.co.nz/RoadCodeQuizController/getSet", "https://www.aa.co.nz/RoadCodeQuizController/getSet", "a[class=xst gv]");
//        Jobs jobs = new Jobs("csdn-crawler", "https://blog.csdn.net/dQCFKyQDXYm3F8rB0/article/details/79990871", "https:\\/\\/blog\\.csdn\\.net\\/.*\\/article\\/details\\/.*", "h1[class=csdn_top]");
        
//        Jobs jobs = new Jobs("10why-crawler", "http://www.10why.net/", "http:\\/\\/www\\.10why\\.net.+", "a");
//        Jobs jobs = new Jobs("text-crawler", "https://www.pdflibr.com/SMSContent/4", "https://www.pdflibr.com/SMSContent/4", "tr");
//        Jobs jobs = new Jobs("zhidao-crawler", "https://zhidao.baidu.com", "https:\\/\\/zhidao\\.baidu\\.com\\/question.+", "span");
        Jobs jobs = new Jobs("texts-crawler", "https://www.pdflibr.com/", "https:\\/\\/www\\.pdflibr\\.com\\/\\?page=.+", "div.sms-number-list");
        NewsCrawler crawler = new NewsCrawler(jobs);
        crawler.start(2);
        crawler.sets.forEach(kv ->{
        	System.err.println(kv.toString());
//        	crawler.leveldb.put(kv.key(), kv.value());
        });
//        AtomicReference<String> last = new AtomicReference<String>();
//        CrawlDatum cd = new CrawlDatum("https://www.pdflibr.com/SMSContent/4");
//        OkHttpRequester http = new OkHttpRequester().addSuccessCode(200);
//        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
//        service.scheduleWithFixedDelay(() -> {
//	        	try {
//	        		HashSet<String> set = new HashSet<>();
//	        		Page page = http.getResponse(cd);
//	                Elements e = page.select("tr");
//	                e.iterator().forEachRemaining(i->{
//	                	String text = i.text().split("\\s+")[2];
//	                	if(!set.contains(text) && text.contains("素朴网联") || text.contains("爱奇艺")) {
//		                	set.add(text);
//	                	}
//	                });
//	                String msg = set.toString();
//	                if(msg.length() > 240) msg  = msg.substring(0, 239);
//	                if(!msg.equals(last.get())) {
//	                	last.set(msg);
//		                CallableGet post = new CallableGet("http://suppresswarnings.com/wx.http?action=report&token=9si2M&msg=%s", URLEncoder.encode(msg, "UTF-8"));
//		                String ret = post.call();
//		                System.err.println(" === ==== ==== ==== === ==== ==== === === === ==== " + ret);
//	                }
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//        	}, 2, 100, TimeUnit.SECONDS);
    }
	
	@Override
	public void visit(Page page, CrawlDatums arg1) {
		boolean match = page.matchUrl(jobs.getRegex());
		System.out.println(match);
		Elements e = page.select(jobs.getSelect());
		if(e == null) {
			System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~Empty");
			return;
		}
		Iterator<Element> itr = e.iterator();
		while(itr.hasNext()) {
			Element one = itr.next();

			AtomicReference<String> number = new AtomicReference<String>();
            Elements h3 = one.select("div.number-list-flag>h3");
            h3.iterator().forEachRemaining(i -> {
            	number.set(i.text());
            });
            
            AtomicReference<String> path = new AtomicReference<String>();
            Elements a = one.select("div.sms-number-read>a");
            if(a == null) continue;
            a.iterator().forEachRemaining(i->{
            	path.set(i.attr("href"));
            });
            
            if(number.get() != null && number.get().startsWith("+86")) {
            	KeyValue kv = new KeyValue(number.get(), path.get());
            	sets.add(kv);
            }
		}
	}

}

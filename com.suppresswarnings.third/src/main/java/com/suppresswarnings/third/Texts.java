package com.suppresswarnings.third;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicReference;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.suppresswarnings.things.SuppressWarnings;
import com.suppresswarnings.things.Things;
import com.suppresswarnings.things.ThingsManager;
import com.suppresswarnings.things.qr.JFrameQR;

import cn.edu.hfut.dmic.webcollector.model.CrawlDatum;
import cn.edu.hfut.dmic.webcollector.model.CrawlDatums;
import cn.edu.hfut.dmic.webcollector.model.Page;
import cn.edu.hfut.dmic.webcollector.plugin.berkeley.BreadthCrawler;
import cn.edu.hfut.dmic.webcollector.plugin.net.OkHttpRequester;

public class Texts extends BreadthCrawler implements Things {
	Map<String, String> map = new ConcurrentHashMap<>();
	Map<String, AtomicReference<String>> lasts = new ConcurrentHashMap<>();
	ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
	
	public Texts() {
		super("texts-crawler", true);
		setResumable(false);
		addSeed("https://www.pdflibr.com/");
        addRegex("https:\\/\\/www\\.pdflibr\\.com\\/\\?page=.+");
        getConf().setExecuteInterval(1000);
        setThreads(1);
	}

	@Override
	public void visit(Page page, CrawlDatums next) {
		Elements e = page.select("div.sms-number-list");
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
            	map.put(number.get().substring(3), path.get());
            }
		}
	}

	public static void main(String[] args) throws Exception {
		Texts crawler = new Texts();
		crawler.start(2);
        crawler.map.forEach((k,v) ->{
        	System.err.println(k + " " + v);
        });
        
        ThingsManager.connect(crawler);
	}
	
	class TextCrawler implements Callable<String> {
		String path;
		AtomicReference<String> last;
		TextCrawler(String p, AtomicReference<String> l) {
			this.path = p;
			this.last = l;
		}

		@Override
		public String call() {
	        CrawlDatum cd = new CrawlDatum("https://www.pdflibr.com" + path);
	        OkHttpRequester http = new OkHttpRequester().addSuccessCode(200);
	        try {
        		HashSet<String> set = new HashSet<>();
        		Page page = http.getResponse(cd);
                Elements e = page.select("tr");
                StringBuffer sb = new StringBuffer();
                e.iterator().forEachRemaining(i->{
                	String tr = i.text();
                	String text = tr.split("\\s+", 3)[2];
                	if(!set.contains(text) && !text.contains("短信内容")) {
	                	set.add(text);
	                	System.err.println("\t\t" + tr);
	                	sb.append(text).append(";");
                	}
                });
                return sb.toString().replace("\n", "");
			} catch (Exception e) {
				e.printStackTrace();
				return last.get();
			}
		}
	}
	
	@SuppressWarnings("短信验证码")
	public String texts(String input) {
		System.err.println("text: " + input);
		String path = map.get(input);
		if(path != null) {
			AtomicReference<String> last = lasts.get(input); 
			if(last == null) { 
				last = new AtomicReference<String>();
				lasts.put(input, last);
			}
			TextCrawler tc = new TextCrawler(path, last);
			String ret =  tc.call();
			System.out.println(ret);
			return ret;
		}
		return INTERACTIVE;
	}

	@Override
	public String description() {
		return "获取短信验证码";
	}

	@Override
	public String exception(String error) {
		System.err.println(" === === ");
		return "ok";
	}

	@Override
	public String code() {
		return "T_Code_Text_20190503";
	}

	@Override
	public void showQRCode(String remoteQRCodeURL, String text) {
		JFrameQR.show(remoteQRCodeURL, text);
	}
}

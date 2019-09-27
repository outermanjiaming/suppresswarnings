package com.suppresswarnings.corpus.service.handlers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.service.CorpusService;
import com.suppresswarnings.corpus.service.RequestHandler;
import com.suppresswarnings.corpus.service.http.CallableGet;
import com.suppresswarnings.osgi.network.http.Parameter;

public class MiniprogramHandlerFactory {
	static class Record {
		String type;
		String text;
		String name;
		String openid;
		String groupid;
		String textid;
		String avatar;
		String time;
		
		public String getAvatar() {
			return avatar;
		}
		public void setAvatar(String avatar) {
			this.avatar = avatar;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getText() {
			return text;
		}
		public void setText(String text) {
			this.text = text;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getOpenid() {
			return openid;
		}
		public void setOpenid(String openid) {
			this.openid = openid;
		}
		public String getGroupid() {
			return groupid;
		}
		public void setGroupid(String groupid) {
			this.groupid = groupid;
		}
		public String getTextid() {
			return textid;
		}
		public void setTextid(String textid) {
			this.textid = textid;
		}
		public String getTime() {
			return time;
		}
		public void setTime(String time) {
			this.time = time;
		}
		
	}
	
	static class Crew {
		String groupid;
		String openid;// 'openid1', 
		String name;// 'namei', 
		String avatar;// 'namei.png', 
		String role;// 'waitress', 
		String introduce;// '我叫娜美，大家快点击我的头像吧！', 
		String star;// '109', 
		String price;//定价
		String heated;
		String createtime;
		String updatetime;
		
		public String getHeated() {
			return heated;
		}
		public void setHeated(String heated) {
			this.heated = heated;
		}
		public String getGroupid() {
			return groupid;
		}
		public void setGroupid(String groupid) {
			this.groupid = groupid;
		}
		public String getOpenid() {
			return openid;
		}
		public void setOpenid(String openid) {
			this.openid = openid;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getAvatar() {
			return avatar;
		}
		public void setAvatar(String avatar) {
			this.avatar = avatar;
		}
		public String getRole() {
			return role;
		}
		public void setRole(String role) {
			this.role = role;
		}
		public String getIntroduce() {
			return introduce;
		}
		public void setIntroduce(String introduce) {
			this.introduce = introduce;
		}
		public String getStar() {
			return star;
		}
		public void setStar(String star) {
			this.star = star;
		}
		public String getPrice() {
			return price;
		}
		public void setPrice(String price) {
			this.price = price;
		}
		public String getCreatetime() {
			return createtime;
		}
		public void setCreatetime(String createtime) {
			this.createtime = createtime;
		}
		public String getUpdatetime() {
			return updatetime;
		}
		public void setUpdatetime(String updatetime) {
			this.updatetime = updatetime;
		}
		
	}
	
	static class Server {
		String contact;
		String name;
		public String getContact() {
			return contact;
		}
		public void setContact(String contact) {
			this.contact = contact;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		
	}
	
	static class Client {
		String bossid;//openid
		String   groupid;//'1019758015',
		String   name;//'天池酒吧',
		String   iconPath;// "/pages/index/coin.png",
		String   longitude;// '113.3245211',
		String   latitude;// '22.10229',
		String   location;// '耒阳市五一东路电影院旁边一条小箱子进去左转',
		String   contact;// '13727872757',
		String   alert;// 'uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu',
		String   order;// 'true',
		String   type;// 'bar',
		List<Crew> crew;
		
		public String getBossid() {
			return bossid;
		}
		public void setBossid(String bossid) {
			this.bossid = bossid;
		}
		public String getGroupid() {
			return groupid;
		}
		public void setGroupid(String groupid) {
			this.groupid = groupid;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getIconPath() {
			return iconPath;
		}
		public void setIconPath(String iconPath) {
			this.iconPath = iconPath;
		}
		public String getLongitude() {
			return longitude;
		}
		public void setLongitude(String longitude) {
			this.longitude = longitude;
		}
		public String getLatitude() {
			return latitude;
		}
		public void setLatitude(String latitude) {
			this.latitude = latitude;
		}
		public String getLocation() {
			return location;
		}
		public void setLocation(String location) {
			this.location = location;
		}
		public String getContact() {
			return contact;
		}
		public void setContact(String contact) {
			this.contact = contact;
		}
		public String getAlert() {
			return alert;
		}
		public void setAlert(String alert) {
			this.alert = alert;
		}
		public String getOrder() {
			return order;
		}
		public void setOrder(String order) {
			this.order = order;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public List<Crew> getCrew() {
			return crew;
		}
		public void setCrew(List<Crew> crew) {
			this.crew = crew;
		}
		
	}
	
	static class MPLogin {
		String openid;
		String session_key;
		String unionid;
		Integer	number;
		String errmsg;
		public String getOpenid() {
			return openid;
		}
		public void setOpenid(String openid) {
			this.openid = openid;
		}
		public String getSession_key() {
			return session_key;
		}
		public void setSession_key(String session_key) {
			this.session_key = session_key;
		}
		public String getUnionid() {
			return unionid;
		}
		public void setUnionid(String unionid) {
			this.unionid = unionid;
		}
		public Integer getNumber() {
			return number;
		}
		public void setNumber(Integer number) {
			this.number = number;
		}
		public String getErrmsg() {
			return errmsg;
		}
		public void setErrmsg(String errmsg) {
			this.errmsg = errmsg;
		}
		
	}
	
	static org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	static Gson gson = new Gson();
	static Random rand = new Random();
	static RequestHandler myredpacket = (param, service, args) ->{
		List<String> array = new ArrayList<String>();
		String groupid = param.getParameter("groupid");
		String openid =  param.getParameter("openid");
		String code = param.getParameter("code");
		String time =  param.getParameter("time");
		logger.info("[myredpacket] "+ groupid + "/" + openid + ":" + code + " query myredpacket at " + time);
		String myPacketKey = String.join(Const.delimiter, Const.Version.V1, openid, "iBeacon", "Redpacket");
		int head = myPacketKey.length() + 1;
		service.account().page(myPacketKey, myPacketKey, null, 10000, (k,v) -> {
			if(!service.isNull(v)) {
				array.add(v + ',' + k.substring(head));
			}
		});
		return gson.toJson(array);
	};
	
	static RequestHandler avatarUrl = (param, service, args) ->{
		String avatar = param.getParameter("avatar");
		String openid =  param.getParameter("openid");
		String code = param.getParameter("code");
		String time =  param.getParameter("time");
		logger.info("[avatarUrl] "+ avatar + " " + openid + ":" + code + " " + time);
		service.account().put(String.join(Const.delimiter, Const.Version.V1, "Info", "iBeacon", "Avatar", openid), avatar);
		service.data().put(String.join(Const.delimiter, Const.Version.V1, "Info", "iBeacon", "Avatar", openid, time), avatar);
		return "success";
	};
	
	static RequestHandler mine = (param, service, args) ->{
		List<String> array = new ArrayList<String>();
		String openid =  param.getParameter("openid");
		String code = param.getParameter("code");
		String time =  param.getParameter("time");
		Map<String,Object> map = new HashMap<String,Object>();
		logger.info("[mine] " + openid + ":" + code + " query mine at " + time);
		String myPacketKey = String.join(Const.delimiter, Const.Version.V1, openid, "iBeacon", "Redpacket");
		int head = myPacketKey.length() + 1;
		AtomicInteger packetCount = new AtomicInteger(0);
		service.account().page(myPacketKey, myPacketKey, null, 10000, (k,v) -> {
			if(!service.isNull(v)) {
				try {
					int delta = Integer.valueOf(v);
					packetCount.getAndAdd(delta);
					array.add(v + ',' + k.substring(head));
				} catch(Exception e) {
					logger.error("[mine] redpacket is NA: " + k);
				}
			}
		});
		String sendReserveKey = String.join(Const.delimiter, Const.Version.V1, "iBeacon", openid, "Reserve", "Send");
		List<String> reserveSend = new ArrayList<String>();
		service.account().page(sendReserveKey, sendReserveKey, null, 10000, (k,v) -> {
			if(!service.isNull(v)) {
				reserveSend.add(v);
			}
		});
		String haveReserveKey = String.join(Const.delimiter, Const.Version.V1, "iBeacon", openid, "Reserve", "Have");
		List<String> reserveHave = new ArrayList<String>();
		service.account().page(haveReserveKey, haveReserveKey, null, 10000, (k,v) -> {
			if(!service.isNull(v)) {
				reserveHave.add(v);
			}
		});
		String sendGiftKey = String.join(Const.delimiter, Const.Version.V1, "iBeacon", openid, "Gift", "Send");
		List<String> giftSend = new ArrayList<String>();
		service.account().page(sendGiftKey, sendGiftKey, null, 10000, (k,v) -> {
			if(!service.isNull(v)) {
				giftSend.add(v);
			}
		});
		String haveGiftKey = String.join(Const.delimiter, Const.Version.V1, "iBeacon", openid, "Gift", "Have");
		List<String> giftHave = new ArrayList<String>();
		service.account().page(haveGiftKey, haveGiftKey, null, 10000, (k,v) -> {
			if(!service.isNull(v)) {
				giftHave.add(v);
			}
		});
		map.put("packetcount", "" + packetCount.get());
		map.put("packetlist", array);
		map.put("reservesend", reserveSend);
		map.put("reservehave", reserveHave);
		map.put("giftsend", giftSend);
		map.put("gifthave", giftHave);
		String coins = service.updateCoin(openid, 0);
		map.put("coins", coins);
		
		return gson.toJson(map);
	};
	
	static RequestHandler hearted = (param, service, args) ->{
		String groupid = param.getParameter("groupid");
		String openid =  param.getParameter("openid");
		String code = param.getParameter("code");
		String crewid =  param.getParameter("crewid");
		String now = "" + System.currentTimeMillis();
		logger.info("[hearted]" + groupid + "," + code + "," + openid+","+ crewid);
		AtomicInteger count = new AtomicInteger(0);
		String target = String.join(Const.delimiter, groupid, crewid);
		service.account().put(String.join(Const.delimiter, Const.Version.V1, "Clients", "Hearted", target, openid), openid);
		service.account().put(String.join(Const.delimiter, Const.Version.V1, openid, "Clients", "Heart", target), target);
		service.data().put(String.join(Const.delimiter, Const.Version.V1, openid, "Clients", "Heart", target, now), target);
		String head = String.join(Const.delimiter, Const.Version.V1, "Clients", "Hearted", target);
		service.account().page(head, head, null, 999, (k,v)->{
			count.incrementAndGet();
		});
		String star = "" + count.get();
		service.account().put(String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Crew", crewid, "Star"), star);// '109',
		return star;
	};
	
	static RequestHandler accesstoken = (param, service, args) ->{
		String groupid = param.getParameter("groupid");
		String openid =  param.getParameter("openid");
		String code = param.getParameter("code");
		String time =  param.getParameter("time");
		logger.info("[accesstoken] " + groupid + "/" + openid + ":" + code + " generate access token at " + time);
		String myPacketKey = String.join(Const.delimiter, Const.Version.V1, openid, "iBeacon", "AccessToken");
		long expireAt = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(2);
		String token = service.generateRandomToken(openid, expireAt, groupid);
		service.account().put(myPacketKey, token);
		return token;
	};
	
	static RequestHandler robpacket = (param, service, args) ->{
		String groupid = param.getParameter("groupid");
		String openid =  param.getParameter("openid");
		String haspacket =  param.getParameter("haspacket");
		if("false".equals(haspacket)) {
			//local false first
			return "fail";
		}
		AtomicBoolean lock = service.switches("haspacket" + groupid);
		logger.info(groupid + " packet locked: " + lock.get());
		if(lock.get()) {
			//it is locked since it has no money yet
			return "fail";
		}
		synchronized(lock) {
			String myCurrent = String.join(Const.delimiter, Const.Version.V1, openid, "iBeacon", groupid, "Current", "Redpacket");
			String groupCurrent = String.join(Const.delimiter, Const.Version.V1, "iBeacon", groupid, "Current", "Redpacket");
			String current = service.account().get(myCurrent);
			if(service.isNull(current)) {
				current = service.account().get(groupCurrent);
			}
			String packetKey = String.join(Const.delimiter, Const.Version.V1, "iBeacon", groupid, "Redpacket");
			if(service.isNull(current)) {
				current = packetKey;
			}
			final String start = current;
			AtomicBoolean stop = new AtomicBoolean(false);
			AtomicInteger rob = new AtomicInteger(0);
			AtomicReference<String> ref = new AtomicReference<String>();
			service.account().page(packetKey, start, stop, 1000, (k,v) -> {
				if(service.isNull(v)) {
					service.account().put(myCurrent, k);
					service.account().put(groupCurrent, k);
				} else {
					service.account().put(myCurrent, k);
					int val = Integer.parseInt(v);
					logger.info("redpacket left " + val + " of " + k);
					if(val > 0) {
						stop.set(true);
						Random random = new Random();
						int got = val;
						if(val > 1) {
							got = random.nextInt(val);
						}
						if(got > 0) {
							int left = val - got;
							service.account().put(k, "" + left);
							String time = "" + System.currentTimeMillis();
							String rand = "" + random.nextInt(99999);
							ref.set(k);
							String myPacketKey = String.join(Const.delimiter, Const.Version.V1, openid, "iBeacon", "Redpacket", groupid, time, rand);
							service.account().put(myPacketKey, got + "");
							String infoKey = String.join(Const.delimiter, Const.Version.V1, "Ref", "iBeacon", "Redpacketid", myPacketKey);
							service.account().put(infoKey, k);
							String robberKey = String.join(Const.delimiter, Const.Version.V1, "Ref", "iBeacon", "Robberid", k);
							service.account().put(robberKey, myPacketKey);
						}
						rob.set(got);
					}
				}
			});
			logger.info("no more packet: " + stop.get() + " robbed:" + rob.get());
			if(!stop.get()) {
				//redpacket has been robbed
				lock.set(true);
				return "fail";
			} else {
				if(rob.get() > 0) {
					//you robbed a redpacket
					return "" + rob.get();
				} else {
					//you failed to rob
					return "fail";
				}
			}
		}
	};
	
	static RequestHandler moneytree = (param, service, args) ->{
		Gson gson = new Gson();
		String groupid = param.getParameter("groupid");
		String openid = param.getParameter("openid");
		String bindKey = String.join(Const.delimiter, Const.Version.V1, "Info", "iBeacon", "Bind", groupid);
		String bind = service.account().get(bindKey);
		String nameKey = String.join(Const.delimiter, Const.Version.V1, "Info", "iBeacon", "Name", groupid);
		String name = service.account().get(nameKey);
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("binded", service.isNull(bind) ? "false" : "true");
		map.put("name", service.isNull(name) ? "未命名": name);
		String userKey = String.join(Const.delimiter, Const.Version.V1, "Info", "iBeacon", "User", groupid);
		String summary = param.getParameter("summary");
		if("true".equals(summary)) {
			//TODO bugly
			AtomicBoolean lock = service.switches(groupid + "moneytree" + openid);
			if(lock.compareAndSet(true, false)) {
				try {
					TimeUnit.MILLISECONDS.sleep(400);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return "fail";
			}
			lock.set(true);
			logger.debug("sync moneytree summary " + groupid + " by " + openid);
			
			//////////////////////////////
			//    redpacket:haspacket
			//////////////////////////////
			AtomicBoolean locked = service.switches("haspacket" + groupid);
			if(!locked.get()) {
				synchronized(locked) {
					String myCurrent = String.join(Const.delimiter, Const.Version.V1, openid, "iBeacon", groupid, "Current", "Redpacket");
					String groupCurrent = String.join(Const.delimiter, Const.Version.V1, "iBeacon", groupid, "Current", "Redpacket");
					String current = service.account().get(myCurrent);
					if(service.isNull(current)) {
						current = service.account().get(groupCurrent);
					}
					String packetKey = String.join(Const.delimiter, Const.Version.V1, "iBeacon", groupid, "Redpacket");
					if(service.isNull(current)) {
						current = packetKey;
					}
					final String start = current;
					AtomicBoolean stop = new AtomicBoolean(false);
					service.account().page(packetKey, start, stop, 1000, (k,v) -> {
						logger.info("[sync summary] redpacket left " + v + " of " + k);
						if(service.isNull(v)) {
							service.account().put(myCurrent, k);
							service.account().put(groupCurrent, k);
						} else {
							service.account().put(myCurrent, k);
							int val = Integer.parseInt(v);
							if(val > 0) {
								stop.set(true);
								map.put("haspacket", "true");
							}
						}
					});
					if(!stop.get()) {
						locked.set(true);
						map.put("haspacket", "false");
					}
					
				}
			}
			String icon = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Icon"));
			map.put("icon", icon);
			//////////////////////////////
			//    coins
			//////////////////////////////
			String coins = service.updateCoin(openid, 0);
			map.put("coins", coins);
			
			String alert = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "Alert", "iBeacon"));
			if(!service.isNull(alert)) {
				map.put("alert", alert);
			}
			
			AtomicInteger users = new AtomicInteger();
			service.account().page(userKey, userKey, null, 10000, (k, v) ->{
				users.incrementAndGet();
			});
			map.put("users", ""+users.get());
			AtomicInteger texts = new AtomicInteger();
			String groupKey = String.join(Const.delimiter, Const.Version.V1, "iBeacon", "Text", groupid);
			service.data().page(groupKey, groupKey, null, Integer.MAX_VALUE, (k, v) ->{
				texts.incrementAndGet();
			});
			map.put("texts", ""+texts.get());
			String goodsKey = String.join(Const.delimiter, Const.Version.V1, "iBeacon", "Goods", groupid);
			String goods = service.account().get(goodsKey);
			if(!service.isNull(goods)) {
				Map<String, String> goodsMap = new HashMap<String, String>();
				String[] names = goods.split(";");
				String[] keys = {"giftkiss","giftbeer","giftflower"};
				for(int i=0;i<names.length;i++) {
					goodsMap.put(keys[i], names[i]);
				}
				map.put("goods", goodsMap);
			}
			lock.set(false);
		}
		
		String retrieveKey = String.join(Const.delimiter, Const.Version.V1, openid, "iBeacon", "Retrieve", "Last", groupid);
		String lastRetrieved = service.account().get(retrieveKey);
		String groupTextKey = String.join(Const.delimiter, Const.Version.V1, "iBeacon", "Text", groupid);
		List<Record> list = new ArrayList<Record>();
		String start = null;
		AtomicReference<String> last = new AtomicReference<String>();
		if(service.isNull(lastRetrieved)) {
			start = groupTextKey;
		} else {
			start = lastRetrieved;
		}
		last.set(start);
		String head = String.join(Const.delimiter, Const.Version.V1, "iBeacon", "Text");
		int index = head.length() + 1;
		service.data().page(groupTextKey, start + ".0", null, 50, (k, v) ->{
			last.set(k);
			String textid = k.substring(index);
			String[] argv = textid.split("\\.", 5);
			String type = argv.length > 4 ? argv[4] : "text";
			String userid = argv[3];
			String time = argv[2];
			String nickname = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "iBeacon", "NickName", userid));
			String avatar = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "iBeacon", "Avatar", userid));
			Record one = new Record();
			one.setType(type);
			one.setText(v);
			one.setGroupid(groupid);
			one.setOpenid(userid);
			one.setTextid(textid);
			one.setName(nickname);
			one.setTime(time);
			one.setAvatar(avatar);
			list.add(one);
		});
		
		if(start.equals(last.get())) {
			map.put("size", "0");
			list.clear();
		} else {
			map.put("size", "" + list.size());
		}
		service.account().put(retrieveKey, last.get());
		map.put("list", list);
		
		return gson.toJson(map);
	};
	static RequestHandler playcoin = (param, service, args) ->{
		String coins = param.getParameter("coins");
		if(Integer.parseInt(coins) <= 0) {
			return "0";
		}
		String openid = param.getParameter("openid");
		String left = service.updateCoin(openid, -1);
		return left;
	};
	
	static RequestHandler wincoin = (param, service, args) ->{
		String center = param.getParameter("center");
		String square = param.getParameter("square");
		String time = param.getParameter("time");
		String game = param.getParameter("game");
		String evidence = param.getParameter("evidence");
		String openid = param.getParameter("openid");
		String message = "wincoin:" + openid + ":" + center + ":" + square + ":" + time+":" + evidence;
		service.tellAdmins(openid, message);
		logger.info(message);
		int random = 1 + rand.nextInt(10);
		String rate = "" + random;
		String set = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Info", "Game", "Rate", game));
		if(!service.isNull(set)) {
			try {
				int val = Integer.parseInt(set);
				if(val >= 0) {
					rate = set;
				}
			} catch(Exception e) {
				logger.error("game rate is wrong: " + game + " = " + set);
			}
		}
		//TODO just check and add 3
		String left = service.updateCoin(openid, Integer.parseInt(rate));
		return left;
	};
	
	static RequestHandler login = (param, service, args) ->{
		String code = param.getParameter("code");
		logger.info("miniprogram auth.code2Session: " + code);
		String appid = System.getProperty("mp.appid");
		String secret = System.getProperty("mp.secret");
		String argsAppid = param.getParameter("appid");
		if(argsAppid != null) {
			String secretKey = String.join(Const.delimiter, Const.Version.V1, "Info","Secret", argsAppid);
			String secretSet = service.account().get(secretKey);
			if(!service.isNull(secretSet)) {
				appid = argsAppid;
				secret = secretSet;
			}
		}
		logger.info("use appid: " + argsAppid);
		CallableGet get = new CallableGet("https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code", appid, secret, code);
		try {
			String json = get.call();
			logger.info("miniprogram login: " + json);
			Gson gson = new Gson();
			MPLogin map = gson.fromJson(json, MPLogin.class);
			String openid =  map.getOpenid();
			String loginKey = String.join(Const.delimiter, Const.Version.V1, "iBeacon", "Login", openid);
			String now = "" + System.currentTimeMillis();
			service.account().put(String.join(Const.delimiter, Const.Version.V1, "iBeacon", "Link", openid), code);
			service.token().put(String.join(Const.delimiter, Const.Version.V1, "iBeacon", "Link", code), openid);
			logger.info("link Account(code) & Token(openid): " + code + " & " + openid);
			service.account().put(loginKey, now);
			String myloginKey = String.join(Const.delimiter, Const.Version.V1, openid, "iBeacon", "Login");
			service.account().put(myloginKey, now);
			String streamloginKey = String.join(Const.delimiter, Const.Version.V1, openid, "iBeacon", "Login", now);
			service.data().put(streamloginKey, now);
			return openid;
		} catch(Exception e) {
			logger.error("fail to auth.code2Session", e);
		}
		return code;
	};
	static RequestHandler record = (param, service, args) ->{
		String argsAppid = param.getParameter("appid");
		String openid = param.getParameter("openid");
		String groupid = param.getParameter("groupid");
		String name = param.getParameter("name");
		//client time
		String time = param.getParameter("time");
		String text = param.getParameter("text");
		String type = param.getParameter("type");
		//server time
		String now = "" + System.currentTimeMillis();
		String textid = String.join(Const.delimiter, groupid, now, time, openid, type);

		String openidKey = String.join(Const.delimiter, Const.Version.V1, "Info", "iBeacon", "User", groupid, openid);
		service.account().put(openidKey, now);
		service.data().put(openidKey, openid);

		String mygroupKey  = String.join(Const.delimiter, Const.Version.V1, openid, "Info", "iBeacon", "List", groupid);
		service.account().put(mygroupKey, groupid);
		service.data().put(mygroupKey, now);

		String nameKey = String.join(Const.delimiter, Const.Version.V1, "Info", "iBeacon", "NickName", openid);
		service.account().put(nameKey, name);
		
		String groupKey = String.join(Const.delimiter, Const.Version.V1, "Info", "iBeacon", "List", groupid);
		service.account().put(groupKey, groupid);
		
		String textKey = String.join(Const.delimiter, Const.Version.V1, "iBeacon", "Text", textid);
		if("pic".equals(type)) {
			if(!service.imgSecCheck(text)) {
				text =  "alert.png#" + text;
				service.account().put(String.join(Const.delimiter, Const.Version.V1, "Info",  "Alert", argsAppid, groupid, openid), textKey);
			}
			service.token().put(String.join(Const.delimiter, Const.Version.V1, "TODO", "imgSecCheck", "" + System.currentTimeMillis(), argsAppid, groupid, openid, "Data"), textKey);
		}
		service.data().put(textKey, text);
		
		String mytextKey = String.join(Const.delimiter, Const.Version.V1, openid, "iBeacon", "Text", textid);
		service.data().put(mytextKey, text);
		if("goods".equals(type)) {
			//测试商品;1.88;upload/20190915/233609.o1qY65DuZT_dAHuZM2P0N8sSeOZI.1568561769977.tmp_9e26830d762a80f0de8eb5a1cac6f95172be5d6f24bfcc29.jpg;6;o1qY65DuZT_dAHuZM2P0N8sSeOZI
			String[] goods = text.split(";");
			String goodsName = goods[0];
			String goodsPrice = goods[1];
			float price = Float.parseFloat(goodsPrice);
			int pricecent = (int) (price * 100);
			String goodsImage = goods[2];
			String goodsCount = goods[3];
			String goodsBoss = goods[4];
			service.account().put(String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", textid, "Price"), "" + pricecent);
			service.account().put(String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", textid, "What"), goodsImage);
			service.account().put(String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", textid, "Reason"), goodsName);
			service.account().put(String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", textid, "Amount"), goodsCount);
			service.account().put(String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", textid, "Bossid"), goodsBoss);
			service.account().put(String.join(Const.delimiter, Const.Version.V1, "Sell", "Goods", textid, "Type"), type);
			logger.info("goods created: " + textid);
		}
		String sentKey = String.join(Const.delimiter, Const.Version.V1, openid, "iBeacon", "Sent", "Last", groupid);
		//bugfix
		service.account().put(sentKey, textid);
		return textid;
	};
	
	static RequestHandler waiter = (param, service, args) ->{
		String openid = param.getParameter("openid");
		String groupid = param.getParameter("groupid");
		String code = param.getParameter("code");
		return "success";
	};
	
	static RequestHandler waitress = (param, service, args) ->{
		String code = param.getParameter("code");
		String argsAppid = param.getParameter("appid");
		String openid = param.getParameter("openid");
		String groupid = param.getParameter("groupid");
		String name = param.getParameter("name");
		String avatar = param.getParameter("avatar");
		String intro = param.getParameter("intro");
		String crewsKey = String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Crew", "List", openid);
		String crew = service.account().get(crewsKey);
		if(service.isNull(crew)) {
			service.account().put(crewsKey, openid);
			service.account().put(String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Crew", openid, "Role"), "waitress"); 
			service.account().put(String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Crew", openid, "Star"), "1"); 
			service.account().put(String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Crew", openid, "Price"), "50");
			service.tellAdmins(openid, groupid + "服务员注册：" + name);
		}
		logger.info("[waitress] " + groupid + "," + code + "," + openid + "," + name + "," + avatar + "," + intro);
		service.account().put(String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Crew", openid, "Name"), name); 
		
		
		if(!service.isNull(avatar)) {
			String key = String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Crew", openid, "Avatar");
			service.token().put(String.join(Const.delimiter, Const.Version.V1, "TODO", "imgSecCheck", "" + System.currentTimeMillis(), argsAppid, groupid, openid, "Account"), key);
			service.account().put(key, avatar);// 'namei.png',
			if(!service.imgSecCheck(avatar)) {
				service.account().put(key, "alert.png#"+avatar);
				service.account().put(String.join(Const.delimiter, Const.Version.V1, "Info",  "Alert", argsAppid, groupid, openid), key);
			}
		}
		
		if(!service.isNull(intro)) {
			service.account().put(String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Crew", openid, "Introduce"), intro);// '我叫娜美，大家快点击我的头像吧！', 
		}
		
		return "true";
	};
	static RequestHandler crews = (param, service, args) ->{
		String code = param.getParameter("code");
		String openid = param.getParameter("openid");
		String groupid = param.getParameter("groupid");
		logger.info("[crews] " + groupid + "," + code + "," + openid);
		List<Crew> list = new ArrayList<>();
		String crewsKey = String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Crew", "List");
		List<String> crewIds = new ArrayList<String>();
		service.account().page(crewsKey, crewsKey, null, 1000, (k,crew) ->{
			crewIds.add(crew);
		});
		for(String crew : crewIds) {
			Crew c = new Crew();
			String name = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Crew", crew, "Name")); 
			String avatar = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Crew", crew, "Avatar"));// 'namei.png', 
			String role = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Crew", crew, "Role"));// 'waitress', 
			String introduce = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Crew", crew, "Introduce"));// '我叫娜美，大家快点击我的头像吧！', 
			String star = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Crew", crew, "Star"));// '109', 
			String price = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Clients", groupid, "Crew", crew, "Price"));//定价
			String target = String.join(Const.delimiter, groupid, crew);
			String hearted = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Clients", "Hearted", target, openid));
			c.setAvatar("https://suppresswarnings.com/" + avatar);
			c.setGroupid(groupid);
			c.setIntroduce(introduce);
			c.setName(name);
			c.setOpenid(crew);
			c.setPrice(price);
			c.setRole(role);
			c.setStar(star);
			c.setHeated(service.isNull(hearted) ? "false":"true");
			list.add(c);
		}
		return gson.toJson(list);
	};
	static RequestHandler clients = (param, service, args) ->{
		Gson gson = new Gson();
		String argsAppid = param.getParameter("appid");
		String code = param.getParameter("code");
		String openid = param.getParameter("openid");
		logger.info("[clients] " + code + "," + openid);
		Map<String, Object> map = new HashMap<String, Object>();
		String clientsKey = String.join(Const.delimiter, Const.Version.V1, "Info", "Clients", "List");
		List<String> clientIds = new ArrayList<String>();
		service.account().page(clientsKey, clientsKey, null, 1000, (k,client) ->{
			clientIds.add(client);
		});
		List<Client> all = new ArrayList<>();
		for(String client: clientIds) {
			Client c = new Client();
			String   name = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Clients", client, "Name"));//'天池酒吧',
			String   iconPath = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Clients", client, "Icon"));// "/pages/index/coin.png",
			String   longitude = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Clients", client, "Longtitude"));// '113.3245211',
			String   latitude = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Clients", client, "Latitude"));// '22.10229',
			String   location = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Clients", client, "Location"));// '耒阳市五一东路电影院旁边一条小箱子进去左转',
			String   contact = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Clients", client, "Contact"));// '13727872757',
			String   alert = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Clients", client, "Alert"));// 'uuuuuuuuuuuuuuuuuuuuuuuuuuuuuuuu',
			String   order = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Clients", client, "Order"));// 'true',
			String   type = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Clients", client, "Type"));// 'bar',
			String   bossid = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Clients", client, "Bossid"));// 'bar',
			c.setAlert(alert);
			c.setBossid(bossid);
			c.setContact(contact);
			c.setGroupid(client);
			c.setIconPath(iconPath);
			c.setLatitude(latitude);
			c.setLongitude(longitude);
			c.setLocation(location);
			c.setName(name);
			c.setOrder(order);
			c.setType(type);
			all.add(c);
		}
		map.put("clients", all);
		Server server = new Server();
		String   contact = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Clients", "Server", "Contact"));// 'true',
		String   name = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Clients", "Server", "Name"));// 'bar',
		server.setContact(contact);
		server.setName(name);
		map.put("server", server);
		return gson.toJson(map);
	};
	
	public static String handle(Parameter parameter, CorpusService service) {
		String todo = parameter.getParameter("todo");
		if("moneytree".equals(todo)) {
			return moneytree.handler(parameter, service);
		} else if("record".equals(todo)) {
			return record.handler(parameter, service);
		} else if("login".equals(todo)) {
			return login.handler(parameter, service);
		} else if("playcoin".equals(todo)) {
			return playcoin.handler(parameter, service);
		} else if("wincoin".equals(todo)) {
			return wincoin.handler(parameter, service);
		} else if("robpacket".equals(todo)) {
			return robpacket.handler(parameter, service);
		} else if("myredpacket".equals(todo)) {
			return myredpacket.handler(parameter, service);
		} else if("accesstoken".equals(todo)) {
			return accesstoken.handler(parameter, service);
		} else if("hearted".equals(todo)) {
			return hearted.handler(parameter, service);
		} else if("clients".equals(todo)) {
			return clients.handler(parameter, service);
		} else if("crews".equals(todo)) {
			return crews.handler(parameter, service);
		} else if("waitress".equals(todo)) {
			return waitress.handler(parameter, service);
		} else if("mine".equals(todo)) {
			return mine.handler(parameter, service);
		} else if("avatar".equals(todo)) {
			return avatarUrl.handler(parameter, service);
		} else if("service".equals(todo)) {
			logger.info("[mini 后台service]");
			return waiter.handler(parameter, service);
		} else {
			return RequestHandler.simple.handler(parameter, service);
		}
	}
	
	public static void main(String[] args) {
		String ss = "a.v.c.d";
		String[] a = ss.split("\\" + Const.delimiter);
		System.out.println(Arrays.toString(a));
		System.out.println(a[a.length-1]);
	}
}

package com.suppresswarnings.osgi.like.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.osgi.like.LikeHandler;
import com.suppresswarnings.osgi.like.LikeService;
import com.suppresswarnings.osgi.like.model.Page;
import com.suppresswarnings.osgi.like.model.Project;
import com.suppresswarnings.osgi.like.model.User;

public class LikeHandlerImpl implements LikeHandler {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	LikeService service;
	public LikeHandlerImpl(LikeService service) {
		this.service = service;
	}

	@Override
	public Page<Project> listProjects(boolean first, int n, String projectid, String openid) {
		if(projectid == null || projectid.length() < 1) {
			projectid = "Project";
		}
		String head = String.join(Const.delimiter, Const.Version.V2, "Projectid");
		String start = String.join(Const.delimiter, Const.Version.V2, "Projectid", projectid);
		List<String> projectids = new ArrayList<>();
		logger.info("start = " + start);
		service.account().page(head, start, null, n, (k,v) ->{
			projectids.add(v);
		});
		logger.info("projectids = " + projectids);
		Page<Project> page = new Page<Project>();
		List<Project> data = new ArrayList<>();
		int i = 0;
		do {
			String s = projectids.get(i);
			Project project = new Project();
			project.setProjectid(s);
			String userid = service.account().get(String.join(Const.delimiter, Const.Version.V2, "Project", "Openid", project.getProjectid()));
			project.setTitle(service.account().get(String.join(Const.delimiter, Const.Version.V2, "Project", "Title", project.getProjectid())));
			project.setBonusCent(service.account().get(String.join(Const.delimiter, Const.Version.V2, "Project", "BonusCent", project.getProjectid())));
			project.setOpenid(userid);
			KeyValue kv = service.user(userid);
			project.setUname(kv.key());
			project.setFace(kv.value());
			project.setPictures(service.account().get(String.join(Const.delimiter, Const.Version.V2, "Project", "Pictures", project.getProjectid())));
			project.setTime(service.account().get(String.join(Const.delimiter, Const.Version.V2, "Project", "Time", project.getProjectid())));
			project.setLikes(getLikes(s));
			project.setComments(getComments(s));
			data.add(project);
			i++;
		} while(i<projectids.size() - 1);
		
		if(projectids.size() > 1) {
			String s = projectids.get(i);
			page.setNext(s);
		} else {
			page.setNext("null");
		}
		
		if(first) {
			page.setNext("Project");
		}
		page.setEntries(data);
		page.setStart(start);
		return page;
	}
	
	public Page<KeyValue> getComments(String projectid) {
		Page<KeyValue> page = new Page<KeyValue>();
		List<KeyValue> data = new ArrayList<>();
		try {
			String start = String.join(Const.delimiter, Const.Version.V2, "Project", "Comment", projectid);
			int index = start.length() + Const.delimiter.length();
			service.data().page(start, start, null, Integer.MAX_VALUE, (k,v)->{
				String openid_Time = k.substring(index);
				String openid = openid_Time.split("\\.")[0];
				KeyValue kv = service.user(openid);
				kv.value(v);
				data.add(kv);
			});
			
			page.setEntries(data);
		} catch (Exception e) {
			logger.error("fail to get comments", e);
		}
		
		return page;
	}

	public Page<KeyValue> getLikes(String projectid) {
		Page<KeyValue> page = new Page<KeyValue>();
		List<KeyValue> data = new ArrayList<>();
		try {
			String start = String.join(Const.delimiter, Const.Version.V2, "Project", "Like", projectid);
			int index = start.length() + Const.delimiter.length();
			service.data().page(start, start, null, Integer.MAX_VALUE, (k,v)->{
				String openid = k.substring(index);
				KeyValue kv = service.user(openid);
				data.add(kv);
			});
			
			page.setEntries(data);
		} catch (Exception e) {
			logger.error("fail to get likes", e);
		}
		
		return page;
	}

	@Override
	public String likeProject(String projectid, String openid) {
		String time = "" + System.currentTimeMillis();
		String projectLikeKey = String.join(Const.delimiter, Const.Version.V2, "Project", "Like", projectid, openid);
		String userLikeKey = String.join(Const.delimiter, Const.Version.V2, openid, "Project", "Like", projectid);
		service.account().put(userLikeKey, projectid);
		String like = service.data().get(projectLikeKey);
		logger.info("like project: " + userLikeKey + " => " + like);
		if(like == null) {
			//like
			int count = service.like(projectid);
			logger.info("likes = " + count + " for " + projectid);
			service.data().put(projectLikeKey, time);
			service.data().put(userLikeKey, time);
			return "" + count;
		} else {
			String userLikedKey = String.join(Const.delimiter, Const.Version.V2, openid, "Project", "Love", time, projectid);
			service.data().put(userLikedKey, projectid);
			return null;
		}
	}

	@Override
	public String commentProject(String comment, String projectid, String openid, String name) {
		logger.info("( Just ) comment on project: " + projectid + " by openid: " + openid + " named " + name);
		String commented = String.join(Const.delimiter, Const.Version.V2, openid, "Projectid", "Comment", projectid);
		String id = String.join(Const.delimiter, Const.Version.V2, "Project", "Comment", projectid, openid, "" + System.currentTimeMillis());
		String my = String.join(Const.delimiter, Const.Version.V2, openid, "Project", "Comment", projectid, "" + System.currentTimeMillis());
		service.data().put(id, comment);
		service.data().put(my, comment);
		service.account().put(commented, projectid);
		return id;
	}

	@Override
	public User myself(String openid) {
		User user = new User();
		user.setOpenid(openid);
		KeyValue kv = service.user(openid);
		user.setUname(kv.key());
		user.setFace(kv.value());
		List<KeyValue> projects = new ArrayList<>();
		List<String> projectids = new ArrayList<>();
		String start = String.join(Const.delimiter, Const.Version.V2, openid, "Projectid");
		service.account().page(start, start, null, 1000, (k,v) ->{
			projectids.add(v);
		});
		projectids.forEach(project -> {
			String title = service.account().get(String.join(Const.delimiter, Const.Version.V2, "Project", "Title", project));
			KeyValue p = new KeyValue(project, title);
			projects.add(p);
		});
		user.setProjects(projects);
		
		List<KeyValue> comments = new ArrayList<>();
		List<String> commentids = new ArrayList<>();
		start = String.join(Const.delimiter, Const.Version.V2, openid, "Project", "Comment");
		service.account().page(start, start, null, 1000, (k,v) ->{
			commentids.add(v);
		});
		commentids.forEach(project -> {
			String head = String.join(Const.delimiter, Const.Version.V2, openid, "Project", "Comment", project);
			service.data().page(head, head, null, 100, (k,v) ->{
				KeyValue p = new KeyValue(project, v);
				comments.add(p);
			});
			
		});
		user.setComments(comments);
		
		List<KeyValue> likes = new ArrayList<>();
		List<String> likeids = new ArrayList<>();
		start = String.join(Const.delimiter, Const.Version.V2, openid, "Project", "Like");
		service.account().page(start, start, null, 1000, (k,v) ->{
			likeids.add(v);
		});
		likeids.forEach(project -> {
			String title = service.account().get(String.join(Const.delimiter, Const.Version.V2, "Project", "Title", project));
			KeyValue p = new KeyValue(project, title);
			likes.add(p);
		});
		user.setLikes(likes);
		
		List<KeyValue> cashouts = new ArrayList<>();
		String requesting = service.account().get(String.join(Const.delimiter, Const.Version.V2, openid, "Cashout", "Requesting"));
		start = String.join(Const.delimiter, Const.Version.V2, openid, "Cashout", "Request");
		service.account().page(start, start, null, 1000, (k,v) ->{
			try {
				Long time = Long.valueOf(v);
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss发起申请提现");
				String date = sdf.format(new Date(time));
				if(v.equals(requesting)) {
					KeyValue cashout = new KeyValue(date, "正在审核...");
					cashouts.add(cashout);
				} else {
					KeyValue cashout = new KeyValue(date, "提现完成");
					cashouts.add(cashout);
				}
			} catch (Exception e) {
				logger.error("Cashout Request log error");
			}
		});
		user.setCashouts(cashouts);
		return user;
	}

}

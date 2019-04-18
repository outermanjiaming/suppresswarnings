package com.suppresswarnings.osgi.like.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.Const;
import com.suppresswarnings.corpus.common.KeyValue;
import com.suppresswarnings.osgi.like.LikeHandler;
import com.suppresswarnings.osgi.like.LikeService;
import com.suppresswarnings.osgi.like.model.Page;
import com.suppresswarnings.osgi.like.model.Project;

public class LikeHandlerImpl implements LikeHandler {
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	LikeService service;
	public LikeHandlerImpl(LikeService service) {
		this.service = service;
	}

	@Override
	public Page<Project> listProjects(boolean first, int n, String projectid, String openid) {
		if(first || projectid == null || projectid.length() < 1) {
			projectid = "Project";
		}
		String head = String.join(Const.delimiter, Const.Version.V1, "Projectid");
		String start = String.join(Const.delimiter, Const.Version.V1, "Projectid", projectid);
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
			String userid = service.account().get(String.join(Const.delimiter, Const.Version.V1, "Project", "Openid", project.getProjectid()));
			project.setTitle(service.account().get(String.join(Const.delimiter, Const.Version.V1, "Project", "Title", project.getProjectid())));
			project.setBonusCent(service.account().get(String.join(Const.delimiter, Const.Version.V1, "Project", "BonusCent", project.getProjectid())));
			project.setOpenid(userid);
			KeyValue kv = service.user(userid);
			project.setUname(kv.key());
			project.setFace(kv.value());
			project.setPictures(service.account().get(String.join(Const.delimiter, Const.Version.V1, "Project", "Pictures", project.getProjectid())));
			project.setTime(service.account().get(String.join(Const.delimiter, Const.Version.V1, "Project", "Time", project.getProjectid())));
			project.setLikes(getLikes(s));
			project.setComments(getComments(s));
			data.add(project);
			i++;
		} while(i<projectids.size() - 1);
		
		if(projectids.size() > 1) {
			String s = projectids.get(i);
			if(first) {
				page.setNext("Project");
			} else {
				page.setNext(s);
			}
		} else {
			page.setNext("null");
		}
		page.setEntries(data);
		page.setStart(start);
		return page;
	}
	
	public Page<KeyValue> getComments(String projectid) {
		Page<KeyValue> page = new Page<KeyValue>();
		List<KeyValue> data = new ArrayList<>();
		try {
			String start = String.join(Const.delimiter, Const.Version.V1, "Project", "Comment", projectid);
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
			String start = String.join(Const.delimiter, Const.Version.V1, "Project", "Like", projectid);
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
		String projectLikeKey = String.join(Const.delimiter, Const.Version.V1, "Project", "Like", projectid, openid);
		String userLikeKey = String.join(Const.delimiter, Const.Version.V1, openid, "Like", "Project", projectid);
		
		String like = service.data().get(userLikeKey);
		logger.info("like project: " + userLikeKey + " => " + like);
		if(like == null) {
			//like
			int count = service.like(projectid);
			logger.info("likes = " + count + " for " + projectid);
			service.data().put(projectLikeKey, time);
			service.data().put(userLikeKey, time);
			return "1";
		} else {
			String userLikedKey = String.join(Const.delimiter, Const.Version.V1, openid, "Liked", "Project", time, projectid);
			service.data().put(userLikedKey, projectid);
			return null;
		}
	}

	@Override
	public String commentProject(String comment, String projectid, String openid, String name) {
		logger.info("( Just ) comment on project: " + projectid + " by openid: " + openid + " named " + name);
		String id = String.join(Const.delimiter, Const.Version.V1, "Project", "Comment", projectid, openid, "" + System.currentTimeMillis());
		
		service.data().put(id, comment);
		return id;
	}

}

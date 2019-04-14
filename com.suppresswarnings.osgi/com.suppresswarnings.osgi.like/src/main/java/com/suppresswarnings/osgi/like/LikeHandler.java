package com.suppresswarnings.osgi.like;

import com.suppresswarnings.osgi.like.model.Page;
import com.suppresswarnings.osgi.like.model.Project;

public interface LikeHandler {

	public Page<Project> get(String projectid, String openid);
}

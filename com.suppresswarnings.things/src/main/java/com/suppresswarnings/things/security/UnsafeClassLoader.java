/**
 * 
 * Copyright 2019 SuppressWarnings.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
 */
package com.suppresswarnings.things.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.suppresswarnings.things.Config;

public class UnsafeClassLoader extends ClassLoader {
	public UnsafeClassLoader(ClassLoader parent) {
		super(parent);
	}
	Map<String, Class<?>> classes = new HashMap<String, Class<?>>();
	String current;
	byte[] bytes;
	public UnsafeClassLoader of(byte[] bs, String name) {
		this.bytes = bs;
		this.current = name;
		return this;
	}
	
	@Override
	protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
		System.err.println("Loading..." + name);
		if(name == null) {
			Object clas = classes.get(current);
			if(clas != null) {
				return (Class<?>) clas;
			} else {
				Class<?> klass = defineClass(null, bytes, 0, bytes.length);  
		        if (resolve) {
		        	resolveClass(klass);  
		        }
		        classes.put(current, klass);  
		        return klass;
			}
		} else {
			Object cache = classes.get(name);
			if(cache != null) {
				Class<?> clazz = (Class<?>) cache;
				if(resolve) super.resolveClass(clazz);
				return clazz;
			} else {
				return super.loadClass(name, resolve);
			}
		}
	}
	
	public Config load(byte[] bs, String name) throws ClassNotFoundException, IOException, InstantiationException, IllegalAccessException {
		return (Config) this.of(bs, name).loadClass(null, true).newInstance();
	}
}

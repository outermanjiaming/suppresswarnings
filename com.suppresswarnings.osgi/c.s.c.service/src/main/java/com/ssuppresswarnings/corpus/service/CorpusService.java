/**
 * 
 *       # # $
 *       #   #
 *       # # #
 * 
 *  SuppressWarnings
 * 
 */
package com.ssuppresswarnings.corpus.service;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.osgi.framework.console.CommandProvider;
import org.slf4j.LoggerFactory;

import com.suppresswarnings.corpus.common.Context;
import com.suppresswarnings.corpus.common.ContextFactory;
import com.suppresswarnings.corpus.common.Provider;
import com.suppresswarnings.osgi.leveldb.LevelDB;
import com.suppresswarnings.osgi.network.http.HTTPService;
import com.suppresswarnings.osgi.network.http.Parameter;

public class CorpusService implements HTTPService, Runnable, CommandProvider {
	public static final String SUCCESS = "success";
	org.slf4j.Logger logger = LoggerFactory.getLogger("SYSTEM");
	Map<String, LevelDB> levedbs = new HashMap<>();
	Map<String, ContextFactory<Context<CorpusService>>> factories = new HashMap<>();
	
	public void provide(Provider<LevelDB> provider) {
		logger.info("provider: " + provider.description());
		String id = provider.identity();
		LevelDB leveldb = provider.instance();
		LevelDB old = levedbs.put(id, leveldb);
		logger.info("put new leveldb: " + leveldb + " replace if exists: " + old);
	}
	public void clearProvider(Provider<LevelDB> provider) {
		logger.info("clear provider: " + provider.description());
		String id = provider.identity();
		LevelDB leveldb = provider.instance();
		boolean b = levedbs.remove(id, leveldb);
		logger.info("remove leveldb: " + leveldb + " if found: " + b);
	}
	public void factory(ContextFactory<Context<CorpusService>> factory) {
		if(factories.containsKey(factory.command())) {
			logger.warn("factory exist, replace: " + factory.command());
		} else {
			logger.info("new factory register: " + factory.command());
		}
		factories.put(factory.command(), factory);
	}
	public void clearFactory(ContextFactory<Context<CorpusService>> factory) {
		boolean removed = factories.remove(factory.command(), factory);
		logger.info("remove the factory: " + factory.command() + "(" + factory + ") = " + removed);
	}
	
	@Override
	public String getHelp() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("---SuppressWarnings CommandProvider---\n");
		buffer.append("\t which - which <which> - change to use different LevelDB.\n");
		buffer.append("\t putkv - putkv <key> <value> - put value to that key.\n");
		buffer.append("\t getkv - getkv <key> - get value by that key.\n");
		buffer.append("\t listn - listn <start> <limit> - list some values by start limit.\n");
		return buffer.toString();
	}
	@Override
	public void run() {
		//TODO clean context
		//TODO send email
	}
	@Override
	public String getName() {
		return "wx.http";
	}
	@Override
	public String start(Parameter arg0) throws Exception {
		// TODO get action to do things
		return SUCCESS;
	}

}

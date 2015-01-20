package com.config;

import java.util.HashSet;
import java.util.Set;

import javax.websocket.Endpoint;
import javax.websocket.server.ServerApplicationConfig;
import javax.websocket.server.ServerEndpointConfig;

public class ServerConfig implements ServerApplicationConfig {

	@Override
	public Set<Class<?>> getAnnotatedEndpointClasses(Set<Class<?>> scanned) {
		 Set<Class<?>> results = new HashSet<>();
	        for (Class<?> clazz : scanned) {
	        	//注意com后面加.的问题。
	            if (clazz.getPackage().getName().startsWith("com.service")) {
	                results.add(clazz);
	            }
	        }
	        return results;
	}

	@Override
	public Set<ServerEndpointConfig> getEndpointConfigs(Set<Class<? extends Endpoint>> scanned) {
		return null;
	} 

}

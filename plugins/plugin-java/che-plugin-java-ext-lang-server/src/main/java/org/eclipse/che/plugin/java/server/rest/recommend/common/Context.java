package org.eclipse.che.plugin.java.server.rest.recommend.common;

public class Context {

	private static final Context CONTEXT = new Context();

	public static Context sharedContext() {
		return CONTEXT;
	}

	public Context() {
				
	}
	
}
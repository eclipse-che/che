package org.eclipse.che.plugin.languageserver.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ProcessBuilder.Redirect;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Singleton;

import io.typefox.lsapi.services.json.JsonBasedLanguageServer;

@Singleton
public class FatJarBasedLanguageServerRegistrant {

	private final static Logger LOG = LoggerFactory.getLogger(FatJarBasedLanguageServerRegistrant.class);

	private final static String JAVA_EXEC = System.getProperty("java.home") + "/bin/java";

	public void registerLanguageServer(LanguageServerRegistry registry) {
		String suffix = "-languageserver.jar";
		for (File file : new File("/projects").listFiles()) {
			if (file.getName().endsWith(suffix)) {
				try {
					ProcessBuilder languageServerStarter = new ProcessBuilder(JAVA_EXEC, "-jar", file.getAbsolutePath(),
							"-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=1044", "debug");
					languageServerStarter.redirectInput(Redirect.PIPE);
					languageServerStarter.redirectOutput(Redirect.PIPE);
					Process process = languageServerStarter.start();
					StreamReader processMonitor = new StreamReader(process);
					new Thread(processMonitor).start();
					if (!process.isAlive()) {
						LOG.error("Couldn't start process : "+languageServerStarter.command());
						break;
					}
					JsonBasedLanguageServer languageServer = new JsonBasedLanguageServer();
					languageServer.connect(process.getInputStream(), process.getOutputStream());
					registry.register(languageServer);
					LOG.info("Started new process "+languageServerStarter.command());
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
	}
	
	static class StreamReader implements Runnable {
		
		public Process process;
		
		public StreamReader(Process process) {
			this.process = process;
		}

		@Override
		public void run() {
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
			while (process.isAlive()) {
				try {
					String errorLine = reader.readLine();
					LOG.error("languageserver ["+process+"] : " +errorLine);
				} catch (IOException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}
		
	}

}

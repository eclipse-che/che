/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.languageserver.server.factory;

import io.typefox.lsapi.LanguageDescription;
import io.typefox.lsapi.services.LanguageServer;
import io.typefox.lsapi.services.json.JsonBasedLanguageServer;

import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.stream.Stream;

@Singleton
public class FatJarBasedLanguageServerFactory extends LanguageServerFactoryTemplate {

    private final static Logger LOG       = LoggerFactory.getLogger(FatJarBasedLanguageServerFactory.class);
    private final static String JAVA_EXEC = System.getProperty("java.home") + "/bin/java";

    @Override
    protected LanguageServer connectToLanguageServer(Process languageServerProcess) {
        JsonBasedLanguageServer languageServer = new JsonBasedLanguageServer();
        languageServer.connect(languageServerProcess.getInputStream(), languageServerProcess.getOutputStream());
        return languageServer;
    }

    @Override
    protected Process startLanguageServerProcess(String projectPath) {
        Stream<Path> paths;
        try {
            paths = Files.find(Paths.get("/projects"),
                               0,
                               (path, basicFileAttributes) -> path.endsWith("-languageserver.jar"),
                               FileVisitOption.FOLLOW_LINKS);
        } catch (IOException e) {
            String errMsg = "Can't find jar";
            LOG.error(errMsg);

            throw new IllegalStateException(e);
        }

        Path filePath;
        Iterator<Path> iterator = paths.iterator();
        if (!iterator.hasNext()) {
            filePath = iterator.next();
        } else {
            String errMsg = "Can't find jar";
            LOG.error(errMsg);

            throw new IllegalStateException(errMsg);
        }

        ProcessBuilder processBuilder = new ProcessBuilder(JAVA_EXEC, "-jar", filePath.toString(),
                                                           "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=1044",
                                                           "debug");
        processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE);
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE);

        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            String errMsg = "Can't start JSON language server";
            LOG.error(errMsg, e);

            throw new IllegalStateException(errMsg, e);
        }

        Thread thread = new Thread(new StreamReader(process));
        thread.setDaemon(true);
        thread.start();

        if (!process.isAlive()) {
            LOG.error("Couldn't start process : " + processBuilder.command());
        }

        return process;
    }

    @Override
    public LanguageDescription getLanguageDescription() {
        return null;
    }

    private static class StreamReader implements Runnable {
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

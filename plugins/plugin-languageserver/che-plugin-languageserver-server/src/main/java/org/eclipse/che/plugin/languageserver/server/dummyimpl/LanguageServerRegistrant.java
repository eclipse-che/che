package org.eclipse.che.plugin.languageserver.server.dummyimpl;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.che.plugin.languageserver.server.LanguageServerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.typefox.lsapi.json.JsonBasedLanguageServer;

@Singleton
public class LanguageServerRegistrant {

    private final static Logger LOG = LoggerFactory.getLogger(LanguageServerRegistrant.class);
    
    private final static String JAVA_EXEC = System.getProperty("java.home")+"/bin/java";

    @Inject
    public void registerLanguageServer(LanguageServerRegistry registry) {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                String suffix = "-languageserver.jar";
                Set<String> successfullyRegistered = new HashSet<>();
                while (true) {
                    for (File file : new File("/projects").listFiles()) {
                        if (file.getName().endsWith(suffix)) {
                            if (successfullyRegistered.add(file.getName())) {
                                try {
                                    ProcessBuilder languageServerStarter = new ProcessBuilder(JAVA_EXEC, "-jar", file.getAbsolutePath(), "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=1044", "debug");
                                    languageServerStarter.redirectInput(Redirect.PIPE);
                                    languageServerStarter.redirectOutput(Redirect.PIPE);
                                    Process process = languageServerStarter.start();
                                    JsonBasedLanguageServer languageServer = new JsonBasedLanguageServer();
                                    languageServer.connect(process.getInputStream(), process.getOutputStream());
                                    String name = file.getName();
                                    String[] extensions = name.substring(0, name.length() - suffix.length()).split("-");
                                    for (int i = 0; i < extensions.length; i++) {
                                        String extension = extensions[i];
                                        registry.registerForExtension(extension, languageServer);
                                        LOG.info("Registered language server for extension '"+extension+"'.");
                                    }
                                } catch (IOException e) {
                                    successfullyRegistered.remove(file.getName());
                                    LOG.error(e.getMessage(), e);
                                }
                            }
                        }
                    }
                    try {
                        Thread.sleep(10_000L);
                    } catch (InterruptedException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
            }
            
        };
        new Thread(runnable).start();
    }
}

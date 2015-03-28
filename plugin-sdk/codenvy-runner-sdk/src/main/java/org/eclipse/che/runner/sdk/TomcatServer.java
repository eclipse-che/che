/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.runner.sdk;

import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.CommandLine;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.api.core.util.StreamPump;
import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.api.runner.RunnerException;
import org.eclipse.che.api.runner.internal.ApplicationLogger;
import org.eclipse.che.api.runner.internal.ApplicationLogsPublisher;
import org.eclipse.che.api.runner.internal.ApplicationProcess;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.ZipUtils;
import com.google.common.io.CharStreams;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

/**
 * {@link ApplicationServer} implementation to deploy application to Apache Tomcat servlet container.
 *
 * @author Artem Zatsarynnyy
 * @author Eugene Voevodin
 */
@Singleton
public class TomcatServer implements ApplicationServer {
    public static final  String MEM_SIZE_PARAMETER = "runner.tomcat.memory";
    private static final Logger LOG                = LoggerFactory.getLogger(TomcatServer.class);
    private static final String SERVER_XML         =
            "<?xml version='1.0' encoding='utf-8'?>\n" +
            "<Server port=\"-1\">\n" +
            "  <Listener className=\"org.apache.catalina.core.AprLifecycleListener\" SSLEngine=\"on\" />\n" +
            "  <Listener className=\"org.apache.catalina.core.JasperListener\" />\n" +
            "  <Listener className=\"org.apache.catalina.core.JreMemoryLeakPreventionListener\" />\n" +
            "  <Listener className=\"org.apache.catalina.mbeans.GlobalResourcesLifecycleListener\" />\n" +
            "  <Listener className=\"org.apache.catalina.core.ThreadLocalLeakPreventionListener\" />\n" +
            "  <Service name=\"Catalina\">\n" +
            "    <Connector port=\"${PORT}\" protocol=\"HTTP/1.1\"\n" +
            "               connectionTimeout=\"20000\" />\n" +
            "    <Engine name=\"Catalina\" defaultHost=\"localhost\">\n" +
            "      <Host name=\"localhost\"  appBase=\"webapps\"\n" +
            "            unpackWARs=\"true\" autoDeploy=\"true\">\n" +
            "      </Host>\n" +
            "    </Engine>\n" +
            "  </Service>\n" +
            "</Server>\n";

    private final int                        memSize;
    private final ApplicationUpdaterRegistry applicationUpdaterRegistry;
    private final EventService               eventService;

    @Inject
    public TomcatServer(@Named(MEM_SIZE_PARAMETER) int memSize, ApplicationUpdaterRegistry applicationUpdaterRegistry,
                        EventService eventService) {
        this.memSize = memSize;
        this.applicationUpdaterRegistry = applicationUpdaterRegistry;
        this.eventService = eventService;
    }

    @Override
    public final String getName() {
        return "tomcat7";
    }

    @Override
    public String getDescription() {
        return "Apache Tomcat 7.0 is an implementation of the Java Servlet and JavaServer Pages technologies.\n" +
               "Home page: http://tomcat.apache.org/";
    }

    @Override
    public ApplicationProcess deploy(final java.io.File workDir,
                                     ZipFile warToDeploy,
                                     final java.io.File extensionJar,
                                     final SDKRunnerConfiguration runnerConfiguration,
                                     CodeServer.CodeServerProcess codeServerProcess,
                                     ApplicationProcess.Callback callback) throws RunnerException {
        final Path tomcatPath;
        final Path webappsPath;
        final Path apiAppContextPath;
        try {
            tomcatPath = Files.createDirectory(workDir.toPath().resolve("tomcat"));
            ZipUtils.unzip(Utils.getTomcatBinaryDistribution().openStream(), tomcatPath.toFile());
            webappsPath = tomcatPath.resolve("webapps");
            ZipUtils.unzip(new java.io.File(warToDeploy.getName()), webappsPath.resolve("ws").toFile());
            generateServerXml(tomcatPath.toFile(), runnerConfiguration);

            // add JAR with extension to 'api' application's 'lib' directory
            apiAppContextPath = webappsPath.resolve("api");
            ZipUtils.unzip(new java.io.File(webappsPath.resolve("api.war").toString()), apiAppContextPath.toFile());
            IoUtil.copy(extensionJar, apiAppContextPath.resolve("WEB-INF/lib").resolve(extensionJar.getName()).toFile(), null);
        } catch (IOException e) {
            throw new RunnerException(e);
        }

        ApplicationProcess process;
        if (SystemInfo.isUnix()) {
            process = startUnix(workDir, runnerConfiguration, codeServerProcess, callback);
        } else {
            process = startWindows(workDir, runnerConfiguration, codeServerProcess, callback);
        }

        // TODO: unregister updater
        registerUpdater(process, new ApplicationUpdater() {
            @Override
            public void update() throws RunnerException {
                try {
                    final ProjectDescriptor projectDescriptor = runnerConfiguration.getRequest().getProjectDescriptor();
                    final java.io.File destinationDir = Files.createTempDirectory(workDir.toPath(), "sources-").toFile();
                    final java.io.File exportProject = Utils.exportProject(projectDescriptor, destinationDir);
                    final java.io.File sourcesDir = Files.createTempDirectory(workDir.toPath(), "sources-build-").toFile();
                    ZipUtils.unzip(exportProject, sourcesDir);
                    ZipFile artifact = Utils.buildProjectFromSources(sourcesDir.toPath(), extensionJar.getName());
                    // add JAR with extension to 'api' application's 'lib' directory
                    IoUtil.copy(new java.io.File(artifact.getName()),
                                apiAppContextPath.resolve("WEB-INF/lib").resolve(extensionJar.getName()).toFile(), null);
                    LOG.debug("Extension {} updated", workDir);
                } catch (Exception e) {
                    LOG.error("Unable to update extension: {}", workDir);
                    throw new RunnerException(e);
                }
            }
        });

        return process;
    }

    private void registerUpdater(ApplicationProcess process, ApplicationUpdater updater) {
        applicationUpdaterRegistry.registerUpdater(process, updater);
    }

    protected void generateServerXml(java.io.File tomcatDir, SDKRunnerConfiguration runnerConfiguration) throws IOException {
        final String cfg = SERVER_XML.replace("${PORT}", Integer.toString(runnerConfiguration.getHttpPort()));
        final java.io.File serverXmlFile = new java.io.File(new java.io.File(tomcatDir, "conf"), "server.xml");
        Files.write(serverXmlFile.toPath(), cfg.getBytes());
    }

    public int getMemSize() {
        return memSize;
    }

    @Override
    public String toString() {
        return "Tomcat Server";
    }

    // *nix

    protected ApplicationProcess startUnix(java.io.File appDir, SDKRunnerConfiguration runnerCfg,
                                           CodeServer.CodeServerProcess codeServerProcess, ApplicationProcess.Callback callback)
            throws RunnerException {
        final java.io.File startUpScriptFile;
        final java.io.File logsDir = new java.io.File(appDir, "logs");
        try {
            startUpScriptFile = genStartUpScriptUnix(appDir, runnerCfg);
            updateSetenvFileUnix(appDir, runnerCfg);
            Files.createDirectory(logsDir.toPath());
        } catch (IOException e) {
            throw new RunnerException(e);
        }
        final List<java.io.File> logFiles = new ArrayList<>(1);
        logFiles.add(new java.io.File(logsDir, "output.log"));
        return new TomcatProcess(appDir, startUpScriptFile, logFiles, runnerCfg, callback, codeServerProcess, eventService);
    }

    private java.io.File genStartUpScriptUnix(java.io.File appDir, SDKRunnerConfiguration runnerConfiguration) throws IOException {
        final String startupScript = "#!/bin/sh\n" +
                                     exportEnvVariablesUnix(runnerConfiguration) +
                                     "cd tomcat\n" +
                                     "chmod +x bin/*.sh\n" +
                                     catalinaUnix(runnerConfiguration) +
                                     "PID=$!\n" +
                                     "echo \"$PID\" > ../run.pid\n" +
                                     "wait $PID";
        final java.io.File startUpScriptFile = new java.io.File(appDir, "startup.sh");
        Files.write(startUpScriptFile.toPath(), startupScript.getBytes());
        if (!startUpScriptFile.setExecutable(true, false)) {
            throw new IOException("Unable to update attributes of the startup script");
        }
        return startUpScriptFile;
    }

    private void updateSetenvFileUnix(java.io.File tomcatDir, SDKRunnerConfiguration runnerCfg) throws IOException {
        final Path setenvShPath = tomcatDir.toPath().resolve("tomcat/bin/setenv.sh");
        final String setenvShContent =
                new String(Files.readAllBytes(setenvShPath)).replace("${PORT}", Integer.toString(runnerCfg.getHttpPort()));
        Files.write(setenvShPath, setenvShContent.getBytes());
    }

    private String exportEnvVariablesUnix(SDKRunnerConfiguration runnerConfiguration) {
        int memory = runnerConfiguration.getMemory();
        if (memory <= 0) {
            memory = getMemSize();
        }
        final StringBuilder export = new StringBuilder();
        export.append(String.format("export CATALINA_OPTS=\"-Xms%dm -Xmx%dm\"%n", memory, memory));
        export.append(String.format("export SERVER_PORT=%d%n", runnerConfiguration.getHttpPort()));
        final int debugPort = runnerConfiguration.getDebugPort();
        if (debugPort > 0) {
            /*
            From catalina.sh:
            -agentlib:jdwp=transport=$JPDA_TRANSPORT,address=$JPDA_ADDRESS,server=y,suspend=$JPDA_SUSPEND
             */
            export.append(String.format("export JPDA_ADDRESS=%d%n", debugPort));
            export.append(String.format("export JPDA_TRANSPORT=%s%n", "dt_socket"));
            export.append(String.format("export JPDA_SUSPEND=%s%n", runnerConfiguration.isDebugSuspend() ? "y" : "n"));
        }
        return export.toString();
    }

    private String catalinaUnix(SDKRunnerConfiguration runnerConfiguration) {
        final boolean debug = runnerConfiguration.getDebugPort() > 0;
        if (debug) {
            return "./bin/catalina.sh jpda run 2>&1 | tee ../logs/output.log &\n";
        }
        return "./bin/catalina.sh run 2>&1 | tee ../logs/output.log &\n";
    }

    // Windows

    protected ApplicationProcess startWindows(java.io.File appDir, SDKRunnerConfiguration runnerConfiguration,
                                              CodeServer.CodeServerProcess codeServerProcess, ApplicationProcess.Callback callback) {
        throw new UnsupportedOperationException();
    }

    private static class TomcatProcess extends ApplicationProcess {
        final int                          httpPort;
        final List<java.io.File>           logFiles;
        final int                          debugPort;
        final java.io.File                 startUpScriptFile;
        final java.io.File                 workDir;
        final CodeServer.CodeServerProcess codeServerProcess;
        final Callback                     callback;
        final String                       workspace;
        final String                       project;
        final long                         id;
        final EventService                 eventService;

        ApplicationLogger logger;
        Process           process;
        StreamPump        output;

        TomcatProcess(java.io.File appDir, java.io.File startUpScriptFile, List<java.io.File> logFiles,
                      SDKRunnerConfiguration runnerCfg, Callback callback, CodeServer.CodeServerProcess codeServerProcess,
                      EventService eventService) {
            this.httpPort = runnerCfg.getHttpPort();
            this.logFiles = logFiles;
            this.debugPort = runnerCfg.getDebugPort();
            this.startUpScriptFile = startUpScriptFile;
            this.codeServerProcess = codeServerProcess;
            this.workDir = appDir;
            this.callback = callback;
            this.eventService = eventService;
            this.workspace = runnerCfg.getRequest().getWorkspace();
            this.project = runnerCfg.getRequest().getProject();
            this.id = runnerCfg.getRequest().getId();
        }

        @Override
        public synchronized void start() throws RunnerException {
            if (process != null && ProcessUtil.isAlive(process)) {
                throw new IllegalStateException("Process is already started");
            }
            try {
                process = Runtime.getRuntime().exec(new CommandLine(startUpScriptFile.getAbsolutePath()).toShellCommand(), null, workDir);
                logger = new ApplicationLogsPublisher(new TomcatLogger(logFiles, codeServerProcess), eventService, id, workspace, project);
                output = new StreamPump();
                output.start(process, logger);
                try {
                    codeServerProcess.start();
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
                LOG.debug("Start Tomcat at port {}, application {}", httpPort, workDir);
            } catch (IOException e) {
                throw new RunnerException(e);
            }
        }

        @Override
        public synchronized void stop() throws RunnerException {
            if (process == null) {
                throw new IllegalStateException("Process is not started yet");
            }
            // Use ProcessUtil.kill(process) because java.lang.Process.destroy() method doesn't
            // kill all child processes (see http://bugs.sun.com/view_bug.do?bug_id=4770092).
            ProcessUtil.kill(process);
            if (output != null) {
                output.stop();
            }
            try {
                codeServerProcess.stop();
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
            callback.stopped();
            LOG.debug("Stop Tomcat at port {}, application {}", httpPort, workDir);
        }

        @Override
        public int waitFor() throws RunnerException {
            synchronized (this) {
                if (process == null) {
                    throw new IllegalStateException("Process is not started yet");
                }
            }
            try {
                process.waitFor();
            } catch (InterruptedException e) {
                Thread.interrupted();
                ProcessUtil.kill(process);
            } finally {
                if (output != null) {
                    output.stop();
                }
            }
            return process.exitValue();
        }

        @Override
        public synchronized int exitCode() throws RunnerException {
            if (process == null || ProcessUtil.isAlive(process)) {
                return -1;
            }
            return process.exitValue();
        }

        @Override
        public synchronized boolean isRunning() throws RunnerException {
            return process != null && ProcessUtil.isAlive(process);
        }

        @Override
        public synchronized ApplicationLogger getLogger() throws RunnerException {
            if (logger == null) {
                // is not started yet
                return ApplicationLogger.DUMMY;
            }
            return logger;
        }

        private static class TomcatLogger implements ApplicationLogger {
            final List<java.io.File>           logFiles;
            final CodeServer.CodeServerProcess codeServerProcess;

            TomcatLogger(List<java.io.File> logFiles, CodeServer.CodeServerProcess codeServerProcess) {
                this.logFiles = logFiles;
                this.codeServerProcess = codeServerProcess;
            }

            @Override
            public void getLogs(Appendable output) throws IOException {
                for (java.io.File logFile : logFiles) {
                    output.append(String.format("%n====> %1$s <====%n%n", logFile.getName()));
                    try (FileReader r = new FileReader(logFile)) {
                        CharStreams.copy(r, output);
                    }
                    output.append(System.lineSeparator());
                }
                try {
                    codeServerProcess.getLogs(output);
                } catch (Exception ignore) {
                }
            }

            @Override
            public String getContentType() {
                return "text/plain";
            }

            @Override
            public void writeLine(String line) throws IOException {
                // noop since logs already redirected to the file
            }

            @Override
            public void close() throws IOException {
            }
        }
    }
}
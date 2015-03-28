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

import org.eclipse.che.api.core.util.CommandLine;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.api.core.util.SystemInfo;
import org.eclipse.che.api.project.server.ProjectEvent;
import org.eclipse.che.api.project.server.ProjectEventListener;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.api.runner.RunnerException;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.ZipUtils;
import org.eclipse.che.ide.commons.GwtXmlUtils;
import org.eclipse.che.ide.maven.tools.Dependency;
import org.eclipse.che.ide.maven.tools.Model;
import org.eclipse.che.ide.maven.tools.Plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.inject.Singleton;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;

/**
 * GWT code server.
 *
 * @author Artem Zatsarynnyy
 */
@Singleton
public class CodeServer {
    private static final Logger LOG                    = LoggerFactory.getLogger(CodeServer.class);
    /** Id of Maven POM profile used to add (re)sources of custom extension to code server recompilation process. */
    private static final String ADD_SOURCES_PROFILE_ID = "customExtensionSources";

    /**
     * Prepare GWT code server for launching.
     *
     * @param workDirPath
     *         root directory for code server
     * @param runnerConfiguration
     *         runner configuration
     * @param extensionDescriptor
     *         descriptor of extension for which code server should be prepared
     * @param executor
     *         executor service
     * @return {@link CodeServerProcess} that may be launched
     * @throws RunnerException
     */
    public CodeServerProcess prepare(Path workDirPath,
                                     final SDKRunnerConfiguration runnerConfiguration,
                                     Utils.ExtensionDescriptor extensionDescriptor,
                                     final ExecutorService executor) throws RunnerException {
        try {
            ZipUtils.unzip(Utils.getCodenvyPlatformBinaryDistribution().openStream(), workDirPath.toFile());

            final File pom = workDirPath.resolve("pom.xml").toFile();
            final Model model = Model.readFrom(pom);

            model.dependencies()
                 .add(new Dependency(extensionDescriptor.groupId,
                                     extensionDescriptor.artifactId,
                                     extensionDescriptor.version));
            model.writeTo(pom);

            GwtXmlUtils.inheritGwtModule(IoUtil.findFile(SDKRunner.IDE_GWT_XML_FILE_NAME, workDirPath.toFile()).toPath(),
                                         extensionDescriptor.gwtModuleName);
            setCodeServerConfiguration(workDirPath.resolve("pom.xml"), workDirPath, runnerConfiguration);

            // get initial copy of project sources
            final ProjectDescriptor projectDescriptor = runnerConfiguration.getRequest().getProjectDescriptor();
            final Path extensionSourcesPath = Files.createDirectory(workDirPath.resolve("extension-sources"));
            final java.io.File file = Utils.exportProject(projectDescriptor, extensionSourcesPath.toFile());
            ZipUtils.unzip(file, extensionSourcesPath.toFile());

            if (SystemInfo.isUnix()) {
                return startUnix(workDirPath.toFile(), runnerConfiguration, extensionSourcesPath, projectDescriptor.getBaseUrl(), executor);
            } else {
                return startWindows(workDirPath.toFile(), runnerConfiguration, extensionSourcesPath, projectDescriptor.getBaseUrl(),
                                    executor);
            }
        } catch (IOException e) {
            throw new RunnerException(e);
        }
    }

    // *nix

    private CodeServerProcess startUnix(File codeServerWorkDir, SDKRunnerConfiguration runnerConfiguration, Path extensionSourcesPath,
                                        String projectApiBaseUrl, ExecutorService executor) throws RunnerException {
        java.io.File startUpScriptFile = genStartUpScriptUnix(codeServerWorkDir);
        return new CodeServerProcess(runnerConfiguration.getCodeServerBindAddress(),
                                     runnerConfiguration.getCodeServerPort(),
                                     startUpScriptFile,
                                     codeServerWorkDir,
                                     extensionSourcesPath,
                                     projectApiBaseUrl,
                                     executor);
    }

    /** Set the GWT code server configuration in the specified pom.xml file. */
    private void setCodeServerConfiguration(Path pom,
                                            Path codeServerWorkDir,
                                            SDKRunnerConfiguration runnerConfiguration) throws RunnerException {
        try {
            final Model model = Model.readFrom(pom);
            final Plugin gwtPlugin = model.getBuild()
                                          .getPluginsAsMap()
                                          .get("org.codehaus.mojo:gwt-maven-plugin");
            if (codeServerWorkDir != null) {
                gwtPlugin.setConfigProperty("codeServerWorkDir", codeServerWorkDir.toString());
            }

            final String bindAddress = runnerConfiguration.getCodeServerBindAddress();
            if (bindAddress != null) {
                gwtPlugin.setConfigProperty("bindAddress", bindAddress);
            }

            final int port = runnerConfiguration.getCodeServerPort();
            if (port != - 1) {
                gwtPlugin.setConfigProperty("codeServerPort", Integer.toString(port));
            }

            model.save();
        } catch (IOException e) {
            throw new RunnerException(e);
        }
    }

    private java.io.File genStartUpScriptUnix(java.io.File workDir) throws RunnerException {
        final String startupScript = "#!/bin/sh\n" +
                                     "cd war\n" +
                                     codeServerUnix() +
                                     "PID=$!\n" +
                                     "echo \"$PID\" > run.pid\n" +
                                     "wait $PID";
        final java.io.File startUpScriptFile = new java.io.File(workDir, "startup.sh");
        try {
            Files.write(startUpScriptFile.toPath(), startupScript.getBytes());
        } catch (IOException e) {
            throw new RunnerException(e);
        }
        if (!startUpScriptFile.setExecutable(true, false)) {
            throw new RunnerException("Unable to update attributes of the startup script");
        }
        return startUpScriptFile;
    }

    private String codeServerUnix() {
        return String
                .format("mvn clean generate-sources gwt:run-codeserver -Dgwt.compiler.incremental=false -Dgwt.module=org.eclipse.che.ide.IDEPlatform -P%s > stdout.log &\n",
                        ADD_SOURCES_PROFILE_ID);
    }

    // Windows

    private CodeServerProcess startWindows(File codeServerWorkDir, SDKRunnerConfiguration runnerConfiguration, Path extensionSourcesPath,
                                           String projectApiBaseUrl, ExecutorService executor) throws RunnerException {
        throw new UnsupportedOperationException();
    }

    public static class CodeServerProcess implements ProjectEventListener {
        private final String          bindAddress;
        private final int             port;
        private final java.io.File    startUpScriptFile;
        private final java.io.File    workDir;
        private final Path            extensionSourcesPath;
        private final String          projectApiBaseUrl;
        private final ExecutorService executor;

        private Process process;

        protected CodeServerProcess(String bindAddress, int port, File startUpScriptFile, File workDir, Path extensionSourcesPath,
                                    String projectApiBaseUrl, ExecutorService executor) {
            this.bindAddress = bindAddress;
            this.port = port;
            this.startUpScriptFile = startUpScriptFile;
            this.workDir = workDir;
            this.extensionSourcesPath = extensionSourcesPath;
            this.projectApiBaseUrl = projectApiBaseUrl;
            this.executor = executor;
        }

        public synchronized void start() throws RunnerException {
            if (process != null && ProcessUtil.isAlive(process)) {
                throw new IllegalStateException("Code server process is already started");
            }

            try {
                process = Runtime.getRuntime().exec(new CommandLine(startUpScriptFile.getAbsolutePath()).toShellCommand(), null, workDir);
                LOG.debug("Start GWT code server at port {}, working directory {}", port, workDir);
            } catch (IOException e) {
                throw new RunnerException(e);
            }
        }

        public synchronized void stop() {
            if (process == null) {
                throw new IllegalStateException("Code server process is not started yet");
            }
            ProcessUtil.kill(process);
            LOG.debug("Stop GWT code server at port {}, working directory {}", port, workDir);
        }

        public void getLogs(Appendable output) throws IOException, RunnerException {
            final String url = bindAddress + ':' + port + "/log/_app";
            final String logContent = sendGet(new URL(url.startsWith("http://") ? url : "http://" + url));
            output.append(String.format("%n====> GWT-code-server.log <===="));
            output.append(logContent);
            output.append(System.lineSeparator());
        }

        private String sendGet(URL url) throws IOException, RunnerException {
            HttpURLConnection http = null;
            try {
                http = (HttpURLConnection)url.openConnection();
                http.setRequestMethod("GET");
                int responseCode = http.getResponseCode();
                if (responseCode != 200) {
                    responseFail(http);
                }

                try (InputStream data = http.getInputStream()) {
                    return readBodyTagContent(data);
                }
            } finally {
                if (http != null) {
                    http.disconnect();
                }
            }
        }

        private String readBodyTagContent(InputStream stream) throws IOException {
            try {
                DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = domFactory.newDocumentBuilder();
                Document doc = builder.parse(stream);
                return doc.getElementsByTagName("body").item(0).getTextContent();
            } catch (ParserConfigurationException | SAXException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }

        private void responseFail(HttpURLConnection http) throws IOException, RunnerException {
            InputStream errorStream = null;
            try {
                int length = http.getContentLength();
                errorStream = http.getErrorStream();
                String body = null;
                if (errorStream != null) {
                    body = readBody(errorStream, length);
                }
                throw new RunnerException(String.format("Unable to get code server logs. %s", body == null ? "" : body));
            } finally {
                if (errorStream != null) {
                    errorStream.close();
                }
            }
        }

        private String readBody(InputStream input, int contentLength) throws IOException {
            String body = null;
            if (contentLength > 0) {
                byte[] b = new byte[contentLength];
                int off = 0;
                int i;
                while ((i = input.read(b, off, contentLength - off)) > 0) {
                    off += i;
                }
                body = new String(b);
            } else if (contentLength < 0) {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                byte[] buf = new byte[1024];
                int i;
                while ((i = input.read(buf)) != -1) {
                    bout.write(buf, 0, i);
                }
                body = bout.toString();
            }
            return body;
        }

        @Override
        public void onEvent(ProjectEvent event) {
            update(event, extensionSourcesPath, projectApiBaseUrl, executor);
        }

        private static void update(final ProjectEvent event, final Path projectMirrorPath, final String projectApiBaseUrl,
                                   ExecutorService executor) {
            if (event.getType() == ProjectEvent.EventType.DELETED) {
                IoUtil.deleteRecursive(projectMirrorPath.resolve(event.getPath()).toFile());
            } else if (event.getType() == ProjectEvent.EventType.UPDATED || event.getType() == ProjectEvent.EventType.CREATED) {
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        // connect to the project API URL
                        int index = projectApiBaseUrl.indexOf(event.getProject());
                        try {
                            HttpURLConnection conn = (HttpURLConnection)new URL(projectApiBaseUrl.substring(0, index)
                                                                                                 .concat("/file")
                                                                                                 .concat(event.getProject())
                                                                                                 .concat("/")
                                                                                                 .concat(event.getPath())).openConnection();
                            conn.setConnectTimeout(30 * 1000);
                            conn.setRequestMethod("GET");
                            conn.addRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
                            conn.setDoInput(true);
                            conn.setDoOutput(true);
                            conn.connect();

                            // if file has been found, dump the content
                            final int responseCode = conn.getResponseCode();
                            if (responseCode == HttpURLConnection.HTTP_OK) {
                                java.io.File updatedFile = new java.io.File(projectMirrorPath.toString(), event.getPath());
                                byte[] buffer = new byte[8192];
                                try (InputStream input = conn.getInputStream();
                                     OutputStream output = new FileOutputStream(updatedFile)) {
                                    int bytesRead;
                                    while ((bytesRead = input.read(buffer)) != -1) {
                                        output.write(buffer, 0, bytesRead);
                                    }
                                }
                            }
                        } catch (IOException e) {
                            LOG.error("Unable to update mirror of project {} for GWT code server.", event.getProject());
                        }
                    }
                });
            }
        }
    }
}

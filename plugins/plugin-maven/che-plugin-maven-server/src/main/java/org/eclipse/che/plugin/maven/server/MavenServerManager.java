/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.maven.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import org.eclipse.che.maven.data.MavenModel;
import org.eclipse.che.maven.server.MavenRemoteServer;
import org.eclipse.che.maven.server.MavenServer;
import org.eclipse.che.maven.server.MavenServerDownloadListener;
import org.eclipse.che.maven.server.MavenServerLogger;
import org.eclipse.che.maven.server.MavenSettings;
import org.eclipse.che.maven.server.MavenTerminal;
import org.eclipse.che.plugin.maven.server.execution.CommandLine;
import org.eclipse.che.plugin.maven.server.execution.JavaParameters;
import org.eclipse.che.plugin.maven.server.execution.ProcessExecutor;
import org.eclipse.che.plugin.maven.server.execution.ProcessHandler;
import org.eclipse.che.plugin.maven.server.rmi.RmiClient;
import org.eclipse.che.plugin.maven.server.rmi.RmiObjectWrapper;
import org.eclipse.che.rmi.RmiObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.File;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class MavenServerManager extends RmiObjectWrapper<MavenRemoteServer> {
    private static final Logger LOG               = LoggerFactory.getLogger(MavenServerManager.class);
    private static final String MAVEN_SERVER_MAIN = "org.eclipse.che.maven.server.MavenServerMain";

    private RmiClient<MavenRemoteServer> client;
    private RmiLogger                      rmiLogger           = new RmiLogger();
    private RmiMavenServerDownloadListener rmiDownloadListener = new RmiMavenServerDownloadListener();
    private boolean loggerExported;
    private boolean listenerExported;
    private String  mavenServerPath;
    private File    localRepository;

    @Inject
    public MavenServerManager(@Named("che.maven.server.path") String mavenServerPath) {
        this.mavenServerPath = mavenServerPath;

        client = new RmiClient<MavenRemoteServer>(MavenRemoteServer.class) {
            @Override
            protected ProcessExecutor getExecutor() {
                return createExecutor();
            }
        };
    }

    private static void addDirToClasspath(List<String> classPath, File dir) {
        File[] jars = dir.listFiles((dir1, name) -> {
            return name.endsWith(".jar");
        });

        if (jars == null) {
            return;
        }

        for (File jar : jars) {
            classPath.add(jar.getAbsolutePath());
        }
    }

    private ProcessExecutor createExecutor() {
        return () -> {
            JavaParameters parameters = buildMavenServerParameters();
            CommandLine command = parameters.createCommand();
            return new ProcessHandler(command.createProcess());
        };
    }

    public MavenServerWrapper createMavenServer() {
        return new MavenServerWrapper() {
            @Override
            protected MavenServer create() throws RemoteException {
                MavenSettings mavenSettings = new MavenSettings();
                //TODO add more user settings
                mavenSettings.setMavenHome(new File(System.getenv("M2_HOME")));
                mavenSettings.setUserSettings(new File(System.getProperty("user.home"), ".m2/settings.xml"));
                // Setting Global maven setting
                // for more maven info settings visit https://maven.apache.org/settings.html
                mavenSettings.setGlobalSettings(new File(System.getenv("M2_HOME"), "conf/settings.xml"));
                mavenSettings.setLoggingLevel(MavenTerminal.LEVEL_INFO);
                if (localRepository != null) {
                    mavenSettings.setLocalRepository(localRepository);
                }
                return MavenServerManager.this.getOrCreateWrappedObject().createServer(mavenSettings);
            }
        };
    }

    /**
     * For test use only. Sets the path to local maven repository
     *
     * @param localRepository
     */
    public void setLocalRepository(File localRepository) {
        this.localRepository = localRepository;
    }

    public MavenModel interpolateModel(MavenModel model, File projectDir) {
        return perform(() -> getOrCreateWrappedObject().interpolateModel(model, projectDir));
    }

    @PreDestroy
    public void shutdown() {
        client.stopAll(false);
        cleanUp();
    }

    @Override
    protected MavenRemoteServer create() throws RemoteException {
        MavenRemoteServer server;
        try {
            server = client.acquire(this, "");
        } catch (Exception e) {
            throw new RemoteException("Can't start maven server", e);
        }
        if (!loggerExported) {
            Remote loggerRemote = UnicastRemoteObject.exportObject(rmiLogger, 0);
            if (!(loggerExported = loggerRemote != null)) {
                throw new RemoteException("Can't export logger");
            }
        }
        if (!listenerExported) {
            Remote listenerRemote = UnicastRemoteObject.exportObject(rmiDownloadListener, 0);
            if (!(listenerExported = listenerRemote != null)) {
                throw new RemoteException("Can't export download listener");
            }
        }

        server.configure(rmiLogger, rmiDownloadListener);

        return server;
    }

    @Override
    protected synchronized void cleanUp() {
        super.cleanUp();

        if (loggerExported) {
            try {
                UnicastRemoteObject.unexportObject(rmiLogger, true);
            } catch (NoSuchObjectException e) {
                LOG.error("Can't unexport RMI logger", e);
            }

            loggerExported = false;
        }

        if (listenerExported) {
            try {
                UnicastRemoteObject.unexportObject(rmiDownloadListener, true);
            } catch (NoSuchObjectException e) {
                LOG.error("Can't unexport RMI artifact download listener", e);
            }
            listenerExported = false;
        }

    }

    public JavaParameters buildMavenServerParameters() {
        JavaParameters parameters = new JavaParameters();
        parameters.setJavaExecutable("java");
        parameters.setWorkingDirectory(System.getProperty("java.io.tmpdir"));
        parameters.setMainClassName(MAVEN_SERVER_MAIN);
        //TODO read and set MAVEN_OPTS system properties

        List<String> classPath = new ArrayList<>();
        addDirToClasspath(classPath, new File(mavenServerPath));

        String mavenHome = System.getenv("M2_HOME");
        addDirToClasspath(classPath, new File(mavenHome, "lib"));
        File bootDir = new File(mavenHome, "boot");
        File[] classworlds = bootDir.listFiles((dir, name) -> {
            return name.contains("classworlds");
        });

        if (classworlds != null) {
            for (File file : classworlds) {
                classPath.add(file.getAbsolutePath());
            }
        }

        parameters.getClassPath().addAll(classPath);

        parameters.getVmParameters().add("-Xmx512m");

        return parameters;
    }

    private <T> T perform(RunnableRemoteWithResult<T> runnable) {
        RemoteException exception = null;
        for (int i = 0; i < 2; i++) {
            try {
                return runnable.perform();
            } catch (RemoteException e) {
                exception = e;
                onError();
            }
        }
        throw new RuntimeException(exception);
    }


    private interface RunnableRemoteWithResult<T> {
        T perform() throws RemoteException;
    }

    private class RmiLogger extends RmiObject implements MavenServerLogger {

        @Override
        public void info(Throwable t) throws RemoteException {
            LOG.info(t.getMessage(), t);
        }

        @Override
        public void warning(Throwable t) throws RemoteException {
            LOG.warn(t.getMessage(), t);
        }

        @Override
        public void error(Throwable t) throws RemoteException {
            LOG.error(t.getMessage(), t);
        }
    }

    private class RmiMavenServerDownloadListener extends RmiObject implements MavenServerDownloadListener {

        @Override
        public void artifactDownloaded(File file, String relativePath) throws RemoteException {
            System.out.println("On download - " + relativePath);
            //todo notify browser about that
        }
    }
}

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
package org.eclipse.che.plugin.internal.installer;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.plugin.internal.PluginInstallImpl;
import org.eclipse.che.plugin.internal.api.IPluginInstall;
import org.eclipse.che.plugin.internal.api.PluginConfiguration;
import org.eclipse.che.plugin.internal.api.PluginInstaller;
import org.eclipse.che.plugin.internal.api.PluginInstallerException;
import org.eclipse.che.plugin.internal.api.PluginInstallerNotFoundException;

import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static org.eclipse.che.plugin.internal.api.IPluginInstallStatus.FAILED;
import static org.eclipse.che.plugin.internal.api.IPluginInstallStatus.IN_PROGRESS;
import static org.eclipse.che.plugin.internal.api.IPluginInstallStatus.SUCCESS;

/**
 * Handle the installation of plugin extensions and plugins
 * @author Florent Benoit
 */
@Singleton
public class PluginInstallerImpl implements PluginInstaller {

    /**
     * Unique generator of tasks starting at 1.
     */
    private final AtomicLong idGenerator = new AtomicLong(1);

    /**
     * Configuration used to setup this installer
     */
    private final PluginConfiguration pluginConfiguration;

    /**
     * Current execution in progress (if any)
     */
    private AtomicReference<ListenableFuture<Integer>> currentExecution;

    /**
     * Map between current executions and the result
     */
    private Map<Long, PluginInstallImpl> executions;

    /**
     * Current executor.
     */
    private ListeningExecutorService executor;


    /**
     * Setup a new installer based on the given configuration
     * @param pluginConfiguration that will be used to find ChE_HOME or install script
     */
    @Inject
    public PluginInstallerImpl(PluginConfiguration pluginConfiguration) {

        this.currentExecution = new AtomicReference<>();

        this.pluginConfiguration = pluginConfiguration;
        ExecutorService threadPoolExecutor =
                Executors.newFixedThreadPool(2,
                                             new ThreadFactoryBuilder().setNameFormat(PluginInstallerImpl.class.getSimpleName() + "-[%d]")
                                                                       .setDaemon(true).build());

        // decorate this executor to allow callback/listener
        this.executor = MoreExecutors.listeningDecorator(threadPoolExecutor);

        this.executions = new ConcurrentHashMap<>();
    }


    /**
     * Ask to install/uninstall step
     * @param pluginInstallerCallback an optional callback used to notify if install is success or failed
     * @return ID of the current install
     */
    @Override
    public IPluginInstall requireNewInstall(FutureCallback pluginInstallerCallback) throws PluginInstallerException {
        if (currentExecution.get() != null) {
            throw new PluginInstallerException("There is already install in progress. Wait that this install is finished");
        }

        Long id = idGenerator.getAndIncrement();
        PluginInstallImpl pluginInstall = new PluginInstallImpl(id);
        final ListenableFuture<Integer> job = this.executor.submit(() -> {

            // run ext script
            ProcessBuilder pb = new ProcessBuilder(pluginConfiguration.getInstallScript().toAbsolutePath().toString());
            pb.directory(pluginConfiguration.getCheHome().toFile());
            pb = pb.redirectErrorStream(true);
            Process p = pb.start();

            // collect stream
            InputStream is = p.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, Charset.defaultCharset());
            BufferedReader br = new BufferedReader(isr);
            String line;
            String fullLog = "";
            pluginInstall.setLog(fullLog);
            while ((line = br.readLine()) != null) {
                fullLog += (line + "\n");
                pluginInstall.setLog(fullLog);
            }

            return p.waitFor();
        });
        pluginInstall.setFuture(job);


        currentExecution.set(job);

        if (pluginInstallerCallback != null) {
            Futures.addCallback(job, pluginInstallerCallback);
        }

        Futures.addCallback(job, new IntegerFutureCallback(pluginInstall, pluginInstallerCallback));

        job.addListener(() -> {
            resetCurrentExecution();
        }, this.executor);



        this.executions.put(id, pluginInstall);

        // wait a little so empty script callbacks could be setup
        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            throw new PluginInstallerException("Unable to get install ID", e);
        }

        return pluginInstall;
    }


    protected void resetCurrentExecution() {
        this.currentExecution.set(null);
    }


    @PreDestroy
    public void stop() {
        this.executor.shutdown();
    }


    @Override
    public IPluginInstall getInstall(long id) throws PluginInstallerNotFoundException {
        if (this.executions.containsKey(id)) {
            PluginInstallImpl pluginInstall = this.executions.get(id);
            ListenableFuture<Integer> future = pluginInstall.getFuture();

            if (future.isDone()) {
                return pluginInstall;
            } else {
                return pluginInstall.setStatus(IN_PROGRESS);
            }
        }
        throw new PluginInstallerNotFoundException("Unknown install identifier :" + id);
    }


    /**
     * Gets the list of all install
     * @return the IDs of every install
     */
    @Override
    public List<IPluginInstall> listInstall()  {
        List<IPluginInstall> pluginInstalls = new ArrayList<>();
        Iterator<Map.Entry<Long, PluginInstallImpl>> iterator = this.executions.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Long, PluginInstallImpl> entry = iterator.next();
            pluginInstalls.add(new PluginInstallImpl(entry.getKey()).setStatus(entry.getValue().getStatus()));
        }
        return pluginInstalls;

    }


    protected PluginConfiguration getPluginConfiguration() {
        return this.pluginConfiguration;
    }

    private static class IntegerFutureCallback implements FutureCallback<Integer> {
        private final PluginInstallImpl pluginInstall;
        private final FutureCallback pluginInstallerCallback;

        public IntegerFutureCallback(PluginInstallImpl pluginInstall, FutureCallback pluginInstallerCallback) {
            this.pluginInstall = pluginInstall;
            this.pluginInstallerCallback = pluginInstallerCallback;
        }

        @Override
    public void onSuccess(Integer exitValue) {
        if (exitValue != null && 0 == exitValue) {
            pluginInstall.setStatus(SUCCESS);
        } else {
            pluginInstallerCallback
                    .onFailure(new PluginInstallerException("Program exited with exit code different than 0: " + exitValue));
            pluginInstall.setStatus(FAILED);
        }
    }

        @Override
    public void onFailure(Throwable throwable) {
        pluginInstall.setError(throwable);
        pluginInstall.setStatus(FAILED);
    }
    }
}

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
package org.eclipse.che.ide.extension.maven.server.core;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.inject.Inject;

import org.eclipse.che.api.core.util.CancellableProcessWrapper;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.api.core.util.StreamPump;
import org.eclipse.che.api.core.util.Watchdog;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.RegisteredProject;
import org.eclipse.che.ide.ext.java.server.classpath.ClassPathBuilder;
import org.eclipse.che.ide.ext.java.shared.dto.ClassPathBuilderResult;
import org.eclipse.che.ide.extension.maven.server.projecttype.MavenClassPathConfigurator;
import org.eclipse.che.ide.maven.tools.MavenUtils;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Implementation of classpath building for the Maven.
 *
 * @author Valeriy Svydenko
 */
public class MavenClassPathBuilder implements ClassPathBuilder {
    private static final Logger LOG = LoggerFactory.getLogger(MavenClassPathBuilder.class);

    private final ExecutorService executorService;
    private final ProjectManager  projectManager;

    private String workspaceId;

    @Inject
    public MavenClassPathBuilder(ResourcesPlugin resourcesPlugin, ProjectManager projectManager) {
        this.projectManager = projectManager;
        JavaModelManager.getJavaModelManager().containerInitializersCache.put(MavenClasspathContainer.CONTAINER_ID,
                                                                              new MavenClasspathContainerInitializer());

        ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat(MavenClassPathBuilder.class.getSimpleName() + "-%d").build();

        executorService = Executors.newFixedThreadPool(5, threadFactory);
    }

    /** {@inheritDoc} */
    @Override
    public ClassPathBuilderResult buildClassPath(String workspaceId, String projectPath) throws ExecutionException, InterruptedException {
        this.workspaceId = workspaceId;

        //TODO Temporary solution for IDEX-4270
        try {
            RegisteredProject project = projectManager.getProject(projectPath);
            if (project != null) {
                MavenClassPathConfigurator.configure(project.getBaseFolder());
            }
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }

        Callable<ClassPathBuilderResult> callable = () -> {

            ClassPathBuilderResult result = dependencyUpdateProcessor(projectPath);

            IJavaProject javaProject = JavaModelManager.getJavaModelManager().getJavaModel().getJavaProject(projectPath);

            if (ClassPathBuilderResult.Status.SUCCESS.equals(result.getStatus())) {
                IClasspathContainer container = MavenClasspathUtil.readMavenClasspath(javaProject);
                try {
                    JavaCore.setClasspathContainer(container.getPath(), new IJavaProject[]{javaProject},
                                                   new IClasspathContainer[]{container},
                                                   null);
                } catch (JavaModelException e) {
                    LOG.error(e.getMessage(), e);
                }
            }

            return result;

        };

        return executorService.submit(callable).get();
    }

    private ClassPathBuilderResult dependencyUpdateProcessor(String projectPath) {
        String command = MavenUtils.getMavenExecCommand();
        File projectDir = new File(ResourcesPlugin.getPathToWorkspace() + projectPath);

        ProcessBuilder classPathProcessBuilder = new ProcessBuilder().command(command, "dependency:build-classpath",
                                                                              "-Dmdep.outputFile=.codenvy/classpath.maven")
                                                                     .directory(projectDir)
                                                                     .redirectErrorStream(true);
        ClassPathBuilderResult result = executeBuilderProcess(projectPath, classPathProcessBuilder);

        if (ClassPathBuilderResult.Status.SUCCESS.equals(result.getStatus())) {
            ProcessBuilder sourcesProcessBuilder = new ProcessBuilder().command(command, "dependency:sources", "-Dclassifier=sources")
                                                                       .directory(projectDir)
                                                                       .redirectErrorStream(true);
            result = executeBuilderProcess(projectPath, sourcesProcessBuilder);
        }

        return result;
    }

    private ClassPathBuilderResult executeBuilderProcess(final String projectPath, ProcessBuilder processBuilder) {
        StreamPump output = null;
        Watchdog watcher = null;

        ClassPathBuilderResult classPathBuilderResult = newDto(ClassPathBuilderResult.class);
        int timeout = 10; //10 minutes
        int result = -1;
        try {
            Process process = processBuilder.start();

            watcher = new Watchdog("Maven classpath" + "-WATCHDOG", timeout, TimeUnit.MINUTES);
            watcher.start(new CancellableProcessWrapper(process,
                                                        cancellable -> LOG.warn("Update dependency process has been shutdown "
                                                                                + "due to timeout. Project: "
                                                                                + projectPath)));

            String channel = "dependencyUpdate:output:" + workspaceId + ':' + projectPath;

            classPathBuilderResult.setChannel(channel);

            BufferOutputFixedRateSender fixedRateSender = new BufferOutputFixedRateSender(channel, 2_000);
            output = new StreamPump();
            output.start(process, fixedRateSender);
            try {
                result = process.waitFor();
            } catch (InterruptedException e) {
                Thread.interrupted(); // we interrupt thread when cancel task
                ProcessUtil.kill(process);
            }
            try {
                output.await(); // wait for logger

                fixedRateSender.close();
            } catch (InterruptedException e) {
                Thread.interrupted(); // we interrupt thread when cancel task, NOTE: logs may be incomplete
            }
        } catch (IOException e) {
            LOG.error("", e);
        } finally {
            if (watcher != null) {
                watcher.stop();
            }
            if (output != null) {
                output.stop();
            }
        }

        classPathBuilderResult.setStatus(result == 0 ? ClassPathBuilderResult.Status.SUCCESS : ClassPathBuilderResult.Status.ERROR);

        return classPathBuilderResult;
    }

    @PreDestroy
    public void destroy() {
        executorService.shutdown();
    }
}

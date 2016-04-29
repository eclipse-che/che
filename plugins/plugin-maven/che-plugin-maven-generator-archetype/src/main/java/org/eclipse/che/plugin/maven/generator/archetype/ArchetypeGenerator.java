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
package org.eclipse.che.plugin.maven.generator.archetype;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.util.CommandLine;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.api.core.util.StreamPump;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.ZipUtils;
import org.eclipse.che.plugin.maven.generator.archetype.dto.MavenArchetype;
import org.eclipse.che.ide.maven.tools.MavenUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates projects with maven-archetype-plugin.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class ArchetypeGenerator {
    private static final Logger     LOG            = LoggerFactory.getLogger(ArchetypeGenerator.class);
    private static final AtomicLong taskIdSequence = new AtomicLong(1);
    private final ConcurrentMap<Long, GenerationTask> tasks;
    /** Time of keeping the results (generated projects and logs) of project generation. After this time the results may be removed. */
    private final long                                keepResultTimeMillis;
    private       ExecutorService                     executor;
    private       ScheduledExecutorService            scheduler;
    private       Path                                archetypeGeneratorTempFolder;

    @Inject
    public ArchetypeGenerator() {
        this.keepResultTimeMillis = TimeUnit.SECONDS.toMillis(60);
        tasks = new ConcurrentHashMap<>();
    }

    /** Initialize generator. */
    @PostConstruct
    void start() throws IOException {
        archetypeGeneratorTempFolder = Files.createTempDirectory("archetype-generator");

        executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("ArchetypeGenerator-[%d]").setDaemon(true).build());
        scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactoryBuilder().setNameFormat("ArchetypeGeneratorSchedulerPool-%d")
                                                                                         .setDaemon(true).build());
        scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                int num = 0;
                for (Iterator<GenerationTask> i = tasks.values().iterator(); i.hasNext(); ) {
                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    final GenerationTask task = i.next();
                    if (task.isExpired()) {
                        i.remove();
                        try {
                            cleanup(task);
                        } catch (RuntimeException e) {
                            LOG.error(e.getMessage(), e);
                        }
                        num++;
                    }
                }
                if (num > 0) {
                    LOG.debug("Remove {} expired tasks", num);
                }
            }
        }, 1, 1, TimeUnit.MINUTES);
    }

    /** Stops generator and releases any associated resources. */
    @PreDestroy
    void stop() {
        boolean interrupted = false;
        scheduler.shutdownNow();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                LOG.warn("Unable to terminate scheduler");
            }
        } catch (InterruptedException e) {
            interrupted = true;
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                executor.shutdownNow();
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    LOG.warn("Unable to terminate main pool");
                }
            }
        } catch (InterruptedException e) {
            interrupted |= true;
            executor.shutdownNow();
        }
        if (IoUtil.deleteRecursive(archetypeGeneratorTempFolder.toFile())) {
            LOG.warn("Error occurs on removing " + archetypeGeneratorTempFolder.toString());
        }

        tasks.clear();
        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    public GenerationTask getTaskById(Long id) throws ServerException {
        final GenerationTask task = tasks.get(id);
        if (task == null) {
            throw new ServerException(String.format("Invalid task id: %d", id));
        }
        return task;
    }

    /**
     * Generates a new project from the specified archetype.
     *
     * @param archetype
     *         archetype from which need to generate new project
     * @param groupId
     *         groupId of new project
     * @param artifactId
     *         artifactId of new project
     * @param version
     *         version of new project
     * @return generating task
     * @throws ServerException
     *         if an error occurs while generating project
     */
    public GenerationTask generateFromArchetype(@NotNull MavenArchetype archetype, @NotNull String groupId, @NotNull String artifactId,
                                                @NotNull String version) throws ServerException {
        Map<String, String> archetypeProperties = new HashMap<>();
        archetypeProperties.put("-DinteractiveMode", "false"); // get rid of the interactivity of the archetype plugin
        archetypeProperties.put("-DarchetypeGroupId", archetype.getGroupId());
        archetypeProperties.put("-DarchetypeArtifactId", archetype.getArtifactId());
        archetypeProperties.put("-DarchetypeVersion", archetype.getVersion());
        archetypeProperties.put("-DgroupId", groupId);
        archetypeProperties.put("-DartifactId", artifactId);
        archetypeProperties.put("-Dversion", version);
        if (archetype.getRepository() != null) {
            archetypeProperties.put("-DarchetypeRepository", archetype.getRepository());
        }
        if (archetype.getProperties() != null) {
            archetypeProperties.putAll(archetype.getProperties());
        }

        final File workDir;
        try {
            workDir = Files.createTempDirectory(archetypeGeneratorTempFolder, "project-").toFile();
        } catch (IOException e) {
            throw new ServerException(e);
        }

        final File logFile = new File(workDir, workDir.getName() + ".log");
        final GeneratorLogger logger = createLogger(logFile);
        final CommandLine commandLine = createCommandLine(archetypeProperties);
        final Callable<Boolean> callable = createTaskFor(commandLine, logger, workDir);
        final Long internalId = taskIdSequence.getAndIncrement();
        final GenerationTask task = new GenerationTask(callable, internalId, workDir, artifactId, logger);
        tasks.put(internalId, task);
        executor.execute(task);
        return task;
    }

    private GeneratorLogger createLogger(File logFile) throws ServerException {
        try {
            return new GeneratorLogger(logFile);
        } catch (IOException e) {
            throw new ServerException(e);
        }
    }

    private CommandLine createCommandLine(Map<String, String> archetypeProperties) throws ServerException {
        final CommandLine commandLine = new CommandLine(MavenUtils.getMavenExecCommand());
        commandLine.add("--batch-mode");
        commandLine.add("org.apache.maven.plugins:maven-archetype-plugin:RELEASE:generate");
        commandLine.add(archetypeProperties);
        return commandLine;
    }

    private Callable<Boolean> createTaskFor(final CommandLine commandLine,
                                            final GeneratorLogger logger,
                                            final File workDir) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                StreamPump output = null;
                int result = -1;
                try {
                    ProcessBuilder processBuilder = new ProcessBuilder().command(commandLine.toShellCommand())
                                                                        .directory(workDir).redirectErrorStream(true);
                    Process process = processBuilder.start();

                    output = new StreamPump();
                    output.start(process, logger);
                    try {
                        result = process.waitFor();
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                        ProcessUtil.kill(process);
                    }
                    try {
                        output.await(); // wait for logger
                    } catch (InterruptedException e) {
                        Thread.interrupted();
                    }
                } finally {
                    if (output != null) {
                        output.stop();
                    }
                }
                LOG.debug("Done: {}, exit code: {}", commandLine, result);
                return result == 0;
            }
        };
    }

    /**
     * Gets result of GenerationTask.
     *
     * @param task
     *         task
     * @param successful
     *         reports whether generate process terminated normally or not.
     *         Note: {@code true} is not indicated successful generating but only normal process termination.
     * @return GenerationResult
     * @throws ServerException
     *         if an error occurs when try to get result
     */
    private GenerationResult getTaskResult(GenerationTask task, boolean successful) throws ServerException {
        if (!successful) {
            return new GenerationResult(false, null, getLogFile(task));
        }

        boolean mavenSuccess = false;
        BufferedReader logReader = null;
        try {
            logReader = new BufferedReader(task.getLogger().getReader());
            String line;
            while ((line = logReader.readLine()) != null) {
                line = MavenUtils.removeLoggerPrefix(line);
                if ("BUILD SUCCESS".equals(line)) {
                    mavenSuccess = true;
                    break;
                }
            }
        } catch (IOException e) {
            throw new ServerException(e);
        } finally {
            if (logReader != null) {
                try {
                    logReader.close();
                } catch (IOException ignored) {
                }
            }
        }

        if (!mavenSuccess) {
            return new GenerationResult(false, null, getLogFile(task));
        }

        final File workDir = task.getWorkDir();
        final GenerationResult result = new GenerationResult(true, null, getLogFile(task));

        final File projectFolder = new File(workDir, task.getArtifactId());
        if (projectFolder.isDirectory() && projectFolder.list().length > 0) {
            final File zip = new File(workDir, "project.zip");
            try {
                ZipUtils.zipDir(projectFolder.getAbsolutePath(), projectFolder, zip, IoUtil.ANY_FILTER);
            } catch (IOException e) {
                throw new ServerException(e);
            }
            result.setGeneratedProject(zip);
        }

        return result;
    }

    private File getLogFile(GenerationTask task) {
        return task.getLogger().getFile();
    }

    private void cleanup(GenerationTask task) {
        File workDir = task.getWorkDir();
        if (workDir != null && workDir.exists()) {
            if (!IoUtil.deleteRecursive(workDir)) {
                LOG.warn("Unable to delete directory {}", workDir);
            }
        }
    }

    /** Logger that will write to file all the logs of the project generation process. */
    private static class GeneratorLogger implements LineConsumer {
        private final File    file;
        private final Writer  writer;
        private final boolean autoFlush;

        GeneratorLogger(File file) throws IOException {
            this.file = file;
            autoFlush = true;
            writer = Files.newBufferedWriter(file.toPath(), Charset.defaultCharset());
        }

        Reader getReader() throws IOException {
            return Files.newBufferedReader(file.toPath(), Charset.defaultCharset());
        }

        /** Get {@code File} where logs stored. */
        File getFile() {
            return file;
        }

        @Override
        public void writeLine(String line) throws IOException {
            if (line != null) {
                writer.write(line);
            }
            writer.write('\n');
            if (autoFlush) {
                writer.flush();
            }
        }

        @Override
        public void close() throws IOException {
            writer.close();
        }
    }

    class GenerationTask extends FutureTask<Boolean> {
        private final Long             id;
        private final File             workDir;
        private final String           artifactId;
        private final GeneratorLogger  logger;
        private       GenerationResult result;
        /** Time when task was done (successfully ends, fails, cancelled) or -1 if task is not done yet. */
        private       long             endTime;

        GenerationTask(Callable<Boolean> callable, Long id, File workDir, String artifactId, GeneratorLogger logger) {
            super(callable);
            this.id = id;
            this.workDir = workDir;
            this.artifactId = artifactId;
            this.logger = logger;
            endTime = -1L;
        }

        Long getId() {
            return id;
        }

        @Override
        protected void done() {
            super.done();
            endTime = System.currentTimeMillis();
            try {
                logger.close();
                LOG.debug("Close logger {}", logger);
            } catch (IOException e) {
                LOG.warn(e.getMessage(), e);
            }
        }

        GeneratorLogger getLogger() {
            return logger;
        }

        /**
         * Get result of project generation.
         *
         * @return result of project generating or {@code null} if task is not done yet
         * @throws ServerException
         *         if an error occurs when try to start project generating process or get its result.
         */
        GenerationResult getResult() throws ServerException {
            if (!isDone()) {
                return null;
            }
            if (result == null) {
                boolean successful;
                try {
                    successful = super.get();
                } catch (InterruptedException e) {
                    // Should not happen since we checked is task done or not.
                    Thread.currentThread().interrupt();
                    successful = false;
                } catch (ExecutionException e) {
                    final Throwable cause = e.getCause();
                    if (cause instanceof Error) {
                        throw (Error)cause;
                    } else if (cause instanceof ServerException) {
                        throw (ServerException)cause;
                    } else {
                        throw new ServerException(cause.getMessage(), cause);
                    }
                } catch (CancellationException ce) {
                    successful = false;
                }
                result = ArchetypeGenerator.this.getTaskResult(this, successful);
            }
            return result;
        }

        File getWorkDir() {
            return workDir;
        }

        String getArtifactId() {
            return artifactId;
        }

        synchronized boolean isExpired() {
            return endTime > 0 && (endTime + keepResultTimeMillis) < System.currentTimeMillis();
        }
    }
}

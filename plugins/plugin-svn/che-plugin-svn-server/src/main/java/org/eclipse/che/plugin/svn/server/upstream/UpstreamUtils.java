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
package org.eclipse.che.plugin.svn.server.upstream;

import org.eclipse.che.api.core.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.eclipse.che.commons.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Utilities class containing logic copied/pasted from Git extension that could/should be put into a core VCS API.
 */
public class UpstreamUtils {

    private static final Logger LOG = LoggerFactory.getLogger(UpstreamUtils.class);

    /**
     * Private constructor.
     */
    private UpstreamUtils() { }

    /**
     * Executes a command line executable based on the arguments specified.
     *
     * @param env the optional environment variables
     * @param cmd the command to run
     * @param args the optional command arguments
     * @param timeout the optional timeout in milliseconds
     * @param workingDirectory the optional working directory
     *
     * @return the command line result
     *
     * @throws IOException if something goes wrong
     */
    public static CommandLineResult executeCommandLine(@Nullable final Map<String, String> env,
                                                       final String cmd,
                                                       @Nullable final String[] args,
                                                       final long timeout,
                                                       @Nullable final File workingDirectory) throws IOException {
        return executeCommandLine(env, cmd, args, null, timeout, workingDirectory);
    }

    /**
     * Executes a command line executable based on the arguments specified.
     *
     * @param env the optional environment variables
     * @param cmd the command to run
     * @param args the optional command arguments
     * @param redactedArgs additional command arguments that will not be shown in result
     * @param timeout the optional timeout in milliseconds
     * @param workingDirectory the optional working directory
     *
     * @return the command line result
     *
     * @throws IOException if something goes wrong
     */
    public static CommandLineResult executeCommandLine(@Nullable final Map<String, String> env,
                                                       final String cmd,
                                                       @Nullable final String[] args,
                                                       @Nullable final String[] redactedArgs,
                                                       final long timeout,
                                                       @Nullable final File workingDirectory)  throws IOException {
        return executeCommandLine(env, cmd, args, null, timeout, workingDirectory, null);
    }

    /**
     * Executes a command line executable based on the arguments specified.
     *
     * @param env the optional environment variables
     * @param cmd the command to run
     * @param args the optional command arguments
     * @param redactedArgs additional command arguments that will not be shown in result
     * @param timeout the optional timeout in milliseconds
     * @param workingDirectory the optional working directory
     * @param lineConsumerFactory the optional std output line consumer factory
     * 
     * @return the command line result
     * 
     * @throws IOException if something goes wrong
     */
    public static CommandLineResult executeCommandLine(@Nullable final Map<String, String> env,
                                                       final String cmd,
                                                       @Nullable final String[] args,
                                                       @Nullable final String[] redactedArgs,
                                                       final long timeout,
                                                       @Nullable final File workingDirectory,
                                                       @Nullable LineConsumerFactory lineConsumerFactory)
            throws IOException {
        CommandLine command = new CommandLine(cmd);

        if (args != null) {
            for (String arg: args) {
                command.add(arg);
            }
        }

        CommandLine redactedCommand = new CommandLine(command);
        if (redactedArgs != null) {
            for (String arg: redactedArgs) {
                redactedCommand.add(arg);
            }
        }

        LOG.debug("Running command: " + command.toString());
        final ProcessBuilder processBuilder = new ProcessBuilder(redactedCommand.toShellCommand());

        Map<String, String> environment = processBuilder.environment();
        if (env != null) {
            environment.putAll(env);
        }
        environment.put("LANG", "en_US.UTF-8");
        environment.put("GDM_LANG", "en_US.UTF-8");
        environment.put("LANGUAGE", "us");

        processBuilder.directory(workingDirectory);

        LineConsumer lineConsumer = LineConsumer.DEV_NULL;
        if (lineConsumerFactory != null) {
            lineConsumer = lineConsumerFactory.newLineConsumer();
        }

        final CommandLineOutputProcessor stdOutConsumer = new CommandLineOutputProcessor(new ArrayList<String>());
        final CommandLineOutputProcessor stdErrConsumer = new CommandLineOutputProcessor(new ArrayList<String>());

        final Process process = processBuilder.start();

        final Watchdog watcher;

        if (timeout > 0) {
            watcher = new Watchdog(timeout, TimeUnit.MILLISECONDS);

            watcher.start(new CancellableProcessWrapper(process));
        }

        try (LineConsumer consumer = new CompositeLineConsumer(lineConsumer, stdOutConsumer)) {
            ProcessUtil.process(process, consumer, stdErrConsumer);
            process.waitFor();
        } catch (InterruptedException e) {
            throw new IOException(e);
        }

        return new CommandLineResult(command, process.exitValue(), stdOutConsumer.getOutput(), stdErrConsumer.getOutput());
    }
}

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
package org.eclipse.che.git.impl.nativegit;


import org.eclipse.che.api.core.util.CancellableProcessWrapper;
import org.eclipse.che.api.core.util.CommandLine;
import org.eclipse.che.api.core.util.CompositeLineConsumer;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.api.core.util.Watchdog;
import org.eclipse.che.api.git.GitException;
import org.eclipse.che.git.impl.nativegit.commands.GitCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Executes GitCommand.
 *
 * @author Eugene Voevodin
 */
public class CommandProcess {
    private static final Logger LOG = LoggerFactory.getLogger(CommandProcess.class);

    /**
     * @param command
     *         GitCommand that will be executed
     * @param lineConsumerFactory
     *         factory that provides LineConsumer for propagate output of this command
     * @throws GitException
     *         when command execution error occurs
     */
    public static void executeGitCommand(GitCommand command, LineConsumerFactory lineConsumerFactory) throws GitException {
        CommandLine commandLine = command.getCommandLine();
        ProcessBuilder pb = new ProcessBuilder(commandLine.toShellCommand());

        Map<String, String> environment = pb.environment();

        environment.put("HOME", System.getProperty("user.home"));
        environment.put("LANG", "en_US.UTF-8");
        environment.put("GDM_LANG", "en_US.UTF-8");
        environment.put("LANGUAGE", "us");

        //set up and override command specific environment variables
        for (Map.Entry<String, String> entry : ((Map<String, String>)command.getCommandEnvironment()).entrySet()) {
            environment.put(entry.getKey(), entry.getValue());
        }

        pb.directory(command.getRepository());

        LineConsumer lineConsumer = LineConsumer.DEV_NULL;
        if (lineConsumerFactory != null) {
            lineConsumer = lineConsumerFactory.newLineConsumer();
        }

        // Add an external line consumer that comes with factory. It is typically a consumer that sends message events to the client.
        try (LineConsumer consumer = new CompositeLineConsumer(lineConsumer, command)) {
            Process process;
            try {
                process = ProcessUtil.execute(pb, consumer);
            } catch (IOException e) {
                LOG.error("Process creating failed", e);
                throw new GitException("It is not possible to execute command");
            }
            // process will be stopped after timeout
            Watchdog watcher = null;
            if (command.getTimeout() > 0) {
                watcher = new Watchdog(command.getTimeout(), TimeUnit.SECONDS);
                watcher.start(new CancellableProcessWrapper(process));
            }

            try {
                process.waitFor();
                /*
                 * Check process exit value and search for correct error message without hint and warning messages ant throw it to user.
                 */
                if (process.exitValue() != 0) {
                    String message = searchErrorMessage(command.getLines());
                    LOG.debug(String.format("Command failed!\ncommand: %s\nerror: %s", commandLine.toString(), message));
                    throw new GitException(message);
                } else {
                    LOG.debug(String.format("Command successful!\ncommand: %s", commandLine.toString()));
                }
            } catch (InterruptedException e) {
                Thread.interrupted();
            } finally {
                if (watcher != null) {
                    watcher.stop();
                }
            }
        } catch (IOException e) {
            LOG.error("An error occurred while trying to close the lineConsumer", e);
        }
    }

    /**
     * Searches useful information in command output
     *
     * @param output
     *         command execution output
     * @return filtered output as message
     */
    private static String searchErrorMessage(List<String> output) {
        //check if troubles with ssh keys
        int i = 0;
        int length = output.size();
        while (i < length && !output.get(i).contains("fatal:")) {
            i++;
        }
        StringBuilder builder = new StringBuilder();
        if (i == output.size()) {
            for (String line : output) {
                if (!(line.startsWith("hint:") || line.startsWith("Warning:"))) {
                    builder.append(line).append('\n');
                }
            }
        }
        for (; i < output.size(); i++) {
            if (!(output.get(i).startsWith("hint:") || output.get(i).startsWith("Warning:"))) {
                builder.append(output.get(i)).append('\n');
            }
        }
        if (builder.toString().toLowerCase().contains("fatal: the remote end hung up unexpectedly")) {
            builder.append("SSH key doesn't exist or it is not valid");
        }
        return builder.toString();
    }
}

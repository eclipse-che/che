/*******************************************************************************
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.composer.server.projecttype;

import org.eclipse.che.api.core.util.AbstractLineConsumer;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.api.core.util.ValueHolder;
import org.eclipse.che.api.core.util.Watchdog;
import org.eclipse.che.api.core.util.WebsocketMessageConsumer;
import org.eclipse.che.plugin.composer.shared.Constants;
import org.eclipse.che.plugin.composer.shared.dto.ComposerOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ComposerCommandExecutor {

    private static final Logger LOG = LoggerFactory.getLogger(ComposerCommandExecutor.class);
    
    public static void execute(String[] commandLine, File workDir)
            throws TimeoutException, IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(commandLine).redirectErrorStream(true).directory(workDir);

        try (WebsocketMessageConsumer<ComposerOutput> websocketMessageConsumer = new WebsocketMessageConsumer<>(Constants.COMPOSER_CHANNEL_NAME)) {
            websocketMessageConsumer.consume(new ComposerOutputImpl(String.join(" ", commandLine), ComposerOutput.State.START));
            LineConsumer lineConsumer = new AbstractLineConsumer() {
                @Override
                public void writeLine(String line) throws IOException {
                    websocketMessageConsumer.consume(new ComposerOutputImpl(line, ComposerOutput.State.IN_PROGRESS));
                }
            };

            // process will be stopped after timeout
            Watchdog watcher = new Watchdog(10, TimeUnit.MINUTES);

            try {
                final Process process = pb.start();
                final ValueHolder<Boolean> isTimeoutExceeded = new ValueHolder<>(false);
                watcher.start(() -> {
                    isTimeoutExceeded.set(true);
                    ProcessUtil.kill(process);
                });
                // consume logs until process ends
                ProcessUtil.process(process, lineConsumer);
                process.waitFor();
                websocketMessageConsumer.consume(new ComposerOutputImpl("Done", ComposerOutput.State.DONE));
                if (isTimeoutExceeded.get()) {
                    LOG.error("Command time expired : command-line " + Arrays.toString(commandLine));
                    websocketMessageConsumer.consume(new ComposerOutputImpl("Installing dependencies time expired", ComposerOutput.State.ERROR));
                    throw new TimeoutException();
                } else if (process.exitValue() != 0) {
                    LOG.error("Command failed : command-line " + Arrays.toString(commandLine));
                    websocketMessageConsumer.consume(new ComposerOutputImpl("Error occurred", ComposerOutput.State.ERROR));
                    throw new IOException("Process failed. Exit code " + process.exitValue() + " command-line : " + Arrays.toString(commandLine));
                }
            } finally {
                watcher.stop();
            }
        }
    }

}

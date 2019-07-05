/*
 * Copyright (c) 2016-2017 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.composer.server.executor;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.AbstractLineConsumer;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.api.core.util.ValueHolder;
import org.eclipse.che.api.core.util.Watchdog;
import org.eclipse.che.plugin.composer.shared.dto.ComposerOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Kaloyan Raev */
@Singleton
public class ComposerCommandExecutor {

  private EventService eventService;

  @Inject
  public ComposerCommandExecutor(EventService eventService) {
    this.eventService = eventService;
  }

  private static final Logger LOG = LoggerFactory.getLogger(ComposerCommandExecutor.class);

  public void execute(String[] commandLine, File workDir)
      throws TimeoutException, IOException, InterruptedException {
    ProcessBuilder pb =
        new ProcessBuilder(commandLine).redirectErrorStream(true).directory(workDir);

    eventService.publish(
        new ComposerOutputImpl(String.join(" ", commandLine), ComposerOutput.State.START));

    LineConsumer lineConsumer =
        new AbstractLineConsumer() {
          @Override
          public void writeLine(String line) throws IOException {
            eventService.publish(new ComposerOutputImpl(line, ComposerOutput.State.IN_PROGRESS));
          }
        };

    // process will be stopped after timeout
    Watchdog watcher = new Watchdog(10, TimeUnit.MINUTES);

    try {
      final Process process = pb.start();
      final ValueHolder<Boolean> isTimeoutExceeded = new ValueHolder<>(false);
      watcher.start(
          () -> {
            isTimeoutExceeded.set(true);
            ProcessUtil.kill(process);
          });
      // consume logs until process ends
      ProcessUtil.process(process, lineConsumer);
      process.waitFor();
      eventService.publish(new ComposerOutputImpl("Done", ComposerOutput.State.DONE));
      if (isTimeoutExceeded.get()) {
        LOG.error("Command time expired : command-line " + Arrays.toString(commandLine));
        eventService.publish(
            new ComposerOutputImpl(
                "Installing dependencies time expired", ComposerOutput.State.ERROR));
        throw new TimeoutException();
      } else if (process.exitValue() != 0) {
        LOG.error("Command failed : command-line " + Arrays.toString(commandLine));
        eventService.publish(new ComposerOutputImpl("Error occurred", ComposerOutput.State.ERROR));
        throw new IOException(
            "Process failed. Exit code "
                + process.exitValue()
                + " command-line : "
                + Arrays.toString(commandLine));
      }
    } finally {
      watcher.stop();
    }
  }
}

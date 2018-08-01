/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.gdb.server;

import static java.lang.String.format;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.core.util.AbstractLineConsumer;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debugger.server.exceptions.DebuggerException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.gdb.server.exception.GdbException;
import org.eclipse.che.plugin.gdb.server.exception.GdbTerminatedException;
import org.eclipse.che.plugin.gdb.server.parser.GdbBacktrace;
import org.eclipse.che.plugin.gdb.server.parser.GdbBreak;
import org.eclipse.che.plugin.gdb.server.parser.GdbClear;
import org.eclipse.che.plugin.gdb.server.parser.GdbContinue;
import org.eclipse.che.plugin.gdb.server.parser.GdbDelete;
import org.eclipse.che.plugin.gdb.server.parser.GdbDirectory;
import org.eclipse.che.plugin.gdb.server.parser.GdbFile;
import org.eclipse.che.plugin.gdb.server.parser.GdbInfoArgs;
import org.eclipse.che.plugin.gdb.server.parser.GdbInfoBreak;
import org.eclipse.che.plugin.gdb.server.parser.GdbInfoLine;
import org.eclipse.che.plugin.gdb.server.parser.GdbInfoLocals;
import org.eclipse.che.plugin.gdb.server.parser.GdbInfoProgram;
import org.eclipse.che.plugin.gdb.server.parser.GdbOutput;
import org.eclipse.che.plugin.gdb.server.parser.GdbPType;
import org.eclipse.che.plugin.gdb.server.parser.GdbPrint;
import org.eclipse.che.plugin.gdb.server.parser.GdbRun;
import org.eclipse.che.plugin.gdb.server.parser.GdbTargetRemote;
import org.eclipse.che.plugin.gdb.server.parser.GdbVersion;
import org.eclipse.che.plugin.gdb.server.parser.ProcessInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GDB.
 *
 * @author Anatoliy Bazko
 */
public class Gdb extends GdbProcess {
  private static final Logger LOG = LoggerFactory.getLogger(GdbProcess.class);
  private static final String PROCESS_NAME = "gdb";
  private static final String OUTPUT_SEPARATOR = "(gdb) ";

  private GdbVersion gdbVersion;

  Gdb() throws IOException {
    super(OUTPUT_SEPARATOR, PROCESS_NAME);

    try {
      gdbVersion = GdbVersion.parse(grabGdbOutput());
    } catch (InterruptedException | DebuggerException e) {
      LOG.error(e.getMessage(), e);
      gdbVersion = new GdbVersion("Unknown", "Unknown");
    }
  }

  /** Starts GDB. */
  public static Gdb start() throws IOException {
    return new Gdb();
  }

  public GdbVersion getGdbVersion() {
    return gdbVersion;
  }

  /** `run` command. */
  public GdbRun run() throws IOException, InterruptedException, DebuggerException {
    GdbOutput gdbOutput = sendCommand("run");
    return GdbRun.parse(gdbOutput);
  }

  public Location suspend(final String file, boolean isRemoteConnection)
      throws IOException, InterruptedException, DebuggerException {
    if (pid < 0) {
      throw new DebuggerException("Gdb process not found.");
    }

    if (isRemoteConnection) {
      Runtime.getRuntime().exec("kill -SIGINT " + pid).waitFor();
      sendCommand("signal SIGSTOP ");
    } else {
      final List<String> outputs = new ArrayList<>();
      final ProcessBuilder processBuilder =
          new ProcessBuilder().command("ps", "-o", "pid,cmd", "--ppid", String.valueOf(pid));
      final Process process = processBuilder.start();

      LineConsumer stdout =
          new AbstractLineConsumer() {
            @Override
            public void writeLine(String line) throws IOException {
              outputs.add(line);
            }
          };

      ProcessUtil.process(process, stdout);

      int processId = -1;
      for (String output : outputs) {
        try {
          final ProcessInfo processInfo = ProcessInfo.parse(output);
          if (file.equals(processInfo.getProcessName())) {
            processId = processInfo.getProcessId();
          }
        } catch (Exception e) {
          // we can't get info about current process, but we are trying to get info about another
          // processes
        }
      }

      if (processId == -1) {
        throw new DebuggerException(format("Process %s not found.", file));
      }
      Runtime.getRuntime().exec("kill -SIGINT " + processId).waitFor();
    }

    final GdbOutput gdbOutput = sendCommand("backtrace");
    final GdbBacktrace backtrace = GdbBacktrace.parse(gdbOutput);
    final Map<Integer, Location> frames = backtrace.getFrames();

    if (frames.containsKey(0)) {
      return frames.get(0);
    }
    throw new DebuggerException("Unable recognize current location for debugger session. ");
  }

  /** `set var` command. */
  public void setVar(String varName, String value)
      throws IOException, InterruptedException, DebuggerException {
    String command = "set var " + varName + "=" + value;
    sendCommand(command);
  }

  /** `ptype` command. */
  public GdbPType ptype(String variable)
      throws IOException, InterruptedException, DebuggerException {
    GdbOutput gdbOutput = sendCommand("ptype " + variable);
    return GdbPType.parse(gdbOutput);
  }

  /** `print` command. */
  public GdbPrint print(String variable)
      throws IOException, InterruptedException, DebuggerException {
    GdbOutput gdbOutput = sendCommand("print " + variable);
    return GdbPrint.parse(gdbOutput);
  }

  /** `continue` command. */
  public GdbContinue cont() throws IOException, InterruptedException, DebuggerException {
    GdbOutput gdbOutput = sendCommand("continue");
    return GdbContinue.parse(gdbOutput);
  }

  /** `step` command. */
  public GdbInfoLine step() throws IOException, InterruptedException, DebuggerException {
    sendCommand("step");
    return infoLine();
  }

  /** `finish` command. */
  public GdbInfoLine finish() throws IOException, InterruptedException, DebuggerException {
    sendCommand("finish");
    return infoLine();
  }

  /** `next` command. */
  @Nullable
  public GdbInfoLine next() throws IOException, InterruptedException, DebuggerException {
    sendCommand("next");

    GdbInfoProgram gdbInfoProgram = infoProgram();
    if (gdbInfoProgram.getStoppedAddress() == null) {
      return null;
    }

    return infoLine();
  }

  /** `quit` command. */
  public void quit() throws IOException, GdbException, InterruptedException {
    try {
      sendCommand("quit", false);
    } finally {
      stop();
    }
  }

  /** `break` command */
  public void breakpoint(@NotNull String file, int lineNumber)
      throws IOException, InterruptedException, DebuggerException {
    String command = "break " + file + ":" + lineNumber;
    GdbOutput gdbOutput = sendCommand(command);
    GdbBreak.parse(gdbOutput);
  }

  /** `break` command */
  public void breakpoint(int lineNumber)
      throws IOException, InterruptedException, DebuggerException {
    String command = "break " + lineNumber;
    GdbOutput gdbOutput = sendCommand(command);
    GdbBreak.parse(gdbOutput);
  }

  /** `directory` command. */
  public GdbDirectory directory(@NotNull String directory)
      throws IOException, InterruptedException, DebuggerException {
    GdbOutput gdbOutput = sendCommand("directory " + directory);
    return GdbDirectory.parse(gdbOutput);
  }

  /** `file` command. */
  public void file(@NotNull String file)
      throws IOException, InterruptedException, DebuggerException {
    GdbOutput gdbOutput = sendCommand("file " + file);
    GdbFile.parse(gdbOutput);
  }

  /** `clear` command. */
  public void clear(@NotNull String file, int lineNumber)
      throws IOException, InterruptedException, DebuggerException {
    String command = "clear " + file + ":" + lineNumber;
    GdbOutput gdbOutput = sendCommand(command);

    GdbClear.parse(gdbOutput);
  }

  /** `clear` command. */
  public void clear(int lineNumber) throws IOException, InterruptedException, DebuggerException {
    String command = "clear " + lineNumber;
    GdbOutput gdbOutput = sendCommand(command);

    GdbClear.parse(gdbOutput);
  }

  /** `delete` command. */
  public void delete() throws IOException, InterruptedException, DebuggerException {
    GdbOutput gdbOutput = sendCommand("delete");
    GdbDelete.parse(gdbOutput);
  }

  /** `target remote` command. */
  public void targetRemote(String host, int port)
      throws IOException, InterruptedException, DebuggerException {
    String command = "target remote " + (host != null ? host : "") + ":" + port;
    GdbOutput gdbOutput = sendCommand(command);
    GdbTargetRemote.parse(gdbOutput);
  }

  /** `info break` command. */
  public GdbInfoBreak infoBreak() throws IOException, InterruptedException, DebuggerException {
    GdbOutput gdbOutput = sendCommand("info break");
    return GdbInfoBreak.parse(gdbOutput);
  }

  /** `info args` command. */
  public GdbInfoArgs infoArgs() throws IOException, InterruptedException, DebuggerException {
    GdbOutput gdbOutput = sendCommand("info args");
    return GdbInfoArgs.parse(gdbOutput);
  }

  /** `info locals` command. */
  public GdbInfoLocals infoLocals() throws IOException, InterruptedException, DebuggerException {
    GdbOutput gdbOutput = sendCommand("info locals");
    return GdbInfoLocals.parse(gdbOutput);
  }

  /** `info line` command. */
  public GdbInfoLine infoLine() throws IOException, InterruptedException, DebuggerException {
    GdbOutput gdbOutput = sendCommand("info line");
    return GdbInfoLine.parse(gdbOutput);
  }

  /** `info program` command. */
  public GdbInfoProgram infoProgram() throws IOException, InterruptedException, DebuggerException {
    GdbOutput gdbOutput = sendCommand("info program");
    return GdbInfoProgram.parse(gdbOutput);
  }

  private GdbOutput sendCommand(String command)
      throws IOException, GdbTerminatedException, InterruptedException {
    return sendCommand(command, true);
  }

  private synchronized GdbOutput sendCommand(String command, boolean grabOutput)
      throws IOException, GdbTerminatedException, InterruptedException {
    LOG.debug(command);

    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
    writer.write(command);
    writer.newLine();
    writer.flush();

    return grabOutput ? grabGdbOutput() : null;
  }

  private GdbOutput grabGdbOutput() throws InterruptedException, GdbTerminatedException {
    GdbOutput gdbOutput = outputs.take();
    if (gdbOutput.isTerminated()) {
      String errorMsg = "GDB has been terminated with output: " + gdbOutput.getOutput();
      LOG.error(errorMsg);
      throw new GdbTerminatedException(errorMsg);
    }
    return gdbOutput;
  }
}

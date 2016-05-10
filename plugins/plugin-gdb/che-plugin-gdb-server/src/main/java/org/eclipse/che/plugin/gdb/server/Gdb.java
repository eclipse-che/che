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
package org.eclipse.che.plugin.gdb.server;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.plugin.gdb.server.parser.GdbClear;
import org.eclipse.che.plugin.gdb.server.parser.GdbContinue;
import org.eclipse.che.plugin.gdb.server.parser.GdbDelete;
import org.eclipse.che.plugin.gdb.server.parser.GdbInfoLine;
import org.eclipse.che.plugin.gdb.server.parser.GdbBreak;
import org.eclipse.che.plugin.gdb.server.parser.GdbDirectory;
import org.eclipse.che.plugin.gdb.server.parser.GdbFile;
import org.eclipse.che.plugin.gdb.server.parser.GdbInfoArgs;
import org.eclipse.che.plugin.gdb.server.parser.GdbInfoBreak;
import org.eclipse.che.plugin.gdb.server.parser.GdbInfoLocals;
import org.eclipse.che.plugin.gdb.server.parser.GdbInfoProgram;
import org.eclipse.che.plugin.gdb.server.parser.GdbPType;
import org.eclipse.che.plugin.gdb.server.parser.GdbParseException;
import org.eclipse.che.plugin.gdb.server.parser.GdbPrint;
import org.eclipse.che.plugin.gdb.server.parser.GdbRun;
import org.eclipse.che.plugin.gdb.server.parser.GdbTargetRemote;
import org.eclipse.che.plugin.gdb.server.parser.GdbVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.constraints.NotNull;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * GDB.
 *
 * @author Anatoliy Bazko
 */
public class Gdb extends GdbProcess {
    private static final Logger LOG              = LoggerFactory.getLogger(GdbProcess.class);
    private static final String PROCESS_NAME     = "gdb";
    private static final String OUTPUT_SEPARATOR = "(gdb) ";

    private final GdbVersion gdbVersion;

    Gdb() throws IOException, GdbParseException, InterruptedException {
        super(OUTPUT_SEPARATOR, PROCESS_NAME);
        gdbVersion = GdbVersion.parse(outputs.take());
    }

    /**
     * Starts GDB.
     */
    public static Gdb start() throws InterruptedException, GdbParseException, IOException {
        return new Gdb();
    }

    public GdbVersion getGdbVersion() {
        return gdbVersion;
    }

    /**
     * `run` command.
     */
    public GdbRun run() throws IOException, InterruptedException, GdbParseException {
        sendCommand("run");
        return GdbRun.parse(outputs.take());
    }

    /**
     * `set var` command.
     */
    public void setVar(String varName, String value) throws IOException, InterruptedException, GdbParseException {
        String command = "set var " + varName + "=" + value;
        sendCommand(command);
        outputs.take();
    }

    /**
     * `ptype` command.
     */
    public GdbPType ptype(String variable) throws IOException, InterruptedException, GdbParseException {
        sendCommand("ptype " + variable);
        return GdbPType.parse(outputs.take());
    }

    /**
     * `print` command.
     */
    public GdbPrint print(String variable) throws IOException, InterruptedException, GdbParseException {
        sendCommand("print " + variable);
        return GdbPrint.parse(outputs.take());
    }

    /**
     * `continue` command.
     */
    public GdbContinue cont() throws IOException, InterruptedException, GdbParseException {
        sendCommand("continue");
        return GdbContinue.parse(outputs.take());
    }

    /**
     * `step` command.
     */
    public GdbInfoLine step() throws IOException, InterruptedException, GdbParseException {
        sendCommand("step");
        outputs.take();
        return infoLine();
    }

    /**
     * `finish` command.
     */
    public GdbInfoLine finish() throws IOException, InterruptedException, GdbParseException {
        sendCommand("finish");
        outputs.take();
        return infoLine();
    }

    /**
     * `next` command.
     */
    @Nullable
    public GdbInfoLine next() throws IOException, InterruptedException, GdbParseException {
        sendCommand("next");
        outputs.take();

        if (infoProgram().getStoppedAddress() == null) {
            return null;
        }

        return infoLine();
    }

    /**
     * `quit` command.
     */
    public void quit() throws IOException {
        sendCommand("quit");
        stop();
    }

    /**
     * `break` command
     */
    public void breakpoint(@NotNull String file, int lineNumber) throws IOException, InterruptedException, GdbParseException {
        String command = "break " + file + ":" + lineNumber;
        sendCommand(command);
        GdbBreak.parse(outputs.take());
    }

    /**
     * `break` command
     */
    public void breakpoint(int lineNumber) throws IOException, InterruptedException, GdbParseException {
        String command = "break " + lineNumber;
        sendCommand(command);
        GdbBreak.parse(outputs.take());
    }

    /**
     * `directory` command.
     */
    public GdbDirectory directory(@NotNull String directory) throws IOException, GdbParseException, InterruptedException {
        sendCommand("directory " + directory);
        return GdbDirectory.parse(outputs.take());
    }

    /**
     * `file` command.
     */
    public void file(@NotNull String file) throws IOException, GdbParseException, InterruptedException {
        sendCommand("file " + file);
        GdbFile.parse(outputs.take());
    }

    /**
     * `clear` command.
     */
    public void clear(@NotNull String file, int lineNumber) throws IOException, InterruptedException, GdbParseException {
        String command = "clear " + file + ":" + lineNumber;
        sendCommand(command);

        GdbClear.parse(outputs.take());
    }

    /**
     * `clear` command.
     */
    public void clear(int lineNumber) throws IOException, InterruptedException, GdbParseException {
        String command = "clear " + lineNumber;
        sendCommand(command);

        GdbClear.parse(outputs.take());
    }

    /**
     * `delete` command.
     */
    public void delete() throws IOException, InterruptedException, GdbParseException {
        sendCommand("delete");
        GdbDelete.parse(outputs.take());
    }

    /**
     * `target remote` command.
     */
    public void targetRemote(String host, int port) throws IOException, InterruptedException, GdbParseException {
        String command = "target remote " + (host != null ? host : "") + ":" + port;
        sendCommand(command);
        GdbTargetRemote.parse(outputs.take());
    }

    /**
     * `info break` command.
     */
    public GdbInfoBreak infoBreak() throws IOException, InterruptedException, GdbParseException {
        sendCommand("info break");
        return GdbInfoBreak.parse(outputs.take());
    }

    /**
     * `info args` command.
     */
    public GdbInfoArgs infoArgs() throws IOException, InterruptedException, GdbParseException {
        sendCommand("info args");
        return GdbInfoArgs.parse(outputs.take());
    }

    /**
     * `info locals` command.
     */
    public GdbInfoLocals infoLocals() throws IOException, InterruptedException, GdbParseException {
        sendCommand("info locals");
        return GdbInfoLocals.parse(outputs.take());
    }

    /**
     * `info line` command.
     */
    public GdbInfoLine infoLine() throws IOException, InterruptedException, GdbParseException {
        sendCommand("info line");
        return GdbInfoLine.parse(outputs.take());
    }

    /**
     * `info program` command.
     */
    public GdbInfoProgram infoProgram() throws IOException, InterruptedException, GdbParseException {
        sendCommand("info program");
        return GdbInfoProgram.parse(outputs.take());
    }

    private void sendCommand(String command) throws IOException {
        LOG.debug(command);

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
        writer.write(command);
        writer.newLine();
        writer.flush();
    }

}

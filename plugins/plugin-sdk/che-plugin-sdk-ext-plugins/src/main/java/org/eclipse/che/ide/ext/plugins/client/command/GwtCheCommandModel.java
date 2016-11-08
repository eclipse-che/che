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
package org.eclipse.che.ide.ext.plugins.client.command;

import org.eclipse.che.ide.CommandLine;

import static org.eclipse.che.ide.ext.plugins.client.command.GwtCheCommandType.CODE_SERVER_FQN;
import static org.eclipse.che.ide.ext.plugins.client.command.GwtCheCommandType.COMMAND_TEMPLATE;

/**
 * Model of the 'GWT SDM for Che' command.
 *
 * @author Artem Zatsarynnyi
 */
class GwtCheCommandModel {

    private String gwtModule;
    private String codeServerAddress;
    private String classPath;

    GwtCheCommandModel(String gwtModule, String codeServerAddress, String classPath) {
        this.gwtModule = gwtModule;
        this.codeServerAddress = codeServerAddress;
        this.classPath = classPath;
    }

    /** Crates {@link GwtCheCommandModel} instance from the given command line. */
    static GwtCheCommandModel fromCommandLine(String commandLine) {
        final CommandLine cmd = new CommandLine(commandLine);

        final String classPathArgument = cmd.getArgument(2);
        // remove quotes
        final String classPath = classPathArgument.substring(1, classPathArgument.length() - 1);

        final int gwtModuleArgumentIndex = cmd.indexOf(CODE_SERVER_FQN) + 1;
        final String gwtModule = cmd.getArgument(gwtModuleArgumentIndex);

        String codeServerAddress = null;

        for (String arg : cmd.getArguments()) {
            if (arg.equals("-bindAddress")) {
                int bindAddressArgumentIndex = cmd.indexOf(arg) + 1;
                codeServerAddress = cmd.getArgument(bindAddressArgumentIndex);
            }
        }

        return new GwtCheCommandModel(gwtModule, codeServerAddress, classPath);
    }

    String getGwtModule() {
        return gwtModule;
    }

    void setGwtModule(String gwtModule) {
        this.gwtModule = gwtModule;
    }

    String getCodeServerAddress() {
        return codeServerAddress;
    }

    void setCodeServerAddress(String codeServerAddress) {
        this.codeServerAddress = codeServerAddress;
    }

    String getClassPath() {
        return classPath;
    }

    void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    String toCommandLine() {
        final StringBuilder cmd = new StringBuilder(COMMAND_TEMPLATE.replace("$GWT_MODULE", gwtModule.trim())
                                                                    .replace("$CHE_CLASSPATH", '"' + classPath + '"'));
        if (!codeServerAddress.trim().isEmpty()) {
            cmd.append(" -bindAddress ").append(codeServerAddress.trim());
        }

        return cmd.toString();
    }
}

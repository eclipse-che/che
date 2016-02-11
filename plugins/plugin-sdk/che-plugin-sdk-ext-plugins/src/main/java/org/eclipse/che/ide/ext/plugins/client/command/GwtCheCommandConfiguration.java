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

import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Represents 'GWT SDM for Che' command.
 *
 * @author Artem Zatsarynnyi
 */
public class GwtCheCommandConfiguration extends CommandConfiguration {

    private String gwtModule;
    private String codeServerAddress;
    private String classPath;

    protected GwtCheCommandConfiguration(CommandType type, String name, Map<String, String> attributes) {
        super(type, name, attributes);
        gwtModule = "";
        codeServerAddress = "";
        classPath = "";
    }

    public String getGwtModule() {
        return gwtModule;
    }

    public void setGwtModule(String gwtModule) {
        this.gwtModule = gwtModule;
    }

    public String getCodeServerAddress() {
        return codeServerAddress;
    }

    public void setCodeServerAddress(String codeServerAddress) {
        this.codeServerAddress = codeServerAddress;
    }

    public String getClassPath() {
        return classPath;
    }

    public void setClassPath(String classPath) {
        this.classPath = classPath;
    }

    @NotNull
    @Override
    public String toCommandLine() {
        final StringBuilder cmd = new StringBuilder(GwtCheCommandType.COMMAND_TEMPLATE.replace("$GWT_MODULE", gwtModule.trim())
                                                                                      .replace("$CHE_CLASSPATH", '"' + classPath + '"'));
        if (!codeServerAddress.trim().isEmpty()) {
            cmd.append(" -bindAddress ").append(codeServerAddress.trim());
        }
        return cmd.toString();
    }
}

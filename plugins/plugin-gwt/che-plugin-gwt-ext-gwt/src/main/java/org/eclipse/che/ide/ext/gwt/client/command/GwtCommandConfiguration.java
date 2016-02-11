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
package org.eclipse.che.ide.ext.gwt.client.command;

import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;

import javax.validation.constraints.NotNull;
import java.util.Map;

/**
 * Represents GWT command.
 *
 * @author Artem Zatsarynnyi
 */
public class GwtCommandConfiguration extends CommandConfiguration {

    private String workingDirectory;
    private String gwtModule;
    private String codeServerAddress;

    protected GwtCommandConfiguration(CommandType type, String name, Map<String, String> attributes) {
        super(type, name, attributes);
        workingDirectory = "";
        gwtModule = "";
        codeServerAddress = "";
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public void setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
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

    @NotNull
    @Override
    public String toCommandLine() {
        final StringBuilder cmd = new StringBuilder(GwtCommandType.COMMAND_TEMPLATE);
        if (!workingDirectory.trim().isEmpty()) {
            cmd.append(" -f ").append(workingDirectory.trim());
        }
        if (!gwtModule.trim().isEmpty()) {
            cmd.append(" -Dgwt.module=").append(gwtModule.trim());
        }
        if (!codeServerAddress.trim().isEmpty()) {
            cmd.append(" -Dgwt.bindAddress=").append(codeServerAddress.trim());
        }
        return cmd.toString();
    }
}

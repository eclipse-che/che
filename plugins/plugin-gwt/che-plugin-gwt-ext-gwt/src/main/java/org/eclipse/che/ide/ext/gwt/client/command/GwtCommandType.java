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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.command.CommandPage;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.ext.gwt.client.GwtResources;
import org.eclipse.che.ide.extension.machine.client.command.macros.CurrentProjectPathMacro;
import org.eclipse.che.ide.extension.machine.client.command.macros.DevMachineHostNameMacro;

import java.util.LinkedList;
import java.util.List;

/**
 * GWT command type.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class GwtCommandType implements CommandType {

    public static final String COMMAND_TEMPLATE = "mvn clean gwt:run-codeserver";

    private static final String ID = "gwt";

    private final CurrentProjectPathMacro currentProjectPathMacro;
    private final DevMachineHostNameMacro devMachineHostNameMacro;

    private final List<CommandPage> pages;

    @Inject
    public GwtCommandType(GwtResources resources,
                          GwtCommandPagePresenter page,
                          CurrentProjectPathMacro currentProjectPathMacro,
                          DevMachineHostNameMacro devMachineHostNameMacro,
                          IconRegistry iconRegistry) {
        this.currentProjectPathMacro = currentProjectPathMacro;
        this.devMachineHostNameMacro = devMachineHostNameMacro;
        pages = new LinkedList<>();
        pages.add(page);

        iconRegistry.registerIcon(new Icon(ID + ".commands.category.icon", resources.gwtCommandType()));
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "GWT";
    }

    @Override
    public String getDescription() {
        return "Command for launching GWT Super Dev Mode";
    }

    @Override
    public List<CommandPage> getPages() {
        return pages;
    }

    @Override
    public String getCommandLineTemplate() {
        return COMMAND_TEMPLATE + " -f " + currentProjectPathMacro.getName() + " -Dgwt.bindAddress=" +
               devMachineHostNameMacro.getName();
    }

    @Override
    public String getPreviewUrlTemplate() {
        return "";
    }
}

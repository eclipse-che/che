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
package org.eclipse.che.ide.extension.machine.client.command.custom;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.command.CommandPage;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.extension.machine.client.MachineResources;

import java.util.LinkedList;
import java.util.List;

/**
 * Arbitrary command type.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class CustomCommandType implements CommandType {

    private static final String ID               = "custom";
    private static final String COMMAND_TEMPLATE = "echo \"hello\"";

    private final List<CommandPage> pages;

    @Inject
    public CustomCommandType(MachineResources resources, IconRegistry iconRegistry, CustomPagePresenter page) {
        pages = new LinkedList<>();
        pages.add(page);

        iconRegistry.registerIcon(new Icon(ID + ".commands.category.icon", resources.customCommandType()));
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Custom";
    }

    @Override
    public String getDescription() {
        return "Arbitrary command";
    }

    @Override
    public List<CommandPage> getPages() {
        return pages;
    }

    @Override
    public String getCommandLineTemplate() {
        return COMMAND_TEMPLATE;
    }

    @Override
    public String getPreviewUrlTemplate() {
        return "";
    }
}

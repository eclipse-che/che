/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.plugins.client.command;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.command.CommandPage;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.ext.plugins.client.PluginsResources;

import java.util.LinkedList;
import java.util.List;

/**
 * 'GWT SDM for Che' command type.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class GwtCheCommandType implements CommandType {

    public static final String CODE_SERVER_FQN  = "com.google.gwt.dev.codeserver.CodeServer";
    public static final String COMMAND_TEMPLATE =
            "java -classpath $CHE_CLASSPATH " + CODE_SERVER_FQN + " $GWT_MODULE -noincremental -noprecompile";

    private static final String ID             = "gwt_sdm_che";
    private static final String IDE_GWT_MODULE = "org.eclipse.che.ide.IDE";

    private final PluginsResources resources;

    private final List<CommandPage> pages;

    @Inject
    public GwtCheCommandType(PluginsResources resources, GwtCheCommandPagePresenter page, IconRegistry iconRegistry) {
        this.resources = resources;
        pages = new LinkedList<>();
        pages.add(page);

        iconRegistry.registerIcon(new Icon(ID + ".commands.category.icon", resources.gwtCheCommandType()));
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "GWT SDM for Che";
    }

    @Override
    public String getDescription() {
        return "Command for launching GWT Super Dev Mode for the Che project sources";
    }

    @Override
    public List<CommandPage> getPages() {
        return pages;
    }

    @Override
    public String getCommandLineTemplate() {
        return COMMAND_TEMPLATE.replace("$GWT_MODULE", IDE_GWT_MODULE)
                               .replace("$CHE_CLASSPATH", '"' + resources.cheClassPath().getText() + '"') + " -bindAddress 0.0.0.0";
    }

    @Override
    public String getPreviewUrlTemplate() {
        return "";
    }
}

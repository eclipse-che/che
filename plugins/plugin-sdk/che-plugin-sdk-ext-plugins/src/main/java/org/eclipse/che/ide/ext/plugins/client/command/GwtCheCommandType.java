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

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.ext.plugins.client.PluginsResources;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationFactory;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationPage;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.LinkedList;

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
    private static final String DISPLAY_NAME   = "GWT SDM for Che";
    private static final String IDE_GWT_MODULE = "org.eclipse.che.ide.IDE";

    private final PluginsResources                  resources;
    private final GwtCheCommandConfigurationFactory configurationFactory;

    private final Collection<CommandConfigurationPage<? extends CommandConfiguration>> pages;

    @Inject
    public GwtCheCommandType(PluginsResources resources, CommandPagePresenter page, IconRegistry iconRegistry) {
        this.resources = resources;
        configurationFactory = new GwtCheCommandConfigurationFactory(this);
        pages = new LinkedList<>();
        pages.add(page);

        iconRegistry.registerIcon(new Icon(ID + ".commands.category.icon", resources.gwtCheCommandType()));
    }

    @NotNull
    @Override
    public String getId() {
        return ID;
    }

    @NotNull
    @Override
    public String getDisplayName() {
        return DISPLAY_NAME;
    }

    @NotNull
    @Override
    public SVGResource getIcon() {
        return resources.gwtCheCommandType();
    }

    @NotNull
    @Override
    public Collection<CommandConfigurationPage<? extends CommandConfiguration>> getConfigurationPages() {
        return pages;
    }

    @NotNull
    @Override
    public CommandConfigurationFactory<GwtCheCommandConfiguration> getConfigurationFactory() {
        return configurationFactory;
    }

    @NotNull
    @Override
    public String getCommandTemplate() {
        return COMMAND_TEMPLATE.replace("$GWT_MODULE", IDE_GWT_MODULE)
                               .replace("$CHE_CLASSPATH", '"' + resources.cheClassPath().getText() + '"') + " -bindAddress 0.0.0.0";
    }

    @Override
    public String getPreviewUrlTemplate() {
        return "";
    }
}

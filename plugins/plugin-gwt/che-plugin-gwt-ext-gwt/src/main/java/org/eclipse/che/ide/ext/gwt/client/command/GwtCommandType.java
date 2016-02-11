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

import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.ext.gwt.client.GwtResources;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationFactory;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationPage;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.CurrentProjectPathProvider;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.DevMachineHostNameProvider;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.LinkedList;

/**
 * GWT command type.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class GwtCommandType implements CommandType {

    public static final String COMMAND_TEMPLATE = "mvn clean gwt:run-codeserver";

    private static final String ID           = "gwt";
    private static final String DISPLAY_NAME = "GWT";

    private final GwtResources                   resources;
    private final CurrentProjectPathProvider     currentProjectPathProvider;
    private final DevMachineHostNameProvider     devMachineHostNameProvider;
    private final GwtCommandConfigurationFactory configurationFactory;

    private final Collection<CommandConfigurationPage<? extends CommandConfiguration>> pages;

    @Inject
    public GwtCommandType(GwtResources resources,
                          GwtCommandPagePresenter page,
                          CurrentProjectPathProvider currentProjectPathProvider,
                          DevMachineHostNameProvider devMachineHostNameProvider,
                          IconRegistry iconRegistry) {
        this.resources = resources;
        this.currentProjectPathProvider = currentProjectPathProvider;
        this.devMachineHostNameProvider = devMachineHostNameProvider;
        configurationFactory = new GwtCommandConfigurationFactory(this);
        pages = new LinkedList<>();
        pages.add(page);

        iconRegistry.registerIcon(new Icon(ID + ".commands.category.icon", resources.gwtCommandType()));
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
        return resources.gwtCommandType();
    }

    @NotNull
    @Override
    public Collection<CommandConfigurationPage<? extends CommandConfiguration>> getConfigurationPages() {
        return pages;
    }

    @NotNull
    @Override
    public CommandConfigurationFactory<GwtCommandConfiguration> getConfigurationFactory() {
        return configurationFactory;
    }

    @NotNull
    @Override
    public String getCommandTemplate() {
        return COMMAND_TEMPLATE + " -f " + currentProjectPathProvider.getKey() + " -Dgwt.bindAddress=" +
               devMachineHostNameProvider.getKey();
    }

    @Override
    public String getPreviewUrlTemplate() {
        return "";
    }
}

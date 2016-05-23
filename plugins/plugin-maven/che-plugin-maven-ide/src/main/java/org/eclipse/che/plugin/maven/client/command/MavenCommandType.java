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
package org.eclipse.che.plugin.maven.client.command;

import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfiguration;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationFactory;
import org.eclipse.che.ide.extension.machine.client.command.CommandConfigurationPage;
import org.eclipse.che.ide.extension.machine.client.command.CommandType;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.CurrentProjectPathProvider;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.CurrentProjectRelativePathProvider;
import org.eclipse.che.ide.extension.machine.client.command.valueproviders.ServerPortProvider;
import org.eclipse.che.plugin.maven.client.MavenResources;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.LinkedList;

/**
 * Maven command type.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class MavenCommandType implements CommandType {

    private static final String ID               = "mvn";
    private static final String DISPLAY_NAME     = "Maven";
    private static final String COMMAND_TEMPLATE = "mvn clean install";
    private static final String DEF_PORT         = "8080";

    private final MavenResources                                                       resources;
    private final CurrentProjectPathProvider                                           currentProjectPathProvider;
    private final CurrentProjectRelativePathProvider                                   currentProjectRelativePathProvider;
    private final MavenCommandConfigurationFactory                                     configurationFactory;
    private final Collection<CommandConfigurationPage<? extends CommandConfiguration>> pages;

    @Inject
    public MavenCommandType(MavenResources resources,
                            MavenCommandPagePresenter page,
                            CurrentProjectPathProvider currentProjectPathProvider,
                            CurrentProjectRelativePathProvider currentProjectRelativePathProvider,
                            IconRegistry iconRegistry) {
        this.resources = resources;
        this.currentProjectPathProvider = currentProjectPathProvider;
        this.currentProjectRelativePathProvider = currentProjectRelativePathProvider;
        configurationFactory = new MavenCommandConfigurationFactory(this);
        pages = new LinkedList<>();
        pages.add(page);

        iconRegistry.registerIcon(new Icon(ID + ".commands.category.icon", resources.maven()));
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
        return resources.mavenCommandType();
    }

    @NotNull
    @Override
    public Collection<CommandConfigurationPage<? extends CommandConfiguration>> getConfigurationPages() {
        return pages;
    }

    @NotNull
    @Override
    public CommandConfigurationFactory<MavenCommandConfiguration> getConfigurationFactory() {
        return configurationFactory;
    }

    @NotNull
    @Override
    public String getCommandTemplate() {
        return COMMAND_TEMPLATE + " -f " + currentProjectPathProvider.getKey();
    }

    @Override
    public String getPreviewUrlTemplate() {
        //TODO: hardcode http after switching WS Master to https
        return "http://" + ServerPortProvider.KEY_TEMPLATE.replace("%", DEF_PORT) + "/" +
               currentProjectRelativePathProvider.getKey();
    }
}

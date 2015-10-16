/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.internal.builder;

import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.dto.PluginCategory;
import org.eclipse.che.plugin.dto.PluginCheReloaded;
import org.eclipse.che.plugin.dto.PluginDescriptor;
import org.eclipse.che.plugin.dto.PluginInstall;
import org.eclipse.che.plugin.dto.PluginInstallStatus;
import org.eclipse.che.plugin.dto.PluginStatus;
import org.eclipse.che.plugin.internal.api.DtoBuilder;
import org.eclipse.che.plugin.internal.api.IPlugin;
import org.eclipse.che.plugin.internal.api.IPluginInstall;

import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

import static org.eclipse.che.plugin.dto.PluginCheStatus.RELOADED;

/**
 * @author Florent Benoit
 */
@Singleton
public class DtoBuilderImpl implements DtoBuilder {


    private DtoFactory dtoFactory;

    public DtoBuilderImpl() {
        this.dtoFactory = DtoFactory.getInstance();
    }


    @Override
    public PluginDescriptor convert(IPlugin plugin) {
        return dtoFactory.createDto(PluginDescriptor.class).withName(plugin.getName()).withVersion(plugin.getVersion()).withCategory(
                PluginCategory.valueOf(plugin.getCategory().name())).withStatus(PluginStatus.valueOf(plugin.getStatus().name()));
    }

    @Override
    public List<PluginDescriptor> convert(List<IPlugin> plugins) {
        return plugins.stream().map(this::convert).collect(Collectors.toList());
    }


    @Override
    public PluginInstall convert(IPluginInstall pluginInstall) {
        return dtoFactory.createDto(PluginInstall.class).withId(pluginInstall.getId()).withStatus(
                PluginInstallStatus.valueOf(pluginInstall.getStatus().name())).withLog(pluginInstall.getLog());
    }

    @Override
    public List<PluginInstall> convertInstall(List<IPluginInstall> pluginInstalls) {
        return pluginInstalls.stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public PluginCheReloaded cheReloaded() {
        return dtoFactory.createDto(PluginCheReloaded.class).withStatus(RELOADED);
    }
}

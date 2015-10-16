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

import org.eclipse.che.plugin.dto.PluginCategory;
import org.eclipse.che.plugin.dto.PluginCheReloaded;
import org.eclipse.che.plugin.dto.PluginCheStatus;
import org.eclipse.che.plugin.dto.PluginDescriptor;
import org.eclipse.che.plugin.dto.PluginInstall;
import org.eclipse.che.plugin.dto.PluginInstallStatus;
import org.eclipse.che.plugin.dto.PluginStatus;
import org.eclipse.che.plugin.internal.api.IPlugin;
import org.eclipse.che.plugin.internal.api.IPluginCategory;
import org.eclipse.che.plugin.internal.api.IPluginInstall;
import org.eclipse.che.plugin.internal.api.IPluginInstallStatus;
import org.eclipse.che.plugin.internal.api.IPluginStatus;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.testng.Assert.assertEquals;

/**
 * @author Florent Benoit
 */
public class DtoBuilderImplTest {

    private final String PLUGIN_NAME = "florent-plugin";


    @Test
    public void testIPluginUserInstalled() throws Exception {

        PluginDescriptor pluginDescriptor = new DtoBuilderImpl().convert(buildUserInstalledPlugin());
        assertEquals(pluginDescriptor.getName(), PLUGIN_NAME);
        assertEquals(pluginDescriptor.getCategory(), PluginCategory.USER);
        assertEquals(pluginDescriptor.getStatus(), PluginStatus.INSTALLED);

    }

    @Test
    public void testIPluginSystemAvailable() throws Exception {

        PluginDescriptor pluginDescriptor = new DtoBuilderImpl().convert(buildSystemAvailablePlugin());
        assertEquals(pluginDescriptor.getName(), PLUGIN_NAME);
        assertEquals(pluginDescriptor.getCategory(), PluginCategory.SYSTEM);
        assertEquals(pluginDescriptor.getStatus(), PluginStatus.AVAILABLE);

    }


    @Test
    public void testIPluginInstall() throws Exception {
        IPluginInstall pluginInstall = buildPluginInstall(1, "florent", IPluginInstallStatus.FAILED);
        PluginInstall pluginInstallDescriptor = new DtoBuilderImpl().convert(pluginInstall);
        assertEquals(pluginInstallDescriptor.getId(), 1);
        assertEquals(pluginInstallDescriptor.getLog(), "florent");
        assertEquals(pluginInstallDescriptor.getStatus(), PluginInstallStatus.FAILED);

    }

    @Test
    public void testIPluginInstallList() throws Exception {
        IPluginInstall pluginInstall = buildPluginInstall(1, "florent", IPluginInstallStatus.FAILED);
        List<PluginInstall> pluginInstallDescriptorList = new DtoBuilderImpl().convertInstall(Arrays.asList(pluginInstall));
        assertEquals(pluginInstallDescriptorList.size(), 1);

        PluginInstall pluginInstallDescriptor = pluginInstallDescriptorList.get(0);
        assertEquals(pluginInstallDescriptor.getId(), 1);
        assertEquals(pluginInstallDescriptor.getLog(), "florent");
        assertEquals(pluginInstallDescriptor.getStatus(), PluginInstallStatus.FAILED);

    }


    @Test
    public void testListPlugins() throws Exception {

        List<PluginDescriptor> pluginDescriptors = new DtoBuilderImpl().convert(Arrays.asList(buildUserInstalledPlugin(),
                                                                                              buildSystemAvailablePlugin()));

        assertEquals(pluginDescriptors.size(), 2);
        assertEquals(pluginDescriptors.get(0).getCategory(), PluginCategory.USER);
        assertEquals(pluginDescriptors.get(1).getStatus(), PluginStatus.AVAILABLE);

    }

    @Test
    public void testCheReloaded() throws Exception {
        PluginCheReloaded pluginCheReloaded = new DtoBuilderImpl().cheReloaded();
        assertEquals(pluginCheReloaded.getStatus(), PluginCheStatus.RELOADED);

    }



    IPlugin buildUserInstalledPlugin() {
        return buildPlugin(IPluginCategory.USER, IPluginStatus.INSTALLED);
    }

    IPlugin buildSystemAvailablePlugin() {
        return buildPlugin(IPluginCategory.SYSTEM, IPluginStatus.AVAILABLE);
    }

    IPlugin buildPlugin(IPluginCategory pluginCategory, IPluginStatus pluginStatus) {
        IPlugin plugin = mock(IPlugin.class);
        doReturn(PLUGIN_NAME).when(plugin).getName();
        doReturn(pluginCategory).when(plugin).getCategory();
        doReturn(pluginStatus).when(plugin).getStatus();
        return plugin;
    }



    IPluginInstall buildPluginInstall(long id,  String log, IPluginInstallStatus pluginStatus) {
        IPluginInstall pluginInstall = mock(IPluginInstall.class);
        doReturn(id).when(pluginInstall).getId();
        doReturn(pluginStatus).when(pluginInstall).getStatus();
        doReturn(log).when(pluginInstall).getLog();
        return pluginInstall;
    }

}

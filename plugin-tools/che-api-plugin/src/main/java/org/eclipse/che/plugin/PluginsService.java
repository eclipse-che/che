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
package org.eclipse.che.plugin;

import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.plugin.dto.PluginAction;
import org.eclipse.che.plugin.dto.PluginCheReloaded;
import org.eclipse.che.plugin.dto.PluginDescriptor;
import org.eclipse.che.plugin.dto.PluginInstall;
import org.eclipse.che.plugin.internal.api.DtoBuilder;
import org.eclipse.che.plugin.internal.api.IPluginAction;
import org.eclipse.che.plugin.internal.api.PluginException;
import org.eclipse.che.plugin.internal.api.PluginInstallerException;
import org.eclipse.che.plugin.internal.api.PluginInstallerNotFoundException;
import org.eclipse.che.plugin.internal.api.PluginManager;
import org.eclipse.che.plugin.internal.api.PluginManagerException;
import org.eclipse.che.plugin.internal.api.PluginRepositoryException;
import org.eclipse.che.plugin.internal.api.PluginResolverNotFoundException;

import javax.inject.Inject;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.ReflectionException;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import java.io.File;
import java.util.List;

/**
 * JAX-RS Plugins service allowing to list, install, uninstall plugins, etc.
 * @author Florent Benoit
 */
@Path("/plugins")
public class PluginsService {

    /**
     * Manager of the plugins.
     */
    private final PluginManager pluginManager;

    /**
     * DTO builder handler
     */
    private final DtoBuilder dtoBuilder;


    /**
     * Inject all required dependencies of the JAX-RS service
     */
    @Inject
    public PluginsService(final PluginManager pluginManager, final DtoBuilder dtoBuilder) {
        this.pluginManager = pluginManager;
        this.dtoBuilder = dtoBuilder;
    }

    /**
     * @return all plugins from the system. Some of them may not be installed
     */
    @GET
    public List<PluginDescriptor> listPlugins() throws PluginRepositoryException {
        return dtoBuilder.convert(pluginManager.listPlugins());
    }

    /**
     * Gets details on the specified plugin name
     * @param pluginName the name of the plugin
     * @return description of the plugin with its details
     * @throws PluginManagerException if not able to get details of the plugin
     */
    @GET
    @Path("/{plugin-name}")
    public PluginDescriptor getPluginDetails(@PathParam("plugin-name") String pluginName) throws PluginManagerException {
        return dtoBuilder.convert(pluginManager.getPluginDetails(pluginName));

    }

    /**
     * Deletes a plugin by providing its name
     * @param pluginName the mandatory name of the plugin
     * @return a descriptor if plugin has been found and deleted
     * @throws PluginManagerException if there is a failure during removal
     */
    @DELETE
    @Path("/{plugin-name}")
    public PluginDescriptor deletePlugin(@PathParam("plugin-name") String pluginName) throws PluginManagerException {
        return dtoBuilder.convert(pluginManager.removePlugin(pluginName));

    }

    /**
     * Update plugin status. For example allowing to stage a plugin from available state to staged state
     * @param pluginName the name of the plugin
     * @param pluginAction the wanted action on the plugin
     * @return description of the plugin with its new status
     * @throws PluginManagerException if not able to get details of the plugin
     */
    @PUT
    @Path("/{plugin-name}")
    public PluginDescriptor updatePlugin(@PathParam("plugin-name") String pluginName, @QueryParam("action") PluginAction pluginAction)
            throws PluginManagerException, PluginRepositoryException {
        if (pluginName == null) {
            throw new IllegalArgumentException("Should give non null plugin");
        }
        if (pluginAction == null) {
            throw new IllegalArgumentException("Should give non null action");
        }


        final IPluginAction action;
        try {
            action = IPluginAction.valueOf(pluginAction.name());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(String.format("Invalid action given %s", pluginAction), e);
        }


        return dtoBuilder.convert(pluginManager.updatePlugin(pluginName, action));
    }

    /**
     * Register/adds into Eclipse che a new plugin. It will be registered with available status
     * @param plugin the reference of the plugin
     * @return description of the plugin
     * @throws PluginManagerException if not able to add the plugin
     */
    @POST
    public PluginDescriptor addPlugin(@QueryParam("plugin") String plugin) throws PluginManagerException,
                                                                                  PluginResolverNotFoundException {
        if (plugin == null) {
            throw new IllegalArgumentException("Should give non null plugin");
        }
        return dtoBuilder.convert(pluginManager.addPlugin(plugin));
    }


    /**
     * Requires a new install of all staged plugins
     * @return status of the current installation
     */
    @POST
    @Path("/install")
    public PluginInstall newInstall() throws PluginManagerException, PluginInstallerException {
        return dtoBuilder.convert(pluginManager.requireNewInstall());
    }

    /**
     * Gets the list of all install
     * @return the IDs of every install
     */
    @GET
    @Path("/install")
    public List<PluginInstall> listInstall() throws PluginManagerException {
        return dtoBuilder.convertInstall(pluginManager.listInstall());
    }


    /**
     * Gets details for a specific install details
     * @param id the id returned when requesting a new install
     * @return the current details
     * @throws PluginInstallerNotFoundException if specified id is not existing
     */
    @GET
    @Path("/install/{id}")
    public PluginInstall getInstall(@PathParam("id") long id) throws PluginInstallerNotFoundException {
        return dtoBuilder.convert(pluginManager.getInstall(id));
    }

    /**
     * Restart Che war
     * @return the current details
     */
    @POST
    @Path("/che-reload")
    public PluginCheReloaded reloadChe() throws PluginException {

        final ObjectName objectNameQuery;
        try {
            objectNameQuery = new ObjectName("*:j2eeType=WebModule,name=//localhost/che,J2EEApplication=none,J2EEServer=none");
        } catch (MalformedObjectNameException e) {
            throw new PluginException("The che.war JMX objectName has not been found running in the server", e);
        }
        MBeanServer mbeanServer = null;
        ObjectName objectName = null;
        for (final MBeanServer server : MBeanServerFactory.findMBeanServer(null)) {
            if (server.queryNames(objectNameQuery, null).size() > 0) {
                mbeanServer = server;
                objectName = (ObjectName)server.queryNames(objectNameQuery, null).toArray()[0];
                break;
            }
        }

        if (mbeanServer == null) {
            throw new PluginException("Unable to find MBean server");
        }

        try {
            mbeanServer.invoke(objectName, "stop", null, null);
        } catch (InstanceNotFoundException | MBeanException | ReflectionException e) {
            throw new PluginException("Unable to stop the current che.war instance", e);
        }

        File cheBase = new File(System.getProperty("catalina.base"));
        IoUtil.deleteRecursive(new File(new File(cheBase, "webapps"), "che"));

        try {
            mbeanServer.invoke(objectName, "start", null, null);
        } catch (InstanceNotFoundException | MBeanException | ReflectionException e) {
            throw new PluginException("Unable to restart the current che.war instance", e);
        }


        return dtoBuilder.cheReloaded();
    }

}

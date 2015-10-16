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
package org.eclipse.che.plugin.internal;

import org.eclipse.che.plugin.internal.api.IPlugin;
import org.eclipse.che.plugin.internal.api.IPluginCategory;
import org.eclipse.che.plugin.internal.api.IPluginStatus;

import java.nio.file.Path;

/**
 * @author Florent Benoit
 */
public class Plugin implements IPlugin {


    private String          name;
    private Path            path;
    private IPluginStatus status;
    private IPluginCategory category;


    private String version;

    public String getName() {
        return name;
    }

    public Plugin setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Path of the plugin
     */
    @Override
    public Path getPath() {
        return path;
    }

    public Plugin setPath(Path path) {
        this.path = path;
        return this;
    }

    @Override
    public IPluginStatus getStatus() {
        return status;
    }

    public Plugin setStatus(IPluginStatus status) {
        this.status = status;
        return this;
    }


    @Override
    public IPluginCategory getCategory() {
        return category;
    }

    /**
     * @return version of the plugin
     */
    @Override
    public String getVersion() {
        return version;
    }

    public Plugin setVersion(String version) {
        this.version = version;
        return this;
    }


    public Plugin setCategory(IPluginCategory category) {
        this.category = category;
        return this;
    }


}

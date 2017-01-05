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
package org.eclipse.che.plugin.maven.server.core.project;

/**
 * Data class, contains maven modifications that applying during project update
 *
 * @author Evgen Vidolob
 */
public class MavenProjectModifications {
    private boolean packaging;
    private boolean sources;
    private boolean dependencies;
    private boolean plugins;

    public boolean isPackaging() {
        return packaging;
    }

    public void setPackaging(boolean packaging) {
        this.packaging = packaging;
    }

    public boolean isSources() {
        return sources;
    }

    public void setSources(boolean sources) {
        this.sources = sources;
    }

    public boolean isDependencies() {
        return dependencies;
    }

    public void setDependencies(boolean dependencies) {
        this.dependencies = dependencies;
    }

    public boolean isPlugins() {
        return plugins;
    }

    public void setPlugins(boolean plugins) {
        this.plugins = plugins;
    }

    public MavenProjectModifications addChanges(MavenProjectModifications newMod) {
        if (newMod != null) {
            MavenProjectModifications result = new MavenProjectModifications();
            result.packaging = this.packaging | newMod.packaging;
            result.sources = this.sources | newMod.sources;
            result.dependencies = this.dependencies | newMod.dependencies;
            result.plugins = this.plugins | newMod.plugins;
            return result;
        }
        return this;
    }
}

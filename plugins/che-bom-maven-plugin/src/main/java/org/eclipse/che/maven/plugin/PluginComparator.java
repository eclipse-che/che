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
package org.eclipse.che.maven.plugin;

import org.apache.maven.model.Plugin;

import java.io.Serializable;
import java.util.Comparator;

/**
 * {@link Comparator} implementation for {@link Plugin} maven model allowing to sort by groupId, artifactId and then version
 * @author Florent Benoit
 */
public class PluginComparator implements Comparator<Plugin>, Serializable {

    private static final long serialVersionUID = 1L;


    @Override
    public int compare(Plugin plugin1, Plugin plugin2) {

        // first compare groupId
        int i = plugin1.getGroupId().compareTo(plugin2.getGroupId());
        if (i != 0) {
            return i;
        }

        // then artifactId
        i = plugin1.getArtifactId().compareTo(plugin2.getArtifactId());
        if (i != 0) {
            return i;
        }

        return plugin1.getVersion().compareTo(plugin2.getVersion());
    }

}

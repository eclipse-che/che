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

import org.apache.maven.model.Dependency;

import java.io.Serializable;
import java.util.Comparator;

/**
 * {@link Comparator} implementation for {@Link Dependency} maven model allowing to sort by groupId, artifactId and then version
 * @author Florent Benoit
 */
public class DependencyComparator implements Comparator<Dependency>, Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Compare the two dependencies
     * @param dependency1 the first dependency
     * @param dependency2 the second dependency
     * @return comparator integer
     */
    @Override
    public int compare(Dependency dependency1, Dependency dependency2) {

        // first compare groupId
        int i = dependency1.getGroupId().compareTo(dependency2.getGroupId());
        if (i != 0) {
            return i;
        }

        // then artifactId
        i = dependency1.getArtifactId().compareTo(dependency2.getArtifactId());
        if (i != 0) {
            return i;
        }

        return dependency1.getVersion().compareTo(dependency2.getVersion());
    }
}

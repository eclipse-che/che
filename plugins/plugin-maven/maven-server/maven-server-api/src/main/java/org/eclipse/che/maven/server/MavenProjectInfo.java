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
package org.eclipse.che.maven.server;

import org.eclipse.che.maven.data.MavenModel;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Data class. Contains maven project model and active profiles.
 *
 * @author Evgen Vidolob
 */
public class MavenProjectInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private final MavenModel          mavenModel;
    private final Map<String, String> mavenMap;
    private final List<String>        activeProfiles;

    public MavenProjectInfo(MavenModel mavenModel, Map<String, String> mavenMap, List<String> activeProfiles) {
        this.mavenModel = mavenModel;
        this.mavenMap = mavenMap;
        this.activeProfiles = activeProfiles;
    }

    public MavenModel getMavenModel() {
        return mavenModel;
    }

    public Map<String, String> getMavenMap() {
        return mavenMap;
    }

    public List<String> getActiveProfiles() {
        return activeProfiles;
    }
}

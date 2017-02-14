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
package org.eclipse.che.api.project.server.importer;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Vitaly Parfonov
 */
@Singleton
public class ProjectImporterRegistry {

    private final Map<String, ProjectImporter> importers;

    @Inject
    public ProjectImporterRegistry(Set<ProjectImporter> importers) {
        this.importers = new ConcurrentHashMap<>();

        importers.forEach(this::register);
    }

    public void register(ProjectImporter importer) {
        importers.put(importer.getId(), importer);
    }

    public ProjectImporter unregister(String type) {
        if (type == null) {
            return null;
        }
        return importers.remove(type);
    }

    public ProjectImporter getImporter(String type) {
        if (type == null) {
            return null;
        }
        return importers.get(type);
    }

    public List<ProjectImporter> getImporters() {
        return new ArrayList<>(importers.values());
    }
}

/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.server.importer;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ProjectImporterRegistry {

  boolean isRegistered(String type);

  Optional<ProjectImporter> get(String type);

  ProjectImporter getOrNull(String type);

  Set<ProjectImporter> getAll();

  List<ProjectImporter> getAllAsList();
}

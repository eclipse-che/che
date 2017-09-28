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
package org.eclipse.che.api.project.server.api;

import java.util.List;
import java.util.Optional;

public interface ProjectImporterRegistry {
  void register(ProjectImporter importer);

  Optional<ProjectImporter> unregister(String type);

  Optional<ProjectImporter> get(String type);

  List<ProjectImporter> getAll();
}

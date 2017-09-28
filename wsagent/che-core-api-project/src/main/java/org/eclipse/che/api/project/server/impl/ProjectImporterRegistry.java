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
package org.eclipse.che.api.project.server.impl;

import static java.util.Collections.unmodifiableList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.project.server.api.ProjectImporter;

@Singleton
public class ProjectImporterRegistry
    implements org.eclipse.che.api.project.server.api.ProjectImporterRegistry {

  private final Map<String, ProjectImporter> importers = new ConcurrentHashMap<>();

  @Inject
  public ProjectImporterRegistry(Set<ProjectImporter> importers) {
    importers.forEach(this::register);
  }

  public void register(ProjectImporter importer) {
    importers.put(importer.getId(), importer);
  }

  public Optional<ProjectImporter> unregister(String type) {
    return type == null ? empty() : ofNullable(importers.remove(type));
  }

  public Optional<ProjectImporter> get(String type) {
    return type == null ? empty() : ofNullable(importers.get(type));
  }

  public List<ProjectImporter> getAll() {
    return unmodifiableList(new ArrayList<>(this.importers.values()));
  }
}

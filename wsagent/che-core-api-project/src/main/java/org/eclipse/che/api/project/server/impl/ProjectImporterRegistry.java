/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.project.server.impl;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.project.server.ProjectImporter;

/** @author Vitaly Parfonov */
@Singleton
public class ProjectImporterRegistry {

  private final Map<String, ProjectImporter> importers = new ConcurrentHashMap<>();

  @Inject
  public ProjectImporterRegistry(Set<ProjectImporter> importers) {
    importers.forEach(importer -> this.importers.put(importer.getId(), importer));
  }

  public Optional<ProjectImporter> get(String type) {
    return type == null ? empty() : ofNullable(importers.get(type));
  }

  public ProjectImporter getOrNull(String type) {
    return get(type).orElse(null);
  }

  public boolean isRegistered(String type) {
    return type != null && importers.containsKey(type);
  }

  public Set<ProjectImporter> getAll() {
    return unmodifiableSet(new HashSet<>(importers.values()));
  }

  public List<ProjectImporter> getAllAsList() {
    return unmodifiableList(new ArrayList<>(importers.values()));
  }
}

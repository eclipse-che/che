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
import org.eclipse.che.api.project.server.importer.ProjectImporter;
import org.eclipse.che.api.project.server.importer.ProjectImporterRegistry;

/** @author Vitaly Parfonov */
@Singleton
public class SimpleProjectImporterRegistry implements ProjectImporterRegistry {

  private final Map<String, ProjectImporter> importers = new ConcurrentHashMap<>();

  @Inject
  public SimpleProjectImporterRegistry(Set<ProjectImporter> importers) {
    importers.forEach(importer -> this.importers.put(importer.getId(), importer));
  }

  @Override
  public Optional<ProjectImporter> get(String type) {
    return type == null ? empty() : ofNullable(importers.get(type));
  }

  @Override
  public ProjectImporter getOrNull(String type) {
    return get(type).orElse(null);
  }

  @Override
  public boolean isRegistered(String type) {
    return type != null && importers.containsKey(type);
  }

  @Override
  public Set<ProjectImporter> getAll() {
    return unmodifiableSet(new HashSet<>(importers.values()));
  }

  @Override
  public List<ProjectImporter> getAllAsList() {
    return unmodifiableList(new ArrayList<>(importers.values()));
  }
}

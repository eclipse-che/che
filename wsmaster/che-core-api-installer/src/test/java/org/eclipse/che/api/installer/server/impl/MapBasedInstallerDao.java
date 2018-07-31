/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.installer.server.impl;

import com.google.inject.Inject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.installer.server.exception.InstallerAlreadyExistsException;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.installer.server.exception.InstallerNotFoundException;
import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.eclipse.che.api.installer.server.spi.InstallerDao;
import org.eclipse.che.api.installer.shared.model.Installer;

/**
 * Simplified implementation of the {@link InstallerDao}. It is designed to used in tests only
 * instead of mock object.
 *
 * @author Anatolii Bazko
 */
public class MapBasedInstallerDao implements InstallerDao {
  private final Map<InstallerFqn, Installer> installers;

  @Inject
  public MapBasedInstallerDao() {
    this.installers = new LinkedHashMap<>();
  }

  @Override
  public void create(InstallerImpl installer) throws InstallerException {
    InstallerFqn fqn = InstallerFqn.of(installer);
    if (installers.containsKey(fqn)) {
      throw new InstallerAlreadyExistsException("Already exists");
    }

    installers.put(fqn, installer);
  }

  @Override
  public void update(InstallerImpl installer) throws InstallerException {
    InstallerFqn fqn = InstallerFqn.of(installer);
    if (!installers.containsKey(fqn)) {
      throw new InstallerNotFoundException("Not found");
    }

    installers.put(InstallerFqn.of(installer), installer);
  }

  @Override
  public void remove(InstallerFqn fqn) throws InstallerException {
    installers.remove(fqn);
  }

  @Override
  public InstallerImpl getByFqn(InstallerFqn fqn) throws InstallerException {
    if (!installers.containsKey(fqn)) {
      throw new InstallerNotFoundException("Not found");
    }

    return new InstallerImpl(installers.get(fqn));
  }

  @Override
  public List<String> getVersions(String id) throws InstallerException {
    return installers
        .entrySet()
        .stream()
        .filter(e -> e.getKey().getId().equals(id))
        .map(e -> e.getValue().getVersion())
        .collect(Collectors.toList());
  }

  @Override
  public Page<InstallerImpl> getAll(int maxItems, long skipCount) throws InstallerException {
    List<InstallerImpl> result =
        installers
            .entrySet()
            .stream()
            .skip(skipCount)
            .limit(maxItems)
            .map(e -> new InstallerImpl(e.getValue()))
            .collect(Collectors.toList());
    return new Page<>(result, skipCount, maxItems, getTotalCount());
  }

  @Override
  public long getTotalCount() throws InstallerException {
    return installers.size();
  }
}

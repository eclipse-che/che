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
package org.eclipse.che.api.installer.server.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.installer.server.exception.InstallerAlreadyExistsException;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.eclipse.che.api.installer.server.spi.InstallerDao;
import org.eclipse.che.api.installer.shared.model.Installer;

/**
 * Local implementation of the {@link InstallerRegistry}. Persistent layer is represented by {@link
 * InstallerDao}.
 *
 * @author Anatoliy Bazko
 * @author Sergii Leshchenko
 */
@Singleton
public class LocalInstallerRegistry implements InstallerRegistry {
  private final InstallerDao installerDao;

  /** Primary registry initialization with shipped installers. */
  @Inject
  public LocalInstallerRegistry(Set<Installer> installers, InstallerDao installerDao)
      throws InstallerException {
    this.installerDao = installerDao;

    for (Installer i : installers) {
      InstallerImpl installer = new InstallerImpl(i);

      try {
        installerDao.create(installer);
      } catch (InstallerAlreadyExistsException e) {
        // ignore
      }
    }
  }

  @Override
  public void add(Installer installer) throws InstallerException {
    installerDao.create(new InstallerImpl(installer));
  }

  @Override
  public void update(Installer installer) throws InstallerException {
    installerDao.update(new InstallerImpl(installer));
  }

  @Override
  public void remove(String installerKey) throws InstallerException {
    installerDao.remove(InstallerFqn.parse(installerKey));
  }

  @Override
  public Installer getInstaller(String installerKey) throws InstallerException {
    return doGet(InstallerFqn.parse(installerKey));
  }

  @Override
  public List<String> getVersions(String id) throws InstallerException {
    return installerDao.getVersions(id);
  }

  @Override
  public Page<? extends Installer> getInstallers(int maxItems, int skipCount)
      throws InstallerException {
    return installerDao.getAll(maxItems, skipCount);
  }

  @Override
  public List<Installer> getOrderedInstallers(List<String> installers) throws InstallerException {
    Map<InstallerFqn, Installer> sorted = new LinkedHashMap<>();
    Set<InstallerFqn> pending = new HashSet<>();

    for (String installer : installers) {
      doSort(InstallerFqn.parse(installer), sorted, pending);
    }

    return new ArrayList<>(sorted.values());
  }

  private void doSort(
      InstallerFqn installerFqn, Map<InstallerFqn, Installer> sorted, Set<InstallerFqn> pending)
      throws InstallerException {
    if (sorted.containsKey(installerFqn)) {
      return;
    }

    Installer installer = doGet(installerFqn);

    pending.add(installerFqn);

    for (String dependency : installer.getDependencies()) {
      InstallerFqn dependencyFqn = InstallerFqn.parse(dependency);
      if (pending.contains(dependencyFqn)) {
        throw new InstallerException(
            String.format(
                "Installers circular dependency found between '%s' and '%s'",
                dependencyFqn.toString(), installerFqn));
      }
      doSort(dependencyFqn, sorted, pending);
    }

    sorted.put(installerFqn, installer);
    pending.remove(InstallerFqn.of(installer));
  }

  private Installer doGet(InstallerFqn installerFqn) throws InstallerException {
    return installerDao.getByFqn(installerFqn);
  }
}

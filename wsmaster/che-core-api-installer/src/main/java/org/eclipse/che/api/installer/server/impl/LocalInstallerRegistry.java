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

import static java.lang.String.format;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.installer.server.exception.InstallerNotFoundException;
import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.eclipse.che.api.installer.server.spi.InstallerDao;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Local implementation of the {@link InstallerRegistry}. Persistent layer is represented by {@link
 * InstallerDao}.
 *
 * @author Anatoliy Bazko
 * @author Sergii Leshchenko
 */
@Singleton
public class LocalInstallerRegistry implements InstallerRegistry {
  private static final Logger LOG = LoggerFactory.getLogger(LocalInstallerRegistry.class);
  private final InstallerDao installerDao;
  private final InstallerValidator installerValidator;

  /** Primary registry initialization with shipped installers. */
  @Inject
  public LocalInstallerRegistry(
      Set<Installer> installers, InstallerDao installerDao, InstallerValidator installerValidator)
      throws InstallerException {
    this.installerDao = installerDao;
    this.installerValidator = installerValidator;

    for (Installer i : installers) {
      doInit(installerDao, i);
    }
  }

  private void doInit(InstallerDao installerDao, Installer i) throws InstallerException {
    InstallerImpl installer = new InstallerImpl(i);
    InstallerFqn installerFqn = InstallerFqn.of(i);
    String installerKey = installerFqn.toKey();

    installerValidator.validate(i);
    try {
      InstallerImpl existing = installerDao.getByFqn(installerFqn);
      if (existing.equals(installer)) {
        LOG.info(
            format("Latest version of installer '%s' is already in the registry.", installerKey));
      } else {
        installerDao.update(installer);
        LOG.info(format("Installer '%s' updated in the registry.", installerKey));
      }
    } catch (InstallerNotFoundException e) {
      installerDao.create(installer);
      LOG.info(format("Installer '%s' added to the registry.", installerKey));
    }
  }

  @Override
  public void add(Installer installer) throws InstallerException {
    installerValidator.validate(installer);
    installerDao.create(new InstallerImpl(installer));
  }

  @Override
  public void update(Installer installer) throws InstallerException {
    installerValidator.validate(installer);
    installerDao.update(new InstallerImpl(installer));
  }

  @Override
  public void remove(String installerKey) throws InstallerException {
    InstallerFqn installerFqn = InstallerFqn.parse(installerKey);
    installerDao.remove(stripOffLatestTag(installerFqn));
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
  public List<Installer> getOrderedInstallers(List<String> installerKeys)
      throws InstallerException {
    LinkedHashMap<InstallerFqn, Installer> sorted = new LinkedHashMap<>();
    Set<InstallerFqn> pending = new HashSet<>();

    for (String installer : installerKeys) {
      InstallerFqn installerFqn = InstallerFqn.parse(installer);
      doSort(stripOffLatestTag(installerFqn), sorted, pending);
    }

    return new ArrayList<>(sorted.values());
  }

  private void doSort(
      InstallerFqn installerFqn,
      LinkedHashMap<InstallerFqn, Installer> sorted,
      Set<InstallerFqn> pending)
      throws InstallerException {
    if (sorted.keySet().contains(installerFqn)) {
      return;
    }
    pending.add(installerFqn);

    Installer installer = doGet(installerFqn);
    for (String dependencyKey : installer.getDependencies()) {
      InstallerFqn dependencyFqn = stripOffLatestTag(InstallerFqn.parse(dependencyKey));
      if (pending.contains(dependencyFqn)) {
        throw new InstallerException(
            format(
                "Installers circular dependency found between '%s' and '%s'",
                dependencyFqn, installerFqn));
      }

      doSort(dependencyFqn, sorted, pending);
    }

    if (InstallerFqn.idInFqnList(installerFqn.getId(), sorted.keySet())) {
      throw new InstallerException(
          format(
              "Installers dependencies conflict. Several version '%s' and '%s' of the some id '%s",
              installerFqn.getVersion(),
              sorted
                  .keySet()
                  .stream()
                  .filter(i -> i.getId().equals(installerFqn.getId()))
                  .findFirst()
                  .get()
                  .getVersion(),
              installerFqn.getId()));
    }
    sorted.put(installerFqn, installer);
    pending.remove(InstallerFqn.of(installer));
  }

  private Installer doGet(InstallerFqn installerFqn) throws InstallerException {
    return installerDao.getByFqn(stripOffLatestTag(installerFqn));
  }

  private InstallerFqn stripOffLatestTag(InstallerFqn installerFqn) throws InstallerException {
    if (!installerFqn.hasLatestTag()) {
      return installerFqn;
    }

    Optional<ComparableVersion> latestVersion =
        getVersions(installerFqn.getId())
            .stream()
            .map(
                v -> {
                  try {
                    return new ComparableVersion(v);
                  } catch (Exception e) {
                    LOG.error(
                        format(
                            "Invalid version '%s' for installer '%s'. Skipped.",
                            installerFqn.getId(), v));
                    return null;
                  }
                })
            .filter(Objects::nonNull)
            .max(ComparableVersion::compareTo);

    if (!latestVersion.isPresent()) {
      throw new InstallerNotFoundException(
          format("No installer '%s' found of the latest version", installerFqn.getId()));
    }

    return new InstallerFqn(installerFqn.getId(), latestVersion.get().toString());
  }
}

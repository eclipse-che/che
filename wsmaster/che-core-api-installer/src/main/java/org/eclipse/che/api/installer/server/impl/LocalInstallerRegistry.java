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
package org.eclipse.che.api.installer.server.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.installer.server.InstallerRegistry;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.installer.server.exception.InstallerNotFoundException;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.api.installer.shared.model.InstallerKey;
import org.eclipse.che.api.installer.server.model.impl.InstallerKeyImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableCollection;

/**
 * Local implementation of the {@link InstallerRegistry}.
 * The name of the installer might represent a url.
 *
 * @author Anatoliy Bazko
 */
@Singleton
public class LocalInstallerRegistry implements InstallerRegistry {

    protected static final Logger LOG = LoggerFactory.getLogger(LocalInstallerRegistry.class);

    private final Map<InstallerKey, Installer> installers;

    /**
     * LocalInstallerRegistry constructor.
     *
     * @param installers
     *      list of installers to register
     * @throws IllegalArgumentException
     *      if there are several installers with same id and version
     */
    @Inject
    public LocalInstallerRegistry(Set<Installer> installers) throws IllegalArgumentException {
        this.installers = new HashMap<>(installers.size());
        for (Installer installer : installers) {
            InstallerKeyImpl key = new InstallerKeyImpl(installer);
            Installer registeredInstaller = this.installers.put(key, installer);
            if (registeredInstaller != null) {
                throw new IllegalArgumentException(format("Installer with key %s has been registered already.", key));
            }
        }
    }

    @Override
    public Installer getInstaller(InstallerKey installerKey) throws InstallerException {
        return doGetInstaller(installerKey);
    }

    @Override
    public List<String> getVersions(String id) throws InstallerException {
        return installers.entrySet().stream()
                     .filter(e -> e.getKey().getId().equals(id))
                     .map(e -> e.getKey().getVersion())
                     .collect(Collectors.toList());
    }

    @Override
    public Collection<Installer> getInstallers() throws InstallerException {
        return unmodifiableCollection(installers.values());
    }

    @Override
    public List<Installer> getOrderedInstallers(List<InstallerKey> keys) {
        //TODO implement it
        throw new RuntimeException("Not implemented method: LocalInstallerRegistry.getOrderedInstallers()");
    }

    private Installer doGetInstaller(InstallerKey key) throws InstallerException {
        Optional<Installer> installer = Optional.ofNullable(installers.get(key));
        return installer.orElseThrow(() -> new InstallerNotFoundException(format("Installer %s not found", key.getId())));
    }
}

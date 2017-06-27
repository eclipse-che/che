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
import org.eclipse.che.api.installer.server.exception.IllegalInstallerKey;
import org.eclipse.che.api.installer.server.exception.InstallerException;
import org.eclipse.che.api.installer.server.exception.InstallerNotFoundException;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.commons.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.unmodifiableCollection;

/**
 * Local implementation of the {@link InstallerRegistry}.
 *
 * @author Anatoliy Bazko
 * @author Sergii Leshchenko
 */
@Singleton
public class LocalInstallerRegistry implements InstallerRegistry {
    private final Map<InstallerFqn, Installer> installers;

    @Inject
    public LocalInstallerRegistry(Set<Installer> installers) throws IllegalArgumentException {
        this.installers = new HashMap<>(installers.size());
        for (Installer installer : installers) {
            InstallerFqn fqn = new InstallerFqn(installer.getId(), installer.getVersion());
            Installer registeredInstaller = this.installers.put(fqn, installer);
            if (registeredInstaller != null) {
                throw new IllegalArgumentException("Installer with fqn '" + fqn.toString()
                                                   + "' has been registered already.");
            }
        }
    }

    @Override
    public Installer getInstaller(String installerKey) throws InstallerNotFoundException {
        return doGet(InstallerFqn.parse(installerKey));
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
    public List<Installer> getOrderedInstallers(List<String> installers) throws InstallerException {
        Map<InstallerFqn, Installer> sorted = new LinkedHashMap<>();
        Set<InstallerFqn> pending = new HashSet<>();

        for (String installer : installers) {
            doSort(InstallerFqn.parse(installer), sorted, pending);
        }

        return new ArrayList<>(sorted.values());
    }

    private void doSort(InstallerFqn installerFqn,
                        Map<InstallerFqn, Installer> sorted,
                        Set<InstallerFqn> pending) throws InstallerException {
        if (sorted.containsKey(installerFqn)) {
            return;
        }

        Installer installer = doGet(installerFqn);

        pending.add(installerFqn);

        for (String dependency : installer.getDependencies()) {
            InstallerFqn dependencyFqn = InstallerFqn.parse(dependency);
            if (pending.contains(dependencyFqn)) {
                throw new InstallerException(
                        String.format("Installers circular dependency found between '%s' and '%s'",
                                      dependencyFqn.toString(),
                                      installerFqn));
            }
            doSort(dependencyFqn, sorted, pending);
        }

        sorted.put(installerFqn, installer);
        pending.remove(InstallerFqn.of(installer));
    }

    private Installer doGet(InstallerFqn installerFqn) throws InstallerNotFoundException {
        Installer installer = installers.get(installerFqn);

        if (installer == null) {
            throw new InstallerNotFoundException(format("Installer %s not found", installerFqn));
        }
        return installer;
    }

    /**
     * @author Anatolii Bazko
     */
    static class InstallerFqn {
        static final String DEFAULT_VERSION = "latest";

        private final String id;
        private final String version;

        InstallerFqn(String id, @Nullable String version) {
            this.id = id;
            this.version = version == null ? DEFAULT_VERSION : version;
        }

        public String getId() {
            return id;
        }

        public String getVersion() {
            return version;
        }

        /**
         * Factory method. Installer key is basically a string meeting the format: {@code id:version}.
         * The version part can be omitted.
         *
         * @throws IllegalInstallerKey
         *         in case of wrong format
         */
        public static InstallerFqn parse(String installerKey) {
            String[] parts = installerKey.split(":");

            if (parts.length == 1) {
                return new InstallerFqn(parts[0], null);
            } else if (parts.length == 2) {
                return new InstallerFqn(parts[0], parts[1]);
            } else {
                throw new IllegalInstallerKey("Illegal installer key format: " + installerKey);
            }
        }

        /**
         * Factory method for fetching fqn of installer.
         */
        public static InstallerFqn of(Installer installer) {
            return new InstallerFqn(installer.getId(), installer.getVersion());
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InstallerFqn)) return false;
            InstallerFqn installerFqn = (InstallerFqn)o;
            return Objects.equals(id, installerFqn.id) &&
                   Objects.equals(version, installerFqn.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, version);
        }

        @Override
        public String toString() {
            return id + ":" + version;
        }
    }
}

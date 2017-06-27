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
import org.eclipse.che.api.installer.server.model.impl.InstallerKeyImpl;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.api.installer.shared.model.InstallerKey;
import org.eclipse.che.commons.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Sort installers respecting dependencies between them.
 *
 * @author Anatolii Bazko
 */
@Singleton
// TODO not a standalone component, use this logic inside InstallerRegistry.getOrderedInstallers(List <InstallerKey> keys) method
public class InstallerSorter {

    private final InstallerRegistry installerRegistry;

    @Inject
    public InstallerSorter(InstallerRegistry installerRegistry) {
        this.installerRegistry = installerRegistry;
    }

    /**
     * Sort installers respecting dependencies between them.
     * Handles circular dependencies.
     *
     * @see InstallerKey
     * @see Installer#getDependencies()
     * @see InstallerRegistry#getInstaller(InstallerKey)
     *
     * @param installerKeys list of installers to sort
     * @return list of created installers in proper order
     *
     * @throws InstallerException
     *      if circular dependency found or installer creation failed or other unexpected error
     */
    public List<InstallerKey> sort(@Nullable List<String> installerKeys) throws InstallerException {
        List<InstallerKey> sorted = new ArrayList<>();
        Set<String> pending = new HashSet<>();

        if (installerKeys != null) {
            for (String installerKey : installerKeys) {
                if (installerKey != null) {
                    doSort(InstallerKeyImpl.parse(installerKey), sorted, pending);
                }
            }
        }

        return sorted;
    }

    private void doSort(InstallerKey installerKey, List<InstallerKey> sorted, Set<String> pending) throws InstallerException {
        String installerId = installerKey.getId();

        Optional<InstallerKey> alreadySorted = sorted.stream().filter(k -> k.getId().equals(installerId)).findFirst();
        if (alreadySorted.isPresent()) {
            return;
        }
        pending.add(installerId);

        Installer installer = installerRegistry.getInstaller(installerKey);

        for (String dependency : installer.getDependencies()) {
            if (pending.contains(dependency)) {
                throw new InstallerException(
                        String.format("Installers circular dependency found between '%s' and '%s'", dependency, installerId));
            }
            doSort(InstallerKeyImpl.parse(dependency), sorted, pending);
        }

        sorted.add(installerKey);
        pending.remove(installerId);
    }
}

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
package org.eclipse.che.api.installer.server.model.impl;

import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.api.installer.shared.model.InstallerKey;
import org.eclipse.che.commons.annotation.Nullable;

import java.util.Objects;

/**
 * @author Anatolii Bazko
 */
public class InstallerKeyImpl implements InstallerKey {
    private static final String DEFAULT_VERSION = "latest";
    private final String id;
    private final String version;

    public InstallerKeyImpl(String id, @Nullable String version) {
        this.id = id;
        this.version = version == null ? DEFAULT_VERSION : version;
    }

    public InstallerKeyImpl(String id) {
        this(id, null);
    }

    public InstallerKeyImpl(Installer installer) {
        this(installer.getId(), installer.getVersion());
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
     * @throws IllegalArgumentException
     *      in case of wrong format
     */
    public static InstallerKeyImpl parse(String installerKey) throws IllegalArgumentException {
        String[] parts = installerKey.split(":");

        if (parts.length == 1) {
            return new InstallerKeyImpl(parts[0]);
        } else if (parts.length == 2) {
            return new InstallerKeyImpl(parts[0], parts[1]);
        } else {
            throw new IllegalArgumentException("Illegal format: " + installerKey);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InstallerKeyImpl)) return false;
        InstallerKeyImpl installerKey = (InstallerKeyImpl)o;
        return Objects.equals(id, installerKey.id) &&
               Objects.equals(version, installerKey.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, version);
    }

    public String asString() {
        return id + (version != null ? ":" + version : "");
    }

    @Override
    public String toString() {
        return "InstallerImpl{" +
               "id='" + id + '\'' +
               ", version='" + version + "\'}";
    }
}

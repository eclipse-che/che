/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.ssh.server.model.impl;

import org.eclipse.che.api.ssh.shared.model.SshPair;
import org.eclipse.che.commons.annotation.Nullable;

import java.util.Objects;

/**
 * @author Sergii Leschenko
 */
public class SshPairImpl implements SshPair {
    private final String service;
    private final String name;
    private final String publicKey;
    private final String privateKey;

    public SshPairImpl(String service, String name, String publicKey, String privateKey) {
        this.service = service;
        this.name = name;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    public SshPairImpl(SshPair sshPair) {
        this.service = sshPair.getService();
        this.name = sshPair.getName();
        this.publicKey = sshPair.getPublicKey();
        this.privateKey = sshPair.getPrivateKey();
    }

    @Override
    public String getService() {
        return service;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    @Nullable
    public String getPublicKey() {
        return publicKey;
    }

    @Override
    @Nullable
    public String getPrivateKey() {
        return privateKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SshPairImpl)) return false;
        final SshPairImpl other = (SshPairImpl)obj;
        return Objects.equals(service, other.service) &&
               Objects.equals(name, other.name) &&
               Objects.equals(publicKey, other.publicKey) &&
               Objects.equals(privateKey, other.privateKey);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(service);
        hash = 31 * hash + Objects.hashCode(name);
        hash = 31 * hash + Objects.hashCode(publicKey);
        hash = 31 * hash + Objects.hashCode(privateKey);
        return hash;
    }

    @Override
    public String toString() {
        return "SshPairImpl{" +
               "service='" + service + '\'' +
               ", name='" + name + '\'' +
               ", publicKey='" + publicKey + '\'' +
               ", privateKey='" + privateKey + '\'' +
               '}';
    }
}

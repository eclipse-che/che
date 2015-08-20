/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.server;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.che.ide.ext.ssh.server.SshKey;
import org.eclipse.che.ide.ext.ssh.server.SshKeyPair;
import org.eclipse.che.ide.ext.ssh.server.SshKeyStore;
import org.eclipse.che.ide.ext.ssh.server.SshKeyStoreException;

import javax.inject.Singleton;
import java.util.Set;

/**
 * //
 *
 * @author Vitalii Parfonov
 */
@Singleton
public class RemoteSshKeyStore implements SshKeyStore {
    @Override
    public void addPrivateKey(String host, byte[] key) throws SshKeyStoreException {
        throw new NotImplementedException("Not implement yet");
    }

    @Override
    public SshKey getPrivateKey(String host) throws SshKeyStoreException {
        throw new NotImplementedException("Not implement yet");
    }

    @Override
    public SshKey getPublicKey(String host) throws SshKeyStoreException {
        throw new NotImplementedException("Not implement yet");
    }

    @Override
    public SshKeyPair genKeyPair(String host, String comment, String passPhrase) throws SshKeyStoreException {
        throw new NotImplementedException("Not implement yet");
    }

    @Override
    public SshKeyPair genKeyPair(String host, String comment, String passPhrase, String keyMail) throws SshKeyStoreException {
        throw new NotImplementedException("Not implement yet");
    }

    @Override
    public void removeKeys(String host) throws SshKeyStoreException {
        throw new NotImplementedException("Not implement yet");
    }

    @Override
    public Set<String> getAll() throws SshKeyStoreException {
        throw new NotImplementedException("Not implement yet");
    }
}

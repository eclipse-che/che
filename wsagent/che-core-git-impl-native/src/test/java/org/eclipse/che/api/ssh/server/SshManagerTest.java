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
package org.eclipse.che.api.ssh.server;

import com.jcraft.jsch.JSch;

import org.eclipse.che.api.ssh.server.model.impl.SshPairImpl;
import org.eclipse.che.api.ssh.server.spi.SshDao;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class SshManagerTest {
    private final String OWNER = "user123";

    @Captor
    ArgumentCaptor<SshPairImpl> sshPairCaptor;

    @Mock
    SshDao sshDao;

    @InjectMocks
    SshManager sshManager;

    @Test
    public void shouldGenerateSshPair() throws Exception {
        SshPairImpl generatedPair = sshManager.generatePair(OWNER, "service", "name");

        verify(sshDao).create(sshPairCaptor.capture());
        SshPairImpl storedSshPair = sshPairCaptor.getValue();
        assertEquals(generatedPair, storedSshPair);
        assertEquals(generatedPair.getName(), "name");
        assertEquals(generatedPair.getService(), "service");
        assertNotNull(generatedPair.getPrivateKey());
        assertNotNull(generatedPair.getPublicKey());
    }


    @Test
    public void shouldCreateSshPair() throws Exception {
        SshPairImpl sshPair = createSshPair();
        sshManager.createPair(sshPair);

        verify(sshDao).create(eq(sshPair));
    }

    @Test
    public void shouldBeAbleToRemoveSshPair() throws Exception {
        sshManager.removePair(OWNER, "service", "name");

        sshDao.remove(eq(OWNER), eq("service"), eq("name"));
    }

    @Test
    public void shouldBeAbleToGetPair() throws Exception {
        SshPairImpl sshPair = createSshPair();
        when(sshDao.get(anyString(), anyString(), anyString())).thenReturn(sshPair);

        SshPairImpl foundPair = sshManager.getPair(OWNER, "service", "name");

        verify(sshDao).get(OWNER, "service", "name");
        assertEquals(sshPair, foundPair);
    }

    @Test
    public void shouldBeAbleToGetPairs() throws Exception {
        SshPairImpl sshPair = createSshPair();
        when(sshDao.get(anyString(), anyString())).thenReturn(Collections.singletonList(sshPair));

        List<SshPairImpl> foundPairs = sshManager.getPairs(OWNER, "service");

        verify(sshDao).get(OWNER, "service");
        assertEquals(foundPairs.size(), 1);
        assertEquals(foundPairs.get(0), sshPair);
    }

    private SshPairImpl createSshPair() {
        return new SshPairImpl(OWNER,
                               "service",
                               "name",
                               "publicKey",
                               "privateKey");
    }

}

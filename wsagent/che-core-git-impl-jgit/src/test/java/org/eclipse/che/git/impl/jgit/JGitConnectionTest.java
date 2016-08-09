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
package org.eclipse.che.git.impl.jgit;

import org.eclipse.che.api.git.CredentialsLoader;
import org.eclipse.che.api.git.GitUserResolver;
import org.eclipse.che.plugin.ssh.key.script.SshKeyProvider;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.testng.AssertJUnit.assertEquals;

/**
 * Test class for {@link JGitConnection}
 *
 * @author Igor Vinokur
 */
@Listeners(value = {MockitoTestNGListener.class})
public class JGitConnectionTest {

    @Mock
    Repository repository;
    @Mock
    CredentialsLoader credentialsLoader;
    @Mock
    SshKeyProvider sshKeyProvider;
    @Mock
    GitUserResolver gitUserResolver;

    @InjectMocks
    JGitConnection jGitConnection;

    @DataProvider(name = "gitUrlsWithCredentialsProvider")
    public static Object[][] gitUrlsWithCredentials() {
        return new Object[][]{{"http://username:password@host.xz/path/to/repo.git"},
                              {"https://username:password@host.xz/path/to/repo.git"}};
    }

    @Test(dataProvider = "gitUrlsWithCredentials")
    public void shouldExecuteRemoteCommandByHttpOrHttpsUrlWithCredentials(String url) throws Exception {
        //given
        TransportCommand transportCommand = mock(TransportCommand.class);
        ArgumentCaptor<UsernamePasswordCredentialsProvider> captor = ArgumentCaptor.forClass(UsernamePasswordCredentialsProvider.class);
        Field usernameField = UsernamePasswordCredentialsProvider.class.getDeclaredField("username");
        Field passwordField = UsernamePasswordCredentialsProvider.class.getDeclaredField("password");
        usernameField.setAccessible(true);
        passwordField.setAccessible(true);

        //when
        jGitConnection.executeRemoteCommand(url, transportCommand);

        //then
        verify(transportCommand).setCredentialsProvider(captor.capture());
        UsernamePasswordCredentialsProvider credentialsProvider = captor.getValue();
        String username = (String)usernameField.get(credentialsProvider);
        char[] password = (char[])passwordField.get(credentialsProvider);
        assertEquals("username", username);
        assertEquals("password", String.valueOf(password));
    }
}

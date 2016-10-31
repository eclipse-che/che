/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *   SAP           - implementation
 *******************************************************************************/
package org.eclipse.che.git.impl.jgit;

import org.eclipse.che.api.git.CredentialsLoader;
import org.eclipse.che.api.git.GitUserResolver;
import org.eclipse.che.plugin.ssh.key.script.SshKeyProvider;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.TransportHttp;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.lang.reflect.Field;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

/**
 * Test class for {@link JGitConnection}
 *
 * @author Igor Vinokur
 */
@Listeners(value = {MockitoTestNGListener.class})
public class JGitConnectionTest {

    @Mock
    private Repository repository;
    @Mock
    private CredentialsLoader credentialsLoader;
    @Mock
    private SshKeyProvider sshKeyProvider;
    @Mock
    private GitUserResolver gitUserResolver;
    @Mock
    private TransportCommand transportCommand;
    @InjectMocks
    private JGitConnection jGitConnection;

    @DataProvider(name = "gitUrlsWithCredentialsProvider")
    private static Object[][] gitUrlsWithCredentials() {
        return new Object[][]{{"http://username:password@host.xz/path/to/repo.git"},
                              {"https://username:password@host.xz/path/to/repo.git"}};
    }

    @DataProvider(name = "gitUrlsWithoutOrWrongCredentialsProvider")
    private static Object[][] gitUrlsWithoutOrWrongCredentials() {
        return new Object[][]{{"http://host.xz/path/to/repo.git"},
                              {"https://host.xz/path/to/repo.git"},
                              {"http://username:@host.xz/path/to/repo.git"},
                              {"https://username:@host.xz/path/to/repo.git"},
                              {"http://:password@host.xz/path/to/repo.git"},
                              {"https://:password@host.xz/path/to/repo.git"}};
    }

    @Test(dataProvider = "gitUrlsWithCredentials")
    public void shouldExecuteRemoteCommandByHttpOrHttpsUrlWithCredentials(String url) throws Exception {
        //given
        ArgumentCaptor<UsernamePasswordCredentialsProvider> captor = ArgumentCaptor.forClass(UsernamePasswordCredentialsProvider.class);
        Field usernameField = UsernamePasswordCredentialsProvider.class.getDeclaredField("username");
        Field passwordField = UsernamePasswordCredentialsProvider.class.getDeclaredField("password");
        usernameField.setAccessible(true);
        passwordField.setAccessible(true);

        //when
        jGitConnection.executeRemoteCommand(url, transportCommand, null, null);

        //then
        verify(transportCommand).setCredentialsProvider(captor.capture());
        UsernamePasswordCredentialsProvider credentialsProvider = captor.getValue();
        String username = (String)usernameField.get(credentialsProvider);
        char[] password = (char[])passwordField.get(credentialsProvider);
        assertEquals("username", username);
        assertEquals("password", String.valueOf(password));
    }

    @Test(dataProvider = "gitUrlsWithoutOrWrongCredentials")
    public void shouldNotSetCredentialsProviderIfUrlDoesNotContainCredentials(String url) throws Exception{
        //when
        jGitConnection.executeRemoteCommand(url, transportCommand, null, null);

        //then
        verify(transportCommand, never()).setCredentialsProvider(any());
    }

    @Test
    public void shouldSetSshSessionFactoryWhenSshTransportReceived() throws Exception{
        //given
        SshTransport sshTransport = mock(SshTransport.class);
        when(sshKeyProvider.getPrivateKey(anyString())).thenReturn(new byte[0]);
        doAnswer(invocation -> {
            TransportConfigCallback callback = (TransportConfigCallback)invocation.getArguments()[0];
            callback.configure(sshTransport);
            return null;
        }).when(transportCommand).setTransportConfigCallback(any());

        //when
        jGitConnection.executeRemoteCommand("ssh://host.xz/repo.git", transportCommand, null, null);

        //then
        verify(sshTransport).setSshSessionFactory(any());
    }

    @Test
    public void shouldDoNothingWhenTransportHttpReceived() throws Exception{
        //given
        TransportHttp transportHttp = mock(TransportHttp.class);
        when(sshKeyProvider.getPrivateKey(anyString())).thenReturn(new byte[0]);
        doAnswer(invocation -> {
            TransportConfigCallback callback = (TransportConfigCallback)invocation.getArguments()[0];
            callback.configure(transportHttp);
            return null;
        }).when(transportCommand).setTransportConfigCallback(any());

        //when
        jGitConnection.executeRemoteCommand("ssh://host.xz/repo.git", transportCommand, null, null);

        //then
        verifyZeroInteractions(transportHttp);
    }

    /**
     * Check branch using current repository reference is returned
     * @throws Exception if it fails
     */
    @Test
    public void checkCurrentBranch() throws Exception {
        String branchTest = "helloWorld";
        Ref ref = mock(Ref.class);
        when(repository.exactRef(Constants.HEAD)).thenReturn(ref);
        when(ref.getLeaf()).thenReturn(ref);
        when(ref.getName()).thenReturn(branchTest);
        String branchName = jGitConnection.getCurrentBranch();

        assertEquals(branchTest, branchName);
    }
}

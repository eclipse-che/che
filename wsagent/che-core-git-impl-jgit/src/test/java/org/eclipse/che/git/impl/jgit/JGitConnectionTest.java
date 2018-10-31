/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *   SAP           - implementation
 */
package org.eclipse.che.git.impl.jgit;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyObject;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import java.io.File;
import java.lang.reflect.Field;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.util.LineConsumerFactory;
import org.eclipse.che.api.git.CredentialsLoader;
import org.eclipse.che.api.git.GitUserResolver;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.params.CloneParams;
import org.eclipse.che.api.git.params.CommitParams;
import org.eclipse.che.api.git.shared.GitUser;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.che.plugin.ssh.key.script.SshKeyProvider;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryState;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.TransportHttp;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test class for {@link JGitConnection}
 *
 * @author Igor Vinokur
 */
@Listeners(value = MockitoTestNGListener.class)
public class JGitConnectionTest {

  @Mock private Repository repository;
  @Mock private CredentialsLoader credentialsLoader;
  @Mock private SshKeyProvider sshKeyProvider;
  @Mock private GitUserResolver gitUserResolver;
  @Mock private TransportCommand transportCommand;
  @Mock private GitUserResolver userResolver;
  @Mock private EventService eventService;
  @Mock private File directory;

  private JGitConnection jGitConnection;

  @BeforeMethod
  public void setup() {
    jGitConnection =
        spy(
            new JGitConnection(
                repository, credentialsLoader, sshKeyProvider, eventService, userResolver));

    RepositoryState repositoryState = mock(RepositoryState.class);
    GitUser gitUser = mock(GitUser.class);
    lenient().when(repositoryState.canAmend()).thenReturn(true);
    lenient().when(repositoryState.canCommit()).thenReturn(true);
    lenient().when(repository.getRepositoryState()).thenReturn(repositoryState);
    lenient().when(gitUser.getName()).thenReturn("username");
    lenient().when(gitUser.getEmail()).thenReturn("email");
    lenient().when(userResolver.getUser()).thenReturn(gitUser);
    lenient().when(repository.getDirectory()).thenReturn(directory);
    lenient().when(directory.getPath()).thenReturn("path");
  }

  @DataProvider(name = "gitUrlsWithCredentialsProvider")
  private static Object[][] gitUrlsWithCredentials() {
    return new Object[][] {
      {"http://username:password@host.xz/path/to/repo.git"},
      {"https://username:password@host.xz/path/to/repo.git"}
    };
  }

  @DataProvider(name = "gitUrlsWithoutOrWrongCredentialsProvider")
  private static Object[][] gitUrlsWithoutOrWrongCredentials() {
    return new Object[][] {
      {"http://host.xz/path/to/repo.git"},
      {"https://host.xz/path/to/repo.git"},
      {"http://username:@host.xz/path/to/repo.git"},
      {"https://username:@host.xz/path/to/repo.git"},
      {"http://:password@host.xz/path/to/repo.git"},
      {"https://:password@host.xz/path/to/repo.git"}
    };
  }

  @Test(dataProvider = "gitUrlsWithCredentials")
  public void shouldExecuteRemoteCommandByHttpOrHttpsUrlWithCredentials(String url)
      throws Exception {
    // given
    ArgumentCaptor<UsernamePasswordCredentialsProvider> captor =
        ArgumentCaptor.forClass(UsernamePasswordCredentialsProvider.class);
    Field usernameField = UsernamePasswordCredentialsProvider.class.getDeclaredField("username");
    Field passwordField = UsernamePasswordCredentialsProvider.class.getDeclaredField("password");
    usernameField.setAccessible(true);
    passwordField.setAccessible(true);

    // when
    jGitConnection.executeRemoteCommand(url, transportCommand, null, null);

    // then
    verify(transportCommand).setCredentialsProvider(captor.capture());
    UsernamePasswordCredentialsProvider credentialsProvider = captor.getValue();
    String username = (String) usernameField.get(credentialsProvider);
    char[] password = (char[]) passwordField.get(credentialsProvider);
    assertEquals(username, "username");
    assertEquals(String.valueOf(password), "password");
  }

  @Test(dataProvider = "gitUrlsWithoutOrWrongCredentials")
  public void shouldNotSetCredentialsProviderIfUrlDoesNotContainCredentials(String url)
      throws Exception {
    // when
    jGitConnection.executeRemoteCommand(url, transportCommand, null, null);

    // then
    verify(transportCommand, never()).setCredentialsProvider(any());
  }

  @Test
  public void shouldSetSshSessionFactoryWhenSshTransportReceived() throws Exception {
    // given
    SshTransport sshTransport = mock(SshTransport.class);
    when(sshKeyProvider.getPrivateKey(anyString())).thenReturn(new byte[0]);
    doAnswer(
            invocation -> {
              TransportConfigCallback callback =
                  (TransportConfigCallback) invocation.getArguments()[0];
              callback.configure(sshTransport);
              return null;
            })
        .when(transportCommand)
        .setTransportConfigCallback(any());

    // when
    jGitConnection.executeRemoteCommand("ssh://host.xz/repo.git", transportCommand, null, null);

    // then
    verify(sshTransport).setSshSessionFactory(any());
  }

  @Test
  public void shouldDoNothingWhenTransportHttpReceived() throws Exception {
    // given

    /*
     * We need create {@link TransportHttp} mock, but this class has parent
     * abstract class {@link Transport}. Class Transport uses fields of children
     * classes for static initialization collection {@link Transport#protocols}.
     * When we create mock for {@link TransportHttp} - Mockito mocks fields and
     * they return null value. For full mock creation TransportHttp Mockito
     * launches static block in the parent class {@link Transport}, but static
     * block initializes collection with help mocked children fields which
     * return null values, so Transport class loses real field value in the
     * collection. It creates troubles in other tests when we use real object
     * of TransportHttp(collection 'protocols' contains not all values).
     * To realize right initialization {@link Transport#protocols} we create
     * mock of {@link Transport} and this class initializes collection "protocols"
     * with  help real children {@link TransportHttp}, which returns real not null
     * value. And then we can create mock {@link TransportHttp}.
     */
    org.eclipse.jgit.transport.Transport transport =
        mock(org.eclipse.jgit.transport.Transport.class);
    TransportHttp transportHttp = mock(TransportHttp.class);
    when(sshKeyProvider.getPrivateKey(anyString())).thenReturn(new byte[0]);
    doAnswer(
            invocation -> {
              TransportConfigCallback callback =
                  (TransportConfigCallback) invocation.getArguments()[0];
              callback.configure(transportHttp);
              return null;
            })
        .when(transportCommand)
        .setTransportConfigCallback(any());

    // when
    jGitConnection.executeRemoteCommand("ssh://host.xz/repo.git", transportCommand, null, null);

    // then
    verifyZeroInteractions(transportHttp);
  }

  /**
   * Check branch using current repository reference is returned
   *
   * @throws Exception if it fails
   */
  @Test
  public void checkCurrentBranch() throws Exception {
    String branchTest = "helloWorld";
    Ref ref = mock(Ref.class);
    when(repository.exactRef(Constants.HEAD)).thenReturn(ref);
    when(ref.getLeaf()).thenReturn(ref);
    when(ref.getName()).thenReturn(branchTest);
    String branchName = jGitConnection.getCurrentReference().getName();

    assertEquals(branchName, branchTest);
  }

  /** Test for workaround related to https://bugs.eclipse.org/bugs/show_bug.cgi?id=510685. */
  @Test(
      expectedExceptions = GitException.class,
      expectedExceptionsMessageRegExp =
          "Changes are present but not changed path was specified for commit.")
  public void testCommitNotChangedSpecifiedPathsWithAmendWhenOtherStagedChangesArePresent()
      throws Exception {
    // given
    Status status = mock(Status.class);
    when(status.getChanged()).thenReturn(singletonList("ChangedNotSpecified"));
    doReturn(status).when(jGitConnection).status(anyObject());

    // when
    jGitConnection.commit(
        CommitParams.create("message")
            .withFiles(singletonList("NotChangedSpecified"))
            .withAmend(true));
  }

  /** Test for workaround related to https://bugs.eclipse.org/bugs/show_bug.cgi?id=510685. */
  @Test(
      expectedExceptions = GitException.class,
      expectedExceptionsMessageRegExp =
          "Changes are present but not changed path was specified for commit.")
  public void
      testCommitNotChangedSpecifiedPathsWithAmendAndWithAllWhenOtherUnstagedChangesArePresent()
          throws Exception {
    // given
    Status status = mock(Status.class);
    when(status.getModified()).thenReturn(singletonList("ChangedNotSpecified"));
    doReturn(status).when(jGitConnection).status(anyObject());

    // when
    jGitConnection.commit(
        CommitParams.create("message")
            .withFiles(singletonList("NotChangedSpecified"))
            .withAmend(true)
            .withAll(true));
  }

  @Test
  public void shouldCloseCloneCommand() throws Exception {
    // given
    File fileMock = mock(File.class);
    Git cloneCommand = mock(Git.class);
    jGitConnection.setOutputLineConsumerFactory(mock(LineConsumerFactory.class));
    when(repository.getWorkTree()).thenReturn(fileMock);
    when(repository.getDirectory()).thenReturn(fileMock);
    when(repository.getConfig()).thenReturn(mock(StoredConfig.class));
    doReturn(cloneCommand)
        .when(jGitConnection)
        .executeRemoteCommand(
            nullable(String.class),
            nullable(TransportCommand.class),
            nullable(String.class),
            nullable(String.class));

    // when
    jGitConnection.clone(CloneParams.create("url").withWorkingDir("fakePath"));

    // then
    verify(cloneCommand).close();
  }
}

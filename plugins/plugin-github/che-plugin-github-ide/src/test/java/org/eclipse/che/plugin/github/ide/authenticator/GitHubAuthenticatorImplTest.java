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
 */
package org.eclipse.che.plugin.github.ide.authenticator;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.SUCCESS;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.RETURNS_DEFAULTS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwtmockito.GwtMockitoTestRunner;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.ssh.shared.dto.SshPairDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentUser;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.ssh.SshServiceClient;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.message.MessageDialog;
import org.eclipse.che.plugin.github.ide.GitHubLocalizationConstant;
import org.eclipse.che.plugin.ssh.key.client.SshKeyUploader;
import org.eclipse.che.plugin.ssh.key.client.SshKeyUploaderRegistry;
import org.eclipse.che.plugin.ssh.key.client.manage.SshKeyManagerPresenter;
import org.eclipse.che.security.oauth.OAuthStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

/**
 * Testing {@link GitHubAuthenticatorImpl} functionality.
 *
 * @author Roman Nikitenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class GitHubAuthenticatorImplTest {
  public static final String GITHUB_HOST = "github.com";

  @Captor private ArgumentCaptor<AsyncCallback<Void>> generateKeyCallbackCaptor;

  @Captor private ArgumentCaptor<Operation<List<SshPairDto>>> operationSshPairDTOsCapture;

  @Captor private ArgumentCaptor<Operation<Void>> operationVoid;

  private Promise<List<SshPairDto>> sshPairDTOsPromise;

  private Promise<Void> voidPromise;

  @Mock private GitHubAuthenticatorView view;
  @Mock private SshServiceClient sshServiceClient;
  @Mock private DialogFactory dialogFactory;
  @Mock private DtoUnmarshallerFactory dtoUnmarshallerFactory;
  @Mock private NotificationManager notificationManager;
  @Mock private GitHubLocalizationConstant locale;
  @Mock private AppContext appContext;
  @Mock private SshKeyUploaderRegistry registry;
  @InjectMocks private GitHubAuthenticatorImpl gitHubAuthenticator;

  @Before
  @SuppressWarnings("unchecked")
  public void setUp() throws Exception {
    sshPairDTOsPromise = createPromise();
    voidPromise = createPromise();

    when(sshServiceClient.getPairs(nullable(String.class))).thenReturn(sshPairDTOsPromise);
    when(sshServiceClient.deletePair(nullable(String.class), nullable(String.class)))
        .thenReturn(voidPromise);
  }

  private Promise createPromise() {
    return mock(
        Promise.class,
        new Answer() {
          @Override
          public Object answer(InvocationOnMock invocation) throws Throwable {
            if (invocation.getMethod().getReturnType().isInstance(invocation.getMock())) {
              return invocation.getMock();
            }
            return RETURNS_DEFAULTS.answer(invocation);
          }
        });
  }

  @Test
  public void delegateShouldBeSet() throws Exception {
    verify(view).setDelegate(gitHubAuthenticator);
  }

  @Test
  public void dialogShouldBeShow() throws Exception {
    AsyncCallback<OAuthStatus> callback = getCallBack();
    gitHubAuthenticator.authenticate(null, callback);

    verify(view).showDialog();
    assertThat(gitHubAuthenticator.callback, is(callback));
  }

  @Test
  public void onAuthenticatedWhenGenerateKeysIsSelected() throws Exception {
    String userId = "userId";
    OAuthStatus authStatus = mock(OAuthStatus.class);

    SshKeyUploader sshKeyUploader = mock(SshKeyUploader.class);

    CurrentUser user = mock(CurrentUser.class);
    when(view.isGenerateKeysSelected()).thenReturn(true);

    when(registry.getUploader(GITHUB_HOST)).thenReturn(sshKeyUploader);

    when(appContext.getCurrentUser()).thenReturn(user);
    when(user.getId()).thenReturn(userId);

    gitHubAuthenticator.onAuthenticated(authStatus);

    verify(view).isGenerateKeysSelected();
    verify(registry).getUploader(eq(GITHUB_HOST));
    verify(appContext).getCurrentUser();
    verify(sshKeyUploader)
        .uploadKey(eq(userId), org.mockito.ArgumentMatchers.<AsyncCallback<Void>>anyObject());
  }

  @Test
  public void onAuthenticatedWhenGenerateKeysIsNotSelected() throws Exception {
    String userId = "userId";
    OAuthStatus authStatus = mock(OAuthStatus.class);

    CurrentUser user = mock(CurrentUser.class);
    when(view.isGenerateKeysSelected()).thenReturn(false);
    when(appContext.getCurrentUser()).thenReturn(user);
    when(user.getId()).thenReturn(userId);

    gitHubAuthenticator.authenticate(null, getCallBack());
    gitHubAuthenticator.onAuthenticated(authStatus);

    verify(view).isGenerateKeysSelected();
    verifyNoMoreInteractions(registry);
  }

  @Test
  public void onAuthenticatedWhenGenerateKeysIsSuccess() throws Exception {
    String userId = "userId";
    OAuthStatus authStatus = mock(OAuthStatus.class);
    SshKeyUploader keyProvider = mock(SshKeyUploader.class);

    CurrentUser user = mock(CurrentUser.class);
    when(view.isGenerateKeysSelected()).thenReturn(true);
    when(registry.getUploader(GITHUB_HOST)).thenReturn(keyProvider);

    when(appContext.getCurrentUser()).thenReturn(user);
    when(user.getId()).thenReturn(userId);

    gitHubAuthenticator.authenticate(null, getCallBack());
    gitHubAuthenticator.onAuthenticated(authStatus);

    verify(keyProvider).uploadKey(eq(userId), generateKeyCallbackCaptor.capture());
    AsyncCallback<Void> generateKeyCallback = generateKeyCallbackCaptor.getValue();
    generateKeyCallback.onSuccess(null);

    verify(view).isGenerateKeysSelected();
    verify(registry).getUploader(eq(GITHUB_HOST));
    verify(appContext).getCurrentUser();
    verify(notificationManager).notify(nullable(String.class), eq(SUCCESS), eq(FLOAT_MODE));
  }

  @Test
  public void onAuthenticatedWhenGenerateKeysIsFailure() throws Exception {
    String userId = "userId";
    OAuthStatus authStatus = mock(OAuthStatus.class);

    SshKeyUploader keyProvider = mock(SshKeyUploader.class);

    CurrentUser user = mock(CurrentUser.class);
    MessageDialog messageDialog = mock(MessageDialog.class);
    when(view.isGenerateKeysSelected()).thenReturn(true);
    when(registry.getUploader(GITHUB_HOST)).thenReturn(keyProvider);

    when(appContext.getCurrentUser()).thenReturn(user);
    when(user.getId()).thenReturn(userId);
    when(dialogFactory.createMessageDialog(
            nullable(String.class),
            nullable(String.class),
            org.mockito.ArgumentMatchers.<ConfirmCallback>anyObject()))
        .thenReturn(messageDialog);

    gitHubAuthenticator.authenticate(null, getCallBack());
    gitHubAuthenticator.onAuthenticated(authStatus);

    verify(keyProvider).uploadKey(eq(userId), generateKeyCallbackCaptor.capture());
    AsyncCallback<Void> generateKeyCallback = generateKeyCallbackCaptor.getValue();
    generateKeyCallback.onFailure(new Exception(""));

    verify(view).isGenerateKeysSelected();
    verify(registry).getUploader(eq(GITHUB_HOST));
    verify(appContext).getCurrentUser();
    verify(dialogFactory)
        .createMessageDialog(
            nullable(String.class),
            nullable(String.class),
            org.mockito.ArgumentMatchers.<ConfirmCallback>anyObject());
    verify(messageDialog).show();
    verify(sshServiceClient).getPairs(eq(SshKeyManagerPresenter.VCS_SSH_SERVICE));
  }

  @Test
  public void onAuthenticatedWhenGetFailedKeyIsSuccess() throws Exception {
    String userId = "userId";
    SshPairDto pair = mock(SshPairDto.class);
    List<SshPairDto> pairs = new ArrayList<>();
    pairs.add(pair);
    OAuthStatus authStatus = mock(OAuthStatus.class);
    SshKeyUploader keyUploader = mock(SshKeyUploader.class);

    CurrentUser user = mock(CurrentUser.class);
    MessageDialog messageDialog = mock(MessageDialog.class);
    when(view.isGenerateKeysSelected()).thenReturn(true);
    when(registry.getUploader(GITHUB_HOST)).thenReturn(keyUploader);

    when(appContext.getCurrentUser()).thenReturn(user);
    when(user.getId()).thenReturn(userId);
    when(dialogFactory.createMessageDialog(
            nullable(String.class),
            nullable(String.class),
            org.mockito.ArgumentMatchers.<ConfirmCallback>anyObject()))
        .thenReturn(messageDialog);
    when(pair.getName()).thenReturn(GITHUB_HOST);
    when(pair.getService()).thenReturn(SshKeyManagerPresenter.VCS_SSH_SERVICE);

    gitHubAuthenticator.authenticate(null, getCallBack());
    gitHubAuthenticator.onAuthenticated(authStatus);

    verify(keyUploader).uploadKey(eq(userId), generateKeyCallbackCaptor.capture());
    AsyncCallback<Void> generateKeyCallback = generateKeyCallbackCaptor.getValue();
    generateKeyCallback.onFailure(new Exception(""));

    verify(sshPairDTOsPromise).then(operationSshPairDTOsCapture.capture());
    operationSshPairDTOsCapture.getValue().apply(pairs);

    verify(view).isGenerateKeysSelected();
    verify(registry).getUploader(eq(GITHUB_HOST));
    verify(appContext).getCurrentUser();
    verify(dialogFactory)
        .createMessageDialog(
            nullable(String.class),
            nullable(String.class),
            org.mockito.ArgumentMatchers.<ConfirmCallback>anyObject());
    verify(messageDialog).show();
    verify(sshServiceClient).getPairs(eq(SshKeyManagerPresenter.VCS_SSH_SERVICE));
    verify(sshServiceClient)
        .deletePair(eq(SshKeyManagerPresenter.VCS_SSH_SERVICE), eq(GITHUB_HOST));
  }

  private AsyncCallback<OAuthStatus> getCallBack() {
    return new AsyncCallback<OAuthStatus>() {
      @Override
      public void onFailure(Throwable caught) {}

      @Override
      public void onSuccess(OAuthStatus result) {}
    };
  }
}

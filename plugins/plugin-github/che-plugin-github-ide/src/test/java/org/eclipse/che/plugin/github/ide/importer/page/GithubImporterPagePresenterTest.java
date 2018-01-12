/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.github.ide.importer.page;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwtmockito.GwtMockitoTestRunner;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.project.shared.dto.ProjectImporterDescriptor;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentUser;
import org.eclipse.che.ide.api.auth.OAuthServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.oauth.OAuth2Authenticator;
import org.eclipse.che.ide.api.oauth.OAuth2AuthenticatorRegistry;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.wizard.Wizard;
import org.eclipse.che.ide.commons.exception.UnauthorizedException;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.plugin.github.ide.GitHubLocalizationConstant;
import org.eclipse.che.plugin.github.ide.GitHubServiceClient;
import org.eclipse.che.plugin.github.ide.load.ProjectData;
import org.eclipse.che.plugin.github.shared.GitHubUser;
import org.eclipse.che.security.oauth.OAuthStatus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * Testing {@link GithubImporterPagePresenter} functionality.
 *
 * @author Roman Nikitenko
 */
@RunWith(GwtMockitoTestRunner.class)
public class GithubImporterPagePresenterTest {

  @Captor private ArgumentCaptor<AsyncCallback<OAuthStatus>> asyncCallbackCaptor;

  @Mock private Wizard.UpdateDelegate updateDelegate;
  @Mock private DtoFactory dtoFactory;
  @Mock private GithubImporterPageView view;
  @Mock private GitHubServiceClient gitHubClientService;
  @Mock private GitHubLocalizationConstant locale;
  @Mock private MutableProjectConfig dataObject;
  @Mock private MutableProjectConfig.MutableSourceStorage source;
  @Mock private GitHubUser gitHubUser;
  @Mock private PromiseError promiseError;
  @Mock private OAuth2Authenticator gitHubAuthenticator;
  @Mock private OAuth2AuthenticatorRegistry gitHubAuthenticatorRegistry;
  @Mock private AppContext appContext;
  @Mock private OAuthServiceClient oAuthServiceClient;
  @Mock private NotificationManager notificationManager;

  private GithubImporterPagePresenter presenter;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    when(dataObject.getSource()).thenReturn(source);
    doReturn("AccountName").when(gitHubUser).getName();
    doReturn("AccountLogin").when(gitHubUser).getLogin();
    when(gitHubAuthenticatorRegistry.getAuthenticator(eq("github")))
        .thenReturn(gitHubAuthenticator);
    presenter =
        spy(
            new GithubImporterPagePresenter(
                view,
                gitHubAuthenticatorRegistry,
                gitHubClientService,
                dtoFactory,
                appContext,
                locale,
                oAuthServiceClient,
                notificationManager));
    doReturn(Collections.singletonList(gitHubUser))
        .when(presenter)
        .toOrgList(nullable(JsArrayMixed.class));
    doReturn(Collections.emptyList()).when(presenter).toRepoList(nullable(JsArrayMixed.class));
    presenter.setUpdateDelegate(updateDelegate);
    presenter.init(dataObject);
  }

  @Test
  public void delegateShouldBeSet() throws Exception {
    verify(view).setDelegate(any(GithubImporterPagePresenter.class));
  }

  @Test
  public void testGo() throws Exception {
    String importerDescription = "description";
    AcceptsOneWidget container = mock(AcceptsOneWidget.class);
    ProjectImporterDescriptor projectImporter = mock(ProjectImporterDescriptor.class);
    // when(wizardContext.getData(ImportProjectWizard.PROJECT_IMPORTER)).thenReturn(projectImporter);
    when(projectImporter.getDescription()).thenReturn(importerDescription);

    presenter.go(container);

    verify(view).setInputsEnableState(eq(true));
    verify(container).setWidget(eq(view));
    verify(view).focusInUrlInput();
  }

  @Test
  public void onLoadRepoClickedWhenGetUserReposIsSuccessful() throws Exception {
    when(view.getAccountName()).thenReturn("AccountName");

    presenter.onLoadRepoClicked();

    verify(gitHubClientService).getRepositoriesList(anyString());
    verify(gitHubClientService).getUserInfo(anyString());
    verify(gitHubClientService).getOrganizations(anyString());

    verify(view).setLoaderVisibility(eq(true));
    verify(view).setInputsEnableState(eq(false));
    verify(view).setLoaderVisibility(eq(false));
    verify(view).setInputsEnableState(eq(true));
    verify(view).setAccountNames(org.mockito.ArgumentMatchers.<Set>any());
    verify(view, times(2)).showGithubPanel();
    verify(view).setRepositories(org.mockito.ArgumentMatchers.any());
    verify(view).reset();
  }

  @Test
  public void onLoadRepoClickedWhenGetUserReposIsFailed() throws Exception {
    presenter.onLoadRepoClicked();

    verify(gitHubClientService).getRepositoriesList(anyString());
    verify(gitHubClientService).getUserInfo(anyString());
    verify(gitHubClientService).getOrganizations(anyString());

    verify(view).setLoaderVisibility(eq(true));
    verify(view).setInputsEnableState(eq(false));
    verify(view).setLoaderVisibility(eq(false));
    verify(view).setInputsEnableState(eq(true));
    verify(view, never()).setAccountNames(any());
    verify(view, never()).showGithubPanel();
    verify(view, never()).setRepositories(org.mockito.ArgumentMatchers.any());
  }

  @Test
  public void onRepositorySelectedTest() {
    ProjectData projectData =
        new ProjectData(
            "name", "description", "type", Collections.emptyList(), "repoUrl", "readOnlyUrl");

    presenter.onRepositorySelected(projectData);

    verify(dataObject).setName(eq("name"));
    verify(dataObject).setDescription(eq("description"));
    verify(source).setLocation(eq("repoUrl"));
    verify(view).setProjectName(anyString());
    verify(view).setProjectDescription(anyString());
    verify(view).setProjectUrl(anyString());
    verify(updateDelegate).updateControls();
  }

  @Test
  public void projectUrlStartWithWhiteSpaceEnteredTest() {
    String incorrectUrl = " https://github.com/codenvy/ide.git";
    when(view.getProjectName()).thenReturn("");

    presenter.onProjectUrlChanged(incorrectUrl);

    verify(view).markURLInvalid();
    verify(view).setURLErrorMessage(eq(locale.importProjectMessageStartWithWhiteSpace()));
    verify(source).setLocation(eq(incorrectUrl));
    verify(view).setProjectName(anyString());
    verify(updateDelegate).updateControls();
  }

  @Test
  public void testUrlMatchScpLikeSyntax() {
    // test for url with an alternative scp-like syntax: [user@]host.xz:path/to/repo.git/
    String correctUrl = "host.xz:path/to/repo.git";
    when(view.getProjectName()).thenReturn("");

    presenter.onProjectUrlChanged(correctUrl);

    verifyInvocationsForCorrectUrl(correctUrl);
  }

  @Test
  public void testUrlWithoutUsername() {
    String correctUrl = "git@hostname.com:projectName.git";
    when(view.getProjectName()).thenReturn("");

    presenter.onProjectUrlChanged(correctUrl);

    verifyInvocationsForCorrectUrl(correctUrl);
  }

  @Test
  public void testSshUriWithHostBetweenDoubleSlashAndSlash() {
    // Check for type uri which start with ssh:// and has host between // and /
    String correctUrl = "ssh://host.com/some/path";
    when(view.getProjectName()).thenReturn("");

    presenter.onProjectUrlChanged(correctUrl);

    verifyInvocationsForCorrectUrl(correctUrl);
  }

  @Test
  public void testSshUriWithHostBetweenDoubleSlashAndColon() {
    // Check for type uri with host between // and :
    String correctUrl = "ssh://host.com:port/some/path";
    when(view.getProjectName()).thenReturn("");

    presenter.onProjectUrlChanged(correctUrl);

    verifyInvocationsForCorrectUrl(correctUrl);
  }

  @Test
  public void testGitUriWithHostBetweenDoubleSlashAndSlash() {
    // Check for type uri which start with git:// and has host between // and /
    String correctUrl = "git://host.com/user/repo";
    when(view.getProjectName()).thenReturn("");

    presenter.onProjectUrlChanged(correctUrl);

    verifyInvocationsForCorrectUrl(correctUrl);
  }

  @Test
  public void testSshUriWithHostBetweenAtAndColon() {
    // Check for type uri with host between @ and :
    String correctUrl = "user@host.com:login/repo";
    when(view.getProjectName()).thenReturn("");

    presenter.onProjectUrlChanged(correctUrl);

    verifyInvocationsForCorrectUrl(correctUrl);
  }

  @Test
  public void testSshUriWithHostBetweenAtAndSlash() {
    // Check for type uri with host between @ and /
    String correctUrl = "ssh://user@host.com/some/path";
    when(view.getProjectName()).thenReturn("");

    presenter.onProjectUrlChanged(correctUrl);

    verifyInvocationsForCorrectUrl(correctUrl);
  }

  @Test
  public void projectUrlWithIncorrectProtocolEnteredTest() {
    String correctUrl = "htps://github.com/codenvy/ide.git";
    when(view.getProjectName()).thenReturn("");

    presenter.onProjectUrlChanged(correctUrl);

    verify(view).markURLInvalid();
    verify(view).setURLErrorMessage(eq(locale.importProjectMessageProtocolIncorrect()));
    verify(source).setLocation(eq(correctUrl));
    verify(view).setProjectName(anyString());
    verify(updateDelegate).updateControls();
  }

  @Test
  public void correctProjectNameEnteredTest() {
    String correctName = "angularjs";
    when(view.getProjectName()).thenReturn(correctName);

    presenter.onProjectNameChanged(correctName);

    verify(dataObject).setName(eq(correctName));
    verify(view).markNameValid();
    verify(view, never()).markNameInvalid();
    verify(updateDelegate).updateControls();
  }

  @Test
  public void correctProjectNameWithPointEnteredTest() {
    String correctName = "Test.project..ForCodenvy";
    when(view.getProjectName()).thenReturn(correctName);

    presenter.onProjectNameChanged(correctName);

    verify(dataObject).setName(eq(correctName));
    verify(view).markNameValid();
    verify(view, never()).markNameInvalid();
    verify(updateDelegate).updateControls();
  }

  @Test
  public void emptyProjectNameEnteredTest() {
    String emptyName = "";
    when(view.getProjectName()).thenReturn(emptyName);

    presenter.onProjectNameChanged(emptyName);

    verify(dataObject).setName(eq(emptyName));
    verify(updateDelegate).updateControls();
  }

  @Test
  public void incorrectProjectNameEnteredTest() {
    String incorrectName = "angularjs+";
    when(view.getProjectName()).thenReturn(incorrectName);

    presenter.onProjectNameChanged(incorrectName);

    verify(dataObject).setName(eq(incorrectName));
    verify(view).markNameInvalid();
    verify(updateDelegate).updateControls();
  }

  @Test
  public void projectDescriptionChangedTest() {
    String description = "description";
    presenter.onProjectDescriptionChanged(description);

    verify(dataObject).setDescription(eq(description));
  }

  @Test
  public void keepDirectorySelectedTest() {
    Map<String, String> parameters = new HashMap<>();
    when(source.getParameters()).thenReturn(parameters);
    when(view.getDirectoryName()).thenReturn("directory");

    presenter.onKeepDirectorySelected(true);

    assertEquals("directory", parameters.get("keepDir"));
    verify(dataObject).setType("blank");
    verify(view).highlightDirectoryNameField(eq(false));
    verify(view).focusDirectoryNameField();
  }

  @Test
  public void keepDirectoryUnSelectedTest() {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("keepDir", "directory");
    when(source.getParameters()).thenReturn(parameters);

    presenter.onKeepDirectorySelected(false);

    assertTrue(parameters.isEmpty());
    verify(dataObject).setType(eq(null));
    verify(view).highlightDirectoryNameField(eq(false));
  }

  @Test
  public void recursiveCloneSelectedTest() {
    Map<String, String> parameters = new HashMap<>();
    when(source.getParameters()).thenReturn(parameters);
    when(view.getDirectoryName()).thenReturn("recursive");

    presenter.onRecursiveSelected(true);

    assertTrue(parameters.containsKey("recursive"));
  }

  @Test
  public void recursiveCloneUnSelectedTest() {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("recursive", null);
    when(source.getParameters()).thenReturn(parameters);

    presenter.onRecursiveSelected(false);

    assertTrue(parameters.isEmpty());
  }

  @Test
  public void keepDirectoryNameChangedAndKeepDirectorySelectedTest() {
    Map<String, String> parameters = new HashMap<>();
    when(source.getParameters()).thenReturn(parameters);
    when(view.getDirectoryName()).thenReturn("directory");
    when(view.keepDirectory()).thenReturn(true);

    presenter.onKeepDirectoryNameChanged("directory");

    assertEquals("directory", parameters.get("keepDir"));
    verify(dataObject, never()).setPath(any());
    verify(dataObject).setType(eq("blank"));
    verify(view).highlightDirectoryNameField(eq(false));
  }

  @Test
  public void keepDirectoryNameChangedAndKeepDirectoryUnSelectedTest() {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("keepDir", "directory");
    when(source.getParameters()).thenReturn(parameters);
    when(view.keepDirectory()).thenReturn(false);

    presenter.onKeepDirectoryNameChanged("directory");

    assertTrue(parameters.isEmpty());
    verify(dataObject, never()).setPath(any());
    verify(dataObject).setType(eq(null));
    verify(view).highlightDirectoryNameField(eq(false));
  }

  @Test
  public void branchSelectedTest() {
    Map<String, String> parameters = new HashMap<>();
    when(source.getParameters()).thenReturn(parameters);
    when(view.getBranchName()).thenReturn("someBranch");

    presenter.onBranchCheckBoxSelected(true);
    verify(view).enableBranchNameField(true);
    verify(view).focusBranchNameField();
    assertEquals("someBranch", parameters.get("branch"));
  }

  @Test
  public void branchNotSelectedTest() {
    Map<String, String> parameters = new HashMap<>();
    parameters.put("branch", "someBranch");
    when(source.getParameters()).thenReturn(parameters);

    presenter.onBranchCheckBoxSelected(false);
    verify(view).enableBranchNameField(false);
    assertTrue(parameters.isEmpty());
  }

  @Test
  public void onLoadRepoClickedWhenAuthorizeIsFailed() throws Exception {
    String userId = "userId";
    CurrentUser user = mock(CurrentUser.class);

    when(appContext.getCurrentUser()).thenReturn(user);
    when(user.getId()).thenReturn(userId);

    final Throwable exception = mock(UnauthorizedException.class);
    doReturn(exception).when(promiseError).getCause();

    presenter.onLoadRepoClicked();

    verify(gitHubClientService).getRepositoriesList(anyString());
    verify(gitHubClientService).getUserInfo(anyString());
    verify(gitHubClientService).getOrganizations(anyString());

    verify(gitHubAuthenticator).authenticate(anyString(), asyncCallbackCaptor.capture());
    AsyncCallback<OAuthStatus> asyncCallback = asyncCallbackCaptor.getValue();
    asyncCallback.onFailure(exception);

    verify(view, times(2)).setLoaderVisibility(eq(true));
    verify(view, times(2)).setInputsEnableState(eq(false));
    verify(view, times(2)).setInputsEnableState(eq(true));
    verify(view, never()).setAccountNames(any());
    verify(view, never()).showGithubPanel();
    verify(view, never()).setRepositories(org.mockito.ArgumentMatchers.any());
  }

  @Test
  public void onLoadRepoClickedWhenAuthorizeIsSuccessful() throws Exception {
    final Throwable exception = mock(UnauthorizedException.class);
    String userId = "userId";
    CurrentUser user = mock(CurrentUser.class);
    doReturn(exception).when(promiseError).getCause();

    when(appContext.getCurrentUser()).thenReturn(user);
    when(user.getId()).thenReturn(userId);

    presenter.onLoadRepoClicked();

    verify(gitHubClientService).getRepositoriesList(anyString());
    verify(gitHubClientService).getUserInfo(anyString());
    verify(gitHubClientService).getOrganizations(anyString());

    verify(gitHubAuthenticator).authenticate(anyString(), asyncCallbackCaptor.capture());
    AsyncCallback<OAuthStatus> asyncCallback = asyncCallbackCaptor.getValue();
    asyncCallback.onSuccess(null);

    verify(view, times(3)).setLoaderVisibility(eq(true));
    verify(view, times(3)).setInputsEnableState(eq(false));
    verify(view, times(3)).setInputsEnableState(eq(true));
  }

  private void verifyInvocationsForCorrectUrl(String correctUrl) {
    verify(view, never()).markURLInvalid();
    verify(source).setLocation(eq(correctUrl));
    verify(view).markURLValid();
    verify(view).setProjectName(anyString());
    verify(updateDelegate).updateControls();
  }
}

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

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.JsArrayMixed;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.auth.OAuthServiceClient;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.oauth.OAuth2Authenticator;
import org.eclipse.che.ide.api.oauth.OAuth2AuthenticatorRegistry;
import org.eclipse.che.ide.api.oauth.OAuth2AuthenticatorUrlProvider;
import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.wizard.AbstractWizardPage;
import org.eclipse.che.ide.commons.exception.UnauthorizedException;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.util.NameUtils;
import org.eclipse.che.plugin.github.ide.GitHubLocalizationConstant;
import org.eclipse.che.plugin.github.ide.GitHubServiceClient;
import org.eclipse.che.plugin.github.ide.load.ProjectData;
import org.eclipse.che.plugin.github.shared.GitHubRepository;
import org.eclipse.che.plugin.github.shared.GitHubUser;
import org.eclipse.che.security.oauth.OAuthStatus;

/** @author Roman Nikitenko */
public class GithubImporterPagePresenter extends AbstractWizardPage<MutableProjectConfig>
    implements GithubImporterPageView.ActionDelegate {

  // An alternative scp-like syntax: [user@]host.xz:path/to/repo.git/
  private static final RegExp SCP_LIKE_SYNTAX =
      RegExp.compile("([A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-:]+)+:");
  // the transport protocol
  private static final RegExp PROTOCOL = RegExp.compile("((http|https|git|ssh|ftp|ftps)://)");
  // the address of the remote server between // and /
  private static final RegExp HOST1 = RegExp.compile("//([A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-:]+)+/");
  // the address of the remote server between @ and : or /
  private static final RegExp HOST2 =
      RegExp.compile("@([A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-:]+)+[:/]");
  // the repository name
  private static final RegExp REPO_NAME = RegExp.compile("/[A-Za-z0-9_.\\-]+$");
  // start with white space
  private static final RegExp WHITE_SPACE = RegExp.compile("^\\s");

  private final DtoFactory dtoFactory;
  private GitHubServiceClient gitHubClientService;
  private Map<String, List<GitHubRepository>> repositories;
  private GitHubLocalizationConstant locale;
  private GithubImporterPageView view;
  private final String baseUrl;
  private final AppContext appContext;
  private OAuth2Authenticator gitHubAuthenticator;
  private final OAuthServiceClient oAuthServiceClient;
  private final NotificationManager notificationManager;

  private boolean ignoreChanges;

  @Inject
  public GithubImporterPagePresenter(
      GithubImporterPageView view,
      OAuth2AuthenticatorRegistry gitHubAuthenticatorRegistry,
      GitHubServiceClient gitHubClientService,
      DtoFactory dtoFactory,
      AppContext appContext,
      GitHubLocalizationConstant locale,
      OAuthServiceClient oAuthServiceClient,
      NotificationManager notificationManager) {
    this.view = view;
    this.baseUrl = appContext.getMasterApiEndpoint();
    this.appContext = appContext;
    this.gitHubAuthenticator = gitHubAuthenticatorRegistry.getAuthenticator("github");
    this.gitHubClientService = gitHubClientService;
    this.dtoFactory = dtoFactory;
    this.oAuthServiceClient = oAuthServiceClient;
    this.notificationManager = notificationManager;
    this.view.setDelegate(this);
    this.locale = locale;
  }

  @Override
  public boolean isCompleted() {
    return isGitUrlCorrect(dataObject.getSource().getLocation());
  }

  @Override
  public void onProjectNameChanged(@NotNull String name) {
    if (ignoreChanges) {
      return;
    }

    dataObject.setName(name);
    updateDelegate.updateControls();

    validateProjectName();
  }

  /** Validates project name and highlights input when error. */
  private void validateProjectName() {
    if (NameUtils.checkProjectName(view.getProjectName())) {
      view.markNameValid();
    } else {
      view.markNameInvalid();
    }
  }

  @Override
  public void onProjectUrlChanged(@NotNull String url) {
    if (ignoreChanges) {
      return;
    }

    dataObject.getSource().setLocation(url);
    isGitUrlCorrect(url);

    String projectName = view.getProjectName();
    if (projectName.isEmpty()) {
      projectName = extractProjectNameFromUri(url);

      dataObject.setName(projectName);
      view.setProjectName(projectName);
      validateProjectName();
    }

    updateDelegate.updateControls();
  }

  @Override
  public void onRecursiveSelected(boolean recursiveSelected) {
    if (recursiveSelected) {
      projectParameters().put("recursive", null);
    } else {
      projectParameters().remove("recursive");
    }
  }

  @Override
  public void onProjectDescriptionChanged(@NotNull String projectDescription) {
    dataObject.setDescription(projectDescription);
    updateDelegate.updateControls();
  }

  /**
   * Returns project parameters map.
   *
   * @return parameters map
   */
  private Map<String, String> projectParameters() {
    Map<String, String> parameters = dataObject.getSource().getParameters();
    if (parameters == null) {
      parameters = new HashMap<>();
      dataObject.getSource().setParameters(parameters);
    }

    return parameters;
  }

  @Override
  public void onKeepDirectorySelected(boolean keepDirectory) {
    view.enableDirectoryNameField(keepDirectory);

    if (keepDirectory) {
      projectParameters().put("keepDir", view.getDirectoryName());
      dataObject.setType("blank");
      view.highlightDirectoryNameField(!NameUtils.checkProjectName(view.getDirectoryName()));
      view.focusDirectoryNameField();
    } else {
      projectParameters().remove("keepDir");
      dataObject.setType(null);
      view.highlightDirectoryNameField(false);
    }
  }

  @Override
  public void onKeepDirectoryNameChanged(@NotNull String directoryName) {
    if (view.keepDirectory()) {
      projectParameters().put("keepDir", directoryName);
      dataObject.setType("blank");
      view.highlightDirectoryNameField(!NameUtils.checkProjectName(view.getDirectoryName()));
    } else {
      projectParameters().remove("keepDir");
      dataObject.setType(null);
      view.highlightDirectoryNameField(false);
    }
  }

  @Override
  public void onBranchCheckBoxSelected(boolean isSelected) {
    view.enableBranchNameField(isSelected);

    if (isSelected) {
      projectParameters().put("branch", view.getBranchName());
      view.focusBranchNameField();
    } else {
      projectParameters().remove("branch");
    }
  }

  @Override
  public void onBranchNameChanged(@NotNull String branchName) {
    if (view.isBranchCheckBoxSelected()) {
      projectParameters().put("branch", branchName);
    } else {
      projectParameters().remove("branch");
    }
  }

  @Override
  public void go(@NotNull AcceptsOneWidget container) {
    container.setWidget(view);

    if (Strings.isNullOrEmpty(dataObject.getName())
        && Strings.isNullOrEmpty(dataObject.getSource().getLocation())) {
      ignoreChanges = true;

      view.unmarkURL();
      view.unmarkName();
      view.setURLErrorMessage(null);
    }

    view.setProjectName(dataObject.getName());
    view.setProjectDescription(dataObject.getDescription());
    view.setProjectUrl(dataObject.getSource().getLocation());

    view.setKeepDirectoryChecked(false);
    view.setBranchCheckBoxSelected(false);
    view.setDirectoryName("");
    view.setBranchName("");
    view.enableDirectoryNameField(false);
    view.enableBranchNameField(false);
    view.highlightDirectoryNameField(false);

    view.setInputsEnableState(true);
    view.focusInUrlInput();

    ignoreChanges = false;
  }

  @Override
  public void onLoadRepoClicked() {
    getUserRepositoriesAndOrganizations();
  }

  /** Get the list of all authorized user's repositories. */
  private void getUserRepositoriesAndOrganizations() {
    showProcessing(true);

    oAuthServiceClient
        .getToken(gitHubAuthenticator.getProviderName())
        .thenPromise(
            token ->
                Promises.all(
                    gitHubClientService.getUserInfo(token.getToken()),
                    gitHubClientService.getOrganizations(token.getToken()),
                    gitHubClientService.getRepositoriesList(token.getToken())))
        .then(this::onSuccessRequest)
        .catchError(this::onFailRequest);
  }

  protected void onSuccessRequest(JsArrayMixed arg) {
    onListLoaded(toOrgList(arg), toRepoList(arg));
    showProcessing(false);
  }

  protected List<GitHubRepository> toRepoList(JsArrayMixed arg) {
    return dtoFactory.createListDtoFromJson(arg.getObject(2).toString(), GitHubRepository.class);
  }

  protected List<GitHubUser> toOrgList(JsArrayMixed arg) {
    List<GitHubUser> organizations =
        dtoFactory.createListDtoFromJson(arg.getObject(1).toString(), GitHubUser.class);
    organizations.add(dtoFactory.createDtoFromJson(arg.getObject(0).toString(), GitHubUser.class));
    return organizations;
  }

  protected void onFailRequest(PromiseError arg) {
    showProcessing(false);
    if (arg.getCause() instanceof UnauthorizedException) {
      authorize();
    } else {
      notificationManager.notify(locale.authorizationFailed(), FAIL, FLOAT_MODE);
    }
  }

  /** Authorizes on GitHub. */
  private void authorize() {
    showProcessing(true);
    gitHubAuthenticator.authenticate(
        OAuth2AuthenticatorUrlProvider.get(
            baseUrl,
            "github",
            appContext.getCurrentUser().getId(),
            Lists.asList("user", new String[] {"repo", "write:public_key"})),
        new AsyncCallback<OAuthStatus>() {
          @Override
          public void onFailure(Throwable caught) {
            showProcessing(false);
          }

          @Override
          public void onSuccess(OAuthStatus result) {
            showProcessing(false);
            getUserRepositoriesAndOrganizations();
          }
        });
  }

  @Override
  public void onRepositorySelected(@NotNull ProjectData repository) {
    dataObject.setName(repository.getName());
    dataObject.setDescription(repository.getDescription());
    dataObject.getSource().setLocation(repository.getRepositoryUrl());

    view.setProjectName(repository.getName());
    view.setProjectDescription(repository.getDescription());
    view.setProjectUrl(repository.getRepositoryUrl());

    updateDelegate.updateControls();
  }

  @Override
  public void onAccountChanged() {
    refreshProjectList();
  }

  /**
   * Perform actions when the list of repositories was loaded.
   *
   * @param gitHubRepositories loaded list of repositories
   * @param gitHubOrganizations
   */
  protected void onListLoaded(
      @NotNull List<GitHubUser> gitHubOrganizations,
      @NotNull List<GitHubRepository> gitHubRepositories) {
    this.repositories = new HashMap<>();

    Map<String, String> login2OrgName = getLogin2OrgName(gitHubOrganizations);
    for (String orgName : login2OrgName.values()) {
      repositories.put(orgName, new ArrayList<>());
    }

    for (GitHubRepository gitHubRepository : gitHubRepositories) {
      String orgName = login2OrgName.get(gitHubRepository.getOwnerLogin());
      if (orgName != null && repositories.containsKey(orgName)) {
        repositories.get(orgName).add(gitHubRepository);
      }
    }

    view.setAccountNames(repositories.keySet());
    refreshProjectList();
    view.showGithubPanel();
  }

  private Map<String, String> getLogin2OrgName(List<GitHubUser> organizations) {
    Map<String, String> result = new HashMap<>();
    for (GitHubUser gitHubUser : organizations) {
      String userName = gitHubUser.getName() != null ? gitHubUser.getName() : gitHubUser.getLogin();
      result.put(gitHubUser.getLogin(), userName);
    }

    return result;
  }

  /** Refresh project list on view. */
  private void refreshProjectList() {
    List<ProjectData> projectsData = new ArrayList<>();

    String accountName = view.getAccountName();
    if (repositories.containsKey(accountName)) {
      List<GitHubRepository> repo = repositories.get(accountName);

      for (GitHubRepository repository : repo) {
        ProjectData projectData =
            new ProjectData(
                repository.getName(),
                repository.getDescription(),
                null,
                null,
                repository.getSshUrl(),
                repository.getGitUrl());
        projectsData.add(projectData);
      }

      projectsData.sort(Comparator.comparing(ProjectData::getName));

      view.setRepositories(projectsData);
      view.reset();
      view.showGithubPanel();
    }
  }

  /** Shown the state that the request is processing. */
  private void showProcessing(boolean inProgress) {
    view.setLoaderVisibility(inProgress);
    view.setInputsEnableState(!inProgress);
  }

  /** Gets project name from uri. */
  private String extractProjectNameFromUri(@NotNull String uri) {
    int indexFinishProjectName = uri.lastIndexOf(".");
    int indexStartProjectName =
        uri.lastIndexOf("/") != -1 ? uri.lastIndexOf("/") + 1 : (uri.lastIndexOf(":") + 1);

    if (indexStartProjectName != 0 && indexStartProjectName < indexFinishProjectName) {
      return uri.substring(indexStartProjectName, indexFinishProjectName);
    }
    if (indexStartProjectName != 0) {
      return uri.substring(indexStartProjectName);
    }
    return "";
  }

  /**
   * Validate url
   *
   * @param url url for validate
   * @return <code>true</code> if url is correct
   */
  private boolean isGitUrlCorrect(@NotNull String url) {
    if (WHITE_SPACE.test(url)) {
      view.markURLInvalid();
      view.setURLErrorMessage(locale.importProjectMessageStartWithWhiteSpace());
      return false;
    }

    if (SCP_LIKE_SYNTAX.test(url)) {
      view.markURLValid();
      view.setURLErrorMessage(null);
      return true;
    }

    if (!PROTOCOL.test(url)) {
      view.markURLInvalid();
      view.setURLErrorMessage(locale.importProjectMessageProtocolIncorrect());
      return false;
    }

    if (!(HOST1.test(url) || HOST2.test(url))) {
      view.markURLInvalid();
      view.setURLErrorMessage(locale.importProjectMessageHostIncorrect());
      return false;
    }

    if (!(REPO_NAME.test(url))) {
      view.markURLInvalid();
      view.setURLErrorMessage(locale.importProjectMessageNameRepoIncorrect());
      return false;
    }

    view.markURLValid();
    view.setURLErrorMessage(null);
    return true;
  }
}

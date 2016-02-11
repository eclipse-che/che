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
package org.eclipse.che.ide.ext.github.client.importer.page;

import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.wizard.AbstractWizardPage;
import org.eclipse.che.ide.commons.exception.UnauthorizedException;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.ext.github.client.GitHubClientService;
import org.eclipse.che.ide.ext.github.client.GitHubLocalizationConstant;
import org.eclipse.che.ide.ext.github.client.authenticator.GitHubAuthenticator;
import org.eclipse.che.ide.ext.github.client.load.ProjectData;
import org.eclipse.che.ide.ext.github.client.marshaller.AllRepositoriesUnmarshaller;
import org.eclipse.che.ide.ext.github.shared.GitHubRepository;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.util.NameUtils;
import org.eclipse.che.security.oauth.OAuthStatus;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Roman Nikitenko
 */
public class GithubImporterPagePresenter extends AbstractWizardPage<ProjectConfigDto> implements GithubImporterPageView.ActionDelegate {

    // An alternative scp-like syntax: [user@]host.xz:path/to/repo.git/
    private static final RegExp SCP_LIKE_SYNTAX = RegExp.compile("([A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-:]+)+:");
    // the transport protocol
    private static final RegExp PROTOCOL        = RegExp.compile("((http|https|git|ssh|ftp|ftps)://)");
    // the address of the remote server between // and /
    private static final RegExp HOST1           = RegExp.compile("//([A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-:]+)+/");
    // the address of the remote server between @ and : or /
    private static final RegExp HOST2           = RegExp.compile("@([A-Za-z0-9_\\-]+\\.[A-Za-z0-9_\\-:]+)+[:/]");
    // the repository name
    private static final RegExp REPO_NAME       = RegExp.compile("/[A-Za-z0-9_.\\-]+$");
    // start with white space
    private static final RegExp WHITE_SPACE     = RegExp.compile("^\\s");

    private final DtoFactory                          dtoFactory;
    private       GitHubClientService                 gitHubClientService;
    private       Map<String, List<GitHubRepository>> repositories;
    private       GitHubLocalizationConstant          locale;
    private       GithubImporterPageView              view;
    private       GitHubAuthenticator                 gitHubAuthenticator;

    @Inject
    public GithubImporterPagePresenter(GithubImporterPageView view,
                                       GitHubAuthenticator gitHubAuthenticator,
                                       GitHubClientService gitHubClientService,
                                       DtoFactory dtoFactory,
                                       GitHubLocalizationConstant locale) {
        this.view = view;
        this.gitHubAuthenticator = gitHubAuthenticator;
        this.gitHubClientService = gitHubClientService;
        this.dtoFactory = dtoFactory;
        this.view.setDelegate(this);
        this.locale = locale;
    }

    @Override
    public boolean isCompleted() {
        return isGitUrlCorrect(dataObject.getSource().getLocation());
    }

    @Override
    public void projectNameChanged(@NotNull String name) {
        dataObject.setName(name);
        updateDelegate.updateControls();

        validateProjectName();
    }

    /**
     * Validates project name and highlights input when error.
     */
    private void validateProjectName() {
        if (NameUtils.checkProjectName(view.getProjectName())) {
            view.hideNameError();
        } else {
            view.showNameError();
        }
    }

    @Override
    public void projectUrlChanged(@NotNull String url) {
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
    public void projectDescriptionChanged(@NotNull String projectDescription) {
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
    public void keepDirectorySelected(boolean keepDirectory) {
        view.enableDirectoryNameField(keepDirectory);

        if (keepDirectory) {
            projectParameters().put("keepDirectory", view.getDirectoryName());
            dataObject.withType("blank");
            view.highlightDirectoryNameField(!NameUtils.checkProjectName(view.getDirectoryName()));
            view.focusDirectoryNameFiend();
        } else {
            projectParameters().remove("keepDirectory");
            dataObject.withType(null);
            view.highlightDirectoryNameField(false);
        }
    }

    @Override
    public void keepDirectoryNameChanged(@NotNull String directoryName) {
        if (view.keepDirectory()) {
            projectParameters().put("keepDirectory", directoryName);
            dataObject.setPath(view.getDirectoryName());
            dataObject.withType("blank");
            view.highlightDirectoryNameField(!NameUtils.checkProjectName(view.getDirectoryName()));
        } else {
            projectParameters().remove("keepDirectory");
            dataObject.setPath(null);
            dataObject.withType(null);
            view.highlightDirectoryNameField(false);
        }
    }

    @Override
    public void go(@NotNull AcceptsOneWidget container) {
        container.setWidget(view);

        updateView();

        view.setKeepDirectoryChecked(false);
        view.setDirectoryName("");
        view.enableDirectoryNameField(false);
        view.highlightDirectoryNameField(false);

        view.setInputsEnableState(true);
        view.focusInUrlInput();
    }

    /**
     * Updates the view.
     */
    private void updateView() {
        view.setProjectName(dataObject.getName());
        view.setProjectDescription(dataObject.getDescription());
        view.setProjectUrl(dataObject.getSource().getLocation());
    }

    @Override
    public void onLoadRepoClicked() {
        getUserRepos();
    }

    /** Get the list of all authorized user's repositories. */
    private void getUserRepos() {
        showProcessing(true);
        gitHubClientService.getAllRepositories(
                new AsyncRequestCallback<Map<String, List<GitHubRepository>>>(new AllRepositoriesUnmarshaller(dtoFactory)) {
                    @Override
                    protected void onSuccess(Map<String, List<GitHubRepository>> result) {
                        showProcessing(false);
                        onListLoaded(result);
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        showProcessing(false);
                        if (exception instanceof UnauthorizedException) {
                            authorize();
                        }
                    }
                });
    }

    /**
     * Authorizes on GitHub.
     */
    private void authorize() {
        showProcessing(true);
        gitHubAuthenticator.authorize(new AsyncCallback<OAuthStatus>() {
            @Override
            public void onFailure(Throwable caught) {
                showProcessing(false);
            }

            @Override
            public void onSuccess(OAuthStatus result) {
                showProcessing(false);
                getUserRepos();
            }
        });
    }

    @Override
    public void onRepositorySelected(@NotNull ProjectData repository) {
        dataObject.setName(repository.getName());
        dataObject.setDescription(repository.getDescription());
        dataObject.getSource().setLocation(repository.getRepositoryUrl());

        updateView();

        updateDelegate.updateControls();
    }

    @Override
    public void onAccountChanged() {
        refreshProjectList();
    }

    /**
     * Perform actions when the list of repositories was loaded.
     *
     * @param repositories
     *         loaded list of repositories
     */
    private void onListLoaded(@NotNull Map<String, List<GitHubRepository>> repositories) {
        this.repositories = repositories;
        view.setAccountNames(repositories.keySet());
        refreshProjectList();
        view.showGithubPanel();
    }

    /**
     * Refresh project list on view.
     */
    private void refreshProjectList() {
        List<ProjectData> projectsData = new ArrayList<>();

        String accountName = view.getAccountName();
        if (repositories.containsKey(accountName)) {
            List<GitHubRepository> repo = repositories.get(accountName);

            for (GitHubRepository repository : repo) {
                ProjectData projectData =
                        new ProjectData(repository.getName(), repository.getDescription(), null, null, repository.getSshUrl(),
                                        repository.getGitUrl());
                projectsData.add(projectData);
            }

            Collections.sort(projectsData, new Comparator<ProjectData>() {
                @Override
                public int compare(ProjectData o1, ProjectData o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

            view.setRepositories(projectsData);
            view.reset();
            view.showGithubPanel();
        }
    }

    /**
     * Shown the state that the request is processing.
     */
    private void showProcessing(boolean inProgress) {
        view.setLoaderVisibility(inProgress);
        view.setInputsEnableState(!inProgress);
    }

    /**
     * Gets project name from uri.
     */
    private String extractProjectNameFromUri(@NotNull String uri) {
        int indexFinishProjectName = uri.lastIndexOf(".");
        int indexStartProjectName = uri.lastIndexOf("/") != -1 ? uri.lastIndexOf("/") + 1 : (uri.lastIndexOf(":") + 1);

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
     * @param url
     *         url for validate
     * @return <code>true</code> if url is correct
     */
    private boolean isGitUrlCorrect(@NotNull String url) {
        if (WHITE_SPACE.test(url)) {
            view.showUrlError(locale.importProjectMessageStartWithWhiteSpace());
            return false;
        }
        if (SCP_LIKE_SYNTAX.test(url)) {
            view.hideUrlError();
            return true;
        }
        if (!PROTOCOL.test(url)) {
            view.showUrlError(locale.importProjectMessageProtocolIncorrect());
            return false;
        }
        if (!(HOST1.test(url) || HOST2.test(url))) {
            view.showUrlError(locale.importProjectMessageHostIncorrect());
            return false;
        }
        if (!(REPO_NAME.test(url))) {
            view.showUrlError(locale.importProjectMessageNameRepoIncorrect());
            return false;
        }
        view.hideUrlError();
        return true;
    }
}

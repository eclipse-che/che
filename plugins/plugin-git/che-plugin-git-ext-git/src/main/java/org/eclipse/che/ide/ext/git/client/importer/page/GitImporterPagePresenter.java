/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.git.client.importer.page;

import com.google.common.base.Strings;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.project.MutableProjectConfig;
import org.eclipse.che.ide.api.wizard.AbstractWizardPage;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.util.NameUtils;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Roman Nikitenko
 */
public class GitImporterPagePresenter extends AbstractWizardPage<MutableProjectConfig> implements GitImporterPageView.ActionDelegate {

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

    private GitLocalizationConstant locale;
    private GitImporterPageView     view;

    private boolean                 ignoreChanges;

    @Inject
    public GitImporterPagePresenter(GitImporterPageView view,
                                    GitLocalizationConstant locale) {
        this.view = view;
        this.view.setDelegate(this);
        this.locale = locale;
    }

    @Override
    public boolean isCompleted() {
        return isGitUrlCorrect(dataObject.getSource().getLocation());
    }

    @Override
    public void projectNameChanged(@NotNull String name) {
        if (ignoreChanges) {
            return;
        }

        dataObject.setName(name);
        updateDelegate.updateControls();

        validateProjectName();
    }

    private void validateProjectName() {
        if (NameUtils.checkProjectName(view.getProjectName())) {
            view.markNameValid();
        } else {
            view.markNameInvalid();
        }
    }

    @Override
    public void projectUrlChanged(@NotNull String url) {
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
    public void keepDirectoryNameChanged(@NotNull String directoryName) {
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
    public void branchSelected(boolean branch) {
        view.enableBranchNameField(branch);

        if (view.isBranchName()) {
            projectParameters().put("branch", view.getBranchName());
            view.focusBranchNameField();
        } else {
            projectParameters().remove("branch");
        }
    }

    @Override
    public void branchNameChanged(@NotNull String branch) {
        if (view.isBranchName()) {
            projectParameters().put("branch", branch);
        } else {
            projectParameters().remove("branch");
        }
    }

    @Override
    public void go(@NotNull AcceptsOneWidget container) {
        container.setWidget(view);

        if (Strings.isNullOrEmpty(dataObject.getName()) && Strings.isNullOrEmpty(dataObject.getSource().getLocation())) {
            ignoreChanges = true;

            view.unmarkURL();
            view.unmarkName();
            view.setURLErrorMessage(null);
        }

        view.setProjectName(dataObject.getName());
        view.setProjectDescription(dataObject.getDescription());
        view.setProjectUrl(dataObject.getSource().getLocation());

        view.setKeepDirectoryChecked(false);
        view.setBranchChecked(false);
        view.setDirectoryName("");
        view.setBranchName("");
        view.enableDirectoryNameField(false);
        view.enableBranchNameField(false);
        view.highlightDirectoryNameField(false);

        view.setInputsEnableState(true);
        view.focusInUrlInput();

        ignoreChanges = false;
    }

    /** Gets project name from uri. */
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

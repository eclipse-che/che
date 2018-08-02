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
package org.eclipse.che.plugin.github.ide.importer.page;

import com.google.inject.ImplementedBy;
import java.util.List;
import java.util.Set;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.plugin.github.ide.load.ProjectData;

/** @author Roman Nikitenko */
@ImplementedBy(GithubImporterPageViewImpl.class)
public interface GithubImporterPageView extends View<GithubImporterPageView.ActionDelegate> {

  interface ActionDelegate {
    /**
     * Performs any actions appropriate in response to the user having changed the project's name.
     */
    void onProjectNameChanged(@NotNull String name);

    /**
     * Performs any actions appropriate in response to the user having changed the project's URL.
     */
    void onProjectUrlChanged(@NotNull String url);

    /** Performs any actions appropriate in response to the user has selected recursive checkbox. */
    void onRecursiveSelected(boolean recursiveSelected);

    /**
     * Performs any actions appropriate in response to the user having changed the project's
     * description.
     */
    void onProjectDescriptionChanged(@NotNull String projectDescriptionValue);

    /**
     * Performs any actions appropriate in response to the user having clicked the 'LoadRepo' key.
     */
    void onLoadRepoClicked();

    /**
     * Performs any actions appropriate in response to the user having selected a repository.
     *
     * @param repository selected repository
     */
    void onRepositorySelected(@NotNull ProjectData repository);

    /** Performs any actions appropriate in response to the user having changed account field. */
    void onAccountChanged();

    /** Perform actions when selecting Keep Directory checkbox. */
    void onKeepDirectorySelected(boolean keepDirectory);

    /** Perform actions when changing the name of a directory. */
    void onKeepDirectoryNameChanged(@NotNull String url);

    /** Perform actions when selecting Branch checkbox. */
    void onBranchCheckBoxSelected(boolean isSelected);

    /** Perform actions when changing the name of a branch. */
    void onBranchNameChanged(@NotNull String branchName);
  }

  /** Marks URL field containing valid value. */
  void markURLValid();

  /** Marks URL field containing invalid value. */
  void markURLInvalid();

  /** Removes mark from URL field. */
  void unmarkURL();

  /**
   * Displays error message under URL field.
   *
   * @param message message
   */
  void setURLErrorMessage(@NotNull String message);

  /** Marks name field containing valid value. */
  void markNameValid();

  /** Marks name field containing invalid value. */
  void markNameInvalid();

  /** Removes mark from Name field. */
  void unmarkName();

  /**
   * Set the project's URL.
   *
   * @param url the project's URL to set
   */
  void setProjectUrl(@NotNull String url);

  /**
   * Get the project's name value.
   *
   * @return {@link String} project's name
   */
  @NotNull
  String getProjectName();

  /**
   * Set the project's name value.
   *
   * @param projectName project's name to set
   */
  void setProjectName(@NotNull String projectName);

  /**
   * Set the project's description value.
   *
   * @param projectDescription project's description to set
   */
  void setProjectDescription(@NotNull String projectDescription);

  /** Focuses URL field. */
  void focusInUrlInput();

  /**
   * Set the enable state of the inputs.
   *
   * @param isEnabled <code>true</code> if enabled, <code>false</code> if disabled
   */
  void setInputsEnableState(boolean isEnabled);

  /**
   * Set available repositories for account.
   *
   * @param repositories available repositories
   */
  void setRepositories(@NotNull List<ProjectData> repositories);

  /** @return account name */
  @NotNull
  String getAccountName();

  /**
   * Set available account names.
   *
   * @param names available names
   */
  void setAccountNames(@NotNull Set<String> names);

  /** Close github panel. */
  void closeGithubPanel();

  /** Show github panel. */
  void showGithubPanel();

  /** Reset the page. */
  void reset();

  /**
   * Set the visibility state of the loader.
   *
   * @param isVisible <code>true</code> if visible.
   */
  void setLoaderVisibility(boolean isVisible);

  /**
   * Returns whether user wants to checkout a special directory.
   *
   * @return <b>true</b> if user has checked the Keep Director checkbox, otherwise returns
   *     <b>false</b>
   */
  boolean keepDirectory();

  /**
   * Sets new value of Keep Directory checkbox.
   *
   * @param checked <b>true</b> to check the field or <b>false</b> to leave it unchecked
   */
  void setKeepDirectoryChecked(boolean checked);

  /**
   * Returns the name of a directory to checkout.
   *
   * @return name of a directory to checkout
   */
  String getDirectoryName();

  /**
   * Sets new value of Directory name field.
   *
   * @param directoryName new value of directory name field
   */
  void setDirectoryName(String directoryName);

  /**
   * Enables or disables Directory name field.
   *
   * @param enable true or false to enable or disable the field
   */
  void enableDirectoryNameField(boolean enable);

  /**
   * * Highlights Directory name field to notify its value is invalid.
   *
   * @param highlight <b>true</b> to highlight the field or <b>false</b> to remove highlighting
   */
  void highlightDirectoryNameField(boolean highlight);

  /** Focuses directory name field. */
  void focusDirectoryNameField();

  /**
   * Sets whether user wants to checkout to a special branch.
   *
   * @param selected <b>true</b> to check the field or <b>false</b> to leave it unchecked
   */
  void setBranchCheckBoxSelected(boolean selected);

  /**
   * Returns whether user wants to checkout to a special branch.
   *
   * @return <b>true</b> if user has selected the Branch checkbox, otherwise returns <b>false</b>
   */
  boolean isBranchCheckBoxSelected();

  /**
   * Sets new value of Branch name field.
   *
   * @param branchName new value of Branch name field
   */
  void setBranchName(String branchName);

  /**
   * Returns the name of a branch to checkout.
   *
   * @return name of a branch to checkout
   */
  String getBranchName();

  /**
   * Enables or disables Branch name field.
   *
   * @param enabled <b>true</b> or <b>false</b> to enable or disable the field
   */
  void enableBranchNameField(boolean enabled);

  /** Focuses Branch name field. */
  void focusBranchNameField();
}

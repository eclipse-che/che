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
package org.eclipse.che.plugin.github.ide;

import com.google.gwt.i18n.client.Messages;

/** @author Evgen Vidolob */
public interface GitHubLocalizationConstant extends Messages {
  // MESSAGES
  @Key("authorization.failed")
  String authorizationFailed();

  @Key("authorization.request.rejected")
  String authorizationRequestRejected();

  @Key("importProject.message.startWithWhiteSpace")
  String importProjectMessageStartWithWhiteSpace();

  @Key("importProject.message.nameRepoIncorrect")
  String importProjectMessageNameRepoIncorrect();

  @Key("importProject.message.protocolIncorrect")
  String importProjectMessageProtocolIncorrect();

  @Key("importProject.message.hostIncorrect")
  String importProjectMessageHostIncorrect();

  /*
   * ImportFromGitHub
   */
  @Key("import.github.account")
  String importFromGithubAccount();

  // Authorization
  @Key("authorization.dialog.title")
  String authorizationDialogTitle();

  @Key("authorization.dialog.text")
  String authorizationDialogText(String productName);

  @Key("authorization.message.unableCreateSshKey")
  String authMessageUnableCreateSshKey();

  @Key("authorization.generateKeyLabel")
  String authGenerateKeyLabel();

  @Key("authorization.message.keyUploadSuccess")
  String authMessageKeyUploadSuccess();

  @Key("message.sshKey.generation.promt")
  String messageSshKeyGenerationPromt();

  /*
   * SamplesListGrid
   */
  @Key("samplesListGrid.column.name")
  String samplesListRepositoryColumn();

  @Key("samplesListGrid.column.description")
  String samplesListDescriptionColumn();

  // GithubImporterPage
  @Key("view.import.githubImporterPage.projectUrl")
  String githubImporterPageProjectUrl();

  @Key("view.import.githubImporterPage.recursive")
  String githubImporterPageRecursive();

  @Key("view.import.githubImporterPage.projectInfo")
  String githubImporterPageProjectInfo();

  @Key("view.import.githubImporterPage.projectName")
  String githubImporterPageProjectName();

  @Key("view.import.githubImporterPageProjectNamePrompt")
  String githubImporterPageProjectNamePrompt();

  @Key("view.import.githubImporterPage.projectDescription")
  String githubImporterPageProjectDescription();

  @Key("view.import.githubImporterPage.projectDescriptionPrompt")
  String githubImporterPageProjectDescriptionPrompt();

  @Key("view.import.githubImporterPage.keepDirectory")
  String githubImporterPageKeepDirectory();

  @Key("view.import.githubImporterPage.keepDirectoryField")
  String githubImporterPageKeepDirectoryField();

  @Key("view.import.githubImporterPage.branchField")
  String githubImporterPageBranchField();

  @Key("view.import.githubImporterPage.branch")
  String githubImporterPageBranch();
}

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
package org.eclipse.che.plugin.svn.ide;

import com.google.gwt.i18n.client.Messages;

/**
 * Interface providing access to localization constants.
 *
 * @author Jeremy Whitlock
 */
public interface SubversionExtensionLocalizationConstants extends Messages {

    @Key("svn.console.project.name")
    String consoleProjectName(String projectName);

    /** Commands Constants */
    @Key("command.add")
    String commandAdd();

    @Key("command.cleanup")
    String commandCleanup();

    @Key("command.commit")
    String commandCommit();

    @Key("command.copy")
    String commandCopy();

    @Key("command.diff")
    String commandDiff();

    @Key("command.export")
    String commandExport();

    @Key("command.info")
    String commandInfo();

    @Key("command.lock")
    String commandLock();

    @Key("command.unlock")
    String commandUnlock();

    @Key("command.log")
    String commandLog();

    @Key("command.merge")
    String commandMerge();

    @Key("command.move")
    String commandMove();

    @Key("command.property")
    String commandProperty();

    @Key("command.remove")
    String commandRemove();

    @Key("command.resolve")
    String commandResolve();

    @Key("command.revert")
    String commandRevert();

    @Key("command.status")
    String commandStatus();

    @Key("command.switch")
    String commandSwitch();

    @Key("command.update")
    String commandUpdate();

    /** Action Constants */
    @Key("action.not.implemented")
    String actionNotImplemented();

    @Key("waiting.credentials")
    String waitingCredentials();

    @Key("add.description")
    String addDescription();

    @Key("add.failed")
    String addFailed();

    @Key("add.started")
    String addStarted(long pathCount);

    @Key("add.warning")
    String addWarning();

    @Key("add.successful")
    String addSuccessful();

    @Key("add.title")
    String addTitle();

    @Key("apply.patch.description")
    String applyPatchDescription();

    @Key("apply.patch.title")
    String applyPatchTitle();

    @Key("branch.tag.description")
    String branchTagDescription();

    @Key("branch.tag.title")
    String branchTagTitle();

    @Key("cleanup.description")
    String cleanupDescription();

    @Key("cleanup.title")
    String cleanupTitle();

    @Key("commit.description")
    String commitDescription();

    @Key("commit.title")
    String commitTitle();

    @Key("commit.placeholder")
    String commitPlaceholder();

    @Key("commit.message.empty")
    String commitMessageEmpty();

    @Key("commit.diff.unavailable")
    String commitDiffUnavailable();

    @Key("copy.description")
    String copyDescription();

    @Key("copy.title")
    String copyTitle();

    @Key("copy.fail.to.get.project")
    String copyFailToGetProject();

    @Key("copy.view.title.file")
    String copyViewTitleFile();

    @Key("copy.view.title.directory")
    String copyViewTitleDirectory();

    @Key("copy.notification.started")
    String copyNotificationStarted(String path);

    @Key("copy.notification.successful")
    String copyNotificationSuccessful();

    @Key("copy.notification.failed")
    String copyNotificationFailed();

    @Key("copy.empty.target")
    String copyEmptyTarget();

    @Key("copy.item.equal")
    String copyItemEqual();

    @Key("copy.source.wrong.url")
    String copySourceWrongURL();

    @Key("copy.target.wrong.url")
    String copyTargetWrongURL();

    @Key("copy.item.child.detect")
    String copyItemChildDetect();

    @Key("create.patch.description")
    String createPatchDescription();

    @Key("create.patch.title")
    String createPatchTitle();

    @Key("diff.description")
    String diffTitle();

    @Key("diff.title")
    String diffDescription();

    @Key("export.description")
    String exportDescription();

    @Key("export.title")
    String exportTitle();

    @Key("merge.description")
    String mergeDescription();

    @Key("merge.title")
    String mergeTitle();

    @Key("merge.dialog.title")
    String mergeDialogTitle();

    @Key("lock.description")
    String lockDescription();

    @Key("lock.title")
    String lockTitle();

    @Key("log.description")
    String logDescription();

    @Key("log.title")
    String logTitle();

    @Key("properties.description")
    String propertiesDescription();

    @Key("properties.title")
    String propertiesTitle();

    @Key("relocate.description")
    String relocateDescription();

    @Key("relocate.title")
    String relocateTitle();

    @Key("remove.description")
    String removeDescription();

    @Key("remove.failed")
    String removeFailed();

    @Key("remove.started")
    String removeStarted(long pathCount);

    @Key("remove.successful")
    String removeSuccessful();

    @Key("remove.title")
    String removeTitle();

    @Key("rename.description")
    String renameDescription();

    @Key("rename.title")
    String renameTitle();

    @Key("resolved.description")
    String resolvedDescription();

    @Key("resolved.title")
    String resolvedTitle();

    @Key("revert.description")
    String revertDescription();

    @Key("revert.title")
    String revertTitle();

    @Key("revert.confirm.text")
    String revertConfirmText(String paths);

    @Key("revert.started")
    String revertStarted();

    @Key("revert.successful")
    String revertSuccessful();

    @Key("revert.warning")
    String revertWarning();

    @Key("revert.failed")
    String revertFailed();

    @Key("status.description")
    String statusDescription();

    @Key("status.failed")
    String statusFailed();

    @Key("status.title")
    String statusTitle();

    @Key("status.started")
    String statusStarted(String suffix);

    @Key("switch.description")
    String switchDescription();

    @Key("switch.title")
    String switchTitle();

    @Key("switch.switchTo.label")
    String switchToLabel();

    @Key("switch.location.label")
    String switchLocationLabel();

    @Key("switch.switchTo.tag")
    String switchToTag();

    @Key("switch.switchTo.branch")
    String switchToBranch();

    @Key("switch.ignoreAncestry")
    String switchIgnoreAncestry();

    @Key("switch.force")
    String switchForce();

    @Key("switch.ignoreExternals")
    String switchIgnoreExternals();

    @Key("switch.switchTo.trunk")
    String switchToTrunk();

    @Key("switch.switchTo.otherLocation")
    String switchToOtherLocation();

    @Key("switch.depth.label")
    String switchDepthLabel();

    @Key("switch.workingCopyDepth.label")
    String switchWorkingCopyDepthLabel();

    @Key("switch.conflictResolution.label")
    String switchConflictResolutionLabel();

    @Key("switch.revision.head")
    String switchRevisionHead();

    @Key("switch.revision.at.label")
    String switchRevisionAtLabel();

    @Key("switch.revision.revision")
    String switchRevisionRevision();

    @Key("switch.selectLocation.title")
    String switchSelectLocationTitle();

    @Key("switch.request.error")
    String switchRequestError(String location);

    @Key("list.request.error")
    String listRequestError(String location);

    @Key("list.branches.request.error")
    String listBranchesRequestError(String location);

    @Key("list.tags.request.error")
    String listTagsRequestError(String location);

    @Key("info.request.error")
    String infoRequestError(String project);

    @Key("unlock.description")
    String unlockDescription();

    @Key("unlock.title")
    String unlockTitle();

    @Key("update.description")
    String updateDescription();

    @Key("update.successful")
    String updateSuccessful(String revision);

    @Key("update.title")
    String updateTitle();

    @Key("update.to.revision.description")
    String updateToRevisionDescription();

    @Key("update.to.revision.title")
    String updateToRevisionTitle();

    @Key("change.credentials.title")
    String changeCredentialsTitle();

    @Key("change.credentials.description")
    String changeCredentialsDescription();

    /** Subversion Constants */
    @Key("subversion.label")
    String subversionLabel();

    @Key("subversion.homepage.url")
    String subversionHomepageUrl();

    @Key("subversion.repository.url.trunk")
    String subversionRepositoryUrlTrunk();

    @Key("subversion.depth.infinity.label")
    String subversionDepthInfinityLabel();

    @Key("subversion.depth.immediates.label")
    String subversionDepthImmediatesLabel();

    @Key("subversion.depth.files.label")
    String subversionDepthFilesLabel();

    @Key("subversion.depth.empty.label")
    String subversionDepthEmptyLabel();

    @Key("subversion.workingCopyDepth.empty.label")
    String subversionWorkingCopyDepthEmptyLabel();

    @Key("subversion.workingCopyDepth.files.label")
    String subversionWorkingCopyDepthFilesLabel();

    @Key("subversion.workingCopyDepth.immediates.label")
    String subversionWorkingCopyDepthImmediatesLabel();

    @Key("subversion.workingCopyDepth.infinity.label")
    String subversionWorkingCopyDepthInfinityLabel();

    @Key("subversion.accept.postpone.label")
    String subversionAcceptPostponeLabel();

    @Key("subversion.accept.mineFull.label")
    String subversionAcceptMineFullLabel();

    @Key("subversion.accept.theirsFull.label")
    String subversionAcceptTheirsFullLabel();

    /** View Constants */
    @Key("button.cancel")
    String buttonCancel();

    @Key("button.commit")
    String buttonCommit();

    @Key("button.merge")
    String buttonMerge();

    @Key("button.resolve")
    String buttonResolve();

    @Key("button.update")
    String buttonUpdate();

    @Key("button.switch")
    String buttonSwitch();

    @Key("button.log")
    String buttonLog();

    @Key("button.select")
    String buttonSelect();

    @Key("update.depth.label")
    String updateDepthLabel();

    @Key("update.head.revision.label")
    String updateHeadRevisionLabel();

    @Key("update.ignore.externals.label")
    String updateIgnoreExternalsLabel();

    @Key("update.failed")
    String updateFailed();

    @Key("update.revision.label")
    String updateRevisionLabel();

    @Key("update.to.revision.started")
    String updateToRevisionStarted(String revision);

    @Key("importer.importerInfo")
    String importerImporterInfo();

    @Key("importer.projectUrl")
    String importerProjectUrl();

    @Key("importer.projectRelativePath")
    String projectImporterProjectRelativePath();

    @Key("importer.projectInfo")
    String importerProjectInfo();

    @Key("importer.projectName")
    String importerProjectName();

    @Key("importer.projectNamePrompt")
    String importerProjectNamePrompt();

    @Key("importer.projectDescription")
    String importerProjectDescription();

    @Key("importer.projectDescriptionPrompt")
    String importerProjectDescriptionPrompt();

    @Key("importer.default.relativePath")
    String importerDefaultRelativePath();

    @Key("importer.url.incorrect.message")
    String importProjectUrlIncorrectMessage();

    @Key("status.successful")
    String statusSuccessful();

    /* Commit dialog */

    @Key("dialog.commit.commitAllFieldTitle")
    String commitAllFieldTitle();

    @Key("dialog.commit.commitSelectionFieldTitle")
    String commitSelectionFieldTitle();

    @Key("dialog.commit.commitKeepLocksFieldTitle")
    String commitKeepLocksFieldTitle();

    @Key("error.commit.failed")
    String commitFailed();

    /* cleanup */
    @Key("cleanup.successful")
    String cleanupSuccessful();

    @Key("cleanup.failed")
    String cleanupFailed();

    /* Resolve */
    @Key("resolve.noconflict.title")
    String resolveNoConflictTitle();

    @Key("resolve.noconflict.content")
    String resolveNoConflictContent();

    /* Credentials dialog */

    /** The dialog title. */
    @Key("ask.credentials.title")
    String askCredentialsTitle();

    @Key("ask.credentials.validate")
    String askCredentialsValidate();

    @Key("ask.credentials.cancel")
    String askCredentialsCancel();

    @Key("ask.credentials.label")
    String askCredentialsLabel();

    @Key("ask.credentials.field.username")
    String askCredentialsUsername();

    @Key("ask.credentials.field.password")
    String askCredentialsPassword();

    @Key("ask.credentials.warn.notsubversion")
    String warnNotSubversionproject();

    // lock&unlock

    @Key("lock.dialog.title")
    String lockDialogTitle();

    @Key("unlock.dialog.title")
    String unlockDialogTitle();

    @Key("lock.dialog.content")
    String lockDialogContent();

    @Key("unlock.dialog.content")
    String unlockDialogContent();

    @Key("lock.dialog.button.force")
    String lockButtonWithForceLabel();

    @Key("lock.dialog.button.noforce")
    String lockButtonWithoutForceLabel();

    @Key("unlock.dialog.button.force")
    String unlockButtonWithForceLabel();

    @Key("unlock.dialog.button.noforce")
    String unlockButtonWithoutForceLabel();

    @Key("error.lock.directory")
    String errorMessageLockDirectory();

    @Key("error.unlock.directory")
    String errorMessageUnlockDirectory();

    @Key("error.lock.directory.dialog.title")
    String dialogTitleLockDirectory();

    @Key("error.unlock.directory.dialog.title")
    String dialogTitleUnlockDirectory();

    /* Export dialog */

    @Key("download.title")
    String downloadTitle();

    @Key("download.finished")
    String downloadFinished(String path);

    @Key("download.button")
    String downloadButton();

    @Key("download.button.cancel")
    String downloadButtonCanceled();

    @Key("export.started")
    String exportStarted(String path);

    @Key("export.successful")
    String exportSuccessful(String path);

    @Key("export.fail.noprojectpath")
    String exportFailedNoProjectPath();

    @Key("export.fail.command.execution")
    String exportCommandExecutionError();

    @Key("export.fail.revision.donotexist")
    String exportRevisionDoNotExistForPath(String givenRevision, String path);

    @Key("export.fail.norevision")
    String exportNoRevisionForPath(String path);

    /* Move dialog */
    @Key("move.fail.to.get.project")
    String moveFailToGetProject();

    @Key("move.view.title")
    String moveViewTitle();

    @Key("move.notification.started")
    String moveNotificationStarted(String path);

    @Key("move.notification.successful")
    String moveNotificationSuccessful();

    @Key("move.notification.failed")
    String moveNotificationFailed();

    @Key("move.source.url.empty")
    String moveSourceUrlEmpty();

    @Key("move.target.url.empty")
    String moveTargetUrlEmpty();

    @Key("move.source.and.target.not.equals")
    String moveSourceAndTargetNotEquals();

    @Key("move.comment.empty")
    String moveCommentEmpty();

    @Key("move.button")
    String moveButton();

    @Key("move.action.title")
    String moveActionTitle();

    @Key("move.action.description")
    String moveActionDescription();

    @Key("move.item.child.detect")
    String moveItemChildDetected();

    /* Credentials dialog */
    @Key("credentials.dialog.title")
    String credentialsDialogTitle();

    @Key("credentials.dialog.username")
    String credentialsDialogUsername();

    @Key("credentials.dialog.password")
    String credentialsDialogPassword();

    @Key("credentials.dialog.authenticate.button")
    String credentialsDialogAuthenticateButton();

    @Key("credentials.dialog.cancel.button")
    String credentialsDialogCancelButton();

    /** Property */
    @Key("property.modify.start")
    String propertyModifyStart();

    @Key("property.modify.finished")
    String propertyModifyFinished();

    @Key("property.modifying.failed")
    String propertyModifyFailed();

    @Key("property.remove.start")
    String propertyRemoveStart();

    @Key("property.remove.finished")
    String propertyRemoveFinished();

    @Key("property.remove.failed")
    String propertyRemoveFailed();

    @Key("button.console.clear")
    String consoleClearButton();

    @Key("button.console.scroll")
    String consoleScrollButton();
}

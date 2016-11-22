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
package org.eclipse.che.ide;

import com.google.gwt.i18n.client.Messages;

/** @author Andrey Plotnikov */
public interface CoreLocalizationConstant extends Messages {
    @Key("createProjectFromTemplate.nameField")
    String createProjectFromTemplateName();

    @Key("createProjectFromTemplate.project.exists")
    String createProjectFromTemplateProjectExists(String projectName);

    @Key("extension.title")
    String extensionTitle();

    @Key("extension.category")
    String extensionCategory();

    @Key("action.navigateToFile.text")
    String actionNavigateToFileText();

    @Key("action.navigateToFile.description")
    String actionNavigateToFileDescription();

    @Key("navigateToFile.view.title")
    String navigateToFileViewTitle();

    @Key("navigateToFile.view.file.field.title")
    String navigateToFileViewFileFieldTitle();

    @Key("navigateToFile.view.file.field.prompt")
    String navigateToFileViewFileFieldPrompt();

    @Key("navigateToFile.searchIsCaseSensitive")
    String navigateToFileSearchIsCaseSensitive();

    @Key("appearance.title")
    String appearanceTitle();

    @Key("appearance.category")
    String appearanceCategory();

    /* DeleteItem */
    @Key("action.delete.text")
    String deleteItemActionText();

    @Key("action.delete.description")
    String deleteItemActionDescription();

    /* Cut */
    @Key("action.cut.text")
    String cutItemsActionText();

    @Key("action.cut.description")
    String cutItemsActionDescription();

    /* Copy */
    @Key("action.copy.text")
    String copyItemsActionText();

    @Key("action.copy.description")
    String copyItemsActionDescription();

    /* Paste */
    @Key("action.paste.text")
    String pasteItemsActionText();

    @Key("action.paste.description")
    String pasteItemsActionDescription();

    @Key("action.export.text")
    String exportConfigText();

    @Key("deleteDialogTitle")
    String deleteDialogTitle();

    @Key("deleteAddToIndexDialogTitle")
    String deleteAddToIndexDialogTitle();

    @Key("deleteAddToIndexDialogText")
    String deleteAddToIndexDialogText();

    @Key("deleteAddToIndexIndexFailedToUpdate")
    String deleteAddToIndexIndexFailedToUpdate();

    @Key("deleteAddToIndexIndexUpdated")
    String deleteAddToIndexIndexUpdated();

    @Key("deleteAddToIndexDialogNotification")
    String deleteAddToIndexDialogNotification();

    @Key("deleteAllFilesAndSubdirectories")
    String deleteAllFilesAndSubdirectories(String name);

    @Key("deleteFilesAndSubdirectoriesInTheSelectedDirectory")
    String deleteFilesAndSubdirectoriesInTheSelectedDirectory();

    @Key("mixedProjectDeleteMessage")
    String mixedProjectDeleteMessage();

    /* RenameItem */
    @Key("action.rename.text")
    String renameItemActionText();

    @Key("action.rename.description")
    String renameItemActionDescription();

    @Key("renameNodeDialogTitle")
    String renameNodeDialogTitle();

    @Key("renameFileDialogTitle")
    String renameFileDialogTitle(String name);

    @Key("renameFolderDialogTitle")
    String renameFolderDialogTitle(String name);

    @Key("renameProjectDialogTitle")
    String renameProjectDialogTitle(String name);

    @Key("renameDialogNewNameLabel")
    String renameDialogNewNameLabel();

    @Key("createProjectFromTemplate.descriptionField")
    String createProjectFromTemplateDescription();

    @Key("createProjectFromTemplate.projectPrivacy")
    String createProjectFromTemplateProjectPrivacy();

    @Key("createProjectFromTemplate.public")
    String createProjectFromTemplatePublic();

    @Key("createProjectFromTemplate.private")
    String createProjectFromTemplatePrivate();

    @Key("projectWizard.defaultTitleText")
    String projectWizardDefaultTitleText();

    @Key("projectWizard.titleText")
    String projectWizardTitleText();

    @Key("projectWizard.defaultSaveButtonText")
    String projectWizardDefaultSaveButtonText();

    @Key("projectWizard.saveButtonText")
    String projectWizardSaveButtonText();

    @Key("format.name")
    String formatName();

    @Key("format.description")
    String formatDescription();

    @Key("undo.name")
    String undoName();

    @Key("undo.description")
    String undoDescription();

    @Key("redo.name")
    String redoName();

    @Key("redo.description")
    String redoDescription();

    @Key("uploadFile.name")
    String uploadFileName();

    @Key("uploadFile.description")
    String uploadFileDescription();

    @Key("uploadFile.title")
    String uploadFileTitle();

    @Key("uploadFile.overwrite")
    String uploadFileOverwrite();

    @Key("uploadZipFolder.title")
    String uploadZipFolderTitle();

    @Key("uploadFolderFromZip.name")
    String uploadFolderFromZipName();

    @Key("uploadFolderFromZip.description")
    String uploadFolderFromZipDescription();

    @Key("downloadZip.project.name")
    String downloadProjectAsZipName();

    @Key("downloadZip.project.description")
    String downloadProjectAsZipDescription();

    @Key("download.item.name")
    String downloadItemName();

    @Key("download.item.description")
    String downloadItemDescription();

    @Key("uploadFolderFromZip.overwrite")
    String uploadFolderFromZipOverwrite();

    @Key("uploadFolderFromZip.skipFirstLevel")
    String uploadFolderFromZipSkipFirstLevel();

    @Key("cancelButton")
    String cancelButton();

    @Key("uploadButton")
    String uploadButton();

    @Key("openFileFieldTitle")
    String openFileFieldTitle();

    @Key("uploadFolderFromZip.openZipFieldTitle")
    String uploadFolderFromZipOpenZipFieldTitle();

    @Key("projectExplorer.button.title")
    String projectExplorerButtonTitle();

    @Key("projectExplorer.titleBar.text")
    String projectExplorerTitleBarText();

    @Key("importProject.message.success")
    String importProjectMessageSuccess(String projectName);

    @Key("importProject.message.failure")
    String importProjectMessageFailure(String projectName);

    @Key("importProject.message.startWithWhiteSpace")
    String importProjectMessageStartWithWhiteSpace();

    @Key("importProject.message.urlInvalid")
    String importProjectMessageUrlInvalid();

    @Key("importProject.message.unableGetSshKey")
    String importProjectMessageUnableGetSshKey();

    @Key("importProjectFromLocation.name")
    String importProjectFromLocationName();

    @Key("importProjectFromLocation.description")
    String importProjectFromLocationDescription();

    @Key("importLocalProject.name")
    String importLocalProjectName();

    @Key("importLocalProject.description")
    String importLocalProjectDescription();

    @Key("importLocalProject.openZipTitle")
    String importLocalProjectOpenZipTitle();

    @Key("importProject.importButton")
    String importProjectButton();

    @Key("importProject.importing")
    String importingProject();

    @Key("importProject.uriFieldTitle")
    String importProjectUriFieldTitle();

    @Key("importProject.viewTitle")
    String importProjectViewTitle();

    @Key("importProject.importer.info")
    String importProjectImporterInfo();

    @Key("importProject.project.info")
    String importProjectInfo();

    @Key("importProject.name.prompt")
    String importProjectNamePrompt();

    @Key("importProject.description.prompt")
    String importProjectDescriptionPrompt();

    @Key("importProject.zipImporter.skipFirstLevel")
    String importProjectZipImporterSkipFirstLevel();

    @Key("import.project.error")
    String importProjectError();

    /* Authorization */
    @Key("authorization.dialog.title")
    String authorizationDialogTitle();

    @Key("authorization.dialog.text")
    String authorizationDialogText();

    @Key("oauth.failed.to.get.authenticator.title")
    String oauthFailedToGetAuthenticatorTitle();

    @Key("oauth.failed.to.get.authenticator.text")
    String oauthFailedToGetAuthenticatorText();

    @Key("importProject.ssh.key.upload.failed.title")
    String importProjectSshKeyUploadFailedTitle();

    @Key("importProject.ssh.key.upload.failed.text")
    String importProjectSshKeyUploadFailedText();

    /* Actions */
    @Key("action.newFolder.title")
    String actionNewFolderTitle();

    @Key("action.newFolder.description")
    String actionNewFolderDescription();

    @Key("action.newFile.title")
    String actionNewFileTitle();

    @Key("action.newFile.description")
    String actionNewFileDescription();

    @Key("action.newFile.add.to.index.title")
    String actionNewFileAddToIndexTitle();

    @Key("action.newFile.add.to.index.text")
    String actionNewFileAddToIndexText(String file);

    @Key("action.newFile.add.to.index.notification")
    String actionNewFileAddToIndexNotification(String file);

    @Key("action.git.index.updated")
    String actionGitIndexUpdated();

    @Key("action.git.index.update.failed")
    String actionGitIndexUpdateFailed();

    @Key("action.newXmlFile.title")
    String actionNewXmlFileTitle();

    @Key("action.newXmlFile.description")
    String actionNewXmlFileDescription();

    @Key("action.link.with.editor")
    String actionLinkWithEditor();

    @Key("action.projectConfiguration.description")
    String actionProjectConfigurationDescription();

    @Key("action.projectConfiguration.title")
    String actionProjectConfigurationTitle();

    @Key("action.findAction.description")
    String actionFindActionDescription();

    @Key("action.findAction.title")
    String actionFindActionTitle();

    @Key("action.showHiddenFiles.title")
    String actionShowHiddenFilesTitle();

    @Key("action.showHiddenFiles.description")
    String actionShowHiddenFilesDescription();

    /* NewResource */
    @Key("newResource.title")
    String newResourceTitle(String title);

    @Key("newResource.label")
    String newResourceLabel(String title);

    @Key("newResource.invalidName")
    String invalidName();

    /* Messages */
    @Key("messages.changesMayBeLost")
    String changesMayBeLost();

    @Key("messages.someFilesCanNotBeSaved")
    String someFilesCanNotBeSaved();

    @Key("messages.saveChanges")
    String messagesSaveChanges(String name);

    @Key("messages.promptSaveChanges")
    String messagesPromptSaveChanges();

    @Key("messages.unableOpenResource")
    String unableOpenResource(String path);

    @Key("messages.canNotOpenFileWithoutParams")
    String canNotOpenFileWithoutParams();

    @Key("messages.fileToOpenIsNotSpecified")
    String fileToOpenIsNotSpecified();

    @Key("messages.canNotOpenNodeWithoutParams")
    String canNotOpenNodeWithoutParams();

    @Key("messages.nodeToOpenIsNotSpecified")
    String nodeToOpenIsNotSpecified();

    @Key("messages.noOpenedProject")
    String noOpenedProject();

    @Key("messages.startingOperation")
    String startingOperation(String operation);

    @Key("messages.startingMachine")
    String startingMachine(String machineName);

    /* Buttons */
    @DefaultMessage("Yes")
    String yesButtonTitle();

    @DefaultMessage("No")
    String noButtonTitle();

    @Key("ok")
    String ok();

    @Key("cancel")
    String cancel();

    @Key("open")
    String open();

    @Key("next")
    String next();

    @Key("back")
    String back();

    @Key("close")
    String close();

    @Key("save")
    String save();

    @Key("apply")
    String apply();

    @Key("refresh")
    String refresh();

    @Key("delete")
    String delete();

    @Key("print")
    String print();

    @Key("debug")
    String debug();

    @Key("projectProblem.title")
    String projectProblemTitle();

    @Key("projectProblem.message")
    String projectProblemMessage();

    @Key("action.expandEditor.title")
    String actionExpandEditorTitle();

    @Key("askWindow.close.title")
    String askWindowCloseTitle();

    @Key("action.completions.title")
    String actionCompetitionsTitle();

    /* Preferences widget */
    @Key("unable.to.save.preference")
    String unableToSavePreference();

    @Key("unable.to.load.preference")
    String unableToLoadPreference();

    @Key("create.ws.title")
    String createWsTitle();

    @Key("create.ws.recipe.url")
    String createWsRecipeUrl();

    @Key("create.ws.find.by.tags")
    String createWsFindByTags();

    @Key("create.ws.name")
    String createWsName();

    @Key("create.ws.url.not.valid")
    String createWsUrlNotValid();

    @Key("create.ws.recipe.not.found")
    String createWsRecipeNotFound();

    @Key("create.ws.button")
    String createWsButton();

    @Key("create.ws.default.name")
    String createWsDefaultName();

    @Key("create.ws.name.is.not.correct")
    String createWsNameIsNotCorrect();

    @Key("create.ws.predefined.recipe")
    String createWsPredefinedRecipe();

    @Key("placeholder.input.recipe.url")
    String placeholderInputRecipeUrl();

    @Key("placeholder.choose.predefined")
    String placeholderChoosePredefined();

    @Key("placeholder.find.by.tags")
    String placeholderFindByTags();

    @Key("start.ws.button")
    String startWsButton();

    @Key("placeholder.select.ws.to.start")
    String placeholderSelectWsToStart();

    @Key("start.ws.title")
    String startWsTitle();

    @Key("start.ws.select.to.start")
    String startWsSelectToStart();

    @Key("stop.ws.title")
    String stopWsTitle();

    @Key("stop.ws.description")
    String stopWsDescription();

    @Key("started.ws")
    String startedWs();

    @Key("create.snapshot.title")
    String createSnapshotTitle();

    @Key("create.snapshot.description")
    String createSnapshotDescription();

    @Key("create.snapshot.progress")
    String createSnapshotProgress();

    @Key("create.snapshot.success")
    String createSnapshotSuccess();

    @Key("create.snapshot.failed")
    String createSnapshotFailed();

    @Key("ext.server.started")
    String extServerStarted();

    @Key("ext.server.stopped")
    String extServerStopped();

    @Key("workspace.recovering.dialog.title")
    String workspaceRecoveringDialogTitle();

    @Key("workspace.recovering.dialog.text")
    String workspaceRecoveringDialogText();

    @Key("workspace.restore.snapshot")
    String restoreWorkspaceFromSnapshot();

    @Key("workspace.subscribe.on.events.failed")
    String workspaceSubscribeOnEventsFailed();

    @Key("workspace.start.failed")
    String workspaceStartFailed();

    @Key("failed.to.load.factory")
    String failedToLoadFactory();

    @Key("workspace.config.undefined")
    String workspaceConfigUndefined();

    @Key("workspace.id.undefined")
    String workspaceIdUndefined();

    @Key("workspace.get.failed")
    String workspaceGetFailed();

    @Key("workspace.not.ready")
    String workspaceNotReady(String workspaceId);

    @Key("workspace.not.running")
    String workspaceNotRunning();

    @Key("start.ws.error.title")
    String startWsErrorTitle();

    @Key("start.ws.error.content")
    String startWsErrorContent(String workspaceName, String reason);

    @Key("create.ws.name.length.is.not.correct")
    String createWsNameLengthIsNotCorrect();

    @Key("create.ws.name.already.exist")
    String createWsNameAlreadyExist();

    @Key("get.ws.error.dialog.title")
    String getWsErrorDialogTitle();

    @Key("get.ws.error.dialog.content")
    String getWsErrorDialogContent(String reason);

    @Key("project.explorer.project.configuration.failed")
    String projectExplorerProjectConfigurationFailed(String name);

    @Key("project.explorer.project.update.failed")
    String projectExplorerProjectUpdateFailed();

    @Key("project.explorer.projects.load.failed")
    String projectExplorerProjectsLoadFailed();

    @Key("project.explorer.invalid.project.detected")
    String projectExplorerInvalidProjectDetected();

    @Key("project.explorer.detected.unconfigured.project")
    String projectExplorerDetectedUnconfiguredProject();

    @Key("project.explorer.extension.server.stopped")
    String projectExplorerExtensionServerStopped();

    @Key("project.explorer.extension.server.stopped.description")
    String projectExplorerExtensionServerStoppedDescription();

    @Key("project.explorer.part.tooltip")
    String projectExplorerPartTooltip();

    @Key("switch.to.left.editor.action.description")
    String switchToLeftEditorActionDescription();

    @Key("switch.to.left.editor.action")
    String switchToLeftEditorAction();

    @Key("switch.to.right.editor.action.description")
    String switchToRightEditorActionDescription();

    @Key("switch.to.right.editor.action")
    String switchToRightEditorAction();

    @Key("key.bindings.action.name")
    String keyBindingsActionName();

    @Key("key.bindings.action.description")
    String keyBindingsActionDescription();

    @Key("key.bindings.dialog.title")
    String keyBindingsDialogTitle();

    @Key("hot.keys.table.action.description.title")
    String hotKeysTableActionDescriptionTitle();

    @Key("hot.keys.table.item.title")
    String hotKeysTableItemTitle();

    @Key("action.full.text.search")
    String actionFullTextSearch();

    @Key("action.full.text.search.description")
    String actionFullTextSearchDescription();

    @Key("text.search.wholeword.label")
    String textSearchFileWholeWordLabel();

    String search();

    @Key("text.search.scope.label")
    String textSearchScopeLabel();

    @Key("text.search.fileFilter.label")
    String textSearchFileFilterLabel();

    @Key("text.search.content.label")
    String textSearchContentLabel();

    @Key("text.search.title")
    String textSearchTitle();

    @Key("text.search.file.mask")
    String textSearchFileMask();

    @Key("text.search.directory")
    String textSearchDirectory();

    @Key("select.path.window.title")
    String selectPathWindowTitle();

    @Key("open.recent.file.title")
    String openRecentFileTitle();

    @Key("open.recent.file.description")
    String openRecentFileDescription();

    @Key("open.recent.files.title")
    String openRecentFilesTitle();

    @Key("open.recent.file.clear.title")
    String openRecentFileClearTitle();

    @Key("open.recent.file.clear.description")
    String openRecentFileClearDescription();

    @Key("editor.pane.menu.close.pane")
    String editorClosePane();

    @Key("editor.pane.menu.close.pane.description")
    String editorClosePaneDescription();

    @Key("editor.pane.menu.close.all.in.pane")
    String editorCloseAllTabsInPane();

    @Key("editor.pane.menu.close.all.in.pane.description")
    String editorCloseAllTabsInPaneDescription();

    @Key("editor.tab.context.menu.split.vertically")
    String editorTabSplitVertically();

    @Key("editor.tab.context.menu.split.vertically.description")
    String editorTabSplitVerticallyDescription();

    @Key("editor.tab.context.menu.split.horizontally")
    String editorTabSplitHorizontally();

    @Key("editor.tab.context.menu.split.horizontally.description")
    String editorTabSplitHorizontallyDescription();

    @Key("editor.tab.context.menu.close")
    String editorTabClose();

    @Key("editor.tab.context.menu.close.description")
    String editorTabCloseDescription();

    @Key("editor.tab.context.menu.close.all")
    String editorTabCloseAll();

    @Key("editor.tab.context.menu.close.all.description")
    String editorTabCloseAllDescription();

    @Key("editor.tab.context.menu.close.all.but.pinned")
    String editorTabCloseAllButPinned();

    @Key("editor.tab.context.menu.close.all.but.pinned.description")
    String editorTabCloseAllButPinnedDescription();

    @Key("editor.tab.context.menu.close.all.except.selected")
    String editorTabCloseAllExceptSelected();

    @Key("editor.tab.context.menu.close.all.except.selected.description")
    String editorTabCloseAllExceptSelectedDescription();

    @Key("editor.tab.context.menu.pin")
    String editorTabPin();

    @Key("editor.tab.context.menu.pin.description")
    String editorTabPinDescription();

    @Key("editor.tab.context.menu.reopen.closed.tab")
    String editorTabReopenClosedTab();

    @Key("editor.tab.context.menu.reopen.closed.tab.description")
    String editorTabReopenClosedTabDescription();

    @Key("failed.to.copy.items")
    String failedToCopyItems();

    @Key("failed.to.move.items")
    String failedToMoveItems();

    @Key("failed.to.update.project")
    String failedToUpdateProject(String name);

    @Key("failed.to.import.project")
    String failedToImportProject();

    @Key("failed.to.upload.files")
    String failedToUploadFiles();

    @Key("failed.to.upload.files.from.zip")
    String failedToUploadFilesFromZip();

    @Key("synchronize.dialog.title")
    String synchronizeDialogTitle();

    @Key("button.import")
    String buttonImport();

    @Key("button.remove")
    String buttonRemove();

    @Key("project.removed")
    String projectRemoved(String projectName);

    @Key("project.remove.error")
    String projectRemoveError(String projectName);

    @Key("location.incorrect")
    String locationIncorrect();

    @Key("exist.in.workspace.dialog.content")
    String existInWorkspaceDialogContent(String projectName);

    @Key("exist.in.file.system.dialog.content")
    String existInFileSystemDialogContent(String projectName);

    @Key("button.configure")
    String buttonConfigure();

    @Key("location.dialog.title")
    String locationDialogTitle();

    @Key("project.configuration.changed")
    String projectConfigurationChanged();

    @Key("button.keep.blank")
    String buttonKeepBlank();

    @Key("project.update.error")
    String projectUpdateError(String projectName);

    @Key("project.status.title")
    String projectStatusTitle();

    @Key("project.status.content")
    String projectStatusContent(String projectName);

    @Key("error.configuration.title")
    String errorConfigurationTitle();

    @Key("error.configuration.content")
    String errorConfigurationContent();

    @Key("show.reference")
    String showReference();

    @Key("reference.label")
    String referenceLabel();

    @Key("path.label")
    String pathLabel();

    @Key("parent.directory")
    String projectRoot();

    @Key("action.convert.folder.to.project")
    String actionConvertFolderToProject();

    @Key("action.convert.folder.to.project.description")
    String actionConvertFolderToProjectDescription();

    @Key("signature.name")
    String signatureName();

    @Key("signature.description")
    String signatureDescription();


    @Key("macro.editor.current.project.name.description")
    String macroEditorCurrentProjectNameDescription();

    @Key("macro.editor.current.project.type.description")
    String macroEditorCurrentProjectTypeDescription();

    @Key("macro.editor.current.file.relpath.description")
    String macroEditorCurrentFileRelpathDescription();

    @Key("macro.editor.current.file.name.description")
    String macroEditorCurrentFileNameDescription();

    @Key("macro.editor.current.file.path.description")
    String macroEditorCurrentFilePathDescription();

    @Key("macro.workspace.name.description")
    String macroWorkspaceNameDescription();

    @Key("macro.explorer.current.file.name.description")
    String macroExplorerCurrentFileNameDescription();

    @Key("macro.explorer.current.project.name.description")
    String macroExplorerCurrentProjectNameDescription();

    @Key("macro.explorer.current.file.relpath.description")
    String macroExplorerCurrentFileRelpathDescription();

    @Key("macro.explorer.current.project.type.description")
    String macroExplorerCurrentProjectTypeDescription();

    @Key("macro.explorer.current.file.parent.path.description")
    String macroExplorerCurrentFileParentPathDescription();

    @Key("macro.explorer.current.file.path.description")
    String macroExplorerCurrentFilePathDescription();

    @Key("empty.state.no.projects")
    String emptyStateNoProjects();

    @Key("empty.state.no.files")
    String emptyStateNoFiles();
}

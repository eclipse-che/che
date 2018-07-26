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

  @Key("action.revealResource.text")
  String actionRevealResourceText();

  @Key("action.revealResource.description")
  String actionRevealResourceDescription();

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

  @Key("general.title")
  String generalTitle();

  @Key("general.category")
  String generalCategory();

  /* Add resources to File Watcher excludes */
  @Key("action.fileWatcher.add.excludes.text")
  String addToFileWatcherExludesName();

  @Key("action.fileWatcher.add.excludes.description")
  String addToFileWatcherExludesDescription();

  /* Remove resources from File Watcher excludes */
  @Key("action.fileWatcher.remove.excludes.text")
  String removeFromFileWatcherExludesName();

  @Key("action.fileWatcher.remove.excludes.description")
  String removeFromFileWatcherExludesDescription();

  /* DeleteItem */
  @Key("action.delete.text")
  String deleteItemActionText();

  @Key("action.delete.description")
  String deleteItemActionDescription();

  @Key("action.goInto.text")
  String goIntoActionText();

  @Key("action.goBack.text")
  String goBackActionText();

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

  @Key("action.switch.editor.displaying.title")
  String switchEditorDisplayingTitle();

  @Key("action.switch.editor.displaying.description")
  String switchEditorDisplayingDescription();

  @Key("action.switch.project.explorer.displaying.title")
  String switchProjectExplorerDisplayingTitle();

  @Key("action.switch.project.explorer.displaying.description")
  String switchProjectExplorerDisplayingDescription();

  @Key("action.switch.command.explorer.displaying.title")
  String switchCommandExplorerDisplayingTitle();

  @Key("action.switch.command.explorer.displaying.description")
  String switchCommandExplorerDisplayingDescription();

  @Key("action.switch.find.part.displaying.title")
  String switchFindPartDisplayingTitle();

  @Key("action.switch.find.part.displaying.description")
  String switchFindPartDisplayingDescription();

  @Key("action.switch.event.logs.displaying.title")
  String switchEventLogsDisplayingTitle();

  @Key("action.switch.event.logs.displaying.description")
  String switchEventLogsDisplayingDescription();

  @Key("action.switch.terminal.displaying.title")
  String switchTerminalDisplayingTitle();

  @Key("action.switch.terminal.displaying.description")
  String switchTerminalDisplayingDescription();

  /* RenameItem */
  @Key("action.rename.text")
  String renameItemActionText();

  @Key("action.rename.description")
  String renameItemActionDescription();

  @Key("action.collapseAll.title")
  String collapseAllActionTitle();

  @Key("action.collapseAll.description")
  String collapseAllActionDescription();

  @Key("action.refresh.title")
  String refreshActionTitle();

  @Key("action.refresh.description")
  String refreshActionDescription();

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

  @Key("softWrap")
  String softWrap();

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

  @Key("projectExplorer.linkWithEditor.tooltip")
  String projectExplorerLinkWithEditorTooltip();

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
  String importingProject(String projectName);

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

  @Key("action.show.toolbar")
  String actionShowToolbar();

  @Key("action.projectConfiguration.description")
  String actionProjectConfigurationDescription();

  @Key("action.projectConfiguration.title")
  String actionProjectConfigurationTitle();

  @Key("action.previewImage.description")
  String actionPreviewImageDescription();

  @Key("action.previewImage.title")
  String actionPreviewImageTitle();

  @Key("action.findAction.description")
  String actionFindActionDescription();

  @Key("action.findAction.title")
  String actionFindActionTitle();

  @Key("action.showHiddenFiles.title")
  String actionShowHiddenFilesTitle();

  @Key("action.showHiddenFiles.description")
  String actionShowHiddenFilesDescription();

  @Key("action.maximizePart.title")
  String actionMaximizePartTitle();

  @Key("action.maximizePart.description")
  String actionMaximizePartDescription();

  @Key("action.restorePart.title")
  String actionRestorePartTitle();

  @Key("action.restorePart.description")
  String actionRestorePartDescription();

  @Key("action.hidePart.title")
  String actionHidePartTitle();

  @Key("action.hidePart.description")
  String actionHidePartDescription();

  @Key("maximizePartStack.title")
  String maximizePartStackTitle();

  @Key("restorePartStack.title")
  String restorePartStackTitle();

  @Key("minimizePartStack.title")
  String minimizePartStackTitle();

  @Key("partStackOptions.title")
  String partStackOptionsTitle();

  /* NewResource */
  @Key("newResource.title")
  String newResourceTitle(String title);

  @Key("newResource.label")
  String newResourceLabel(String title);

  @Key("newResource.invalidName")
  String invalidName();

  /* Messages */
  @Key("messages.closeTabConfirmation")
  String closeTabConfirmation();

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

  @Key("messages.canNotOpenFileInSplitMode")
  String canNotOpenFileInSplitMode(String path);

  @Key("messages.canNotOpenFileWithoutParams")
  String canNotOpenFileWithoutParams();

  @Key("messages.fileToOpenIsNotSpecified")
  String fileToOpenIsNotSpecified();

  @Key("messages.fileToOpenLineIsNotANumber")
  String fileToOpenLineIsNotANumber();

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

  @Key("restart.ws.agent.button")
  String restartWsAgentButton();

  @Key("ignore.ws.agent.button")
  String ignoreWsAgentButton();

  @Key("placeholder.select.ws.to.start")
  String placeholderSelectWsToStart();

  @Key("start.ws.title")
  String startWsTitle();

  @Key("start.ws.description")
  String startWsDescription();

  @Key("stop.ws.title")
  String stopWsTitle();

  @Key("stop.ws.description")
  String stopWsDescription();

  @Key("started.ws")
  String startedWs();

  @Key("ext.server.started")
  String extServerStarted();

  @Key("ext.server.stopped")
  String extServerStopped();

  @Key("workspace.recovering.dialog.title")
  String workspaceRecoveringDialogTitle();

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

  @Key("workspace.status.title")
  String workspaceStatusTitle();

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

  @Key("macro.editor.current.file.base.name.description")
  String macroEditorCurrentFileBaseNameDescription();

  @Key("macro.editor.current.file.path.description")
  String macroEditorCurrentFilePathDescription();

  @Key("macro.workspace.name.description")
  String macroWorkspaceNameDescription();

  @Key("macro.workspace.namespace.description")
  String macroWorkspaceNamespaceDescription();

  @Key("macro.explorer.current.file.name.description")
  String macroExplorerCurrentFileNameDescription();

  @Key("macro.explorer.current.file.base.name.description")
  String macroExplorerCurrentFileBaseNameDescription();

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

  @Key("authentication.dialog.title")
  String authenticationDialogTitle();

  @Key("authentication.dialog.username")
  String authenticationDialogUsername();

  @Key("authentication.dialog.password")
  String authenticationDialogPassword();

  @Key("authentication.dialog.authenticate.button")
  String authenticationDialogAuthenticate();

  @Key("authentication.dialog.rejected.by.user")
  String authenticationDialogRejectedByUser();

  /* Factories */
  @Key("projects.import.configuring.cloning")
  String cloningSource();

  @Key("create.factory.action.title")
  String createFactoryActionTitle();

  @Key("create.factory.already.exist")
  String createFactoryAlreadyExist();

  @Key("create.factory.unable.create.from.current.workspace")
  String createFactoryFromCurrentWorkspaceFailed();

  @Key("create.factory.form.title")
  String createFactoryTitle();

  @Key("create.factory.label.name")
  String createFactoryName();

  @Key("create.factory.label.link")
  String createFactoryLink();

  @Key("create.factory.button.create")
  String createFactoryButton();

  @Key("create.factory.button.close")
  String createFactoryButtonClose();

  @Key("create.factory.configure.button.tooltip")
  String createFactoryConfigureTooltip();

  @Key("create.factory.launch.button.tooltip")
  String createFactoryLaunchTooltip();

  @Key("import.config.view.name")
  String importFromConfigurationName();

  @Key("import.config.view.description")
  String importFromConfigurationDescription();

  @Key("project.import.configured.cloned")
  String clonedSource(String projectName);

  @Key("import.config.form.button.import")
  String importButton();

  @Key("import.config.view.title")
  String importFromConfigurationTitle();

  @Key("import.config.form.prompt")
  String configFileTitle();

  @Key("project.already.imported")
  String projectAlreadyImported(String projectName);

  @Key("project.import.cloned.with.checkout")
  String clonedSourceWithCheckout(String projectName, String repoName, String ref, String branch);

  @Key("project.import.cloned.with.checkout.start.point")
  String clonedWithCheckoutOnStartPoint(
      String projectName, String repoName, String startPoint, String branch);

  @Key("project.import.configuring.cloning")
  String cloningSource(String projectName);

  @Key("project.import.ssh.key.upload.failed.title")
  String cloningSourceSshKeyUploadFailedTitle();

  @Key("project.import.ssh.key.upload.failed.text")
  String cloningSourcesSshKeyUploadFailedText();

  @Key("message.ssh.key.not.found.text")
  String acceptSshNotFoundText();

  @Key("project.import.cloning.failed.without.start.point")
  String cloningSourceWithCheckoutFailed(String branch, String repoName);

  @Key("project.import.cloning.failed.with.start.point")
  String cloningSourceCheckoutFailed(String project, String branch);

  @Key("project.import.cloning.failed.title")
  String cloningSourceFailedTitle(String projectName);

  @Key("project.import.configuring.failed")
  String configuringSourceFailed(String projectName);

  @Key("welcome.preferences.title")
  String welcomePreferencesTitle();

  @Key("export.config.view.name")
  String exportConfigName();

  @Key("export.config.view.description")
  String exportConfigDescription();

  @Key("export.config.error.message")
  String exportConfigErrorMessage();

  @Key("export.config.dialog.not.under.vcs.title")
  String exportConfigDialogNotUnderVcsTitle();

  @Key("export.config.dialog.not.under.vcs.text")
  String exportConfigDialogNotUnderVcsText();

  @Key("macro.current.project.path.description")
  String macroCurrentProjectPathDescription();

  @Key("macro.current.project.eldest.parent.path.description")
  String macroCurrentProjectEldestParentPathDescription();

  @Key("macro.current.project.relpath.description")
  String macroCurrentProjectRelpathDescription();

  @Key("macro.machine.dev.hostname.description")
  String macroMachineDevHostnameDescription();

  @Key("failed.to.connect.the.terminal")
  String failedToConnectTheTerminal();

  @Key("connection.failed.with.terminal")
  String connectionFailedWithTerminal();

  @Key("terminal.error.connection")
  String terminalErrorConnection();

  @Key("terminal.can.not.load.script")
  String terminalCanNotLoadScript();

  @Key("terminal.restart.trying")
  String terminalTryRestarting();

  @Key("terminal.error.start")
  String terminalErrorStart();

  @Key("view.processes.title")
  String viewProcessesTitle();

  @Key("view.processes.tooltip")
  String viewProcessesTooltip();

  @Key("messages.machine.not.found")
  String machineNotFound(String machineId);

  @Key("message.projectCreated")
  String projectCreated(String projectName);

  @Key("message.switch.editors.in.readOnly.mode")
  String messageSwitchEditorsInReadOnlyMode();

  @Key("ssh.connect.info")
  String sshConnectInfo(
      String machineName,
      String machineHost,
      String machinePort,
      String workspaceName,
      String userName,
      String sshKeyDetails);

  @Key("ssh.connect.ssh.key.available")
  String sshConnectInfoPrivateKey(String privateKey);

  @Key("ssh.connect.ssh.key.not.available")
  String sshConnectInfoNoPrivateKey();

  @Key("failed.to.execute.command")
  String failedToExecuteCommand();

  /* OutputsContainerView */
  @Key("view.outputsConsole.stopProcessConfirmation")
  String outputsConsoleViewStopProcessConfirmation(String processName);

  @Key("view.processes.terminal.node.title")
  String viewProcessesTerminalNodeTitle(String terminalIndex);

  @Key("failed.to.get.processes")
  String failedToGetProcesses(String machineId);

  @Key("control.runCommand.empty.params")
  String runCommandEmptyParamsMessage();

  @Key("control.runCommand.empty.name")
  String runCommandEmptyNameMessage();

  @Key("control.terminal.new")
  String newTerminal();

  @Key("control.terminal.create.description")
  String newTerminalDescription();

  @Key("control.open.in.terminal")
  @DefaultMessage("Open in Terminal")
  String openInTerminalAction();

  @Key("machine.output.action.title")
  String machineOutputActionTitle();

  @Key("machine.output.action.description")
  String machineOutputActionDescription();

  @Key("machine.ssh.action.title")
  String machineSSHActionTitle();

  @Key("machine.ssh.action.description")
  String machineSSHActionDescription();

  @Key("control.connect.ssh")
  String connectViaSSH();

  @Key("action.showConsoleTree.title")
  String actionShowConsoleTreeTitle();

  @Key("control.rerun.title")
  String reRunControlTitle();

  @Key("control.rerun.description")
  String reRunControlDescription();

  @Key("control.stop.title")
  String stopControlTitle();

  @Key("control.stop.description")
  String stopControlDescription();

  @Key("control.close.title")
  String closeControlTitle();

  @Key("control.close.description")
  String closeControlDescription();

  @Key("consoles.reRunButton.tooltip")
  String consolesReRunButtonTooltip();

  @Key("consoles.stopButton.tooltip")
  String consolesStopButtonTooltip();

  @Key("consoles.clearOutputsButton.tooltip")
  String consolesClearOutputsButtonTooltip();

  @Key("consoles.wrapTextButton.tooltip")
  String consolesWrapTextButtonTooltip();

  @Key("consoles.autoScrollButton.tooltip")
  String consolesAutoScrollButtonTooltip();

  @Key("view.processes.command.title")
  String viewProcessesCommandTitle();

  @Key("view.stop.process.tooltip")
  String viewStropProcessTooltip();

  @Key("view.new.terminal.tooltip")
  String viewNewTerminalTooltip();

  @Key("view.machine.running.tooltip")
  String viewMachineRunningTooltip();

  @Key("view.close.processOutput.tooltip")
  String viewCloseProcessOutputTooltip();

  @Key("failed.to.find.machine")
  String failedToFindMachine(String machineId);

  @Key("menu.loader.machineStarting")
  String menuLoaderMachineStarting(String machine);

  @Key("menu.loader.machineRunning")
  String menuLoaderMachineRunning(String machine);

  @Key("menu.loader.workspaceStarted")
  String menuLoaderWorkspaceStarted();

  @Key("menu.loader.workspaceStopping")
  String menuLoaderWorkspaceStopping();

  @Key("menu.loader.workspaceStopped")
  String menuLoaderWorkspaceStopped();

  @Key("menu.loader.waitingWorkspace")
  String menuLoaderWaitingWorkspace();

  @Key("menu.loader.pullingImage")
  String menuLoaderPullingImage(String image);

  @Key("gwt_recompile.action.setup.title")
  String gwtDevModeSetUpActionTitle();

  @Key("gwt_recompile.action.off.title")
  String gwtDevModeOffActionTitle();

  @Key("gwt_recompile.dialog.title")
  String gwtRecompileDialogTitle();

  @Key("gwt_recompile.dialog.message.recompiling")
  String gwtRecompileDialogRecompilingMessage(String host);

  @Key("gwt_recompile.dialog.message.no_server")
  String gwtRecompileDialogNoServerMessage();

  @Key("resource.copy.move.error.title")
  String resourceCopyMoveErrorTitle();

  @Key("resource.copy.move.same.path.error.message")
  String resourceCopyMoveSamePathErrorMessage(String sourceName);

  @Key("resource.copy.move.already.exist.error.message")
  String resourceCopyMoveAlreadyExistErrorMessage(String sourceName, String path);

  @Key("low.disk.space")
  String lowDiskSpace();

  @Key("low.disk.space.description")
  String lowDiskSpaceDescription();

  @Key("low.disk.space.status.bar.message")
  String lowDiskSpaceStatusBarMessage();
}

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
package org.eclipse.che.ide.core;

import com.google.gwt.resources.client.ClientBundle;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.actions.CloseCurrentFile;
import org.eclipse.che.ide.actions.CollapseAllAction;
import org.eclipse.che.ide.actions.CompleteAction;
import org.eclipse.che.ide.actions.CopyAction;
import org.eclipse.che.ide.actions.CreateModuleAction;
import org.eclipse.che.ide.actions.CreateProjectAction;
import org.eclipse.che.ide.actions.CutAction;
import org.eclipse.che.ide.actions.DeleteItemAction;
import org.eclipse.che.ide.actions.DownloadAsZipAction;
import org.eclipse.che.ide.actions.DownloadItemAction;
import org.eclipse.che.ide.actions.ExpandEditorAction;
import org.eclipse.che.ide.actions.ExpandNodeAction;
import org.eclipse.che.ide.actions.FoldersAlwaysOnTopAction;
import org.eclipse.che.ide.actions.FormatterAction;
import org.eclipse.che.ide.actions.FullTextSearchAction;
import org.eclipse.che.ide.actions.GoIntoAction;
import org.eclipse.che.ide.actions.HotKeysListAction;
import org.eclipse.che.ide.actions.ImportLocalProjectAction;
import org.eclipse.che.ide.actions.ImportProjectAction;
import org.eclipse.che.ide.actions.LoaderAction;
import org.eclipse.che.ide.actions.NavigateToFileAction;
import org.eclipse.che.ide.actions.OpenFileAction;
import org.eclipse.che.ide.actions.OpenSelectedFileAction;
import org.eclipse.che.ide.actions.PasteAction;
import org.eclipse.che.ide.actions.ProjectConfigurationAction;
import org.eclipse.che.ide.actions.RedoAction;
import org.eclipse.che.ide.actions.RenameItemAction;
import org.eclipse.che.ide.actions.SaveAction;
import org.eclipse.che.ide.actions.SaveAllAction;
import org.eclipse.che.ide.actions.SettingsAction;
import org.eclipse.che.ide.actions.ShowHiddenFilesAction;
import org.eclipse.che.ide.actions.ShowPreferencesAction;
import org.eclipse.che.ide.actions.SwitchLeftTabAction;
import org.eclipse.che.ide.actions.SwitchRightTabAction;
import org.eclipse.che.ide.actions.UndoAction;
import org.eclipse.che.ide.actions.UploadFileAction;
import org.eclipse.che.ide.actions.UploadFolderAction;
import org.eclipse.che.ide.actions.find.FindActionAction;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.connection.WsConnectionListener;
import org.eclipse.che.ide.imageviewer.ImageViewerProvider;
import org.eclipse.che.ide.newresource.NewFileAction;
import org.eclipse.che.ide.newresource.NewFolderAction;
import org.eclipse.che.ide.part.editor.actions.CloseAction;
import org.eclipse.che.ide.part.editor.actions.CloseAllAction;
import org.eclipse.che.ide.part.editor.actions.CloseAllExceptPinnedAction;
import org.eclipse.che.ide.part.editor.actions.CloseOtherAction;
import org.eclipse.che.ide.part.editor.actions.PinEditorTabAction;
import org.eclipse.che.ide.part.editor.actions.ReopenClosedFileAction;
import org.eclipse.che.ide.part.editor.recent.OpenRecentFilesAction;
import org.eclipse.che.ide.ui.loaders.request.MessageLoaderResources;
import org.eclipse.che.ide.ui.toolbar.MainToolbar;
import org.eclipse.che.ide.ui.toolbar.ToolbarPresenter;
import org.eclipse.che.ide.util.browser.UserAgent;
import org.eclipse.che.ide.util.input.KeyCodeMap;
import org.eclipse.che.ide.xml.NewXmlFileAction;
import org.vectomatic.dom.svg.ui.SVGResource;

import static org.eclipse.che.ide.api.action.IdeActions.GROUP_FILE_NEW;
import static org.eclipse.che.ide.api.constraints.Constraints.FIRST;
import static org.eclipse.che.ide.projecttype.BlankProjectWizardRegistrar.BLANK_CATEGORY;

/**
 * Initializer for standard components i.e. some basic menu commands (Save, Save As etc)
 *
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 */
@Singleton
public class StandardComponentInitializer {
    public interface ParserResource extends ClientBundle {
        @Source("org/eclipse/che/ide/blank.svg")
        SVGResource samplesCategoryBlank();
    }

    @Inject
    private EditorRegistry editorRegistry;

    @Inject
    private FileTypeRegistry fileTypeRegistry;

    @Inject
    private Resources resources;

    @Inject
    private KeyBindingAgent keyBinding;

    @Inject
    private ActionManager actionManager;

    @Inject
    private SaveAction saveAction;

    @Inject
    private SaveAllAction saveAllAction;

    @Inject
    private ShowPreferencesAction showPreferencesAction;

    @Inject
    private SettingsAction settingsAction;

    @Inject
    private FindActionAction findActionAction;

//    @Inject
//    private FindReplaceAction findReplaceAction;

    @Inject
    private NavigateToFileAction navigateToFileAction;

    @Inject
    @MainToolbar
    private ToolbarPresenter toolbarPresenter;

    @Inject
    private CutAction cutAction;

    @Inject
    private CopyAction copyAction;

    @Inject
    private PasteAction pasteAction;

    @Inject
    private DeleteItemAction deleteItemAction;

    @Inject
    private RenameItemAction renameItemAction;

    @Inject
    private CollapseAllAction collapseAllAction;

    @Inject
    private FoldersAlwaysOnTopAction foldersAlwaysOnTopAction;

    @Inject
    private CloseAction closeAction;

    @Inject
    private CloseAllAction closeAllAction;

    @Inject
    private CloseOtherAction closeOtherAction;

    @Inject
    private CloseAllExceptPinnedAction closeAllExceptPinnedAction;

    @Inject
    private ReopenClosedFileAction reopenClosedFileAction;

    @Inject
    private PinEditorTabAction pinEditorTabAction;

    @Inject
    private GoIntoAction goIntoAction;

    @Inject
    private ExpandNodeAction expandNodeAction;

    @Inject
    private OpenSelectedFileAction openSelectedFileAction;

    @Inject
    private OpenFileAction openFileAction;

    @Inject
    private ShowHiddenFilesAction showHiddenFilesAction;

    @Inject
    private FormatterAction formatterAction;

    @Inject
    private UndoAction undoAction;

    @Inject
    private RedoAction redoAction;

    @Inject
    private UploadFileAction uploadFileAction;

    @Inject
    private UploadFolderAction uploadFolderAction;

    @Inject
    private DownloadAsZipAction downloadAsZipAction;

    @Inject
    private DownloadItemAction downloadItemAction;

    @Inject
    private ImportProjectAction importProjectAction;

    @Inject
    private ImportLocalProjectAction importLocalProjectAction;

    @Inject
    private CreateProjectAction createProjectAction;

    @Inject
    private CreateModuleAction createModuleAction;

    @Inject
    private FullTextSearchAction fullTextSearchAction;

    @Inject
    private NewFolderAction newFolderAction;

    @Inject
    private NewFileAction newFileAction;

    @Inject
    private NewXmlFileAction newXmlFileAction;

    @Inject
    private ImageViewerProvider imageViewerProvider;

    @Inject
    private ProjectConfigurationAction projectConfigurationAction;

    @Inject
    private ExpandEditorAction expandEditorAction;

    @Inject
    private CompleteAction completeAction;

    @Inject
    private SwitchLeftTabAction switchLeftTabAction;

    @Inject
    private SwitchRightTabAction switchRightTabAction;

    @Inject
    private LoaderAction loaderAction;

    @Inject
    private HotKeysListAction hotKeysListAction;

    @Inject
    private OpenRecentFilesAction openRecentFilesAction;

    @Inject
    private CloseCurrentFile closeCurrentFile;

    @Inject
    private MessageLoaderResources messageLoaderResources;

    @Inject
    @Named("XMLFileType")
    private FileType xmlFile;

    @Inject
    @Named("TXTFileType")
    private FileType txtFile;

    @Inject
    @Named("JsonFileType")
    private FileType jsonFile;

    @Inject
    @Named("MDFileType")
    private FileType mdFile;

    @Inject
    @Named("PNGFileType")
    private FileType pngFile;

    @Inject
    @Named("BMPFileType")
    private FileType bmpFile;

    @Inject
    @Named("GIFFileType")
    private FileType gifFile;

    @Inject
    @Named("ICOFileType")
    private FileType iconFile;

    @Inject
    @Named("SVGFileType")
    private FileType svgFile;

    @Inject
    @Named("JPEFileType")
    private FileType jpeFile;

    @Inject
    @Named("JPEGFileType")
    private FileType jpegFile;

    @Inject
    @Named("JPGFileType")
    private FileType jpgFile;

    @Inject
    private WsConnectionListener wsConnectionListener;

    /** Instantiates {@link StandardComponentInitializer} an creates standard content. */
    @Inject
    public StandardComponentInitializer(IconRegistry iconRegistry, StandardComponentInitializer.ParserResource parserResource) {
        iconRegistry.registerIcon(new Icon(BLANK_CATEGORY + ".samples.category.icon", parserResource.samplesCategoryBlank()));
    }

    public void initialize() {
        //initialize loader resources
        messageLoaderResources.Css().ensureInjected();

        fileTypeRegistry.registerFileType(xmlFile);

        fileTypeRegistry.registerFileType(txtFile);

        fileTypeRegistry.registerFileType(jsonFile);

        fileTypeRegistry.registerFileType(mdFile);

        fileTypeRegistry.registerFileType(pngFile);
        editorRegistry.registerDefaultEditor(pngFile, imageViewerProvider);

        fileTypeRegistry.registerFileType(bmpFile);
        editorRegistry.registerDefaultEditor(bmpFile, imageViewerProvider);

        fileTypeRegistry.registerFileType(gifFile);
        editorRegistry.registerDefaultEditor(gifFile, imageViewerProvider);

        fileTypeRegistry.registerFileType(iconFile);
        editorRegistry.registerDefaultEditor(iconFile, imageViewerProvider);

        fileTypeRegistry.registerFileType(svgFile);
        editorRegistry.registerDefaultEditor(svgFile, imageViewerProvider);

        fileTypeRegistry.registerFileType(jpeFile);
        editorRegistry.registerDefaultEditor(jpeFile, imageViewerProvider);

        fileTypeRegistry.registerFileType(jpegFile);
        editorRegistry.registerDefaultEditor(jpegFile, imageViewerProvider);

        fileTypeRegistry.registerFileType(jpgFile);
        editorRegistry.registerDefaultEditor(jpgFile, imageViewerProvider);

        // Workspace (New Menu)
        DefaultActionGroup workspaceGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_WORKSPACE);

        actionManager.registerAction("importProject", importProjectAction);
        workspaceGroup.add(importProjectAction);

        actionManager.registerAction("createProject", createProjectAction);
        workspaceGroup.add(createProjectAction);

        actionManager.registerAction("downloadAsZipAction", downloadAsZipAction);
        workspaceGroup.add(downloadAsZipAction);

        workspaceGroup.addSeparator();

        // Project (New Menu)
        DefaultActionGroup projectGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_PROJECT);

        DefaultActionGroup newGroup = new DefaultActionGroup("New", true, actionManager);
        newGroup.getTemplatePresentation().setDescription("Create...");
        newGroup.getTemplatePresentation().setSVGResource(resources.newResource());
        actionManager.registerAction(GROUP_FILE_NEW, newGroup);
        projectGroup.add(newGroup);

        newGroup.addSeparator();

        actionManager.registerAction("newFile", newFileAction);
        newGroup.addAction(newFileAction);

        actionManager.registerAction("newFolder", newFolderAction);
        newGroup.addAction(newFolderAction);

        newGroup.addSeparator();

        actionManager.registerAction("newXmlFile", newXmlFileAction);
        newXmlFileAction.getTemplatePresentation().setSVGResource(xmlFile.getSVGImage());
        newGroup.addAction(newXmlFileAction);

        actionManager.registerAction("createModuleAction", createModuleAction);
        projectGroup.addAction(createModuleAction);

        actionManager.registerAction("uploadFile", uploadFileAction);
        projectGroup.add(uploadFileAction);

        actionManager.registerAction("uploadFolder", uploadFolderAction);
        projectGroup.add(uploadFolderAction);

        projectGroup.add(downloadAsZipAction);

        actionManager.registerAction("showHideHiddenFiles", showHiddenFilesAction);
        projectGroup.add(showHiddenFilesAction);

        projectGroup.addSeparator();

        actionManager.registerAction("projectConfiguration", projectConfigurationAction);
        projectGroup.add(projectConfigurationAction);

        // Edit (New Menu)
        DefaultActionGroup editGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_EDIT);

        actionManager.registerAction("openRecentFiles", openRecentFilesAction);
        editGroup.add(openRecentFilesAction);

        editGroup.addSeparator();

        actionManager.registerAction("closeCurrentFile", closeCurrentFile);
        editGroup.add(closeCurrentFile);

        actionManager.registerAction("format", formatterAction);
        editGroup.add(formatterAction);

        actionManager.registerAction("undo", undoAction);
        editGroup.add(undoAction);

        actionManager.registerAction("redo", redoAction);
        editGroup.add(redoAction);

        actionManager.registerAction("cut", cutAction);
        editGroup.add(cutAction);

        actionManager.registerAction("copy", copyAction);
        editGroup.add(copyAction);

        actionManager.registerAction("paste", pasteAction);
        editGroup.add(pasteAction);

        actionManager.registerAction("renameResource", renameItemAction);
        editGroup.add(renameItemAction);

        actionManager.registerAction("deleteItem", deleteItemAction);
        editGroup.add(deleteItemAction);

        actionManager.registerAction("fullTextSearch", fullTextSearchAction);
        editGroup.add(fullTextSearchAction);

        // Assistant (New Menu)
        DefaultActionGroup assistantGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_ASSISTANT);

        actionManager.registerAction("findActionAction", findActionAction);
        assistantGroup.add(findActionAction);

        actionManager.registerAction("hotKeysList", hotKeysListAction);
        assistantGroup.add(hotKeysListAction);

        assistantGroup.addSeparator();

        actionManager.registerAction("callCompletion", completeAction);
        assistantGroup.add(completeAction);

        actionManager.registerAction("importLocalProjectAction", importLocalProjectAction);
        actionManager.registerAction("downloadItemAction", downloadItemAction);
        actionManager.registerAction("navigateToFile", navigateToFileAction);
        assistantGroup.add(navigateToFileAction);

        // Compose Save group
        DefaultActionGroup saveGroup = new DefaultActionGroup(actionManager);
        actionManager.registerAction("saveGroup", saveGroup);
        actionManager.registerAction("save", saveAction);
        actionManager.registerAction("saveAll", saveAllAction);
        saveGroup.addSeparator();
        saveGroup.add(saveAction);
        saveGroup.add(saveAllAction);

        // Compose Help menu
        DefaultActionGroup helpGroup = (DefaultActionGroup) actionManager.getAction(IdeActions.GROUP_HELP);
        helpGroup.addSeparator();

        actionManager.registerAction("showPreferences", showPreferencesAction);
        helpGroup.add(showPreferencesAction);

        actionManager.registerAction("setupProjectAction", settingsAction);
        helpGroup.add(settingsAction);

        // Compose main context menu
        DefaultActionGroup resourceOperation = new DefaultActionGroup(actionManager);
        actionManager.registerAction("resourceOperation", resourceOperation);
        resourceOperation.addSeparator();
        resourceOperation.add(goIntoAction);
        resourceOperation.add(openSelectedFileAction);

        resourceOperation.add(cutAction);
        resourceOperation.add(copyAction);
        resourceOperation.add(pasteAction);
        resourceOperation.add(renameItemAction);
        resourceOperation.add(deleteItemAction);
        resourceOperation.addSeparator();
        resourceOperation.add(downloadItemAction);
        resourceOperation.addSeparator();
        resourceOperation.add(createModuleAction);

        DefaultActionGroup mainContextMenuGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_MAIN_CONTEXT_MENU);
        mainContextMenuGroup.add(newGroup);
        mainContextMenuGroup.addSeparator();
        mainContextMenuGroup.add(resourceOperation);

        actionManager.registerAction("expandEditor", expandEditorAction);
        DefaultActionGroup rightMenuGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_RIGHT_MAIN_MENU);
        rightMenuGroup.add(expandEditorAction, FIRST);

        // Compose main toolbar
        DefaultActionGroup changeResourceGroup = new DefaultActionGroup(actionManager);
        actionManager.registerAction("changeResourceGroup", changeResourceGroup);
        actionManager.registerAction("openSelectedFile", openSelectedFileAction);

        actionManager.registerAction("collapseAll", collapseAllAction);

//        actionManager.registerAction("findReplace", findReplaceAction);
        actionManager.registerAction("openFile", openFileAction);
        actionManager.registerAction("expandNode", expandNodeAction);
        actionManager.registerAction("switchLeftTab", switchLeftTabAction);
        actionManager.registerAction("switchRightTab", switchRightTabAction);

        changeResourceGroup.add(cutAction);
        changeResourceGroup.add(copyAction);
        changeResourceGroup.add(pasteAction);
        changeResourceGroup.add(deleteItemAction);

        DefaultActionGroup mainToolbarGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_MAIN_TOOLBAR);
        mainToolbarGroup.add(newGroup);
        mainToolbarGroup.add(changeResourceGroup);
        toolbarPresenter.bindMainGroup(mainToolbarGroup);

        DefaultActionGroup centerToolbarGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_CENTER_TOOLBAR);
        toolbarPresenter.bindCenterGroup(centerToolbarGroup);

        DefaultActionGroup rightToolbarGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_RIGHT_TOOLBAR);
        toolbarPresenter.bindRightGroup(rightToolbarGroup);

        DefaultActionGroup projectExplorerContextMenu =
                (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_PROJECT_EXPLORER_CONTEXT_MENU);
        projectExplorerContextMenu.add(foldersAlwaysOnTopAction);
        actionManager.registerAction("foldersAlwaysOnTop", foldersAlwaysOnTopAction);

        //Editor context menu group
        DefaultActionGroup editorTabContextMenu =
                (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_EDITOR_TAB_CONTEXT_MENU);
        editorTabContextMenu.add(closeAction);
        actionManager.registerAction("closeEditor", closeAction);
        editorTabContextMenu.add(closeAllAction);
        actionManager.registerAction("closeAllEditors", closeAllAction);
        editorTabContextMenu.add(closeOtherAction);
        actionManager.registerAction("closeOtherEditorExceptCurrent", closeOtherAction);
        editorTabContextMenu.add(closeAllExceptPinnedAction);
        actionManager.registerAction("closeAllEditorExceptPinned", closeAllExceptPinnedAction);
        editorTabContextMenu.addSeparator();
        editorTabContextMenu.add(reopenClosedFileAction);
        actionManager.registerAction("reopenClosedEditorTab", reopenClosedFileAction);
        editorTabContextMenu.add(pinEditorTabAction);
        actionManager.registerAction("pinEditorTab", pinEditorTabAction);

        final DefaultActionGroup loaderToolbarGroup = new DefaultActionGroup("loader", false, actionManager);
        actionManager.registerAction("loader", loaderToolbarGroup);
        actionManager.registerAction("loaderAction", loaderAction);
        centerToolbarGroup.add(loaderToolbarGroup);
        loaderToolbarGroup.add(loaderAction);

        actionManager.registerAction("noOpAction", new NoOpAction());

        // Define hot-keys
        keyBinding.getGlobal().addKey(new KeyBuilder().action().alt().charCode('n').build(), "navigateToFile");
        keyBinding.getGlobal().addKey(new KeyBuilder().action().charCode('F').build(), "fullTextSearch");
        keyBinding.getGlobal().addKey(new KeyBuilder().action().charCode('A').build(), "findActionAction");
        keyBinding.getGlobal().addKey(new KeyBuilder().alt().charCode('L').build(), "format");
        keyBinding.getGlobal().addKey(new KeyBuilder().action().charCode('c').build(), "copy");
        keyBinding.getGlobal().addKey(new KeyBuilder().action().charCode('x').build(), "cut");
        keyBinding.getGlobal().addKey(new KeyBuilder().action().charCode('v').build(), "paste");
        keyBinding.getGlobal().addKey(new KeyBuilder().alt().charCode(KeyCodeMap.ARROW_LEFT).build(), "switchLeftTab");
        keyBinding.getGlobal().addKey(new KeyBuilder().alt().charCode(KeyCodeMap.ARROW_RIGHT).build(), "switchRightTab");
        keyBinding.getGlobal().addKey(new KeyBuilder().action().charCode('e').build(), "openRecentFiles");
        keyBinding.getGlobal().addKey(new KeyBuilder().action().charCode('s').build(), "noOpAction");

        if (UserAgent.isMac()) {
            keyBinding.getGlobal().addKey(new KeyBuilder().control().charCode('w').build(), "closeCurrentFile");
        } else {
            keyBinding.getGlobal().addKey(new KeyBuilder().alt().charCode('w').build(), "closeCurrentFile");
        }

    }

    /** Action that does nothing. It's just for disabling (catching) browser's hot key. */
    private class NoOpAction extends Action {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }
}

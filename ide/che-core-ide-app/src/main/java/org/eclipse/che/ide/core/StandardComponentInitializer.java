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
import org.eclipse.che.ide.actions.CloseActiveEditorAction;
import org.eclipse.che.ide.actions.CollapseAllAction;
import org.eclipse.che.ide.actions.CompleteAction;
import org.eclipse.che.ide.actions.ConvertFolderToProjectAction;
import org.eclipse.che.ide.actions.CreateProjectAction;
import org.eclipse.che.ide.actions.DeleteResourceAction;
import org.eclipse.che.ide.actions.DownloadProjectAction;
import org.eclipse.che.ide.actions.DownloadResourceAction;
import org.eclipse.che.ide.actions.DownloadWsAction;
import org.eclipse.che.ide.actions.EditFileAction;
import org.eclipse.che.ide.actions.ExpandEditorAction;
import org.eclipse.che.ide.actions.FormatterAction;
import org.eclipse.che.ide.actions.FullTextSearchAction;
import org.eclipse.che.ide.actions.GoIntoAction;
import org.eclipse.che.ide.actions.HotKeysListAction;
import org.eclipse.che.ide.actions.ImportProjectAction;
import org.eclipse.che.ide.actions.LinkWithEditorAction;
import org.eclipse.che.ide.actions.NavigateToFileAction;
import org.eclipse.che.ide.actions.OpenFileAction;
import org.eclipse.che.ide.actions.ProjectConfigurationAction;
import org.eclipse.che.ide.actions.RedoAction;
import org.eclipse.che.ide.actions.RefreshPathAction;
import org.eclipse.che.ide.actions.RenameItemAction;
import org.eclipse.che.ide.actions.SaveAction;
import org.eclipse.che.ide.actions.SaveAllAction;
import org.eclipse.che.ide.actions.ShowHiddenFilesAction;
import org.eclipse.che.ide.actions.ShowPreferencesAction;
import org.eclipse.che.ide.actions.ShowReferenceAction;
import org.eclipse.che.ide.actions.SignatureHelpAction;
import org.eclipse.che.ide.actions.UndoAction;
import org.eclipse.che.ide.actions.UploadFileAction;
import org.eclipse.che.ide.actions.UploadFolderAction;
import org.eclipse.che.ide.actions.find.FindActionAction;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.action.IdeActions;
import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.editor.texteditor.EditorResources;
import org.eclipse.che.ide.api.filetypes.FileType;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.ide.api.icon.IconRegistry;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.api.keybinding.KeyBuilder;
import org.eclipse.che.ide.connection.WsConnectionListener;
import org.eclipse.che.ide.imageviewer.ImageViewerProvider;
import org.eclipse.che.ide.macro.ServerHostNameMacro;
import org.eclipse.che.ide.macro.ServerMacro;
import org.eclipse.che.ide.macro.ServerPortMacro;
import org.eclipse.che.ide.macro.ServerProtocolMacro;
import org.eclipse.che.ide.newresource.NewFileAction;
import org.eclipse.che.ide.newresource.NewFolderAction;
import org.eclipse.che.ide.part.editor.actions.CloseAction;
import org.eclipse.che.ide.part.editor.actions.CloseAllAction;
import org.eclipse.che.ide.part.editor.actions.CloseAllExceptPinnedAction;
import org.eclipse.che.ide.part.editor.actions.CloseOtherAction;
import org.eclipse.che.ide.part.editor.actions.PinEditorTabAction;
import org.eclipse.che.ide.part.editor.actions.ReopenClosedFileAction;
import org.eclipse.che.ide.part.editor.actions.SplitHorizontallyAction;
import org.eclipse.che.ide.part.editor.actions.SplitVerticallyAction;
import org.eclipse.che.ide.part.editor.actions.SwitchNextEditorAction;
import org.eclipse.che.ide.part.editor.actions.SwitchPreviousEditorAction;
import org.eclipse.che.ide.part.editor.recent.ClearRecentListAction;
import org.eclipse.che.ide.part.editor.recent.OpenRecentFilesAction;
import org.eclipse.che.ide.part.explorer.project.TreeResourceRevealer;
import org.eclipse.che.ide.resources.action.CopyResourceAction;
import org.eclipse.che.ide.resources.action.CutResourceAction;
import org.eclipse.che.ide.resources.action.PasteResourceAction;
import org.eclipse.che.ide.resources.action.RevealResourceAction;
import org.eclipse.che.ide.ui.loaders.request.MessageLoaderResources;
import org.eclipse.che.ide.ui.popup.PopupResources;
import org.eclipse.che.ide.ui.toolbar.MainToolbar;
import org.eclipse.che.ide.ui.toolbar.ToolbarPresenter;
import org.eclipse.che.ide.util.browser.UserAgent;
import org.eclipse.che.ide.util.input.KeyCodeMap;
import org.eclipse.che.ide.xml.NewXmlFileAction;
import org.vectomatic.dom.svg.ui.SVGResource;

import static org.eclipse.che.ide.actions.EditorActions.CLOSE;
import static org.eclipse.che.ide.actions.EditorActions.CLOSE_ALL;
import static org.eclipse.che.ide.actions.EditorActions.CLOSE_ALL_EXCEPT_PINNED;
import static org.eclipse.che.ide.actions.EditorActions.CLOSE_OTHER;
import static org.eclipse.che.ide.actions.EditorActions.PIN_TAB;
import static org.eclipse.che.ide.actions.EditorActions.REOPEN_CLOSED;
import static org.eclipse.che.ide.actions.EditorActions.SPLIT_HORIZONTALLY;
import static org.eclipse.che.ide.actions.EditorActions.SPLIT_VERTICALLY;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_FILE_NEW;
import static org.eclipse.che.ide.api.constraints.Constraints.FIRST;
import static org.eclipse.che.ide.api.constraints.Constraints.LAST;
import static org.eclipse.che.ide.part.editor.recent.RecentFileStore.RECENT_GROUP_ID;
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
    private FindActionAction findActionAction;

    @Inject
    private NavigateToFileAction navigateToFileAction;

    @Inject
    @MainToolbar
    private ToolbarPresenter toolbarPresenter;

    @Inject
    private CutResourceAction cutResourceAction;

    @Inject
    private CopyResourceAction copyResourceAction;

    @Inject
    private PasteResourceAction pasteResourceAction;

    @Inject
    private DeleteResourceAction deleteResourceAction;

    @Inject
    private RenameItemAction renameItemAction;

    @Inject
    private CollapseAllAction collapseAllAction;

    @Inject
    private SplitVerticallyAction splitVerticallyAction;

    @Inject
    private SplitHorizontallyAction splitHorizontallyAction;

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
    private EditFileAction editFileAction;

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
    private DownloadProjectAction downloadProjectAction;

    @Inject
    private DownloadWsAction downloadWsAction;

    @Inject
    private DownloadResourceAction downloadResourceAction;

    @Inject
    private ImportProjectAction importProjectAction;

    @Inject
    private CreateProjectAction createProjectAction;

    @Inject
    private ConvertFolderToProjectAction convertFolderToProjectAction;

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
    private SwitchPreviousEditorAction switchPreviousEditorAction;

    @Inject
    private SwitchNextEditorAction switchNextEditorAction;

    @Inject
    private HotKeysListAction hotKeysListAction;

    @Inject
    private OpenRecentFilesAction openRecentFilesAction;

    @Inject
    private ClearRecentListAction clearRecentFilesAction;

    @Inject
    private CloseActiveEditorAction closeActiveEditorAction;

    @Inject
    private MessageLoaderResources messageLoaderResources;

    @Inject
    private EditorResources editorResources;

    @Inject
    private PopupResources popupResources;

    @Inject
    private ShowReferenceAction showReferenceAction;

    @Inject
    private RevealResourceAction revealResourceAction;

    @Inject
    private RefreshPathAction refreshPathAction;

    @Inject
    private LinkWithEditorAction linkWithEditorAction;

    @Inject
    private SignatureHelpAction signatureHelpAction;

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

    @Inject
    private TreeResourceRevealer treeResourceRevealer; //just to work with it

    // do not remove the injections below
    @Inject
    private ServerMacro serverMacro;

    @Inject
    private ServerProtocolMacro serverProtocolMacro;

    @Inject
    private ServerHostNameMacro serverHostNameMacro;

    @Inject
    private ServerPortMacro serverPortMacro;


    /** Instantiates {@link StandardComponentInitializer} an creates standard content. */
    @Inject
    public StandardComponentInitializer(IconRegistry iconRegistry,
                                        StandardComponentInitializer.ParserResource parserResource) {
        iconRegistry.registerIcon(new Icon(BLANK_CATEGORY + ".samples.category.icon", parserResource.samplesCategoryBlank()));
    }

    public void initialize() {
        messageLoaderResources.Css().ensureInjected();
        editorResources.editorCss().ensureInjected();
        popupResources.popupStyle().ensureInjected();

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

        actionManager.registerAction("downloadWsAsZipAction", downloadWsAction);
        workspaceGroup.add(downloadWsAction);

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
        newXmlFileAction.getTemplatePresentation().setSVGResource(xmlFile.getImage());
        newGroup.addAction(newXmlFileAction);

        actionManager.registerAction("uploadFile", uploadFileAction);
        projectGroup.add(uploadFileAction);

        actionManager.registerAction("uploadFolder", uploadFolderAction);
        projectGroup.add(uploadFolderAction);

        actionManager.registerAction("convertFolderToProject", convertFolderToProjectAction);
        projectGroup.add(convertFolderToProjectAction);

        actionManager.registerAction("downloadAsZipAction", downloadProjectAction);
        projectGroup.add(downloadProjectAction);

        actionManager.registerAction("showHideHiddenFiles", showHiddenFilesAction);
        projectGroup.add(showHiddenFilesAction);

        projectGroup.addSeparator();

        actionManager.registerAction("projectConfiguration", projectConfigurationAction);
        projectGroup.add(projectConfigurationAction);

        // Edit (New Menu)
        DefaultActionGroup editGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_EDIT);
        DefaultActionGroup recentGroup = new DefaultActionGroup(RECENT_GROUP_ID, true, actionManager);
        actionManager.registerAction(IdeActions.GROUP_RECENT_FILES, recentGroup);
        actionManager.registerAction("clearRecentList", clearRecentFilesAction);
        recentGroup.addSeparator();
        recentGroup.add(clearRecentFilesAction, LAST);
        editGroup.add(recentGroup);
        actionManager.registerAction("openRecentFiles", openRecentFilesAction);
        editGroup.add(openRecentFilesAction);

        editGroup.addSeparator();

        actionManager.registerAction("closeActiveEditor", closeActiveEditorAction);
        editGroup.add(closeActiveEditorAction);

        actionManager.registerAction("format", formatterAction);
        editGroup.add(formatterAction);

        actionManager.registerAction("undo", undoAction);
        editGroup.add(undoAction);

        actionManager.registerAction("redo", redoAction);
        editGroup.add(redoAction);

        actionManager.registerAction("cut", cutResourceAction);
        editGroup.add(cutResourceAction);

        actionManager.registerAction("copy", copyResourceAction);
        editGroup.add(copyResourceAction);

        actionManager.registerAction("paste", pasteResourceAction);
        editGroup.add(pasteResourceAction);

        actionManager.registerAction("renameResource", renameItemAction);
        editGroup.add(renameItemAction);

        actionManager.registerAction("deleteItem", deleteResourceAction);
        editGroup.add(deleteResourceAction);

        actionManager.registerAction("fullTextSearch", fullTextSearchAction);
        editGroup.add(fullTextSearchAction);

        editGroup.addSeparator();
        editGroup.add(switchPreviousEditorAction);
        editGroup.add(switchNextEditorAction);

        editGroup.addSeparator();
        editGroup.add(revealResourceAction);

        // Assistant (New Menu)
        DefaultActionGroup assistantGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_ASSISTANT);

        actionManager.registerAction("findActionAction", findActionAction);
        assistantGroup.add(findActionAction);

        actionManager.registerAction("hotKeysList", hotKeysListAction);
        assistantGroup.add(hotKeysListAction);

        assistantGroup.addSeparator();

        actionManager.registerAction("callCompletion", completeAction);
        assistantGroup.add(completeAction);

        actionManager.registerAction("downloadItemAction", downloadResourceAction);
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

        //Compose Profile menu
        DefaultActionGroup profileGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_PROFILE);
        actionManager.registerAction("showPreferences", showPreferencesAction);

        profileGroup.add(showPreferencesAction);

        // Compose Help menu
        DefaultActionGroup helpGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_HELP);
        helpGroup.addSeparator();

        // Compose main context menu
        DefaultActionGroup resourceOperation = new DefaultActionGroup(actionManager);
        actionManager.registerAction("resourceOperation", resourceOperation);
        actionManager.registerAction("refreshPathAction", refreshPathAction);
        actionManager.registerAction("linkWithEditor", linkWithEditorAction);
        resourceOperation.addSeparator();
        resourceOperation.add(showReferenceAction);
        resourceOperation.add(goIntoAction);
        resourceOperation.add(editFileAction);

        resourceOperation.add(cutResourceAction);
        resourceOperation.add(copyResourceAction);
        resourceOperation.add(pasteResourceAction);
        resourceOperation.add(renameItemAction);
        resourceOperation.add(deleteResourceAction);
        resourceOperation.addSeparator();
        resourceOperation.add(downloadResourceAction);
        resourceOperation.add(refreshPathAction);
        resourceOperation.add(linkWithEditorAction);
        resourceOperation.addSeparator();
        resourceOperation.add(convertFolderToProjectAction);
        resourceOperation.addSeparator();

        DefaultActionGroup mainContextMenuGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_MAIN_CONTEXT_MENU);
        mainContextMenuGroup.add(newGroup, Constraints.FIRST);
        mainContextMenuGroup.addSeparator();
        mainContextMenuGroup.add(resourceOperation);

        actionManager.registerAction("expandEditor", expandEditorAction);
        DefaultActionGroup rightMenuGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_RIGHT_MAIN_MENU);
        rightMenuGroup.add(expandEditorAction, FIRST);

        // Compose main toolbar
        DefaultActionGroup changeResourceGroup = new DefaultActionGroup(actionManager);
        actionManager.registerAction("changeResourceGroup", changeResourceGroup);
        actionManager.registerAction("editFile", editFileAction);
        actionManager.registerAction("goInto", goIntoAction);
        actionManager.registerAction("showReference", showReferenceAction);

        actionManager.registerAction("collapseAll", collapseAllAction);

        actionManager.registerAction("openFile", openFileAction);
        actionManager.registerAction("switchLeftTab", switchPreviousEditorAction);
        actionManager.registerAction("switchRightTab", switchNextEditorAction);
        actionManager.registerAction("scrollFromSource", revealResourceAction);

        changeResourceGroup.add(cutResourceAction);
        changeResourceGroup.add(copyResourceAction);
        changeResourceGroup.add(pasteResourceAction);
        changeResourceGroup.add(deleteResourceAction);

        DefaultActionGroup mainToolbarGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_MAIN_TOOLBAR);
        mainToolbarGroup.add(newGroup);
        mainToolbarGroup.add(changeResourceGroup);
        toolbarPresenter.bindMainGroup(mainToolbarGroup);

        DefaultActionGroup centerToolbarGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_CENTER_TOOLBAR);
        toolbarPresenter.bindCenterGroup(centerToolbarGroup);

        DefaultActionGroup rightToolbarGroup = (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_RIGHT_TOOLBAR);
        toolbarPresenter.bindRightGroup(rightToolbarGroup);

        //Editor context menu group
        DefaultActionGroup editorTabContextMenu =
                (DefaultActionGroup)actionManager.getAction(IdeActions.GROUP_EDITOR_TAB_CONTEXT_MENU);
        editorTabContextMenu.add(closeAction);
        actionManager.registerAction(CLOSE, closeAction);
        editorTabContextMenu.add(closeAllAction);
        actionManager.registerAction(CLOSE_ALL, closeAllAction);
        editorTabContextMenu.add(closeOtherAction);
        actionManager.registerAction(CLOSE_OTHER, closeOtherAction);
        editorTabContextMenu.add(closeAllExceptPinnedAction);
        actionManager.registerAction(CLOSE_ALL_EXCEPT_PINNED, closeAllExceptPinnedAction);
        editorTabContextMenu.addSeparator();
        editorTabContextMenu.add(reopenClosedFileAction);
        actionManager.registerAction(REOPEN_CLOSED, reopenClosedFileAction);
        editorTabContextMenu.add(pinEditorTabAction);
        actionManager.registerAction(PIN_TAB, pinEditorTabAction);
        editorTabContextMenu.addSeparator();
        actionManager.registerAction(SPLIT_HORIZONTALLY, splitHorizontallyAction);
        editorTabContextMenu.add(splitHorizontallyAction);
        actionManager.registerAction(SPLIT_VERTICALLY, splitVerticallyAction);
        editorTabContextMenu.add(splitVerticallyAction);

        actionManager.registerAction("noOpAction", new NoOpAction());
        actionManager.registerAction("signatureHelp", signatureHelpAction);

        DefaultActionGroup editorContextMenuGroup = new DefaultActionGroup(actionManager);
        actionManager.registerAction(IdeActions.GROUP_EDITOR_CONTEXT_MENU, editorContextMenuGroup);

        editorContextMenuGroup.add(undoAction);
        editorContextMenuGroup.add(redoAction);
        editorContextMenuGroup.addSeparator();
        editorContextMenuGroup.add(formatterAction);

        editorContextMenuGroup.addSeparator();
        editorContextMenuGroup.add(fullTextSearchAction);
        editorContextMenuGroup.add(closeActiveEditorAction);


        DefaultActionGroup emptyEditorsPanel = new DefaultActionGroup(actionManager);
        actionManager.registerAction(IdeActions.GROUP_EMPTY_EDITOR_PANEL, emptyEditorsPanel);
        emptyEditorsPanel.add(newFileAction);
        emptyEditorsPanel.add(createProjectAction);

        DefaultActionGroup emptyProjectPanel = new DefaultActionGroup(actionManager);
        actionManager.registerAction(IdeActions.GROUP_EMPTY_PROJECT_PANEL, emptyProjectPanel);
        emptyProjectPanel.add(importProjectAction);
        emptyProjectPanel.add(createProjectAction);

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
        keyBinding.getGlobal().addKey(new KeyBuilder().charCode(KeyCodeMap.DELETE).build(), "deleteItem");

        if (UserAgent.isMac()) {
            keyBinding.getGlobal().addKey(new KeyBuilder().control().charCode('w').build(), "closeActiveEditor");
            keyBinding.getGlobal().addKey(new KeyBuilder().control().charCode('p').build(), "signatureHelp");
        } else {
            keyBinding.getGlobal().addKey(new KeyBuilder().alt().charCode('w').build(), "closeActiveEditor");
            keyBinding.getGlobal().addKey(new KeyBuilder().action().charCode('p').build(), "signatureHelp");
        }
    }

    /** Action that does nothing. It's just for disabling (catching) browser's hot key. */
    private class NoOpAction extends Action {
        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }

}

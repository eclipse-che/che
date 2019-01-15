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
package org.eclipse.che.plugin.languageserver.ide.rename;

import static org.eclipse.che.plugin.languageserver.ide.editor.LanguageServerEditorConfiguration.INITIAL_DOCUMENT_VERSION;
import static org.slf4j.LoggerFactory.getLogger;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;
import org.eclipse.che.api.languageserver.shared.model.ExtendedTextDocumentEdit;
import org.eclipse.che.api.languageserver.shared.model.ExtendedTextEdit;
import org.eclipse.che.api.languageserver.shared.model.ExtendedWorkspaceEdit;
import org.eclipse.che.api.languageserver.shared.model.RenameResult;
import org.eclipse.che.ide.actions.RenameItemAction;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.position.PositionConverter.PixelCoordinates;
import org.eclipse.che.ide.api.editor.text.Position;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.parts.PartStackType;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.runtime.OperationCanceledException;
import org.eclipse.che.plugin.languageserver.ide.LanguageServerLocalization;
import org.eclipse.che.plugin.languageserver.ide.editor.quickassist.ApplyWorkspaceEditAction;
import org.eclipse.che.plugin.languageserver.ide.registry.LanguageServerRegistry;
import org.eclipse.che.plugin.languageserver.ide.rename.RenameView.ActionDelegate;
import org.eclipse.che.plugin.languageserver.ide.rename.model.RenameChange;
import org.eclipse.che.plugin.languageserver.ide.rename.model.RenameFile;
import org.eclipse.che.plugin.languageserver.ide.rename.model.RenameFolder;
import org.eclipse.che.plugin.languageserver.ide.rename.model.RenameProject;
import org.eclipse.che.plugin.languageserver.ide.service.TextDocumentServiceClient;
import org.eclipse.che.plugin.languageserver.ide.util.DtoBuildHelper;
import org.eclipse.lsp4j.DidCloseTextDocumentParams;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.RenameParams;
import org.eclipse.lsp4j.ResourceOperation;
import org.eclipse.lsp4j.TextDocumentEdit;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.WorkspaceEdit;
import org.eclipse.lsp4j.jsonrpc.messages.Either;
import org.slf4j.Logger;

/** Main controller for rename feature, calls rename and shows rename edits */
@Singleton
public class RenamePresenter extends BasePresenter implements ActionDelegate {

  private static final Logger LOG = getLogger(RenamePresenter.class);

  private final LanguageServerLocalization localization;
  private final TextDocumentServiceClient client;
  private final DtoFactory dtoFactory;
  private final DtoBuildHelper dtoHelper;
  private final Provider<RenameInputBox> renameInputBoxProvider;
  private final RenameView view;
  private final WorkspaceAgent workspaceAgent;
  private final AppContext appContext;
  private final ApplyWorkspaceEditAction workspaceEditAction;
  private final RenameItemAction renameItemAction;
  private final LanguageServerRegistry lsRegistry;
  private final Provider<RenameDialog> renameWindow;
  private final Map<List<ExtendedTextDocumentEdit>, List<RenameProject>> projectCache =
      new HashMap<>();
  private RenameInputBox inputBox;
  private boolean showPreview;
  private TextEditor textEditor;

  @Inject
  public RenamePresenter(
      LanguageServerLocalization localization,
      TextDocumentServiceClient client,
      DtoFactory dtoFactory,
      DtoBuildHelper dtoHelper,
      Provider<RenameInputBox> renameInputBoxProvider,
      RenameView view,
      WorkspaceAgent workspaceAgent,
      AppContext appContext,
      ApplyWorkspaceEditAction workspaceEditAction,
      RenameItemAction renameItemAction,
      LanguageServerRegistry lsRegistry,
      Provider<RenameDialog> renameWindow) {
    this.localization = localization;
    this.client = client;
    this.dtoFactory = dtoFactory;
    this.dtoHelper = dtoHelper;
    this.renameInputBoxProvider = renameInputBoxProvider;
    this.view = view;
    this.workspaceAgent = workspaceAgent;
    this.appContext = appContext;
    this.workspaceEditAction = workspaceEditAction;
    this.renameItemAction = renameItemAction;
    this.lsRegistry = lsRegistry;
    this.renameWindow = renameWindow;
    view.setDelegate(this);

    addResourceRenameAction();
  }

  /**
   * A workaround to be able to inform a language server that a file is renamed when it is being
   * renamed through resource management component which does not related to language server client
   * component so under normal circumstances language server would not know that something is being
   * renamed.
   */
  private void addResourceRenameAction() {
    renameItemAction.addCustomAction(
        (oldResource, newResource) -> {
          if (!oldResource.isFile()) {
            return;
          }

          if (!lsRegistry.isLsRegistered(newResource)) {
            return;
          }

          newResource
              .asFile()
              .getContent()
              .then(
                  text -> {
                    DidCloseTextDocumentParams didCloseDto =
                        dtoFactory.createDto(DidCloseTextDocumentParams.class);
                    TextDocumentIdentifier didCloseTDDto =
                        dtoFactory.createDto(TextDocumentIdentifier.class);
                    didCloseTDDto.setUri(oldResource.getLocation().toString());
                    didCloseDto.setTextDocument(didCloseTDDto);
                    client.didClose(didCloseDto);

                    DidOpenTextDocumentParams didOpenDto =
                        dtoFactory.createDto(DidOpenTextDocumentParams.class);
                    TextDocumentItem didOpenTDDto = dtoFactory.createDto(TextDocumentItem.class);
                    didOpenTDDto.setUri(newResource.getLocation().toString());
                    didOpenTDDto.setLanguageId(
                        lsRegistry.getLanguageFilter(newResource.asFile()).getLanguageId());
                    didOpenTDDto.setText(text);
                    didOpenTDDto.setVersion(INITIAL_DOCUMENT_VERSION);
                    didOpenDto.setTextDocument(didOpenTDDto);
                    client.didOpen(didOpenDto);
                  });
        });
  }

  @Override
  public String getTitle() {
    return localization.renameViewTitle();
  }

  @Override
  public IsWidget getView() {
    return view;
  }

  @Override
  public String getTitleToolTip() {
    return localization.renameViewTooltip();
  }

  @Override
  public void go(AcceptsOneWidget container) {
    container.setWidget(view);
  }

  public void rename(TextEditor editor) {
    textEditor = editor;
    TextPosition cursorPosition = textEditor.getCursorPosition();
    int cursorOffset = editor.getCursorOffset();
    Position wordAtOffset = editor.getWordAtOffset(cursorOffset);
    if (wordAtOffset == null) {
      LOG.debug("Can't find word to rename");
      return;
    }
    PixelCoordinates pixelCoordinates =
        editor.getPositionConverter().offsetToPixel(wordAtOffset.offset);
    String oldName = editor.getDocument().getContentRange(wordAtOffset.offset, wordAtOffset.length);

    if (inputBox != null) {
      showWindow(cursorPosition, editor, oldName);
      return;
    }
    projectCache.clear();

    inputBox = renameInputBoxProvider.get();
    inputBox
        .setPositionAndShow(
            pixelCoordinates.getX(),
            pixelCoordinates.getY(),
            oldName,
            () -> showWindow(cursorPosition, editor, oldName))
        .then(
            newName -> {
              editor.setFocus();
              inputBox = null;
              if (!oldName.equals(newName)) {
                callRename(newName, cursorPosition, editor);
              }
            })
        .catchError(
            err -> {
              editor.setFocus();
              inputBox = null;
              if (!(err.getCause() instanceof OperationCanceledException)) {
                LOG.error(err.getMessage());
              }
            });
  }

  private void showWindow(TextPosition cursorPosition, TextEditor editor, String oldName) {
    String value = inputBox.getInputValue();
    inputBox.hide(false);
    inputBox = null;

    showPreview = false;
    RenameDialog renameDialog = this.renameWindow.get();
    renameDialog.show(
        value,
        oldName,
        newName -> {
          renameDialog.closeDialog();
          callRename(newName, cursorPosition, editor);
        },
        newName -> {
          showPreview = true;
          renameDialog.closeDialog();
          callRename(newName, cursorPosition, editor);
        },
        () -> {
          renameDialog.closeDialog();
          editor.setFocus();
        });
  }

  private void callRename(String newName, TextPosition cursorPosition, TextEditor editor) {
    RenameParams dto = dtoFactory.createDto(RenameParams.class);

    TextDocumentIdentifier identifier = dtoHelper.createTDI(editor.getEditorInput().getFile());

    dto.setNewName(newName);
    dto.setTextDocument(identifier);

    org.eclipse.lsp4j.Position position = dtoFactory.createDto(org.eclipse.lsp4j.Position.class);
    position.setCharacter(cursorPosition.getCharacter());
    position.setLine(cursorPosition.getLine());
    dto.setPosition(position);
    client
        .rename(dto)
        .then(this::handleRename)
        .catchError(
            arg -> {
              LOG.error(arg.getMessage());
            });
  }

  private void handleRename(RenameResult renameResult) {
    if (!renameResult.getRenameResults().isEmpty()) {
      ExtendedWorkspaceEdit workspaceEdit =
          renameResult
              .getRenameResults()
              .get(renameResult.getRenameResults().keySet().iterator().next());
      if (renameResult.getRenameResults().size() == 1
          && workspaceEdit.getDocumentChanges().size() == 1
          && !showPreview) {
        List<RenameProject> renameProjects = convert(workspaceEdit.getDocumentChanges());
        applyRename(renameProjects);
      } else {
        workspaceAgent.openPart(this, PartStackType.INFORMATION);
        workspaceAgent.setActivePart(this);
        view.showRenameResult(renameResult.getRenameResults());
      }
    }
  }

  @Override
  public List<RenameProject> convert(List<ExtendedTextDocumentEdit> documentChanges) {
    if (projectCache.containsKey(documentChanges)) {
      return projectCache.get(documentChanges);
    }
    Project[] projects = appContext.getProjects();
    Map<Project, List<ExtendedTextDocumentEdit>> projectMap = new HashMap<>();
    for (ExtendedTextDocumentEdit documentChange : documentChanges) {
      String filePath = documentChange.getTextDocument().getUri();
      Project project = getProject(filePath, projects);
      if (project == null) {
        continue;
      }
      if (!projectMap.containsKey(project)) {
        projectMap.put(project, new ArrayList<>());
      }
      projectMap.get(project).add(documentChange);
    }

    List<RenameProject> result = new ArrayList<>();
    for (Project project : projectMap.keySet()) {
      result.add(new RenameProject(project, getRenameFolders(project, projectMap.get(project))));
    }

    return result;
  }

  @Override
  public void cancel() {
    workspaceAgent.hidePart(this);
    workspaceAgent.removePart(this);
    textEditor.setFocus();
  }

  @Override
  public void applyRename() {
    List<RenameProject> projects = view.getRenameProjects();
    applyRename(projects);
    workspaceAgent.hidePart(this);
    workspaceAgent.removePart(this);
    textEditor.setFocus();
  }

  private void applyRename(List<RenameProject> projects) {
    List<Either<TextDocumentEdit, ResourceOperation>> edits = new ArrayList<>();
    for (RenameProject project : projects) {
      project.getTextDocumentEdits().forEach(edit -> edits.add(Either.forLeft(edit)));
    }
    workspaceEditAction.applyWorkspaceEdit(new WorkspaceEdit(edits));
  }

  private List<RenameFolder> getRenameFolders(
      Project project, List<ExtendedTextDocumentEdit> edits) {

    List<RenameFolder> result = new ArrayList<>();

    Map<String, List<RenameFile>> files = new HashMap<>();
    for (ExtendedTextDocumentEdit edit : edits) {
      String uri = edit.getTextDocument().getUri();
      String filePath = uri;
      filePath = filePath.substring(project.getPath().length() + 1);
      String folderPath = filePath.substring(0, filePath.lastIndexOf('/'));
      String fileName = filePath.substring(filePath.lastIndexOf('/') + 1, filePath.length());
      if (!files.containsKey(folderPath)) {
        files.put(folderPath, new ArrayList<>());
      }
      files
          .get(folderPath)
          .add(new RenameFile(fileName, uri, getRenameChanges(edit.getEdits(), uri)));
    }

    for (String folderPath : files.keySet()) {
      result.add(new RenameFolder(folderPath, files.get(folderPath)));
    }

    return result;
  }

  private List<RenameChange> getRenameChanges(List<ExtendedTextEdit> edits, String filePath) {
    return edits
        .stream()
        .map(edit -> new RenameChange(edit, filePath))
        .collect(Collectors.toList());
  }

  private Project getProject(String filePath, Project[] projects) {
    Project selectedProject = null;
    for (Project project : projects) {
      if (filePath.startsWith(project.getPath())) {
        if (selectedProject == null) {
          selectedProject = project;
        } else if (selectedProject.getPath().length() < project.getPath().length()) {
          selectedProject = project;
        }
      }
    }
    return selectedProject;
  }
}

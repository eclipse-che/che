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
package org.eclipse.che.ide.ext.java.client.refactoring.rename;

import static org.eclipse.che.ide.api.resources.Resource.FILE;
import static org.eclipse.che.ide.ext.java.client.util.JavaUtil.isJavaProject;

import com.google.common.base.Optional;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import java.util.List;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.parts.ActivePartChangedEvent;
import org.eclipse.che.ide.api.parts.ActivePartChangedHandler;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactorInfo;
import org.eclipse.che.ide.ext.java.client.refactoring.move.RefactoredItemType;
import org.eclipse.che.ide.ext.java.client.refactoring.rename.wizard.RenamePresenter;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.util.loging.Log;

/**
 * Action for launch rename refactoring of java files
 *
 * @author Alexander Andrienko
 * @author Valeriy Svydenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class RenameRefactoringAction extends AbstractPerspectiveAction
    implements ActivePartChangedHandler {

  private final EditorAgent editorAgent;
  private final RenamePresenter renamePresenter;
  private final JavaLocalizationConstant locale;
  private final JavaRefactoringRename javaRefactoringRename;
  private final AppContext appContext;
  private final FileTypeRegistry fileTypeRegistry;
  private final DialogFactory dialogFactory;

  private boolean editorInFocus;

  @Inject
  public RenameRefactoringAction(
      EditorAgent editorAgent,
      RenamePresenter renamePresenter,
      EventBus eventBus,
      JavaLocalizationConstant locale,
      JavaRefactoringRename javaRefactoringRename,
      AppContext appContext,
      FileTypeRegistry fileTypeRegistry,
      DialogFactory dialogFactory) {
    super(null, locale.renameRefactoringActionName(), locale.renameRefactoringActionDescription());
    this.editorAgent = editorAgent;
    this.renamePresenter = renamePresenter;
    this.locale = locale;
    this.javaRefactoringRename = javaRefactoringRename;
    this.appContext = appContext;
    this.fileTypeRegistry = fileTypeRegistry;
    this.dialogFactory = dialogFactory;
    this.editorInFocus = false;

    eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    List<EditorPartPresenter> dirtyEditors = editorAgent.getDirtyEditors();
    if (dirtyEditors.isEmpty()) {
      performAction();
      return;
    }

    AsyncCallback<Void> savingOperationCallback =
        new AsyncCallback<Void>() {
          @Override
          public void onFailure(Throwable caught) {
            Log.error(getClass(), caught);
          }

          @Override
          public void onSuccess(Void result) {
            performAction();
          }
        };

    dialogFactory
        .createConfirmDialog(
            locale.unsavedDataDialogTitle(),
            locale.unsavedDataDialogPromptSaveChanges(),
            () -> editorAgent.saveAll(savingOperationCallback),
            null)
        .show();
  }

  private void performAction() {
    if (editorInFocus) {
      final EditorPartPresenter editorPart = editorAgent.getActiveEditor();
      if (editorPart == null || !(editorPart instanceof TextEditor)) {
        return;
      }

      javaRefactoringRename.refactor((TextEditor) editorPart);
    } else {
      final Resource[] resources = appContext.getResources();

      if (resources == null || resources.length > 1) {
        return;
      }

      final Resource resource = resources[0];

      final Project project = resource.getProject();

      if (!isJavaProject(project)) {
        return;
      }

      final Optional<Resource> srcFolder = resource.getParentWithMarker(SourceFolderMarker.ID);

      if (!srcFolder.isPresent() || resource.getLocation().equals(srcFolder.get().getLocation())) {
        return;
      }

      RefactoredItemType renamedItemType = null;

      if (resource.getResourceType() == FILE && isJavaFile((File) resource)) {
        renamedItemType = RefactoredItemType.COMPILATION_UNIT;
      } else if (resource instanceof Container) {
        renamedItemType = RefactoredItemType.PACKAGE;
      }

      if (renamedItemType == null) {
        return;
      }

      renamePresenter.show(RefactorInfo.of(renamedItemType, resources));
    }
  }

  @Override
  public void updateInPerspective(ActionEvent event) {
    event.getPresentation().setVisible(true);

    if (editorInFocus) {
      final EditorPartPresenter editorPart = editorAgent.getActiveEditor();
      if (editorPart == null || !(editorPart instanceof TextEditor)) {
        event.getPresentation().setEnabledAndVisible(false);
        return;
      }

      final VirtualFile file = editorPart.getEditorInput().getFile();

      if (file instanceof File) {
        final Project project = ((File) file).getProject();

        if (project == null) {
          event.getPresentation().setEnabledAndVisible(false);
          return;
        }

        event.getPresentation().setEnabledAndVisible(isJavaProject(project) && isJavaFile(file));
      } else {
        event.getPresentation().setEnabledAndVisible(isJavaFile(file));
      }

    } else {
      final Resource[] resources = appContext.getResources();

      if (resources == null || resources.length != 1) {
        event.getPresentation().setEnabledAndVisible(false);
        return;
      }

      final Resource resource = resources[0];

      final Project project = resource.getProject();

      if (project == null) {
        event.getPresentation().setEnabledAndVisible(false);
        return;
      }

      final Optional<Resource> srcFolder = resource.getParentWithMarker(SourceFolderMarker.ID);

      if (resource.getResourceType() == FILE) {
        event
            .getPresentation()
            .setEnabledAndVisible(
                isJavaProject(project) && srcFolder.isPresent() && isJavaFile((File) resource));
      } else if (resource instanceof Container) {
        event
            .getPresentation()
            .setEnabledAndVisible(isJavaProject(project) && srcFolder.isPresent());
      }
    }
  }

  protected boolean isJavaFile(VirtualFile file) {
    String fileExtension = fileTypeRegistry.getFileTypeByFile(file).getExtension();

    return fileExtension != null && (fileExtension.equals("java") || fileExtension.equals("class"));
  }

  @Override
  public void onActivePartChanged(ActivePartChangedEvent event) {
    editorInFocus = event.getActivePart() instanceof EditorPartPresenter;
  }
}

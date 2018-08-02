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
package org.eclipse.che.ide.ext.java.client.refactoring.move;

import static org.eclipse.che.ide.api.resources.Resource.FILE;
import static org.eclipse.che.ide.ext.java.client.refactoring.move.MoveType.REFACTOR_MENU;
import static org.eclipse.che.ide.ext.java.client.refactoring.move.RefactoredItemType.COMPILATION_UNIT;
import static org.eclipse.che.ide.ext.java.client.refactoring.move.RefactoredItemType.PACKAGE;

import com.google.common.base.Optional;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.refactoring.RefactorInfo;
import org.eclipse.che.ide.ext.java.client.refactoring.move.wizard.MovePresenter;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;
import org.eclipse.che.ide.ext.java.client.util.JavaUtil;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.util.loging.Log;

/**
 * @author Dmitry Shnurenko
 * @author Valeriy Svydenko
 */
@Singleton
public class MoveAction extends AbstractPerspectiveAction {

  private final JavaLocalizationConstant locale;
  private final MovePresenter movePresenter;
  private final AppContext appContext;
  private final FileTypeRegistry fileTypeRegistry;
  private final DialogFactory dialogFactory;
  private EditorAgent editorAgent;

  @Inject
  public MoveAction(
      JavaLocalizationConstant locale,
      MovePresenter movePresenter,
      AppContext appContext,
      FileTypeRegistry fileTypeRegistry,
      DialogFactory dialogFactory,
      EditorAgent editorAgent) {
    super(null, locale.moveActionName(), locale.moveActionDescription());
    this.locale = locale;

    this.movePresenter = movePresenter;
    this.appContext = appContext;
    this.fileTypeRegistry = fileTypeRegistry;
    this.dialogFactory = dialogFactory;
    this.editorAgent = editorAgent;
  }

  /** {@inheritDoc} */
  @Override
  public void updateInPerspective(ActionEvent event) {
    event.getPresentation().setVisible(true);

    final Resource[] resources = appContext.getResources();

    if (resources == null || resources.length != 1) {
      event.getPresentation().setEnabled(false);
      return;
    }

    final Resource resource = resources[0];

    final Optional<Project> project = resource.getRelatedProject();

    if (!project.isPresent()) {
      event.getPresentation().setEnabled(false);
      return;
    }

    final Optional<Resource> srcFolder = resource.getParentWithMarker(SourceFolderMarker.ID);

    if (resource.getResourceType() == FILE) {
      event
          .getPresentation()
          .setEnabled(
              JavaUtil.isJavaProject(project.get())
                  && srcFolder.isPresent()
                  && isJavaFile((File) resource));
    } else if (resource instanceof Container) {
      event
          .getPresentation()
          .setEnabled(JavaUtil.isJavaProject(project.get()) && srcFolder.isPresent());
    }
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent actionEvent) {
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
    final Resource[] resources = appContext.getResources();

    if (resources == null || resources.length > 1) {
      return;
    }

    final Resource resource = resources[0];

    final Optional<Project> project = resource.getRelatedProject();

    if (!JavaUtil.isJavaProject(project.get())) {
      return;
    }

    final Optional<Resource> srcFolder = resource.getParentWithMarker(SourceFolderMarker.ID);

    if (!srcFolder.isPresent() || resource.getLocation().equals(srcFolder.get().getLocation())) {
      return;
    }

    RefactoredItemType renamedItemType = null;

    if (resource.getResourceType() == FILE && isJavaFile((File) resource)) {
      renamedItemType = COMPILATION_UNIT;
    } else if (resource instanceof Container) {
      renamedItemType = PACKAGE;
    }

    if (renamedItemType == null) {
      return;
    }

    movePresenter.show(RefactorInfo.of(REFACTOR_MENU, renamedItemType, resources));
  }

  protected boolean isJavaFile(VirtualFile file) {
    final String ext = fileTypeRegistry.getFileTypeByFile(file).getExtension();

    return "java".equals(ext) || "class".equals(ext);
  }
}

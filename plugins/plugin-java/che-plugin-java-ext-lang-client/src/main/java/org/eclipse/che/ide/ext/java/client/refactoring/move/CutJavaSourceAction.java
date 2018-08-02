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
import static org.eclipse.che.ide.ext.java.client.util.JavaUtil.isJavaProject;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.filetypes.FileTypeRegistry;
import org.eclipse.che.ide.api.parts.ActivePartChangedEvent;
import org.eclipse.che.ide.api.parts.ActivePartChangedHandler;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker;

/**
 * The action is called Move presenter when the user has clicked Ctrl+X on some class or package
 * into the 'Project Explorer'.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class CutJavaSourceAction extends BaseAction implements ActivePartChangedHandler {
  private final MoveAction moveAction;
  private final FileTypeRegistry fileTypeRegistry;
  private final AppContext appContext;

  private boolean isEditorPartActive;

  @Inject
  public CutJavaSourceAction(
      JavaLocalizationConstant locale,
      MoveAction moveAction,
      EventBus eventBus,
      FileTypeRegistry fileTypeRegistry,
      AppContext appContext) {
    super(locale.moveActionName(), locale.moveActionDescription());

    this.moveAction = moveAction;
    this.fileTypeRegistry = fileTypeRegistry;
    this.appContext = appContext;

    eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
  }

  @Override
  public void update(ActionEvent event) {

    event.getPresentation().setVisible(true);

    final Resource[] resources = appContext.getResources();

    if (resources == null || resources.length > 1) {
      event.getPresentation().setEnabled(false);
      return;
    }

    final Resource resource = resources[0];

    final Optional<Project> project = resource.getRelatedProject();
    final Optional<Resource> srcFolder = resource.getParentWithMarker(SourceFolderMarker.ID);

    if (resource.getResourceType() == FILE) {
      event
          .getPresentation()
          .setEnabled(
              !isEditorPartActive
                  && isJavaProject(project.get())
                  && srcFolder.isPresent()
                  && isJavaFile((File) resource));
    } else if (resource instanceof Container) {
      event
          .getPresentation()
          .setEnabled(!isEditorPartActive && isJavaProject(project.get()) && srcFolder.isPresent());
    }
  }

  @Override
  public void actionPerformed(ActionEvent actionEvent) {
    moveAction.actionPerformed(actionEvent);
  }

  @Override
  public void onActivePartChanged(ActivePartChangedEvent event) {
    isEditorPartActive = event.getActivePart() instanceof EditorPartPresenter;
  }

  protected boolean isJavaFile(VirtualFile file) {
    final String ext = fileTypeRegistry.getFileTypeByFile(file).getExtension();

    return "java".equals(ext) || "class".equals(ext);
  }
}

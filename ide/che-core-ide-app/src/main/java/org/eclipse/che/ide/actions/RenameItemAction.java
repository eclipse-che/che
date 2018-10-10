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
package org.eclipse.che.ide.actions;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Sets.newConcurrentHashSet;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.api.resources.Resource.FILE;
import static org.eclipse.che.ide.api.resources.Resource.FOLDER;
import static org.eclipse.che.ide.api.resources.Resource.PROJECT;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.WorkspaceAgent;
import org.eclipse.che.ide.api.resources.RenamingSupport;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.input.InputCallback;
import org.eclipse.che.ide.ui.dialogs.input.InputDialog;
import org.eclipse.che.ide.ui.dialogs.input.InputValidator;
import org.eclipse.che.ide.util.NameUtils;

/**
 * Rename selected resource in the application context.
 *
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 * @see AppContext#getResource()
 */
@Singleton
public class RenameItemAction extends AbstractPerspectiveAction {
  private final CoreLocalizationConstant localization;
  private final Set<RenamingSupport> renamingSupport;
  private final EditorAgent editorAgent;
  private final NotificationManager notificationManager;
  private final DialogFactory dialogFactory;
  private final AppContext appContext;
  private final WorkspaceAgent workspaceAgent;

  private final Set<BiConsumer<Resource, Resource>> customActions = newConcurrentHashSet();

  @Inject
  public RenameItemAction(
      Resources resources,
      CoreLocalizationConstant localization,
      Set<RenamingSupport> renamingSupport,
      EditorAgent editorAgent,
      NotificationManager notificationManager,
      DialogFactory dialogFactory,
      AppContext appContext,
      WorkspaceAgent workspaceAgent) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        localization.renameItemActionText(),
        localization.renameItemActionDescription(),
        resources.rename());
    this.localization = localization;
    this.renamingSupport = renamingSupport;
    this.editorAgent = editorAgent;
    this.notificationManager = notificationManager;
    this.dialogFactory = dialogFactory;
    this.appContext = appContext;
    this.workspaceAgent = workspaceAgent;
  }

  /**
   * Add an action that will be performed right after a successful rename of the resource
   *
   * @param action action represented by a binary consume where the first parameter is a old
   *     resource and a the second - newly renamed resource
   */
  public void addCustomAction(BiConsumer<Resource, Resource> action) {
    customActions.add(action);
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent e) {

    final Resource resource = appContext.getResource();

    checkState(resource != null, "Null resource occurred");

    final String resourceName = resource.getName();
    final int selectionLength =
        resourceName.indexOf('.') >= 0 ? resourceName.lastIndexOf('.') : resourceName.length();
    final InputValidator validator;
    final String dialogTitle;

    if (resource.getResourceType() == FILE) {
      validator = new FileNameValidator(resourceName);
      dialogTitle = localization.renameFileDialogTitle(resourceName);
    } else if (resource.getResourceType() == FOLDER) {
      validator = new FolderNameValidator(resourceName);
      dialogTitle = localization.renameFolderDialogTitle(resourceName);
    } else if (resource.getResourceType() == PROJECT) {
      validator = new ProjectNameValidator(resourceName);
      dialogTitle = localization.renameProjectDialogTitle(resourceName);
    } else {
      throw new IllegalStateException("Not a resource");
    }

    final InputCallback inputCallback =
        new InputCallback() {
          @Override
          public void accepted(final String value) {
            // we shouldn't perform renaming file with the same name
            if (!value.trim().equals(resourceName)) {

              closeRelatedEditors(resource);

              final Path destination = resource.getLocation().parent().append(value);

              resource
                  .move(destination)
                  .then(
                      newResource -> {
                        customActions.forEach(it -> it.accept(resource, newResource));
                      })
                  .catchError(
                      new Operation<PromiseError>() {
                        @Override
                        public void apply(PromiseError arg) throws OperationException {
                          notificationManager.notify("", arg.getMessage(), FAIL, EMERGE_MODE);
                        }
                      });
            }
          }
        };

    InputDialog inputDialog =
        dialogFactory.createInputDialog(
            dialogTitle,
            localization.renameDialogNewNameLabel(),
            resource.getName(),
            0,
            selectionLength,
            inputCallback,
            null);
    inputDialog.withValidator(validator);
    inputDialog.show();
  }

  private void closeRelatedEditors(Resource resource) {
    if (!resource.isProject()) {
      return;
    }

    final List<EditorPartPresenter> openedEditors = editorAgent.getOpenedEditors();

    for (EditorPartPresenter editor : openedEditors) {
      if (resource.getLocation().isPrefixOf(editor.getEditorInput().getFile().getLocation())) {
        editorAgent.closeEditor(editor);
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public void updateInPerspective(@NotNull ActionEvent e) {

    if (workspaceAgent.getActivePart() == null
        || workspaceAgent.getActivePart() instanceof EditorPartPresenter) {
      e.getPresentation().setEnabledAndVisible(false);
      return;
    }

    final Resource[] resources = appContext.getResources();
    e.getPresentation().setVisible(true);

    if (resources == null || resources.length != 1) {
      e.getPresentation().setEnabled(false);
      return;
    }

    for (RenamingSupport validator : renamingSupport) {
      if (!validator.isRenameAllowed(resources[0])) {
        e.getPresentation().setEnabled(false);
        return;
      }
    }

    e.getPresentation().setEnabled(true);
  }

  private abstract class AbstractNameValidator implements InputValidator {
    private final String selfName;

    public AbstractNameValidator(final String selfName) {
      this.selfName = selfName;
    }

    @Override
    public Violation validate(String value) {
      if (value.trim().equals(selfName)) {
        return new Violation() {
          @Override
          public String getMessage() {
            return "";
          }

          @Override
          public String getCorrectedValue() {
            return null;
          }
        };
      }

      return isValidName(value);
    }

    public abstract Violation isValidName(String value);
  }

  private class FileNameValidator extends AbstractNameValidator {

    public FileNameValidator(String selfName) {
      super(selfName);
    }

    @Override
    public Violation isValidName(String value) {
      if (!NameUtils.checkFileName(value)) {
        return new Violation() {
          @Override
          public String getMessage() {
            return localization.invalidName();
          }

          @Nullable
          @Override
          public String getCorrectedValue() {
            return null;
          }
        };
      }
      return null;
    }
  }

  private class FolderNameValidator extends AbstractNameValidator {

    public FolderNameValidator(String selfName) {
      super(selfName);
    }

    @Override
    public Violation isValidName(String value) {
      if (!NameUtils.checkFolderName(value)) {
        return new Violation() {
          @Override
          public String getMessage() {
            return localization.invalidName();
          }

          @Nullable
          @Override
          public String getCorrectedValue() {
            return null;
          }
        };
      }
      return null;
    }
  }

  private class ProjectNameValidator extends AbstractNameValidator {

    public ProjectNameValidator(String selfName) {
      super(selfName);
    }

    @Override
    public Violation isValidName(String value) {
      return new Violation() {
        @Override
        public String getMessage() {
          return localization.invalidName();
        }

        @Nullable
        @Override
        public String getCorrectedValue() {
          if (NameUtils.checkProjectName(value)) {
            return value.contains(" ") ? value.replaceAll(" ", "-") : value;
          }
          return null;
        }
      };
    }
  }
}

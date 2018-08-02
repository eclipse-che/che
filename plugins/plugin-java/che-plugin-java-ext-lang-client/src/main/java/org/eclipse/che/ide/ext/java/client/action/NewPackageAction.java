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
package org.eclipse.che.ide.ext.java.client.action;

import static com.google.common.base.Preconditions.checkState;
import static org.eclipse.che.ide.ext.java.client.resource.SourceFolderMarker.ID;
import static org.eclipse.che.ide.ext.java.client.util.JavaUtil.isJavaProject;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Folder;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.ide.ext.java.client.JavaUtils;
import org.eclipse.che.ide.newresource.AbstractNewResourceAction;
import org.eclipse.che.ide.resources.reveal.RevealResourceEvent;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.input.InputCallback;
import org.eclipse.che.ide.ui.dialogs.input.InputDialog;
import org.eclipse.che.ide.ui.dialogs.input.InputValidator;

/**
 * Action to create new Java package.
 *
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 */
@Singleton
public class NewPackageAction extends AbstractNewResourceAction {

  private final InputValidator nameValidator = new NameValidator();

  @Inject
  public NewPackageAction(
      JavaResources javaResources,
      JavaLocalizationConstant localizationConstant,
      DialogFactory dialogFactory,
      CoreLocalizationConstant coreLocalizationConstant,
      EventBus eventBus,
      AppContext appContext,
      NotificationManager notificationManager,
      Provider<EditorAgent> editorAgentProvider) {
    super(
        localizationConstant.actionNewPackageTitle(),
        localizationConstant.actionNewPackageDescription(),
        javaResources.packageItem(),
        dialogFactory,
        coreLocalizationConstant,
        eventBus,
        appContext,
        notificationManager,
        editorAgentProvider);
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent e) {
    final Resource resource = appContext.getResource();
    if (resource == null) {
      e.getPresentation().setEnabledAndVisible(false);
      return;
    }

    final Optional<Project> project = resource.getRelatedProject();
    if (!project.isPresent()) {
      e.getPresentation().setEnabledAndVisible(false);
      return;
    }

    e.getPresentation()
        .setEnabledAndVisible(
            isJavaProject(project.get()) && resource.getParentWithMarker(ID).isPresent());
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    InputDialog inputDialog =
        dialogFactory
            .createInputDialog(
                "New " + title,
                "Name:",
                new InputCallback() {
                  @Override
                  public void accepted(String value) {
                    onAccepted(value);
                  }
                },
                null)
            .withValidator(nameValidator);
    inputDialog.show();
  }

  private void onAccepted(String value) {
    final Resource resource = appContext.getResource();

    checkState(resource instanceof Container, "Parent should be a container");

    ((Container) resource)
        .newFolder(value.replace('.', '/'))
        .then(
            new Operation<Folder>() {
              @Override
              public void apply(Folder pkg) throws OperationException {
                eventBus.fireEvent(new RevealResourceEvent(pkg));
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError error) throws OperationException {
                dialogFactory
                    .createMessageDialog(
                        coreLocalizationConstant.invalidName(), error.getMessage(), null)
                    .show();
              }
            });
  }

  private class NameValidator implements InputValidator {
    @Nullable
    @Override
    public Violation validate(String value) {
      try {
        JavaUtils.checkPackageName(value);
      } catch (final IllegalStateException e) {
        return new Violation() {
          @Nullable
          @Override
          public String getMessage() {
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.isEmpty()) {
              return coreLocalizationConstant.invalidName();
            }
            return errorMessage;
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
}

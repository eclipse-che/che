/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.newresource;

import static com.google.common.base.Preconditions.checkState;
import static java.util.Collections.singletonList;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.inject.Provider;
import com.google.web.bindery.event.shared.EventBus;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.resources.reveal.RevealResourceEvent;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.input.InputCallback;
import org.eclipse.che.ide.ui.dialogs.input.InputDialog;
import org.eclipse.che.ide.ui.dialogs.input.InputValidator;
import org.eclipse.che.ide.util.NameUtils;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Implementation of an {@link BaseAction} that provides an ability to create new resource (e.g.
 * file, folder). After performing this action, it asks user for the resource's name and then
 * creates resource in the selected folder.
 *
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 * @author Vlad Zhukovskyi
 */
public abstract class AbstractNewResourceAction extends AbstractPerspectiveAction {

  protected final String title;
  protected final DialogFactory dialogFactory;
  protected final CoreLocalizationConstant coreLocalizationConstant;
  protected final EventBus eventBus;
  protected final AppContext appContext;
  protected final Provider<EditorAgent> editorAgentProvider;

  private final InputValidator fileNameValidator;
  private final NotificationManager notificationManager;

  public AbstractNewResourceAction(
      String title,
      String description,
      SVGResource svgIcon,
      DialogFactory dialogFactory,
      CoreLocalizationConstant coreLocalizationConstant,
      EventBus eventBus,
      AppContext appContext,
      NotificationManager notificationManager,
      Provider<EditorAgent> editorAgentProvider) {
    super(singletonList(PROJECT_PERSPECTIVE_ID), title, description, svgIcon);
    this.dialogFactory = dialogFactory;
    this.coreLocalizationConstant = coreLocalizationConstant;
    this.eventBus = eventBus;
    this.appContext = appContext;
    this.notificationManager = notificationManager;
    this.editorAgentProvider = editorAgentProvider;
    this.fileNameValidator = new FileNameValidator();
    this.title = title;
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    InputDialog inputDialog =
        dialogFactory
            .createInputDialog(
                coreLocalizationConstant.newResourceTitle(title),
                coreLocalizationConstant.newResourceLabel(title.toLowerCase()),
                new InputCallback() {
                  @Override
                  public void accepted(String value) {
                    createFile(value);
                  }
                },
                null)
            .withValidator(fileNameValidator);
    inputDialog.show();
  }

  final void createFile(String nameWithoutExtension) {
    final String name =
        getExtension().isEmpty()
            ? nameWithoutExtension
            : nameWithoutExtension + '.' + getExtension();

    Resource resource = appContext.getResource();

    if (!(resource instanceof Container)) {
      final Container parent = resource.getParent();

      checkState(parent != null, "Parent should be a container");

      resource = parent;
    }

    ((Container) resource)
        .newFile(name, getDefaultContent())
        .then(
            new Operation<File>() {
              @Override
              public void apply(File newFile) throws OperationException {
                editorAgentProvider.get().openEditor(newFile);
                eventBus.fireEvent(new RevealResourceEvent(newFile));
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError error) throws OperationException {
                notificationManager.notify(
                    "Failed to create resource", error.getMessage(), FAIL, FLOAT_MODE);
              }
            });
  }

  @Override
  public void updateInPerspective(@NotNull ActionEvent e) {
    e.getPresentation().setVisible(true);

    final Resource[] resources = appContext.getResources();

    if (resources != null && resources.length == 1) {
      final Resource resource = resources[0];

      if (resource instanceof Container) {
        e.getPresentation().setEnabled(true);
      } else {
        e.getPresentation().setEnabled(resource.getParent() != null);
      }

    } else {
      e.getPresentation().setEnabled(false);
    }
  }

  /** Returns extension (without dot) for a new resource. By default, returns an empty string. */
  protected String getExtension() {
    return "";
  }

  /** Returns default content for a new resource. By default, returns an empty string. */
  protected String getDefaultContent() {
    return "";
  }

  private class FileNameValidator implements InputValidator {
    @Nullable
    @Override
    public Violation validate(String value) {
      if (!NameUtils.checkFileName(value)) {
        return new Violation() {
          @Override
          public String getMessage() {
            return coreLocalizationConstant.invalidName();
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

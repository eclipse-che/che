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
package org.eclipse.che.ide.newresource;

import static com.google.common.base.Preconditions.checkState;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.Folder;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.resources.reveal.RevealResourceEvent;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.input.InputCallback;
import org.eclipse.che.ide.ui.dialogs.input.InputDialog;
import org.eclipse.che.ide.ui.dialogs.input.InputValidator;
import org.eclipse.che.ide.util.NameUtils;

/**
 * Action to create new folder.
 *
 * @author Artem Zatsarynnyi
 * @author Vlad Zhukovskyi
 */
@Singleton
public class NewFolderAction extends AbstractNewResourceAction {

  private InputValidator folderNameValidator;

  @Inject
  public NewFolderAction(
      CoreLocalizationConstant localizationConstant,
      Resources resources,
      DialogFactory dialogFactory,
      EventBus eventBus,
      AppContext appContext,
      NotificationManager notificationManager,
      Provider<EditorAgent> editorAgentProvider) {
    super(
        localizationConstant.actionNewFolderTitle(),
        localizationConstant.actionNewFolderDescription(),
        resources.defaultFolder(),
        dialogFactory,
        localizationConstant,
        eventBus,
        appContext,
        notificationManager,
        editorAgentProvider);
    this.folderNameValidator = new FolderNameValidator();
  }

  @Override
  public void actionPerformed(ActionEvent e) {

    InputDialog inputDialog =
        dialogFactory
            .createInputDialog(
                coreLocalizationConstant.newResourceTitle(
                    coreLocalizationConstant.actionNewFolderTitle()),
                coreLocalizationConstant.newResourceLabel(
                    coreLocalizationConstant.actionNewFolderTitle().toLowerCase()),
                new InputCallback() {
                  @Override
                  public void accepted(String value) {
                    createFolder(value);
                  }
                },
                null)
            .withValidator(folderNameValidator);
    inputDialog.show();
  }

  final void createFolder(String name) {
    Resource resource = appContext.getResource();

    if (!(resource instanceof Container)) {
      final Container parent = resource.getParent();

      checkState(parent != null, "Parent should be a container");

      resource = parent;
    }

    ((Container) resource)
        .newFolder(name)
        .then(
            new Operation<Folder>() {
              @Override
              public void apply(Folder folder) throws OperationException {
                eventBus.fireEvent(new RevealResourceEvent(folder));
              }
            })
        .catchError(
            new Operation<PromiseError>() {
              @Override
              public void apply(PromiseError error) throws OperationException {
                dialogFactory.createMessageDialog("Error", error.getMessage(), null).show();
              }
            });
  }

  private class FolderNameValidator implements InputValidator {
    @Nullable
    @Override
    public Violation validate(String value) {
      if (!NameUtils.checkFolderName(value)) {
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

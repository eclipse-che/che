/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.actions;

import static java.util.Collections.singletonList;
import static org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper.createFromCallback;
import static org.eclipse.che.ide.part.perspectives.project.ProjectPerspective.PROJECT_PERSPECTIVE_ID;

import com.google.gwt.core.client.Callback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.AbstractPerspectiveAction;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.PromisableAction;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.api.command.CommandManager;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.parts.ActivePartChangedEvent;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.resources.Resource;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.command.explorer.CommandsExplorerPresenter;
import org.eclipse.che.ide.command.explorer.CommandsExplorerView;
import org.eclipse.che.ide.resources.DeleteResourceManager;

/**
 * Deletes resources which are in application context.
 *
 * @author Artem Zatsarynnyi
 * @author Dmitry Shnurenko
 * @author Vlad Zhukovskyi
 * @see DeleteResourceManager
 */
@Singleton
public class DeleteResourceAction extends AbstractPerspectiveAction implements PromisableAction {

  private final DeleteResourceManager deleteResourceManager;
  private final AppContext appContext;
  private final CommandsExplorerPresenter commandsExplorer;
  private final CommandManager commandManager;

  private Callback<Void, Throwable> actionCompletedCallBack;
  private PartPresenter activePart;

  @Inject
  public DeleteResourceAction(
      Resources resources,
      DeleteResourceManager deleteResourceManager,
      CoreLocalizationConstant localization,
      AppContext appContext,
      EventBus eventBus,
      CommandsExplorerPresenter commandsExplorer,
      CommandManager commandManager) {
    super(
        singletonList(PROJECT_PERSPECTIVE_ID),
        localization.deleteItemActionText(),
        localization.deleteItemActionDescription(),
        resources.delete());
    this.deleteResourceManager = deleteResourceManager;
    this.appContext = appContext;
    this.commandsExplorer = commandsExplorer;
    this.commandManager = commandManager;

    eventBus.addHandler(ActivePartChangedEvent.TYPE, event -> activePart = event.getActivePart());
  }

  /** {@inheritDoc} */
  @Override
  public void actionPerformed(ActionEvent e) {
    if (activePart instanceof CommandsExplorerPresenter) {
      CommandImpl command =
          ((CommandsExplorerView) commandsExplorer.getView()).getSelectedCommand();
      if (command != null) {
        commandManager
            .removeCommand(command.getName())
            .then(this::onSuccess)
            .catchError(this::onFailure);
      }
    } else {
      deleteResourceManager
          .delete(true, appContext.getResources())
          .then(this::onSuccess)
          .catchError(this::onFailure);
    }
  }

  /** {@inheritDoc} */
  @Override
  public void updateInPerspective(@NotNull ActionEvent event) {
    event.getPresentation().setVisible(true);

    if (activePart instanceof CommandsExplorerPresenter) {
      CommandImpl command =
          ((CommandsExplorerView) commandsExplorer.getView()).getSelectedCommand();
      event.getPresentation().setEnabled(command != null);
      return;
    }

    final Resource[] resources = appContext.getResources();

    event
        .getPresentation()
        .setEnabled(
            resources != null
                && resources.length > 0
                && !(activePart instanceof TextEditor)
                && !(activePart.getSelection() instanceof Selection.NoSelectionProvided));
  }

  /** {@inheritDoc} */
  @Override
  public Promise<Void> promise(final ActionEvent event) {
    final CallbackPromiseHelper.Call<Void, Throwable> call =
        callback -> {
          actionCompletedCallBack = callback;
          actionPerformed(event);
        };

    return createFromCallback(call);
  }

  private void onSuccess(Void arg) {
    if (actionCompletedCallBack != null) {
      actionCompletedCallBack.onSuccess(arg);
    }
  }

  private void onFailure(PromiseError error) {
    if (actionCompletedCallBack != null) {
      actionCompletedCallBack.onFailure(error.getCause());
    }
  }
}

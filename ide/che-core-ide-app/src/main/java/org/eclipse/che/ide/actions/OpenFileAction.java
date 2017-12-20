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
package org.eclipse.che.ide.actions;

import static java.lang.Integer.parseInt;
import static org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper.createFromCallback;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

import com.google.gwt.core.client.Callback;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper.Call;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.BaseAction;
import org.eclipse.che.ide.api.action.PromisableAction;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.ActivePartChangedEvent;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.loging.Log;

/**
 * @author Sergii Leschenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class OpenFileAction extends BaseAction implements PromisableAction {

  /** ID of the parameter to specify file path to open. */
  public static final String FILE_PARAM_ID = "file";

  public static final String LINE_PARAM_ID = "line";

  private final EventBus eventBus;
  private final CoreLocalizationConstant localization;
  private final NotificationManager notificationManager;
  private final AppContext appContext;
  private final EditorAgent editorAgent;

  private Callback<Void, Throwable> actionCompletedCallback;

  @Inject
  public OpenFileAction(
      EventBus eventBus,
      CoreLocalizationConstant localization,
      NotificationManager notificationManager,
      AppContext appContext,
      EditorAgent editorAgent) {
    this.eventBus = eventBus;
    this.localization = localization;
    this.notificationManager = notificationManager;
    this.appContext = appContext;
    this.editorAgent = editorAgent;
  }

  @Override
  public void actionPerformed(ActionEvent event) {
    if (event.getParameters() == null) {
      Log.error(getClass(), localization.canNotOpenFileWithoutParams());
      return;
    }

    final String pathToOpen = event.getParameters().get(FILE_PARAM_ID);
    if (pathToOpen == null) {
      Log.error(getClass(), localization.fileToOpenIsNotSpecified());
      return;
    }

    appContext
        .getWorkspaceRoot()
        .getFile(pathToOpen)
        .then(
            optionalFile -> {
              if (optionalFile.isPresent()) {
                if (actionCompletedCallback != null) {
                  actionCompletedCallback.onSuccess(null);
                }

                editorAgent.openEditor(
                    optionalFile.get(),
                    new EditorAgent.OpenEditorCallback() {
                      @Override
                      public void onEditorOpened(EditorPartPresenter editor) {
                        scrollToLine(editor, event.getParameters().get(LINE_PARAM_ID));
                      }

                      @Override
                      public void onInitializationFailed() {}

                      @Override
                      public void onEditorActivated(EditorPartPresenter editor) {
                        scrollToLine(editor, event.getParameters().get(LINE_PARAM_ID));
                      }
                    });

              } else {
                if (actionCompletedCallback != null) {
                  actionCompletedCallback.onFailure(null);
                }

                notificationManager.notify(
                    localization.unableOpenResource(pathToOpen), FAIL, FLOAT_MODE);
              }
            });
  }

  private void scrollToLine(EditorPartPresenter editor, String lineParam) {
    if (!(editor instanceof TextEditor)) {
      return;
    }
    new Timer() {
      @Override
      public void run() {
        try {
          int lineNumber = parseInt(lineParam);
          TextEditor textEditor = (TextEditor) editor;
          textEditor.getDocument().setCursorPosition(new TextPosition(lineNumber - 1, 0));
        } catch (NumberFormatException e) {
          Log.error(getClass(), localization.fileToOpenLineIsNotANumber());
        }
      }
    }.schedule(300);
  }

  @Override
  public Promise<Void> promise(final ActionEvent actionEvent) {
    if (actionEvent.getParameters() == null) {
      return Promises.reject(JsPromiseError.create(localization.canNotOpenFileWithoutParams()));
    }

    final String pathToOpen = actionEvent.getParameters().get(FILE_PARAM_ID);
    if (pathToOpen == null) {
      return Promises.reject(JsPromiseError.create(localization.fileToOpenIsNotSpecified()));
    }

    final Call<Void, Throwable> call =
        new Call<Void, Throwable>() {
          HandlerRegistration handlerRegistration;

          @Override
          public void makeCall(final Callback<Void, Throwable> callback) {
            actionCompletedCallback = callback;
            handlerRegistration =
                eventBus.addHandler(
                    ActivePartChangedEvent.TYPE,
                    event -> {
                      if (event.getActivePart() instanceof EditorPartPresenter) {
                        EditorPartPresenter editor = (EditorPartPresenter) event.getActivePart();
                        handlerRegistration.removeHandler();
                        if (Path.valueOf(pathToOpen)
                            .equals(editor.getEditorInput().getFile().getLocation())) {
                          callback.onSuccess(null);
                        }
                      }
                    });
            actionPerformed(actionEvent);
          }
        };

    return createFromCallback(call);
  }
}

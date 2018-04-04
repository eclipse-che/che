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
package org.eclipse.che.ide.upload.file;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;

import com.google.common.base.Optional;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationListener;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.resources.Container;
import org.eclipse.che.ide.api.resources.File;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.resources.reveal.RevealResourceEvent;

/**
 * The purpose of this class is upload file
 *
 * @author Roman Nikitenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class UploadFilePresenter implements UploadFileView.ActionDelegate {

  private final UploadFileView view;
  private final AppContext appContext;
  private final EventBus eventBus;
  private final NotificationManager notificationManager;
  private final EditorAgent editorAgent;
  private final CoreLocalizationConstant locale;
  private Container container;

  @Inject
  public UploadFilePresenter(
      UploadFileView view,
      AppContext appContext,
      EventBus eventBus,
      NotificationManager notificationManager,
      CoreLocalizationConstant locale,
      EditorAgent editorAgent) {
    this.appContext = appContext;
    this.eventBus = eventBus;
    this.view = view;
    this.locale = locale;
    this.notificationManager = notificationManager;
    this.editorAgent = editorAgent;
    this.view.setDelegate(this);

    this.view.setEnabledUploadButton(false);
    this.view.setEncoding(FormPanel.ENCODING_MULTIPART);
  }

  /** Show dialog. */
  public void showDialog(Container container) {
    this.container = container;
    view.showDialog();
    view.setAction(
        appContext.getWsAgentServerApiEndpoint() + "/project/uploadfile" + container.getLocation());
  }

  /** {@inheritDoc} */
  @Override
  public void onCancelClicked() {
    view.closeDialog();
  }

  /** {@inheritDoc} */
  @Override
  public void onSubmitComplete(String result) {
    if (!isNullOrEmpty(result)) {
      view.closeDialog();
      notificationManager.notify(
          locale.failedToUploadFiles(),
          parseMessage(result),
          StatusNotification.Status.FAIL,
          FLOAT_MODE);
      return;
    }

    container
        .getFile(Path.valueOf(view.getFileName()))
        .then(
            new Operation<Optional<File>>() {
              @Override
              public void apply(final Optional<File> file) throws OperationException {

                if (file.isPresent()) {
                  eventBus.fireEvent(new RevealResourceEvent(file.get()));

                  final NotificationListener notificationListener =
                      new NotificationListener() {
                        boolean clicked = false;

                        @Override
                        public void onClick(Notification notification) {
                          if (!clicked) {
                            editorAgent.openEditor(file.get());
                            clicked = true;
                            notification.setListener(null);
                            notification.setContent("");
                          }
                        }

                        @Override
                        public void onDoubleClick(Notification notification) {
                          // stub
                        }

                        @Override
                        public void onClose(Notification notification) {
                          // stub
                        }
                      };

                  notificationManager.notify(
                      "File '" + view.getFileName() + "' has uploaded successfully",
                      "Click here to open it",
                      StatusNotification.Status.SUCCESS,
                      FLOAT_MODE,
                      notificationListener);

                  view.closeDialog();
                }
              }
            });

    // TODO this should process editor agent
    //        if (view.isOverwriteFileSelected()) {
    //            String path = ((HasStorablePath)getResourceBasedNode()).getStorablePath() + "/" +
    // view.getFileName();
    //            eventBus.fireEvent(new FileContentUpdateEvent(path));
    //        }
  }

  /** {@inheritDoc} */
  @Override
  public void onUploadClicked() {
    view.submit();
  }

  /** {@inheritDoc} */
  @Override
  public void onFileNameChanged() {
    String fileName = view.getFileName();
    boolean enabled = !fileName.isEmpty();
    view.setEnabledUploadButton(enabled);
  }

  private String parseMessage(String message) {
    int startIndex = 0;
    int endIndex = -1;

    if (message.contains("<pre>message:")) {
      startIndex = message.indexOf("<pre>message:") + "<pre>message:".length();
    } else if (message.contains("<pre>")) {
      startIndex = message.indexOf("<pre>") + "<pre>".length();
    }

    if (message.contains("</pre>")) {
      endIndex = message.indexOf("</pre>");
    }
    return (endIndex != -1)
        ? message.substring(startIndex, endIndex)
        : message.substring(startIndex);
  }
}

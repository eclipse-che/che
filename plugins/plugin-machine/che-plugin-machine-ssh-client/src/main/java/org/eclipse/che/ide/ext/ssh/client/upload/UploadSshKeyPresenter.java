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
package org.eclipse.che.ide.ext.ssh.client.upload;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;
import static org.eclipse.che.ide.util.StringUtils.isNullOrEmpty;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.ext.ssh.client.SshLocalizationConstant;

/**
 * Main appointment of this class is upload public SSH key to the server.
 *
 * @author Evgen Vidolob
 */
@Singleton
public class UploadSshKeyPresenter implements UploadSshKeyView.ActionDelegate {
  private UploadSshKeyView view;
  private SshLocalizationConstant constant;
  private String restContext;
  private NotificationManager notificationManager;
  private AsyncCallback<Void> callback;
  private AppContext appContext;

  @Inject
  public UploadSshKeyPresenter(
      UploadSshKeyView view,
      SshLocalizationConstant constant,
      AppContext appContext,
      NotificationManager notificationManager) {
    this.view = view;
    this.view.setDelegate(this);
    this.constant = constant;
    this.restContext = appContext.getMasterEndpoint();
    this.notificationManager = notificationManager;
    this.appContext = appContext;
  }

  /** Show dialog. */
  public void showDialog(@NotNull AsyncCallback<Void> callback) {
    this.callback = callback;
    view.setMessage("");
    view.setHost("");
    view.setEnabledUploadButton(false);
    view.showDialog();
  }

  @Override
  public void onCancelClicked() {
    view.close();
  }

  @Override
  public void onUploadClicked() {
    String host = view.getHost();
    if (host.isEmpty()) {
      view.setMessage(constant.hostValidationError());
      return;
    }
    view.setEncoding(FormPanel.ENCODING_MULTIPART);

    String action = restContext + "/ssh";
    StringBuilder queryParametersBuilder = new StringBuilder();

    String csrfToken = appContext.getProperties().get("X-CSRF-Token");
    if (!isNullOrEmpty(csrfToken)) {
      queryParametersBuilder.append("&X-CSRF-Token=").append(csrfToken);
    }

    String machineToken = appContext.getProperties().get("machineToken");
    if (!isNullOrEmpty(machineToken)) {
      queryParametersBuilder.append("&token=").append(machineToken);
    }

    String queryParameters = queryParametersBuilder.toString();
    if (!isNullOrEmpty(queryParameters)) {
      action += queryParameters.replaceFirst("&", "?");
    }

    view.setAction(action);
    view.submit();
  }

  @Override
  public void onSubmitComplete(@NotNull String result) {
    if (result.isEmpty()) {
      view.close();
      callback.onSuccess(null);
    } else {
      notificationManager.notify(constant.failedToUploadSshKey(), result, FAIL, FLOAT_MODE);
      callback.onFailure(new Throwable(result));
    }
  }

  @Override
  public void onFileNameChanged() {
    String fileName = view.getFileName();
    view.setEnabledUploadButton(!fileName.isEmpty());
  }
}

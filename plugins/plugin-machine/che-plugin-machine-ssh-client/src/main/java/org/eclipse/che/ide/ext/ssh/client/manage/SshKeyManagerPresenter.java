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
package org.eclipse.che.ide.ext.ssh.client.manage;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.ssh.shared.dto.SshPairDto;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.preferences.AbstractPreferencePagePresenter;
import org.eclipse.che.ide.api.ssh.SshServiceClient;
import org.eclipse.che.ide.ext.ssh.client.SshLocalizationConstant;
import org.eclipse.che.ide.ext.ssh.client.upload.UploadSshKeyPresenter;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.util.browser.BrowserUtils;

/**
 * The presenter for managing ssh keys.
 *
 * @author Evgen Vidolob
 * @author Sergii Leschenko
 */
@Singleton
public class SshKeyManagerPresenter extends AbstractPreferencePagePresenter
    implements SshKeyManagerView.ActionDelegate {
  private static final String SSH_SERVICE = "machine";

  private final DialogFactory dialogFactory;
  private final ShowSshKeyView showSshKeyView;
  private final SshKeyManagerView view;
  private final SshServiceClient service;
  private final SshLocalizationConstant constant;
  private final UploadSshKeyPresenter uploadSshKeyPresenter;
  private final NotificationManager notificationManager;

  @Inject
  public SshKeyManagerPresenter(
      SshKeyManagerView view,
      SshServiceClient service,
      SshLocalizationConstant constant,
      UploadSshKeyPresenter uploadSshKeyPresenter,
      NotificationManager notificationManager,
      DialogFactory dialogFactory,
      ShowSshKeyView showSshKeyView) {
    super(constant.sshManagerTitle(), constant.sshManagerCategory());

    this.view = view;
    this.dialogFactory = dialogFactory;
    this.showSshKeyView = showSshKeyView;
    this.view.setDelegate(this);
    this.service = service;
    this.constant = constant;
    this.uploadSshKeyPresenter = uploadSshKeyPresenter;
    this.notificationManager = notificationManager;
  }

  @Override
  public void onViewClicked(@NotNull final SshPairDto pair) {
    showSshKeyView.show(pair.getName(), pair.getPublicKey());
  }

  @Override
  public void onDeleteClicked(@NotNull final SshPairDto pair) {
    dialogFactory
        .createConfirmDialog(
            constant.deleteSshKeyTitle(),
            constant.deleteSshKeyQuestion(pair.getName()),
            () -> deleteKey(pair),
            getCancelCallback())
        .show();
  }

  private CancelCallback getCancelCallback() {
    return () -> {};
  }

  private void deleteKey(final SshPairDto key) {
    service
        .deletePair(key.getService(), key.getName())
        .then(
            arg -> {
              refreshKeys();
            })
        .catchError(
            arg -> {
              notificationManager.notify(arg.getMessage(), FAIL, FLOAT_MODE);
            });
  }

  @Override
  public void onGenerateClicked() {
    dialogFactory
        .createInputDialog(
            constant.generateSshKeyTitle(),
            constant.sshKeyTitle(),
            this::generateKey,
            getCancelCallback())
        .show();
  }

  private void generateKey(String host) {
    service
        .generatePair(SSH_SERVICE, host)
        .then(
            pair -> {
              downloadPrivateKey(pair.getPrivateKey());
              refreshKeys();
            })
        .catchError(
            arg -> {
              notificationManager.notify(
                  constant.failedToGenerateSshKey(), arg.getMessage(), FAIL, FLOAT_MODE);
            });
  }

  private void downloadPrivateKey(final String privateKey) {
    dialogFactory
        .createConfirmDialog(
            constant.downloadPrivateKeyTitle(),
            constant.downloadPrivateKeyMessage(),
            () ->
                BrowserUtils.openInNewTab(
                    "data:application/x-pem-key," + URL.encodePathSegment(privateKey)),
            getCancelCallback())
        .show();
  }

  @Override
  public void onUploadClicked() {
    uploadSshKeyPresenter.showDialog(
        new AsyncCallback<Void>() {
          @Override
          public void onSuccess(Void result) {
            refreshKeys();
          }

          @Override
          public void onFailure(Throwable caught) {}
        });
  }

  @Override
  public boolean isDirty() {
    return false;
  }

  @Override
  public void go(AcceptsOneWidget container) {
    refreshKeys();
    container.setWidget(view);
  }

  private void refreshKeys() {
    service
        .getPairs(SSH_SERVICE)
        .then(view::setPairs)
        .catchError(
            arg -> {
              notificationManager.notify(constant.failedToLoadSshKeys(), FAIL, FLOAT_MODE);
            });
  }

  @Override
  public void storeChanges() {}

  @Override
  public void revertChanges() {}
}

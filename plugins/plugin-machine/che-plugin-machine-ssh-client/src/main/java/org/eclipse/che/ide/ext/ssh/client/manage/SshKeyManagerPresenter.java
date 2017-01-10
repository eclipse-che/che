/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.ssh.client.manage;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.ide.api.ssh.SshServiceClient;
import org.eclipse.che.api.ssh.shared.dto.SshPairDto;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.preferences.AbstractPreferencePagePresenter;
import org.eclipse.che.ide.ext.ssh.client.SshLocalizationConstant;
import org.eclipse.che.ide.ext.ssh.client.upload.UploadSshKeyPresenter;
import org.eclipse.che.ide.api.dialogs.CancelCallback;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.eclipse.che.ide.api.dialogs.InputCallback;

import javax.validation.constraints.NotNull;
import java.util.List;

import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.FAIL;

/**
 * The presenter for managing ssh keys.
 *
 * @author Evgen Vidolob
 * @author Sergii Leschenko
 */
@Singleton
public class SshKeyManagerPresenter extends AbstractPreferencePagePresenter implements SshKeyManagerView.ActionDelegate {
    public static final String SSH_SERVICE = "machine";

    private final DialogFactory dialogFactory;
    private final ShowSshKeyView showSshKeyView;
    private final SshKeyManagerView       view;
    private final SshServiceClient        service;
    private final SshLocalizationConstant constant;
    private final UploadSshKeyPresenter   uploadSshKeyPresenter;
    private final NotificationManager     notificationManager;

    @Inject
    public SshKeyManagerPresenter(SshKeyManagerView view,
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
//        dialogFactory.createMessageDialog(constant.publicSshKeyField() + pair.getName(), pair.getPublicKey(), null).show();
    }

    @Override
    public void onDeleteClicked(@NotNull final SshPairDto pair) {
        dialogFactory.createConfirmDialog(constant.deleteSshKeyTitle(),
                                          constant.deleteSshKeyQuestion(pair.getName()),
                                          new ConfirmCallback() {
                                              @Override
                                              public void accepted() {
                                                  deleteKey(pair);
                                              }
                                          },
                                          getCancelCallback()).show();
    }

    private CancelCallback getCancelCallback() {
        return new CancelCallback() {
            @Override
            public void cancelled() {
            }
        };
    }

    private void deleteKey(final SshPairDto key) {
        service.deletePair(key.getService(), key.getName())
               .then(new Operation<Void>() {
                   @Override
                   public void apply(Void arg) throws OperationException {
                       refreshKeys();
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(PromiseError arg) throws OperationException {
                       notificationManager.notify(arg.getMessage(), FAIL, FLOAT_MODE);
                   }
               });
    }

    @Override
    public void onGenerateClicked() {
        dialogFactory.createInputDialog(constant.generateSshKeyTitle(),
                                        constant.sshKeyTitle(),
                                        new InputCallback() {
                                            @Override
                                            public void accepted(String host) {
                                                generateKey(host);
                                            }
                                        },
                                        getCancelCallback())
                     .show();
    }

    private void generateKey(String host) {
        service.generatePair(SSH_SERVICE, host)
               .then(new Operation<SshPairDto>() {
                   @Override
                   public void apply(SshPairDto pair) throws OperationException {
                       downloadPrivateKey(pair.getPrivateKey());
                       refreshKeys();
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(PromiseError arg) throws OperationException {
                       notificationManager.notify(constant.failedToGenerateSshKey(), arg.getMessage(), FAIL, FLOAT_MODE);
                   }
               });
    }

    private void downloadPrivateKey(final String privateKey) {
        dialogFactory.createConfirmDialog(constant.downloadPrivateKeyTitle(),
                                          constant.downloadPrivateKeyMessage(),
                                          new ConfirmCallback() {
                                              @Override
                                              public void accepted() {
                                                  Window.open("data:application/x-pem-key," + URL.encodePathSegment(privateKey), "_blank", null);
                                              }
                                          },
                                          getCancelCallback()).show();
    }

    @Override
    public void onUploadClicked() {
        uploadSshKeyPresenter.showDialog(new AsyncCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                refreshKeys();
            }

            @Override
            public void onFailure(Throwable caught) {
            }
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
        service.getPairs(SSH_SERVICE)
               .then(new Operation<List<SshPairDto>>() {
                   @Override
                   public void apply(List<SshPairDto> result) throws OperationException {
                       view.setPairs(result);
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(PromiseError arg) throws OperationException {
                       notificationManager.notify(constant.failedToLoadSshKeys(), FAIL, FLOAT_MODE);
                   }
               });
    }

    @Override
    public void storeChanges() {
    }

    @Override
    public void revertChanges() {
    }
}

/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.git.ssh.client.manage;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.ssh.gwt.client.SshServiceClient;
import org.eclipse.che.api.ssh.shared.dto.SshPairDto;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentUser;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.preferences.AbstractPreferencePagePresenter;
import org.eclipse.che.ide.ext.git.ssh.client.GitSshKeyUploaderRegistry;
import org.eclipse.che.ide.ext.git.ssh.client.SshKeyUploader;
import org.eclipse.che.ide.ext.git.ssh.client.SshLocalizationConstant;
import org.eclipse.che.ide.ext.git.ssh.client.upload.UploadSshKeyPresenter;
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
    public static final String GITHUB_HOST     = "github.com";
    public static final String GIT_SSH_SERVICE = "git";

    private final AppContext                appContext;
    private final DialogFactory             dialogFactory;
    private final SshKeyManagerView         view;
    private final SshServiceClient          service;
    private final GitSshKeyUploaderRegistry registry;
    private final SshLocalizationConstant   constant;
    private final UploadSshKeyPresenter     uploadSshKeyPresenter;
    private final NotificationManager       notificationManager;

    @Inject
    public SshKeyManagerPresenter(SshKeyManagerView view,
                                  SshServiceClient service,
                                  AppContext appContext,
                                  SshLocalizationConstant constant,
                                  UploadSshKeyPresenter uploadSshKeyPresenter,
                                  NotificationManager notificationManager,
                                  DialogFactory dialogFactory,
                                  GitSshKeyUploaderRegistry registry) {
        super(constant.sshManagerTitle(), constant.sshManagerCategory());

        this.view = view;
        this.appContext = appContext;
        this.dialogFactory = dialogFactory;
        this.registry = registry;
        this.view.setDelegate(this);
        this.service = service;
        this.constant = constant;
        this.uploadSshKeyPresenter = uploadSshKeyPresenter;
        this.notificationManager = notificationManager;
    }

    /** {@inheritDoc} */
    @Override
    public void onViewClicked(@NotNull final SshPairDto pair) {
        dialogFactory.createMessageDialog(constant.publicSshKeyField() + pair.getName(), pair.getPublicKey(), null).show();
    }

    /** {@inheritDoc} */
    @Override
    public void onDeleteClicked(@NotNull final SshPairDto pair) {
        dialogFactory.createConfirmDialog(constant.deleteSshKeyTitle(),
                                          constant.deleteSshKeyQuestion(pair.getName()).asString(),
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
                //for now do nothing but it need for tests
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

    /** {@inheritDoc} */
    @Override
    public void onGenerateClicked() {
        dialogFactory.createInputDialog(constant.generateSshKeyTitle(),
                                        constant.generateSshKeyHostname(),
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
        service.generatePair(GIT_SSH_SERVICE, host)
               .then(new Operation<SshPairDto>() {
                   @Override
                   public void apply(SshPairDto arg) throws OperationException {
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public void onGenerateGithubKeyClicked() {
        CurrentUser user = appContext.getCurrentUser();
        final SshKeyUploader githubUploader = registry.getUploaders().get(GITHUB_HOST);
        if (user != null && githubUploader != null) {
            githubUploader.uploadKey(user.getProfile().getId(), new AsyncCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    refreshKeys();
                }

                @Override
                public void onFailure(Throwable exception) {
                    getFailedKey(GITHUB_HOST);
                }
            });
        } else {
            notificationManager.notify(constant.failedToGenerateSshKey(), constant.sshKeysProviderNotFound(GITHUB_HOST), FAIL, FLOAT_MODE);
        }
    }

    /** Need to remove failed uploaded keys from local storage if they can't be uploaded to github */
    private void getFailedKey(final String host) {
        service.getPairs(GIT_SSH_SERVICE)
               .then(new Operation<List<SshPairDto>>() {
                   @Override
                   public void apply(List<SshPairDto> result) throws OperationException {
                       for (SshPairDto key : result) {
                           if (key.getName().equals(host)) {
                               removeFailedKey(key);
                               return;
                           }
                       }
                       refreshKeys();
                   }
               })
               .catchError(new Operation<PromiseError>() {
                   @Override
                   public void apply(PromiseError arg) throws OperationException {
                       refreshKeys();
                       notificationManager.notify(constant.failedToLoadSshKeys(), FAIL, FLOAT_MODE);
                   }
               });
    }

    /**
     * Remove failed key.
     *
     * @param key
     *         failed key
     */
    private void removeFailedKey(@NotNull final SshPairDto key) {
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
                       notificationManager.notify(constant.deleteSshKeyFailed(), FAIL, FLOAT_MODE);
                       refreshKeys();
                   }
               });
    }

    /** {@inheritDoc} */
    @Override
    public boolean isDirty() {
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        refreshKeys();
        container.setWidget(view);
    }

    /** Refresh ssh keys. */
    private void refreshKeys() {
        service.getPairs(GIT_SSH_SERVICE)
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

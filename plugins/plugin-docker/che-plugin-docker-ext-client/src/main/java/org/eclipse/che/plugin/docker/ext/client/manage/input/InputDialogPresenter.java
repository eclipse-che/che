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
package org.eclipse.che.plugin.docker.ext.client.manage.input;

import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;

import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.plugin.docker.client.dto.AuthConfig;
import org.eclipse.che.plugin.docker.ext.client.DockerLocalizationConstant;
import org.eclipse.che.plugin.docker.ext.client.manage.input.callback.InputCallback;

import javax.validation.constraints.NotNull;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * {@link InputDialog} implementation.
 *
 * @author Sergii Leschenko
 */
public class InputDialogPresenter implements InputDialog, InputDialogView.ActionDelegate {
    public enum InputMode {
        CREATE,
        EDIT,
        CREATE_DOCKERHUB,
        EDIT_DOCKERHUB
    }

    private final InputDialogView            view;
    private final InputCallback              inputCallback;
    private final DtoFactory                 dtoFactory;
    private final DockerLocalizationConstant locale;

    @AssistedInject
    public InputDialogPresenter(@Assisted InputMode inputMode,
                                @Nullable @Assisted InputCallback inputCallback,
                                @NotNull InputDialogView view,
                                DtoFactory dtoFactory,
                                DockerLocalizationConstant locale) {
        this.locale = locale;
        switch (inputMode) {
            case CREATE:
                view.setTitle(locale.addPrivateRegitryTitle());
                break;
            case EDIT:
                view.setTitle(locale.editPrivateRegistryTitle());
                view.setReadOnlyServer();
                view.setFooterButtonText(locale.inputCredentialsEditButtonText());
                break;
            case CREATE_DOCKERHUB:
                view.setTitle(locale.addDockerhubAccountTitle());
                view.setHideServer();
                break;
            case EDIT_DOCKERHUB:
                view.setTitle(locale.editDockerhubAccountTitle());
                view.setHideServer();
                view.setFooterButtonText(locale.inputCredentialsEditButtonText());
                break;
        }
        this.view = view;
        this.inputCallback = inputCallback;
        this.dtoFactory = dtoFactory;
        this.view.setDelegate(this);
    }

    @Override
    public void cancelled() {
        this.view.closeDialog();
    }

    @Override
    public void accepted() {
        if (isInputValid()) {
            view.closeDialog();
            if (inputCallback != null) {
                inputCallback.saved(dtoFactory.createDto(AuthConfig.class)
                                              .withEmail(view.getEmail())
                                              .withPassword(view.getPassword())
                                              .withServeraddress(view.getServerAddress())
                                              .withUsername(view.getUsername()));
            }
        }
    }

    @Override
    public void onEnterClicked() {
        accepted();
    }

    @Override
    public void dataChanged() {
        view.hideErrorHint();
    }

    @Override
    public void show() {
        view.showDialog();
    }

    @Override
    public void setData(AuthConfig authConfig) {
        view.setUsername(authConfig.getUsername());
        view.setServerAddress(authConfig.getServeraddress());
        view.setEmail(authConfig.getEmail());
        view.setPassword(authConfig.getPassword());
    }

    private boolean isInputValid() {
        String invalidField = null;

        if (view.isVisibleServer() && view.getServerAddress().trim().isEmpty()) {
            invalidField = locale.inputCredentialsServerAddressLabel().toLowerCase();
        }

        if (invalidField == null && view.getUsername().trim().isEmpty()) {
            invalidField = locale.inputCredentialsUsernameLabel().toLowerCase();
        }

        if (invalidField == null && view.getEmail().trim().isEmpty()) {
            invalidField = locale.inputCredentialsEmailLabel().toLowerCase();
        }

        if (invalidField == null && view.getPassword().trim().isEmpty()) {
            invalidField = locale.inputCredentialsPasswordLabel().toLowerCase();
        }

        if (invalidField != null) {
            view.showErrorHint(locale.inputMissedValueOfField(invalidField));
            return false;
        } else {
            view.hideErrorHint();
            return true;
        }
    }
}

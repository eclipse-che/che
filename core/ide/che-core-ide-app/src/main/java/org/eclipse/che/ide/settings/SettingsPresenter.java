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
package org.eclipse.che.ide.settings;

import com.google.inject.Inject;

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.settings.common.SettingsPagePresenter;
import org.eclipse.che.ide.ui.dialogs.CancelCallback;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The class contains business logic which allow us control to settings. Save, revert or change them.
 *
 * @author Dmitry Shnurenko
 */
public class SettingsPresenter implements SettingsView.ActionDelegate, SettingsPagePresenter.DirtyStateListener {

    private final SettingsView               view;
    private final Set<SettingsPagePresenter> settings;
    private final DialogFactory              dialogFactory;
    private final CoreLocalizationConstant   locale;

    private Map<String, Set<SettingsPagePresenter>> settingsMap;

    @Inject
    public SettingsPresenter(SettingsView view,
                             Set<SettingsPagePresenter> settings,
                             DialogFactory dialogFactory,
                             CoreLocalizationConstant locale) {
        this.view = view;
        this.view.setDelegate(this);

        this.settings = settings;
        this.dialogFactory = dialogFactory;
        this.locale = locale;

        this.settingsMap = new HashMap<>();

        for (SettingsPagePresenter preference : settings) {
            preference.setUpdateDelegate(this);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onSaveClicked() {
        for (SettingsPagePresenter preference : settings) {
            if (preference.isDirty()) {
                preference.storeChanges();
            }
        }

        view.enableSaveButton(false);
    }

    /** {@inheritDoc} */
    @Override
    public void onRefreshClicked() {
        for (SettingsPagePresenter presenter : settings) {
            presenter.revertChanges();
        }

        view.enableSaveButton(false);
    }

    /** {@inheritDoc} */
    @Override
    public void onCloseClicked() {
        boolean haveUnsavedData = false;

        for (SettingsPagePresenter presenter : settings) {
            if (presenter.isDirty()) {
                haveUnsavedData = true;
            }
        }

        if (!haveUnsavedData) {
            view.close();

            return;
        }

        dialogFactory.createConfirmDialog("", locale.messagesPromptSaveChanges(), getConfirmCallback(), getCancelCallback()).show();
    }

    private ConfirmCallback getConfirmCallback() {
        return new ConfirmCallback() {
            @Override
            public void accepted() {
                onSaveClicked();

                view.close();
            }
        };
    }

    private CancelCallback getCancelCallback() {
        return new CancelCallback() {
            @Override
            public void cancelled() {
                for (SettingsPagePresenter presenter : settings) {
                    if (presenter.isDirty()) {
                        presenter.revertChanges();
                    }
                }
                view.close();
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public void onSettingsGroupSelected(@NotNull SettingsPagePresenter settings) {
        settings.go(view.getContentPanel());
    }

    /** {@inheritDoc} */
    @Override
    public void onDirtyChanged() {
        for (SettingsPagePresenter presenter : settings) {
            if (presenter.isDirty()) {
                view.enableSaveButton(true);
                return;
            }
        }
        view.enableSaveButton(false);
    }

    /** Shows dialog window which contains properties for project settings. */
    public void show() {
        if (!settingsMap.isEmpty()) {
            view.show();
            return;
        }

        for (SettingsPagePresenter presenter : settings) {
            String category = presenter.getCategory();

            Set<SettingsPagePresenter> settingsCategory = settingsMap.get(category);

            if (settingsCategory == null) {
                settingsCategory = new HashSet<>();
                settingsMap.put(category, settingsCategory);
            }

            settingsCategory.add(presenter);
        }

        view.setSettings(settingsMap);

        view.show();
        view.enableSaveButton(false);

        view.selectSettingGroup(settings.iterator().next());
    }
}

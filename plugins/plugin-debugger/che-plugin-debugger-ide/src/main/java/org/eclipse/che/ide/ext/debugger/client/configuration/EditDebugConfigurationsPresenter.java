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
package org.eclipse.che.ide.ext.debugger.client.configuration;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage.DirtyStateListener;
import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.ext.debugger.client.DebuggerLocalizationConstant;
import org.eclipse.che.ide.ui.dialogs.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.choice.ChoiceDialog;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialog;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Presenter for managing debug configurations.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class EditDebugConfigurationsPresenter implements EditDebugConfigurationsView.ActionDelegate {

    private final EditDebugConfigurationsView       view;
    private final DebugConfigurationTypeRegistry    debugConfigurationTypeRegistry;
    private final DialogFactory                     dialogFactory;
    private final DebuggerLocalizationConstant      locale;
    private final CoreLocalizationConstant          coreLocale;
    private final DebugConfigurationsManager        debugConfigurationsManager;
    private final Set<ConfigurationChangedListener> configurationChangedListeners;
    /** Set of the existing configuration names. */
    private final Set<String>                       configurationNames;

    private DebugConfiguration                         editedConfiguration;
    private String                                     editedConfigurationOriginName;
    private DebugConfigurationPage<DebugConfiguration> editedPage;

    @Inject
    protected EditDebugConfigurationsPresenter(EditDebugConfigurationsView view,
                                               DebugConfigurationTypeRegistry debugConfigurationTypeRegistry,
                                               DialogFactory dialogFactory,
                                               DebuggerLocalizationConstant locale,
                                               CoreLocalizationConstant coreLocale,
                                               DebugConfigurationsManager debugConfigurationsManager) {
        this.view = view;
        this.debugConfigurationTypeRegistry = debugConfigurationTypeRegistry;
        this.dialogFactory = dialogFactory;
        this.locale = locale;
        this.coreLocale = coreLocale;
        this.debugConfigurationsManager = debugConfigurationsManager;
        configurationChangedListeners = new HashSet<>();
        configurationNames = new HashSet<>();

        view.setDelegate(this);
    }

    @Override
    public void onCloseClicked() {
        final DebugConfiguration selectedConfiguration = view.getSelectedConfiguration();
        onNameChanged();
        if (selectedConfiguration != null && isViewModified()) {
            onConfigurationSelected(selectedConfiguration);
        }
        view.close();
    }

    @Override
    public void onSaveClicked() {
        final DebugConfiguration selectedConfiguration;
        if (view.getSelectedConfiguration() == null) {
            return;
        }

        onNameChanged();
        selectedConfiguration = view.getSelectedConfiguration();

        updateConfiguration(selectedConfiguration);

        fetchConfigurations();
        fireConfigurationUpdated(selectedConfiguration);
    }

    private void updateConfiguration(final DebugConfiguration selectedConfiguration) {
        if (editedConfigurationOriginName.trim().equals(selectedConfiguration.getName())) {
            debugConfigurationsManager.removeConfiguration(selectedConfiguration.getName());
            debugConfigurationsManager.addConfiguration(selectedConfiguration);
        } else {
            onNameChanged();
            //generate a new unique name if input one already exists
            final String newName = getUniqueConfigurationName(selectedConfiguration.getType(), selectedConfiguration.getName());

            if (selectedConfiguration.equals(view.getSelectedConfiguration())) {
                //update selected configuration name
                view.getSelectedConfiguration().setName(newName);
            }

            debugConfigurationsManager.removeConfiguration(editedConfigurationOriginName);
            debugConfigurationsManager.addConfiguration(selectedConfiguration);
        }
    }

    @Override
    public void onCancelClicked() {
        fetchConfigurations();
    }

    @Override
    public void onDuplicateClicked() {
        final DebugConfiguration selectedConfiguration = view.getSelectedConfiguration();
        if (selectedConfiguration != null) {
            createNewConfiguration(selectedConfiguration.getType(),
                                   selectedConfiguration.getName(),
                                   selectedConfiguration.getConnectionProperties());
        }
    }

    @Override
    public void onAddClicked() {
        final DebugConfigurationType selectedType = view.getSelectedConfigurationType();
        if (selectedType != null) {
            createNewConfiguration(selectedType, null, null);
        }
    }

    private void createNewConfiguration(final DebugConfigurationType type,
                                        final String customName,
                                        final Map<String, String> attributes) {
        if (!isViewModified()) {
            reset();
            createConfiguration(type, customName, attributes);
            return;
        }

        final ConfirmCallback saveCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                updateConfiguration(editedConfiguration);

                reset();
                createConfiguration(type, customName, attributes);
            }
        };

        final ConfirmCallback discardCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                fetchConfigurations();
                reset();
                createConfiguration(type, customName, attributes);
            }
        };

        final ChoiceDialog dialog = dialogFactory.createChoiceDialog(
                locale.editConfigurationsSaveChangesTitle(),
                locale.editConfigurationsSaveChangesConfirmation(editedConfiguration.getName()),
                coreLocale.save(),
                locale.editConfigurationsSaveChangesDiscard(),
                saveCallback,
                discardCallback);
        dialog.show();
    }

    private void createConfiguration(DebugConfigurationType type,
                                     String customName,
                                     @Nullable Map<String, String> connectionProperties) {
        final DebugConfiguration conf = new DebugConfiguration(type,
                                                               getUniqueConfigurationName(type, customName),
                                                               "localhost",
                                                               8000,
                                                               (connectionProperties != null) ? connectionProperties
                                                                                              : new HashMap<String, String>());
        debugConfigurationsManager.addConfiguration(conf);

        fetchConfigurations();
        fireConfigurationAdded(conf);
        view.setSelectedConfiguration(conf);
    }

    private String getUniqueConfigurationName(DebugConfigurationType configurationType, String customName) {
        final String configurationName;

        if (customName == null || customName.isEmpty()) {
            configurationName = "Remote " + configurationType.getDisplayName();
        } else {
            if (!configurationNames.contains(customName)) {
                return customName;
            }
            configurationName = customName + " copy";
        }

        if (!configurationNames.contains(configurationName)) {
            return configurationName;
        }

        for (int count = 1; count < 1000; count++) {
            if (!configurationNames.contains(configurationName + "-" + count)) {
                return configurationName + "-" + count;
            }
        }

        return configurationName;
    }

    @Override
    public void onRemoveClicked(final DebugConfiguration selectedConfiguration) {
        if (selectedConfiguration == null) {
            return;
        }

        final ConfirmCallback confirmCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                debugConfigurationsManager.removeConfiguration(selectedConfiguration.getName());

                view.selectNextItem();
                fetchConfigurations();
                fireConfigurationRemoved(selectedConfiguration);
            }
        };

        final ConfirmDialog confirmDialog = dialogFactory.createConfirmDialog(
                locale.editConfigurationsViewRemoveTitle(),
                locale.editConfigurationsRemoveConfirmation(selectedConfiguration.getName()),
                confirmCallback,
                null);
        confirmDialog.show();
    }

    @Override
    public void onEnterPressed() {
        if (view.isCancelButtonFocused()) {
            onCancelClicked();
            return;
        }

        if (view.isCloseButtonFocused()) {
            onCloseClicked();
            return;
        }
        onSaveClicked();
    }

    private void reset() {
        editedConfiguration = null;
        editedConfigurationOriginName = null;
        editedPage = null;

        view.setConfigurationName("");
        view.clearDebugConfigurationPageContainer();
    }

    @Override
    public void onConfigurationSelected(final DebugConfiguration configuration) {
        if (!isViewModified()) {
            handleConfigurationSelection(configuration);
            return;
        }

        final ConfirmCallback saveCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                updateConfiguration(editedConfiguration);

                fetchConfigurations();
                fireConfigurationUpdated(editedConfiguration);
                handleConfigurationSelection(configuration);
            }
        };

        final ConfirmCallback discardCallback = new ConfirmCallback() {
            @Override
            public void accepted() {
                reset();
                fetchConfigurations();
                handleConfigurationSelection(configuration);
            }
        };

        final ChoiceDialog dialog = dialogFactory.createChoiceDialog(
                locale.editConfigurationsSaveChangesTitle(),
                locale.editConfigurationsSaveChangesConfirmation(editedConfiguration.getName()),
                coreLocale.save(),
                locale.editConfigurationsSaveChangesDiscard(),
                saveCallback,
                discardCallback);
        dialog.show();
    }

    private void handleConfigurationSelection(DebugConfiguration configuration) {
        editedConfiguration = configuration;
        editedConfigurationOriginName = configuration.getName();

        view.setConfigurationName(configuration.getName());

        final DebugConfigurationPage<? extends DebugConfiguration> page = configuration.getType().getConfigurationPage();
        final DebugConfigurationPage<DebugConfiguration> p = ((DebugConfigurationPage<DebugConfiguration>)page);

        editedPage = p;

        p.setDirtyStateListener(new DirtyStateListener() {
            @Override
            public void onDirtyStateChanged() {
                view.setCancelButtonState(isViewModified());
                view.setSaveButtonState(isViewModified());
            }
        });
        p.resetFrom(configuration);
        p.go(view.getDebugConfigurationPageContainer());
    }

    @Override
    public void onNameChanged() {
        DebugConfiguration selectedConfiguration = view.getSelectedConfiguration();
        if (selectedConfiguration == null || !selectedConfiguration.equals(editedConfiguration)) {
            return;
        }
        selectedConfiguration.setName(view.getConfigurationName());
        view.setCancelButtonState(isViewModified());
        view.setSaveButtonState(isViewModified());
    }

    /** Show dialog. */
    public void show() {
        view.show();
        fetchConfigurations();
    }

    /** Fetch configurations and update view. */
    private void fetchConfigurations() {
        final String originName = editedConfigurationOriginName;

        reset();
        view.setCancelButtonState(false);
        view.setSaveButtonState(false);

        final List<DebugConfiguration> configurationsList = debugConfigurationsManager.readConfigurations();

        configurationNames.clear();

        final Map<DebugConfigurationType, List<DebugConfiguration>> categories = new HashMap<>();

        for (DebugConfigurationType type : debugConfigurationTypeRegistry.getTypes()) {
            final List<DebugConfiguration> settingsCategory = new ArrayList<>();
            for (DebugConfiguration configuration : configurationsList) {
                if (type.getId().equals(configuration.getType().getId())) {
                    settingsCategory.add(configuration);
                    configurationNames.add(configuration.getName());
                    if (configuration.getName().equals(originName)) {
                        view.setSelectedConfiguration(configuration);
                    }
                }
            }

            Collections.sort(settingsCategory, new Comparator<DebugConfiguration>() {
                @Override
                public int compare(DebugConfiguration o1, DebugConfiguration o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            });

            categories.put(type, settingsCategory);
        }

        view.setData(categories);
        view.setFilterState(!configurationsList.isEmpty());

        view.focusCloseButton();
    }

    private boolean isViewModified() {
        if (editedConfiguration == null || editedPage == null) {
            return false;
        }
        return editedPage.isDirty() || !editedConfigurationOriginName.equals(view.getConfigurationName());
    }

    private void fireConfigurationAdded(DebugConfiguration configuration) {
        for (ConfigurationChangedListener listener : configurationChangedListeners) {
            listener.onConfigurationAdded(configuration);
        }
    }

    private void fireConfigurationRemoved(DebugConfiguration configuration) {
        for (ConfigurationChangedListener listener : configurationChangedListeners) {
            listener.onConfigurationRemoved(configuration);
        }
    }

    private void fireConfigurationUpdated(DebugConfiguration configuration) {
        for (ConfigurationChangedListener listener : configurationChangedListeners) {
            listener.onConfigurationsUpdated(configuration);
        }
    }

    public void addConfigurationsChangedListener(ConfigurationChangedListener listener) {
        configurationChangedListeners.add(listener);
    }

    public void removeConfigurationsChangedListener(ConfigurationChangedListener listener) {
        configurationChangedListeners.remove(listener);
    }

    /** Listener that will be called when debug configuration changed. */
    public interface ConfigurationChangedListener {
        void onConfigurationAdded(DebugConfiguration configuration);

        void onConfigurationRemoved(DebugConfiguration configuration);

        void onConfigurationsUpdated(DebugConfiguration configuration);
    }
}

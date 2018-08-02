/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.debugger.ide.configuration;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage;
import org.eclipse.che.ide.api.debug.DebugConfigurationPage.DirtyStateListener;
import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.api.debug.DebugConfigurationsManager;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.ui.dialogs.choice.ChoiceDialog;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmCallback;
import org.eclipse.che.ide.ui.dialogs.confirm.ConfirmDialog;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;

/**
 * Presenter for managing debug configurations.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class EditDebugConfigurationsPresenter
    implements EditDebugConfigurationsView.ActionDelegate {

  private final EditDebugConfigurationsView view;
  private final DebugConfigurationTypeRegistry debugConfigurationTypeRegistry;
  private final DialogFactory dialogFactory;
  private final DebuggerLocalizationConstant locale;
  private final CoreLocalizationConstant coreLocale;
  private final DebugConfigurationsManager debugConfigurationsManager;

  private DebugConfiguration editedConfiguration;
  private String editedConfigurationOriginName;
  private DebugConfigurationPage<DebugConfiguration> editedPage;

  @Inject
  protected EditDebugConfigurationsPresenter(
      EditDebugConfigurationsView view,
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
  }

  private void updateConfiguration(DebugConfiguration selectedConfiguration) {
    if (editedConfigurationOriginName.trim().equals(selectedConfiguration.getName())) {
      debugConfigurationsManager.removeConfiguration(selectedConfiguration);
      debugConfigurationsManager.createConfiguration(
          selectedConfiguration.getType().getId(),
          selectedConfiguration.getName(),
          selectedConfiguration.getHost(),
          selectedConfiguration.getPort(),
          selectedConfiguration.getConnectionProperties());
    } else {
      onNameChanged();

      debugConfigurationsManager.removeConfiguration(editedConfiguration);
      DebugConfiguration conf =
          debugConfigurationsManager.createConfiguration(
              selectedConfiguration.getType().getId(),
              selectedConfiguration.getName(),
              selectedConfiguration.getHost(),
              selectedConfiguration.getPort(),
              selectedConfiguration.getConnectionProperties());
      if (selectedConfiguration.equals(view.getSelectedConfiguration())) {
        // update selected configuration name
        view.getSelectedConfiguration().setName(conf.getName());
      }
    }
  }

  @Override
  public void onCancelClicked() {
    fetchConfigurations();
  }

  @Override
  public void onDebugClicked() {
    DebugConfiguration selectedConfiguration = view.getSelectedConfiguration();
    if (selectedConfiguration != null) {
      debugConfigurationsManager.setCurrentDebugConfiguration(selectedConfiguration);
      debugConfigurationsManager.apply(selectedConfiguration);
      onCloseClicked();
    }
  }

  @Override
  public void onDuplicateClicked() {
    final DebugConfiguration selectedConfiguration = view.getSelectedConfiguration();
    if (selectedConfiguration != null) {
      createNewConfiguration(
          selectedConfiguration.getType(),
          selectedConfiguration.getName(),
          selectedConfiguration.getConnectionProperties());
    }
  }

  @Override
  public void onAddClicked() {
    final DebugConfigurationType selectedType = view.getSelectedConfigurationType();
    if (selectedType != null) {
      createNewConfiguration(selectedType, null, new HashMap<String, String>());
    }
  }

  private void createNewConfiguration(
      final DebugConfigurationType type,
      final String customName,
      final Map<String, String> attributes) {
    if (!isViewModified()) {
      reset();
      createConfiguration(type, customName, attributes);
      return;
    }

    final ConfirmCallback saveCallback =
        new ConfirmCallback() {
          @Override
          public void accepted() {
            updateConfiguration(editedConfiguration);

            reset();
            createConfiguration(type, customName, attributes);
          }
        };

    final ConfirmCallback discardCallback =
        new ConfirmCallback() {
          @Override
          public void accepted() {
            fetchConfigurations();
            reset();
            createConfiguration(type, customName, attributes);
          }
        };

    final ChoiceDialog dialog =
        dialogFactory.createChoiceDialog(
            locale.editConfigurationsSaveChangesTitle(),
            locale.editConfigurationsSaveChangesConfirmation(editedConfiguration.getName()),
            coreLocale.save(),
            locale.editConfigurationsSaveChangesDiscard(),
            saveCallback,
            discardCallback);
    dialog.show();
  }

  private void createConfiguration(
      DebugConfigurationType type, String customName, Map<String, String> connectionProperties) {
    final DebugConfiguration configuration =
        debugConfigurationsManager.createConfiguration(
            type.getId(), customName, "localhost", 8000, connectionProperties);
    fetchConfigurations();
    view.setSelectedConfiguration(configuration);
  }

  @Override
  public void onRemoveClicked(final DebugConfiguration selectedConfiguration) {
    if (selectedConfiguration == null) {
      return;
    }

    final ConfirmCallback confirmCallback =
        new ConfirmCallback() {
          @Override
          public void accepted() {
            debugConfigurationsManager.removeConfiguration(selectedConfiguration);

            view.selectNextItem();
            fetchConfigurations();
          }
        };

    final ConfirmDialog confirmDialog =
        dialogFactory.createConfirmDialog(
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

    final ConfirmCallback saveCallback =
        new ConfirmCallback() {
          @Override
          public void accepted() {
            updateConfiguration(editedConfiguration);

            fetchConfigurations();
            handleConfigurationSelection(configuration);
          }
        };

    final ConfirmCallback discardCallback =
        new ConfirmCallback() {
          @Override
          public void accepted() {
            reset();
            fetchConfigurations();
            handleConfigurationSelection(configuration);
          }
        };

    final ChoiceDialog dialog =
        dialogFactory.createChoiceDialog(
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
    view.setDebugButtonState(true);

    final DebugConfigurationPage<? extends DebugConfiguration> page =
        configuration.getType().getConfigurationPage();
    final DebugConfigurationPage<DebugConfiguration> p =
        ((DebugConfigurationPage<DebugConfiguration>) page);

    editedPage = p;

    p.setDirtyStateListener(
        new DirtyStateListener() {
          @Override
          public void onDirtyStateChanged() {
            view.setCancelButtonState(isViewModified());
            view.setSaveButtonState(isViewModified());
            view.setDebugButtonState(!isViewModified());
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
    view.setDebugButtonState(!isViewModified());
  }

  /** Show dialog. */
  public void show() {
    view.showDialog();
    fetchConfigurations();
  }

  /** Fetch configurations and update view. */
  private void fetchConfigurations() {
    final String originName = editedConfigurationOriginName;

    reset();
    view.setCancelButtonState(false);
    view.setSaveButtonState(false);
    view.setDebugButtonState(view.getSelectedConfiguration() != null);

    final List<DebugConfiguration> configurationsList =
        debugConfigurationsManager.getConfigurations();

    final Map<DebugConfigurationType, List<DebugConfiguration>> categories = new HashMap<>();

    for (DebugConfigurationType type : debugConfigurationTypeRegistry.getTypes()) {
      final List<DebugConfiguration> settingsCategory = new ArrayList<>();
      for (DebugConfiguration configuration : configurationsList) {
        if (type.getId().equals(configuration.getType().getId())) {
          settingsCategory.add(configuration);
          if (configuration.getName().equals(originName)) {
            view.setSelectedConfiguration(configuration);
          }
        }
      }

      Collections.sort(
          settingsCategory,
          new Comparator<DebugConfiguration>() {
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
    return editedPage.isDirty()
        || !editedConfigurationOriginName.equals(view.getConfigurationName());
  }
}

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

import static java.util.Collections.emptyList;

import com.google.common.base.Optional;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.api.debug.DebugConfigurationsManager;
import org.eclipse.che.ide.debug.Debugger;
import org.eclipse.che.ide.debug.DebuggerManager;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.macro.CurrentProjectPathMacro;
import org.eclipse.che.ide.ui.dialogs.DialogFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.util.storage.LocalStorage;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;
import org.eclipse.che.plugin.debugger.ide.DebuggerLocalizationConstant;
import org.eclipse.che.plugin.debugger.ide.configuration.dto.DebugConfigurationDto;

/**
 * Implementation of {@link DebugConfigurationsManager}.
 *
 * @author Artem Zatsarynnyi
 */
public class DebugConfigurationsManagerImpl implements DebugConfigurationsManager {

  private static final String LOCAL_STORAGE_DEBUG_CONF_KEY = "che-debug-configurations";

  private final DtoFactory dtoFactory;
  private final DebugConfigurationTypeRegistry configurationTypeRegistry;
  private final Optional<LocalStorage> localStorageOptional;
  private final Set<ConfigurationChangedListener> configurationChangedListeners;
  private final List<DebugConfiguration> configurations;
  private final DebuggerManager debuggerManager;
  private final DialogFactory dialogFactory;
  private final DebuggerLocalizationConstant localizationConstants;
  private final CurrentProjectPathMacro currentProjectPathMacro;

  private DebugConfiguration currentDebugConfiguration;

  @Inject
  public DebugConfigurationsManagerImpl(
      LocalStorageProvider localStorageProvider,
      DtoFactory dtoFactory,
      DebugConfigurationTypeRegistry debugConfigurationTypeRegistry,
      DebuggerManager debuggerManager,
      DialogFactory dialogFactory,
      DebuggerLocalizationConstant localizationConstants,
      CurrentProjectPathMacro currentProjectPathMacro) {
    this.dtoFactory = dtoFactory;
    this.configurationTypeRegistry = debugConfigurationTypeRegistry;
    this.debuggerManager = debuggerManager;
    this.dialogFactory = dialogFactory;
    this.localizationConstants = localizationConstants;
    this.currentProjectPathMacro = currentProjectPathMacro;
    localStorageOptional = Optional.fromNullable(localStorageProvider.get());
    configurationChangedListeners = new HashSet<>();
    configurations = new ArrayList<>();

    loadConfigurations();
  }

  private void loadConfigurations() {
    for (DebugConfigurationDto descriptor : retrieveConfigurations()) {
      final DebugConfigurationType type =
          configurationTypeRegistry.getConfigurationTypeById(descriptor.getType());
      // skip configuration if it's type isn't registered
      if (type != null) {
        try {
          configurations.add(
              new DebugConfiguration(
                  type,
                  descriptor.getName(),
                  descriptor.getHost(),
                  descriptor.getPort(),
                  descriptor.getConnectionProperties()));
        } catch (IllegalArgumentException e) {
          Log.warn(EditDebugConfigurationsPresenter.class, e.getMessage());
        }
      }
    }
  }

  private List<DebugConfigurationDto> retrieveConfigurations() {
    List<DebugConfigurationDto> configurationsList;

    if (localStorageOptional.isPresent()) {
      final LocalStorage localStorage = localStorageOptional.get();
      final Optional<String> data =
          Optional.fromNullable(localStorage.getItem(LOCAL_STORAGE_DEBUG_CONF_KEY));
      if (data.isPresent() && !data.get().isEmpty()) {
        configurationsList =
            dtoFactory.createListDtoFromJson(data.get(), DebugConfigurationDto.class);
      } else {
        configurationsList = emptyList();
      }
    } else {
      configurationsList = emptyList();
    }

    return configurationsList;
  }

  @Override
  public Optional<DebugConfiguration> getCurrentDebugConfiguration() {
    return Optional.fromNullable(currentDebugConfiguration);
  }

  @Override
  public void setCurrentDebugConfiguration(@Nullable DebugConfiguration debugConfiguration) {
    currentDebugConfiguration = debugConfiguration;
  }

  @Override
  public List<DebugConfiguration> getConfigurations() {
    return new ArrayList<>(configurations);
  }

  @Override
  public DebugConfiguration createConfiguration(
      String typeId, String name, String host, int port, Map<String, String> connectionProperties) {
    final DebugConfigurationType configurationType =
        configurationTypeRegistry.getConfigurationTypeById(typeId);

    final DebugConfiguration configuration =
        new DebugConfiguration(
            configurationType,
            generateUniqueConfigurationName(configurationType, name),
            host,
            port,
            connectionProperties);
    configurations.add(configuration);
    saveConfigurations();
    fireConfigurationAdded(configuration);

    return configuration;
  }

  private String generateUniqueConfigurationName(
      DebugConfigurationType configurationType, String customName) {
    Set<String> configurationNames = new HashSet<>(configurations.size());
    for (DebugConfiguration configuration : configurations) {
      configurationNames.add(configuration.getName());
    }

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
  public void removeConfiguration(DebugConfiguration configuration) {
    if (getCurrentDebugConfiguration().isPresent()
        && getCurrentDebugConfiguration().get().equals(configuration)) {
      setCurrentDebugConfiguration(null);
    }

    configurations.remove(configuration);
    saveConfigurations();
    fireConfigurationRemoved(configuration);
  }

  private void saveConfigurations() {
    if (localStorageOptional.isPresent()) {
      List<DebugConfigurationDto> configurationDtos = new ArrayList<>();

      for (DebugConfiguration configuration : configurations) {
        configurationDtos.add(
            dtoFactory
                .createDto(DebugConfigurationDto.class)
                .withType(configuration.getType().getId())
                .withName(configuration.getName())
                .withHost(configuration.getHost())
                .withPort(configuration.getPort())
                .withConnectionProperties(configuration.getConnectionProperties()));
      }

      localStorageOptional
          .get()
          .setItem(LOCAL_STORAGE_DEBUG_CONF_KEY, dtoFactory.toJson(configurationDtos));
    }
  }

  @Override
  public void addConfigurationsChangedListener(ConfigurationChangedListener listener) {
    configurationChangedListeners.add(listener);
  }

  @Override
  public void removeConfigurationsChangedListener(ConfigurationChangedListener listener) {
    configurationChangedListeners.remove(listener);
  }

  @Override
  public void apply(final DebugConfiguration debugConfiguration) {
    if (debugConfiguration == null) {
      return;
    }

    if (debuggerManager.getActiveDebugger() != null) {
      dialogFactory
          .createMessageDialog(
              localizationConstants.connectToRemote(),
              localizationConstants.debuggerAlreadyConnected(),
              null)
          .show();
      return;
    }

    final Debugger debugger = debuggerManager.getDebugger(debugConfiguration.getType().getId());
    if (debugger != null) {
      debuggerManager.setActiveDebugger(debugger);

      currentProjectPathMacro
          .expand()
          .then(
              new Operation<String>() {
                @Override
                public void apply(String arg) throws OperationException {
                  Map<String, String> connectionProperties =
                      prepareConnectionProperties(debugConfiguration, arg);

                  debugger
                      .connect(connectionProperties)
                      .catchError(
                          new Operation<PromiseError>() {
                            @Override
                            public void apply(PromiseError arg) throws OperationException {
                              debuggerManager.setActiveDebugger(null);
                            }
                          });
                }
              });
    }
  }

  private Map<String, String> prepareConnectionProperties(
      DebugConfiguration debugConfiguration, String currentProjectPath) {
    Map<String, String> connectionProperties =
        new HashMap<>(2 + debugConfiguration.getConnectionProperties().size());
    connectionProperties.put("HOST", debugConfiguration.getHost());
    connectionProperties.put("PORT", String.valueOf(debugConfiguration.getPort()));

    for (Map.Entry<String, String> entry :
        debugConfiguration.getConnectionProperties().entrySet()) {
      String newValue =
          entry.getValue().replace(currentProjectPathMacro.getName(), currentProjectPath);
      connectionProperties.put(entry.getKey(), newValue);
    }
    return connectionProperties;
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
}

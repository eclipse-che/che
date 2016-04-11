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

import com.google.common.base.Optional;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.api.debug.DebugConfiguration;
import org.eclipse.che.ide.api.debug.DebugConfigurationType;
import org.eclipse.che.ide.ext.debugger.shared.DebugConfigurationDto;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.util.storage.LocalStorage;
import org.eclipse.che.ide.util.storage.LocalStorageProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Helps to manage debug configurations in browser's local storage.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class DebugConfigurationsManager {

    public static final String LOCAL_STORAGE_DEBUG_CONF_KEY = "che-debug-configurations";

    private final Optional<LocalStorage>         localStorageOptional;
    private final DtoFactory                     dtoFactory;
    private final DebugConfigurationTypeRegistry debugConfigurationTypeRegistry;

    @Inject
    public DebugConfigurationsManager(LocalStorageProvider localStorageProvider,
                                      DtoFactory dtoFactory,
                                      DebugConfigurationTypeRegistry debugConfigurationTypeRegistry) {
        this.dtoFactory = dtoFactory;
        this.debugConfigurationTypeRegistry = debugConfigurationTypeRegistry;
        localStorageOptional = Optional.fromNullable(localStorageProvider.get());
    }

    public List<DebugConfiguration> readConfigurations() {
        final List<DebugConfiguration> configurationList = new ArrayList<>();

        for (DebugConfigurationDto descriptor : readConfList()) {
            final DebugConfigurationType type = debugConfigurationTypeRegistry.getConfigurationTypeById(descriptor.getType());
            // skip configuration if it's type isn't registered
            if (type != null) {
                try {
                    configurationList.add(new DebugConfiguration(type,
                                                                 descriptor.getName(),
                                                                 descriptor.getHost(),
                                                                 descriptor.getPort(),
                                                                 descriptor.getConnectionProperties()));
                } catch (IllegalArgumentException e) {
                    Log.warn(EditDebugConfigurationsPresenter.class, e.getMessage());
                }
            }
        }

        return configurationList;
    }

    public void addConfiguration(DebugConfiguration configuration) {
        if (localStorageOptional.isPresent()) {
            final DebugConfigurationDto configurationDto = dtoFactory.createDto(DebugConfigurationDto.class)
                                                                     .withName(configuration.getName())
                                                                     .withHost(configuration.getHost())
                                                                     .withPort(configuration.getPort())
                                                                     .withConnectionProperties(configuration.getConnectionProperties())
                                                                     .withType(configuration.getType().getId());

            final List<DebugConfigurationDto> confList = readConfList();
            confList.add(configurationDto);
            localStorageOptional.get().setItem(LOCAL_STORAGE_DEBUG_CONF_KEY, dtoFactory.toJson(confList));
        }
    }

    public void removeConfiguration(String name) {
        final List<DebugConfigurationDto> newList = new ArrayList<>();
        for (DebugConfigurationDto debugConfigurationDto : readConfList()) {
            if (!name.equals(debugConfigurationDto.getName())) {
                newList.add(debugConfigurationDto);
            }
        }

        if (localStorageOptional.isPresent()) {
            localStorageOptional.get().setItem(LOCAL_STORAGE_DEBUG_CONF_KEY, dtoFactory.toJson(newList));
        }
    }

    private List<DebugConfigurationDto> readConfList() {
        List<DebugConfigurationDto> configurationsList;

        if (localStorageOptional.isPresent()) {
            final LocalStorage localStorage = localStorageOptional.get();
            final Optional<String> data = Optional.fromNullable(localStorage.getItem(LOCAL_STORAGE_DEBUG_CONF_KEY));
            if (data.isPresent() && !data.get().isEmpty()) {
                configurationsList = dtoFactory.createListDtoFromJson(data.get(), DebugConfigurationDto.class);
            } else {
                configurationsList = new ArrayList<>(0);
            }
        } else {
            configurationsList = new ArrayList<>(0);
        }

        return configurationsList;
    }
}

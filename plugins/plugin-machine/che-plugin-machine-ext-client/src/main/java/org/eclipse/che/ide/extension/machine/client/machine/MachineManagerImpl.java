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
package org.eclipse.che.ide.extension.machine.client.machine;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.model.machine.MachineConfig;
import org.eclipse.che.api.core.model.machine.MachineSource;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineLimitsDto;
import org.eclipse.che.api.machine.shared.dto.MachineSourceDto;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.machine.DevMachine;
import org.eclipse.che.ide.api.machine.MachineEntity;
import org.eclipse.che.ide.api.machine.MachineManager;
import org.eclipse.che.ide.api.machine.MachineServiceClient;
import org.eclipse.che.ide.api.machine.events.MachineStateEvent;
import org.eclipse.che.ide.api.workspace.WorkspaceServiceClient;
import org.eclipse.che.ide.context.AppContextImpl;
import org.eclipse.che.ide.dto.DtoFactory;

import static org.eclipse.che.ide.api.machine.events.MachineStateEvent.MachineAction.DESTROYED;

/**
 * Manager for machine operations.
 *
 * @author Artem Zatsarynnyi
 * @author Roman Nikitenko
 */
@Singleton
public class MachineManagerImpl implements MachineManager {

    private final MachineServiceClient   machineServiceClient;
    private final WorkspaceServiceClient workspaceServiceClient;
    private final AppContext             appContext;
    private final DtoFactory             dtoFactory;
    private final EventBus               eventBus;

    @Inject
    public MachineManagerImpl(final MachineServiceClient machineServiceClient,
                              final WorkspaceServiceClient workspaceServiceClient,
                              final EventBus eventBus,
                              final AppContext appContext,
                              final DtoFactory dtoFactory) {
        this.machineServiceClient = machineServiceClient;
        this.workspaceServiceClient = workspaceServiceClient;
        this.appContext = appContext;
        this.dtoFactory = dtoFactory;
        this.eventBus = eventBus;
    }

    @Override
    public void restartMachine(final MachineEntity machineState) {
        destroyMachine(machineState).then(new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                final MachineConfig machineConfig = machineState.getConfig();
                final MachineSource machineSource = machineConfig.getSource();
                final String displayName = machineConfig.getName();
                final boolean isDev = machineConfig.isDev();

                startMachine(asDto(machineSource), displayName, isDev, "docker");
            }
        });
    }

    /**
     * Converts {@link MachineSource} to {@link MachineSourceDto}.
     */
    public MachineSourceDto asDto(MachineSource source) {
        return this.dtoFactory.createDto(MachineSourceDto.class)
                              .withType(source.getType())
                              .withLocation(source.getLocation())
                              .withContent(source.getContent());
    }

    @Override
    public void startMachine(String recipeURL, String displayName) {
        startMachine(recipeURL, displayName, false, "dockerfile", "docker");
    }

    @Override
    public void startDevMachine(String recipeURL, String displayName) {
        startMachine(recipeURL, displayName, true, "dockerfile", "docker");
    }

    /**
     * Start new machine in workspace.
     *
     * @param recipeURL
     *         special recipe url to get docker image.
     * @param displayName
     *         display name for machine
     * @param isDev
     * @param sourceType
     *         "dockerfile" or "ssh-config"
     * @param machineType
     *         "docker" or "ssh"
     */
    private void startMachine(final String recipeURL,
                              final String displayName,
                              final boolean isDev,
                              final String sourceType,
                              final String machineType) {
        MachineSourceDto sourceDto = dtoFactory.createDto(MachineSourceDto.class).withType(sourceType).withLocation(recipeURL);
        startMachine(sourceDto, displayName, isDev, machineType);
    }

    /**
     * @param machineSourceDto
     * @param displayName
     * @param isDev
     * @param machineType
     *         "docker" or "ssh"
     */
    private void startMachine(final MachineSourceDto machineSourceDto,
                              final String displayName,
                              final boolean isDev,
                              final String machineType) {

        MachineLimitsDto limitsDto = dtoFactory.createDto(MachineLimitsDto.class).withRam(1024);
        if (isDev) {
            limitsDto.withRam(3072);
        }

        MachineConfigDto configDto = dtoFactory.createDto(MachineConfigDto.class)
                                               .withDev(isDev)
                                               .withName(displayName)
                                               .withSource(machineSourceDto)
                                               .withLimits(limitsDto)
                                               .withType(machineType);
        workspaceServiceClient.createMachine(appContext.getWorkspaceId(), configDto);
    }

    @Override
    public Promise<Void> destroyMachine(final MachineEntity machineState) {
        return machineServiceClient.destroyMachine(machineState.getWorkspaceId(),
                                                   machineState.getId()).then(new Operation<Void>() {
            @Override
            public void apply(Void arg) throws OperationException {
                eventBus.fireEvent(new MachineStateEvent(machineState, DESTROYED));

                final DevMachine devMachine = appContext.getDevMachine();
                if (devMachine != null && machineState.getId().equals(devMachine.getId()) && appContext instanceof AppContextImpl) {
                    ((AppContextImpl)appContext).setDevMachine(null);
                }
            }
        });
    }
}

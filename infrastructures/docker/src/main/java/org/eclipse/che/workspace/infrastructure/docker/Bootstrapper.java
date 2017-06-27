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
package org.eclipse.che.workspace.infrastructure.docker;

import com.google.gson.Gson;
import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.api.installer.server.model.impl.InstallerImpl;
import org.eclipse.che.api.core.model.workspace.runtime.BootstrapperStatus;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.notification.EventSubscriber;
import org.eclipse.che.api.core.util.FileCleaner;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.shared.dto.RuntimeIdentityDto;
import org.eclipse.che.api.workspace.shared.dto.event.BootstrapperStatusEvent;
import org.eclipse.che.commons.lang.TarUtils;
import org.eclipse.che.workspace.infrastructure.docker.server.InstallerEndpoint;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Bootstraps installers.
 *
 * @author Sergii Leshchenko
 */
public class Bootstrapper {
    private static final Gson          GSON         = new Gson();
    private static final AtomicInteger ENDPOINT_IDS = new AtomicInteger();

    private static final String BOOTSTRAPPER_BASE_DIR = "/tmp/";
    private static final String BOOTSTRAPPER_DIR      = BOOTSTRAPPER_BASE_DIR + "bootstrapper/";
    private static final String BOOTSTRAPPER_FILE     = "bootstrapper";
    private static final String CONFIG_FILE           = "config.json";

    private final String                                     machineName;
    private final RuntimeIdentity                            runtimeIdentity;
    private final DockerMachine                              dockerMachine;
    private final List<InstallerImpl>                        agents;
    private final int                                        bootstrappingTimeoutMinutes;
    private final int                                        serverCheckPeriodSeconds;
    private final int                                        installerTimeoutSeconds;
    private final String                                     installerEndpoint;
    private final EventService                               eventService;
    private final EventSubscriber<BootstrapperStatusEvent>   bootstrapperStatusListener;
    private       CompletableFuture<BootstrapperStatusEvent> finishEventFuture;

    @Inject
    public Bootstrapper(@Assisted String machineName,
                        @Assisted RuntimeIdentity runtimeIdentity,
                        @Assisted DockerMachine dockerMachine,
                        @Assisted List<InstallerImpl> agents,
                        @Named("che.workspace.che_server_websocket_endpoint_base") String websocketBaseEndpoint,
                        @Named("che.infra.docker.bootstrapper.timeout_min") int bootstrappingTimeoutMinutes,
                        @Named("che.infra.docker.bootstrapper.installer_timeout_sec") int installerTimeoutSeconds,
                        @Named("che.infra.docker.bootstrapper.server_check_period_sec") int serverCheckPeriodSeconds,
                        EventService eventService) {
        this.machineName = machineName;
        this.runtimeIdentity = runtimeIdentity;
        this.dockerMachine = dockerMachine;
        this.agents = agents;
        this.installerEndpoint = websocketBaseEndpoint + InstallerEndpoint.INSTALLER_WEBSOCKET_ENDPOINT_BASE;
        this.bootstrappingTimeoutMinutes = bootstrappingTimeoutMinutes;
        this.serverCheckPeriodSeconds = serverCheckPeriodSeconds;
        this.installerTimeoutSeconds = installerTimeoutSeconds;
        this.eventService = eventService;
        this.bootstrapperStatusListener = event -> {
            BootstrapperStatus status = event.getStatus();
            //skip starting status event
            if (status.equals(BootstrapperStatus.DONE) || status.equals(BootstrapperStatus.FAILED)) {
                //check boostrapper belongs to current runtime and machine
                RuntimeIdentityDto runtimeId = event.getRuntimeId();
                if (event.getMachineName().equals(machineName)
                    && runtimeIdentity.getEnvName().equals(runtimeId.getEnvName())
                    && runtimeIdentity.getOwner().equals(runtimeId.getOwner())
                    && runtimeIdentity.getWorkspaceId().equals(runtimeId.getWorkspaceId())) {

                    finishEventFuture.complete(event);
                }
            }
        };
    }

    /**
     * Bootstraps installers and wait while they finished.
     *
     * @throws InfrastructureException
     *         when bootstrapping timeout reached
     * @throws InfrastructureException
     *         when bootstrapping failed
     * @throws InfrastructureException
     *         when any other error occurs while bootstrapping
     */
    public void bootstrap() throws InfrastructureException {
        if (finishEventFuture != null) {
            throw new IllegalStateException("Bootstrap method must be called only once.");
        }
        this.finishEventFuture = new CompletableFuture<>();

        injectBootstrapper();

        this.eventService.subscribe(bootstrapperStatusListener, BootstrapperStatusEvent.class);
        try {
            String endpoint = installerEndpoint + ENDPOINT_IDS.getAndIncrement();
            dockerMachine.exec(BOOTSTRAPPER_DIR + BOOTSTRAPPER_FILE +
                               " -machine-name " + machineName +
                               " -runtime-id " + String.format("%s:%s:%s", runtimeIdentity.getWorkspaceId(),
                                                               runtimeIdentity.getEnvName(),
                                                               runtimeIdentity.getOwner()) +
                               " -push-endpoint " + endpoint +
                               " -push-logs-endpoint " + endpoint +
                               " -server-check-period " + serverCheckPeriodSeconds +
                               " -installer-timeout " + installerTimeoutSeconds +
                               " -file " + BOOTSTRAPPER_DIR + CONFIG_FILE,
                               null);

            //waiting for DONE or FAILED bootstrapper status event
            BootstrapperStatusEvent resultEvent = finishEventFuture.get(bootstrappingTimeoutMinutes, TimeUnit.MINUTES);
            if (resultEvent.getStatus().equals(BootstrapperStatus.FAILED)) {
                throw new InfrastructureException(resultEvent.getError());
            }
        } catch (ExecutionException e) {
            throw new InfrastructureException(e.getCause().getMessage(), e);
        } catch (TimeoutException e) {
            throw new InfrastructureException("Bootstrapping of machine " + machineName + " reached timeout");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new InfrastructureException("Bootstrapping of machine " + machineName + " was interrupted");
        } finally {
            this.eventService.unsubscribe(bootstrapperStatusListener, BootstrapperStatusEvent.class);
        }
    }

    private void injectBootstrapper() throws InfrastructureException {
        dockerMachine.putResource(BOOTSTRAPPER_BASE_DIR, Thread.currentThread()
                                                               .getContextClassLoader()
                                                               .getResourceAsStream("bootstrapper.tar.gz"));
        // inject config file
        File configFileArchive = null;
        try {
            configFileArchive = createArchive(CONFIG_FILE, GSON.toJson(agents));
            dockerMachine.putResource(BOOTSTRAPPER_DIR, new FileInputStream(configFileArchive));
        } catch (FileNotFoundException e) {
            throw new InternalInfrastructureException(e.getMessage(), e);
        } finally {
            if (configFileArchive != null) {
                FileCleaner.addFile(configFileArchive);
            }
        }
    }

    private File createArchive(String filename, String content) throws InfrastructureException {
        Path bootstrapperConfTmp = null;
        try {
            bootstrapperConfTmp = Files.createTempDirectory(filename);
            Path configFile = bootstrapperConfTmp.resolve(filename);
            Files.copy(new ByteArrayInputStream(content.getBytes()), configFile);
            Path bootstrapperConfArchive = Files.createTempFile(filename, ".tar.gz");
            TarUtils.tarFiles(bootstrapperConfArchive.toFile(), configFile.toFile());
            return bootstrapperConfArchive.toFile();
        } catch (IOException e) {
            throw new InternalInfrastructureException("Error occurred while injecting bootstrapping conf. "
                                                      + e.getMessage(), e);
        } finally {
            if (bootstrapperConfTmp != null) {
                FileCleaner.addFile(bootstrapperConfTmp.toFile());
            }
        }
    }
}

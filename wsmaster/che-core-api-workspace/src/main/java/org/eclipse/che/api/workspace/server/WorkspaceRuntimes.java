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
package org.eclipse.che.api.workspace.server;

import com.google.common.collect.ImmutableMap;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.Runtime;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalInfrastructureException;
import org.eclipse.che.api.workspace.server.spi.InternalRuntime;
import org.eclipse.che.api.workspace.server.spi.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.RuntimeInfrastructure;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent;
import org.eclipse.che.api.workspace.shared.dto.event.WorkspaceStatusEvent.EventType;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.dto.server.DtoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

// TODO: spi: deal with exceptions

/**
 * Defines an internal API for managing {@link RuntimeImpl} instances.
 *
 * @author Yevhenii Voevodin
 * @author Alexander Garagatyi
 */
@Singleton
public class WorkspaceRuntimes {

    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceRuntimes.class);

    private final ImmutableMap<String, RuntimeInfrastructure> infraByRecipe;

    private final ConcurrentMap<String, InternalRuntime> runtimes;
    private final EventService                           eventsService;
    private final WorkspaceSharedPool                    sharedPool;

    @Inject
    public WorkspaceRuntimes(EventService eventsService,
                             Set<RuntimeInfrastructure> infrastructures,
                             WorkspaceSharedPool sharedPool) {
        this.runtimes = new ConcurrentHashMap<>();
        this.eventsService = eventsService;
        this.sharedPool = sharedPool;

        // TODO: consider extracting to a strategy interface(1. pick the last, 2. fail with conflict)
        Map<String, RuntimeInfrastructure> tmp = new HashMap<>();
        for (RuntimeInfrastructure infra : infrastructures) {
            for (String type : infra.getRecipeTypes()) {
                LOG.info("Register infrastructure '{}' recipe type '{}'", infra.getName(), type);
                RuntimeInfrastructure existingInfra = tmp.put(type, infra);
                if (existingInfra != null) {
                    LOG.warn("Both '{}' and '{}' infrastructures support recipe of type '{}', infrastructure '{}' will be used",
                             infra.getName(),
                             existingInfra.getName(),
                             type,
                             infra.getName());
                }
            }
        }
        infraByRecipe = ImmutableMap.copyOf(tmp);
    }

    @PostConstruct
    private void recover() {
        for (RuntimeInfrastructure infra : infraByRecipe.values()) {
            try {
                for (RuntimeIdentity id : infra.getIdentities()) {
                    runtimes.put(id.getWorkspaceId(), validate(infra.getRuntime(id)));
                }
            } catch (UnsupportedOperationException x) {
                LOG.warn("Not recoverable infrastructure: '{}'", infra.getName());
            } catch (InfrastructureException x) {
                LOG.error("An error occurred while attempted to recover runtimes using infrastructure '{}'. Reason: '{}'",
                          infra.getName(),
                          x.getMessage());
            }
        }
    }

    //TODO do we need some validation on start
    private InternalRuntime validate(InternalRuntime runtime) {
        return runtime;
    }

    public Environment estimate(Environment environment) throws NotFoundException, InfrastructureException, ValidationException {
        // TODO decide whether throw exception when dev machine not found
        String type = environment.getRecipe().getType();
        if (!infraByRecipe.containsKey(type))
            throw new NotFoundException("Infrastructure not found for type: " + type);

        return infraByRecipe.get(type).estimate(environment);
    }


    /**
     * Returns the runtime descriptor describing currently starting/running/stopping
     * workspace runtime.
     *
     * returns a copy of a real {@code Runtime} object,
     * which means that any runtime copy modifications won't affect the
     * real object and also it means that copy won't be affected with modifications applied
     * to the real runtime workspace object state.
     *
     * @param workspaceId
     *         the id of the workspace to get its runtime
     * @return descriptor which describes current state of the workspace runtime
     * @throws NotFoundException
     *         when workspace with given {@code workspaceId} is not found
     * @throws ServerException
     *         if environment is in illegal state
     */
    public Runtime get(String workspaceId) throws NotFoundException, ServerException {

        InternalRuntime runtime = runtimes.get(workspaceId);
        if (runtime != null)
            return runtime;
        else
            throw new NotFoundException("Workspace with id '" + workspaceId + "' is not running.");

    }

    /**
     * Starts all machines from specified workspace environment,
     * creates workspace runtime instance based on that environment.
     *
     * <p>During the start of the workspace its
     * runtime is visible with {@link WorkspaceStatus#STARTING} status.
     *
     * @param workspace
     *         workspace which environment should be started
     * @param envName
     *         the name of the environment to start
     * @param options
     *         whether machines should be recovered(true) or not(false)
     * @return the workspace runtime instance with machines set.
     * @throws ConflictException
     *         when workspace is already running
     * @throws ConflictException
     *         when start is interrupted
     * @throws NotFoundException
     *         when any not found exception occurs during environment start
     * @throws ServerException
     *         other error occurs during environment start
     * @see WorkspaceStatus#STARTING
     * @see WorkspaceStatus#RUNNING
     */
    public Runtime start(Workspace workspace,
                         String envName,
                         Map<String, String> options) throws InfrastructureException,
                                                             ValidationException,
                                                             IOException,
                                                             NotFoundException,
                                                             ConflictException,
                                                             ServerException {

        final EnvironmentImpl environment = copyEnv(workspace, envName);
        final String workspaceId = workspace.getId();
        doStart(environment, workspaceId, envName, options);
        return get(workspaceId);
    }

    /**
     * Starts the workspace like
     * method does, but asynchronously. Nonetheless synchronously checks that workspace
     * doesn't have runtime and makes it {@link WorkspaceStatus#STARTING}.
     */
    public Future<Runtime> startAsync(Workspace workspace,
                                      String envName,
                                      Map<String, String> options) throws ConflictException, ServerException {

        final EnvironmentImpl environment = copyEnv(workspace, envName);
        final String workspaceId = workspace.getId();
        return sharedPool.submit(() -> {
            doStart(environment, workspaceId, envName, options);
            return get(workspaceId);
        });
    }

    /**
     * Stops running workspace runtime.
     *
     * <p>Stops environment in an implementation specific way.
     * During the stop of the workspace its runtime is accessible with {@link WorkspaceStatus#STOPPING stopping} status.
     * Workspace may be stopped only if its status is {@link WorkspaceStatus#RUNNING}.
     *
     * @param workspaceId
     *         identifier of workspace which should be stopped
     * @throws NotFoundException
     *         when workspace with specified identifier is not running
     * @throws ConflictException
     *         when running workspace status is different from {@link WorkspaceStatus#RUNNING}
     * @throws InfrastructureException
     *         when any other error occurs during workspace stopping
     * @see WorkspaceStatus#STOPPING
     */
    public void stop(String workspaceId, Map<String, String> options) throws NotFoundException,
                                                                             InfrastructureException,
                                                                             ConflictException {

        eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                        .withWorkspaceId(workspaceId)
                                        .withPrevStatus(WorkspaceStatus.RUNNING)
                                        .withStatus(WorkspaceStatus.STOPPING)
                                        .withEventType(EventType.STOPPING));

        InternalRuntime runtime = runtimes.get(workspaceId);
        if (runtime == null) {
            throw new NotFoundException("Workspace with id '" + workspaceId + "' is not running.");
        }

        runtime.getContext().stop(options);

        runtimes.remove(workspaceId);

        final WorkspaceStatusEvent event = DtoFactory.newDto(WorkspaceStatusEvent.class)
                                                     .withWorkspaceId(workspaceId)
                                                     .withPrevStatus(WorkspaceStatus.STOPPING);
        event.setStatus(WorkspaceStatus.STOPPED);
        event.setEventType(EventType.STOPPED);
        eventsService.publish(event);
    }

    /**
     * Returns true if workspace was started and its status is
     * {@link WorkspaceStatus#RUNNING running}, {@link WorkspaceStatus#STARTING starting}
     * or {@link WorkspaceStatus#STOPPING stopping} - otherwise returns false.
     *
     * <p> This method is less expensive alternative to {@link #get(String)} + {@code try catch}, see example:
     * <pre>{@code
     *
     *     if (!runtimes.hasRuntime("workspace123")) {
     *         doStuff("workspace123");
     *     }
     *
     *     //vs
     *
     *     try {
     *         runtimes.get("workspace123");
     *     } catch (NotFoundException ex) {
     *         doStuff("workspace123");
     *     }
     *
     * }</pre>
     *
     * @param workspaceId
     *         workspace identifier to perform check
     * @return true if workspace is running, otherwise false
     */
    public boolean hasRuntime(String workspaceId) {
        return runtimes.containsKey(workspaceId);
    }

    private void doStart(EnvironmentImpl environment,
                         String workspaceId, String envName,
                         Map<String, String> options) throws InfrastructureException,
                                                             NotFoundException,
                                                             ConflictException,
                                                             ValidationException,
                                                             IOException {

        requireNonNull(environment, "Environment should not be null " + workspaceId);
        requireNonNull(environment.getRecipe(), "OldRecipe should not be null " + workspaceId);
        requireNonNull(environment.getRecipe().getType(), "OldRecipe type should not be null " + workspaceId);

        RuntimeInfrastructure infra = infraByRecipe.get(environment.getRecipe().getType());
        if (infra == null) {
            throw new NotFoundException("No infrastructure found of type: " + environment.getRecipe().getType() +
                                        " for workspace: " + workspaceId);
        }

        if (runtimes.containsKey(workspaceId)) {
            throw new ConflictException("Could not start workspace '" + workspaceId +
                                        "' because its status is 'RUNNING'");
        }

        eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                        .withWorkspaceId(workspaceId)
                                        .withStatus(WorkspaceStatus.STARTING)
                                        .withEventType(EventType.STARTING)
                                        .withPrevStatus(WorkspaceStatus.STOPPED));


        // Start environment
        if (options == null) {
            options = new HashMap<>();
        }

        Subject subject = EnvironmentContext.getCurrent().getSubject();
        RuntimeIdentity runtimeId = new RuntimeIdentity(workspaceId, envName, subject.getUserName());

        try {
            InternalRuntime runtime = infra.prepare(runtimeId, environment).start(options);

            if (runtime == null) {
                throw new IllegalStateException(
                        "SPI contract violated. RuntimeInfrastructure.start(...) must not return null: "
                        + RuntimeInfrastructure.class);
            }

            runtimes.put(workspaceId, runtime);
            eventsService.publish(DtoFactory.newDto(WorkspaceStatusEvent.class)
                                            .withWorkspaceId(workspaceId)
                                            .withStatus(WorkspaceStatus.RUNNING)
                                            .withEventType(EventType.RUNNING)
                                            .withPrevStatus(WorkspaceStatus.STARTING));
        } catch (InternalInfrastructureException e) {
            LOG.error(format("Error occurs on workspace '%s' start. Error: %s", workspaceId, e));
        } catch (RuntimeException e) {
            LOG.error(format("Error occurs on workspace '%s' start. Error: %s", workspaceId, e));
            throw new InternalInfrastructureException(e.getMessage(), e);
        }
    }

    private static EnvironmentImpl copyEnv(Workspace workspace, String envName) {

        requireNonNull(workspace, "Workspace should not be null.");
        requireNonNull(envName, "Environment name should not be null.");
        final Environment environment = workspace.getConfig().getEnvironments().get(envName);
        if (environment == null) {
            throw new IllegalArgumentException(format("Workspace '%s' doesn't contain environment '%s'",
                                                      workspace.getId(),
                                                      envName));
        }
        return new EnvironmentImpl(environment);
    }

//    /**
//     * Removes all workspaces from the in-memory storage, while
//     * {@link CheEnvironmentEngine} is responsible for environment destroying.
//     */
//    @PreDestroy
//    @VisibleForTesting
//    void cleanup() {
//        isPreDestroyInvoked = true;
//
//        // wait existing tasks to complete
//        sharedPool.terminateAndWait();
//
//        List<String> idsToStop;
//        try (@SuppressWarnings("unused") CloseableLock l = locks.acquireWriteAllLock()) {
//            idsToStop = workspaces.entrySet()
//                                  .stream()
//                                  .filter(e -> e.getValue().status != STOPPING)
//                                  .map(Map.Entry::getKey)
//                                  .collect(Collectors.toList());
//            workspaces.clear();
//        }
//
//        // nothing to stop
//        if (idsToStop.isEmpty()) {
//            return;
//        }
//
//        LOG.info("Shutdown running workspaces, workspaces to shutdown '{}'", idsToStop.size());
//        ExecutorService executor =
//                Executors.newFixedThreadPool(2 * java.lang.Runtime.getRuntime().availableProcessors(),
//                                             new ThreadFactoryBuilder().setNameFormat("StopEnvironmentsPool-%d")
//                                                                       .setDaemon(false)
//                                                                       .build());
//        for (String id : idsToStop) {
//            executor.execute(() -> {
//                try {
//                    networks.stop(id);
//                } catch (Exception x) {
//                    LOG.error(x.getMessage(), x);
//                }
//            });
//        }
//
//        executor.shutdown();
//        try {
//            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
//                executor.shutdownNow();
//                if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
//                    LOG.error("Unable terminate machines pool");
//                }
//            }
//        } catch (InterruptedException e) {
//            executor.shutdownNow();
//            Thread.currentThread().interrupt();
//        }
//    }
//
//    private void ensurePreDestroyIsNotExecuted() throws ServerException {
//        if (isPreDestroyInvoked) {
//            throw new ServerException("Could not perform operation because application server is stopping");
//        }
//    }
//

//    /**
//     * Safely compares current status of given workspace
//     * with {@code from} and if they are equal sets the status to {@code to}.
//     * Returns true if the status of workspace was updated with {@code to} value.
//     */
//    private boolean compareAndSetStatus(String id, WorkspaceStatus from, WorkspaceStatus to) {
//        try (@SuppressWarnings("unused") CloseableLock l = locks.acquireWriteLock(id)) {
//            WorkspaceState state = workspaces.get(id);
//            if (state != null && state.getStatus() == from) {
//                state.status = to;
//                return true;
//            }
//        }
//        return false;
//    }
//
}

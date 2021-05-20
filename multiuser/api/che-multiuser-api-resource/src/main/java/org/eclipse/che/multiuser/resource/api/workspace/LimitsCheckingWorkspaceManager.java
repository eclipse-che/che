/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.resource.api.workspace;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.lang.String.format;
import static java.util.Collections.singletonList;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.config.Environment;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.PreferenceManager;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.WorkspaceRuntimes;
import org.eclipse.che.api.workspace.server.WorkspaceValidator;
import org.eclipse.che.api.workspace.server.devfile.validator.DevfileIntegrityValidator;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.annotation.Traced;
import org.eclipse.che.commons.lang.Size;
import org.eclipse.che.commons.lang.concurrent.Unlocker;
import org.eclipse.che.multiuser.resource.api.exception.NoEnoughResourcesException;
import org.eclipse.che.multiuser.resource.api.type.RamResourceType;
import org.eclipse.che.multiuser.resource.api.type.RuntimeResourceType;
import org.eclipse.che.multiuser.resource.api.type.WorkspaceResourceType;
import org.eclipse.che.multiuser.resource.api.usage.ResourceManager;
import org.eclipse.che.multiuser.resource.api.usage.ResourcesLocks;
import org.eclipse.che.multiuser.resource.api.usage.tracker.EnvironmentRamCalculator;
import org.eclipse.che.multiuser.resource.model.Resource;
import org.eclipse.che.multiuser.resource.spi.impl.ResourceImpl;

/**
 * Manager that checks limits and delegates all its operations to the {@link WorkspaceManager}.
 * Doesn't contain any logic related to start/stop or any kind of operations different from limits
 * checks.
 *
 * @author Yevhenii Voevodin
 * @author Igor Vinokur
 * @author Sergii Leschenko
 */
@Singleton
public class LimitsCheckingWorkspaceManager extends WorkspaceManager {

  private final EnvironmentRamCalculator environmentRamCalculator;
  private final ResourceManager resourceManager;
  private final ResourcesLocks resourcesLocks;
  private final AccountManager accountManager;

  private final long maxRamPerEnvMB;

  @Inject
  public LimitsCheckingWorkspaceManager(
      WorkspaceDao workspaceDao,
      WorkspaceRuntimes runtimes,
      EventService eventService,
      AccountManager accountManager,
      PreferenceManager preferenceManager,
      WorkspaceValidator workspaceValidator,
      // own injects
      @Named("che.limits.workspace.env.ram") String maxRamPerEnv,
      EnvironmentRamCalculator environmentRamCalculator,
      ResourceManager resourceManager,
      ResourcesLocks resourcesLocks,
      DevfileIntegrityValidator devfileIntegrityValidator) {
    super(
        workspaceDao,
        runtimes,
        eventService,
        accountManager,
        preferenceManager,
        workspaceValidator,
        devfileIntegrityValidator);
    this.environmentRamCalculator = environmentRamCalculator;
    this.maxRamPerEnvMB = "-1".equals(maxRamPerEnv) ? -1 : Size.parseSizeToMegabytes(maxRamPerEnv);
    this.resourceManager = resourceManager;
    this.resourcesLocks = resourcesLocks;
    this.accountManager = accountManager;
  }

  @Override
  @Traced
  public WorkspaceImpl createWorkspace(
      WorkspaceConfig config, String namespace, @Nullable Map<String, String> attributes)
      throws ServerException, ConflictException, NotFoundException, ValidationException {
    checkMaxEnvironmentRam(config);
    String accountId = accountManager.getByName(namespace).getId();
    try (@SuppressWarnings("unused")
        Unlocker u = resourcesLocks.lock(accountId)) {
      checkWorkspaceResourceAvailability(accountId);

      return super.createWorkspace(config, namespace, attributes);
    }
  }

  @Override
  public WorkspaceImpl startWorkspace(
      String workspaceId, @Nullable String envName, @Nullable Map<String, String> options)
      throws NotFoundException, ServerException, ConflictException {
    WorkspaceImpl workspace = this.getWorkspace(workspaceId);
    String accountId = workspace.getAccount().getId();
    WorkspaceConfigImpl config = workspace.getConfig();

    try (@SuppressWarnings("unused")
        Unlocker u = resourcesLocks.lock(accountId)) {
      checkRuntimeResourceAvailability(accountId);
      if (config != null) {
        checkRamResourcesAvailability(accountId, workspace.getNamespace(), config, envName);
      }

      return super.startWorkspace(workspaceId, envName, options);
    }
  }

  @Override
  public WorkspaceImpl startWorkspace(
      WorkspaceConfig config, String namespace, boolean isTemporary, Map<String, String> options)
      throws ServerException, NotFoundException, ConflictException, ValidationException {
    checkMaxEnvironmentRam(config);

    String accountId = accountManager.getByName(namespace).getId();
    try (@SuppressWarnings("unused")
        Unlocker u = resourcesLocks.lock(accountId)) {
      checkWorkspaceResourceAvailability(accountId);
      checkRuntimeResourceAvailability(accountId);
      if (config != null) {
        checkRamResourcesAvailability(accountId, namespace, config, null);
      }

      return super.startWorkspace(config, namespace, isTemporary, options);
    }
  }

  @Override
  public WorkspaceImpl updateWorkspace(String id, Workspace update)
      throws ConflictException, ServerException, NotFoundException, ValidationException {
    if (update.getConfig() != null) {
      checkMaxEnvironmentRam(update.getConfig());
    }

    WorkspaceImpl workspace = this.getWorkspace(id);
    String accountId = workspace.getAccount().getId();

    // Workspace must not be updated while the manager checks it's resources to allow start
    try (@SuppressWarnings("unused")
        Unlocker u = resourcesLocks.lock(accountId)) {
      return super.updateWorkspace(id, update);
    }
  }

  @VisibleForTesting
  void checkMaxEnvironmentRam(WorkspaceConfig config) throws ServerException {
    if (maxRamPerEnvMB < 0) {
      return;
    }
    if (config.getEnvironments().isEmpty()) {
      return;
    }
    for (Map.Entry<String, ? extends Environment> envEntry : config.getEnvironments().entrySet()) {
      Environment env = envEntry.getValue();
      final long workspaceRam = environmentRamCalculator.calculate(env);
      if (workspaceRam > maxRamPerEnvMB) {
        throw new LimitExceededException(
            format("You are only allowed to use %d mb. RAM per workspace.", maxRamPerEnvMB),
            ImmutableMap.of(
                "environment_max_ram",
                Long.toString(maxRamPerEnvMB),
                "environment_max_ram_unit",
                "mb",
                "environment_ram",
                Long.toString(workspaceRam),
                "environment_ram_unit",
                "mb"));
      }
    }
  }

  @VisibleForTesting
  void checkRamResourcesAvailability(
      String accountId, String namespace, WorkspaceConfig config, @Nullable String envName)
      throws NotFoundException, ServerException, ConflictException {
    if (config.getEnvironments().isEmpty()) {
      return;
    }
    final Environment environment =
        config.getEnvironments().get(firstNonNull(envName, config.getDefaultEnv()));
    final ResourceImpl ramToUse =
        new ResourceImpl(
            RamResourceType.ID,
            environmentRamCalculator.calculate(environment),
            RamResourceType.UNIT);
    try {
      resourceManager.checkResourcesAvailability(accountId, singletonList(ramToUse));
    } catch (NoEnoughResourcesException e) {
      final Resource requiredRam =
          e.getRequiredResources().get(0); // starting of workspace requires only RAM resource
      final Resource availableRam =
          getResourceOrDefault(
              e.getAvailableResources(), RamResourceType.ID, 0, RamResourceType.UNIT);
      final Resource usedRam =
          getResourceOrDefault(
              resourceManager.getUsedResources(accountId),
              RamResourceType.ID,
              0,
              RamResourceType.UNIT);

      throw new LimitExceededException(
          format(
              "Workspace %s/%s needs %s to start. Your account has %s available and %s in use. "
                  + "The workspace can't be start. Stop other workspaces or grant more resources.",
              namespace,
              config.getName(),
              printResourceInfo(requiredRam),
              printResourceInfo(availableRam),
              printResourceInfo(usedRam)));
    }
  }

  @VisibleForTesting
  void checkWorkspaceResourceAvailability(String accountId)
      throws NotFoundException, ServerException {
    try {
      resourceManager.checkResourcesAvailability(
          accountId,
          singletonList(new ResourceImpl(WorkspaceResourceType.ID, 1, WorkspaceResourceType.UNIT)));
    } catch (NoEnoughResourcesException e) {
      throw new LimitExceededException("You are not allowed to create more workspaces.");
    }
  }

  @VisibleForTesting
  void checkRuntimeResourceAvailability(String accountId)
      throws NotFoundException, ServerException {
    try {
      resourceManager.checkResourcesAvailability(
          accountId,
          singletonList(new ResourceImpl(RuntimeResourceType.ID, 1, RuntimeResourceType.UNIT)));
    } catch (NoEnoughResourcesException e) {
      throw new LimitExceededException("You are not allowed to start more workspaces.");
    }
  }

  /**
   * Returns resource with specified type from list or resource with specified default amount if
   * list doesn't contain it
   */
  private Resource getResourceOrDefault(
      List<? extends Resource> resources,
      String resourceType,
      long defaultAmount,
      String defaultUnit) {
    Optional<? extends Resource> resource = getResource(resources, resourceType);
    if (resource.isPresent()) {
      return resource.get();
    } else {
      return new ResourceImpl(resourceType, defaultAmount, defaultUnit);
    }
  }

  /** Returns resource with specified type from list */
  private Optional<? extends Resource> getResource(
      List<? extends Resource> resources, String resourceType) {
    return resources.stream().filter(r -> r.getType().equals(resourceType)).findAny();
  }

  private String printResourceInfo(Resource resource) {
    return resource.getAmount() + resource.getUnit().toUpperCase();
  }
}

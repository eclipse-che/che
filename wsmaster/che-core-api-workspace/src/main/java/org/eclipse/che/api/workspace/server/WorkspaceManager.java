/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server;

import static com.google.common.base.MoreObjects.firstNonNull;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.util.Objects.requireNonNull;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;

import com.google.inject.Inject;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import javax.inject.Singleton;
import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.shared.model.Account;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.Page;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.api.workspace.shared.event.WorkspaceCreatedEvent;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facade for Workspace related operations.
 *
 * @author gazarenkov
 * @author Alexander Garagatyi
 * @author Yevhenii Voevodin
 * @author Igor Vinokur
 */
@Singleton
public class WorkspaceManager {

  private static final Logger LOG = LoggerFactory.getLogger(WorkspaceManager.class);

  /** This attribute describes time when workspace was created. */
  public static final String CREATED_ATTRIBUTE_NAME = "created";
  /** This attribute describes time when workspace was last updated or started/stopped/recovered. */
  public static final String UPDATED_ATTRIBUTE_NAME = "updated";

  private final WorkspaceDao workspaceDao;
  private final WorkspaceRuntimes runtimes;
  private final AccountManager accountManager;
  private final EventService eventService;
  private final WorkspaceValidator validator;

  @Inject
  public WorkspaceManager(
      WorkspaceDao workspaceDao,
      WorkspaceRuntimes runtimes,
      EventService eventService,
      AccountManager accountManager,
      WorkspaceValidator validator) {
    this.workspaceDao = workspaceDao;
    this.runtimes = runtimes;
    this.accountManager = accountManager;
    this.eventService = eventService;
    this.validator = validator;
  }

  /**
   * Creates a new {@link Workspace} instance based on the given configuration and the instance
   * attributes.
   *
   * @param config the workspace config to create the new workspace instance
   * @param namespace workspace name is unique in this namespace
   * @param attributes workspace instance attributes
   * @return new workspace instance
   * @throws NullPointerException when either {@code config} or {@code namespace} is null
   * @throws NotFoundException when account with given id was not found
   * @throws ConflictException when any conflict occurs (e.g Workspace with such name already exists
   *     for {@code owner})
   * @throws ServerException when any other error occurs
   * @throws ValidationException when incoming configuration or attributes are not valid
   */
  public WorkspaceImpl createWorkspace(
      WorkspaceConfig config, String namespace, @Nullable Map<String, String> attributes)
      throws ServerException, NotFoundException, ConflictException, ValidationException {
    requireNonNull(config, "Required non-null config");
    requireNonNull(namespace, "Required non-null namespace");
    validator.validateConfig(config);
    if (attributes != null) {
      validator.validateAttributes(attributes);
    }
    return doCreateWorkspace(config, accountManager.getByName(namespace), attributes, false);
  }

  /**
   * Gets workspace by composite key.
   *
   * <p>
   *
   * <p>Key rules:
   *
   * <ul>
   *   <li>@Deprecated : If it contains <b>:</b> character then that key is combination of namespace
   *       and workspace name
   *   <li>@Deprecated : <b></>:workspace_name</b> is valid abstract key and current user name will
   *       be used as namespace
   *   <li>If it doesn't contain <b>/</b> character then that key is id(e.g. workspace123456)
   *   <li>If it contains <b>/</b> character then that key is combination of namespace and workspace
   *       name
   * </ul>
   *
   * <p>Note that namespace can contain <b>/</b> character.
   *
   * @param key composite key(e.g. workspace 'id' or 'namespace/name')
   * @return the workspace instance
   * @throws NullPointerException when {@code key} is null
   * @throws NotFoundException when workspace doesn't exist
   * @throws ServerException when any server error occurs
   */
  public WorkspaceImpl getWorkspace(String key) throws NotFoundException, ServerException {
    requireNonNull(key, "Required non-null workspace key");
    return normalizeState(getByKey(key), true);
  }

  /**
   * Gets workspace by name and owner.
   *
   * <p>
   *
   * <p>Returned instance status is either {@link WorkspaceStatus#STOPPED} or defined by its
   * runtime(if exists).
   *
   * @param name the name of the workspace
   * @param namespace the owner of the workspace
   * @return the workspace instance
   * @throws NotFoundException when workspace with such id doesn't exist
   * @throws ServerException when any server error occurs
   */
  public WorkspaceImpl getWorkspace(String name, String namespace)
      throws NotFoundException, ServerException {
    requireNonNull(name, "Required non-null workspace name");
    requireNonNull(namespace, "Required non-null workspace owner");
    // return getByKey(namespace + ":" +name);
    return normalizeState(workspaceDao.get(name, namespace), true);
  }

  /**
   * Gets list of workspaces which user can read
   *
   * <p>
   *
   * <p>Returned workspaces have either {@link WorkspaceStatus#STOPPED} status or status defined by
   * their runtime instances(if those exist).
   *
   * @param user the id of the user
   * @param includeRuntimes if <code>true</code>, will fetch runtime info for workspaces. If <code>
   *     false</code>, will not fetch runtime info.
   * @return the list of workspaces or empty list if user can't read any workspace
   * @throws NullPointerException when {@code user} is null
   * @throws ServerException when any server error occurs while getting workspaces with {@link
   *     WorkspaceDao#getWorkspaces(String)}
   */
  public Page<WorkspaceImpl> getWorkspaces(
      String user, boolean includeRuntimes, int maxItems, long skipCount) throws ServerException {
    requireNonNull(user, "Required non-null user id");
    final Page<WorkspaceImpl> workspaces = workspaceDao.getWorkspaces(user, maxItems, skipCount);
    for (WorkspaceImpl workspace : workspaces.getItems()) {
      normalizeState(workspace, includeRuntimes);
    }
    return workspaces;
  }

  /**
   * Gets list of workspaces which has given namespace
   *
   * <p>
   *
   * <p>Returned workspaces have either {@link WorkspaceStatus#STOPPED} status or status defined by
   * their runtime instances(if those exist).
   *
   * @param namespace the namespace to find workspaces
   * @param includeRuntimes if <code>true</code>, will fetch runtime info for workspaces. If <code>
   *     false</code>, will not fetch runtime info.
   * @return the list of workspaces or empty list if no matches
   * @throws NullPointerException when {@code namespace} is null
   * @throws ServerException when any server error occurs while getting workspaces with {@link
   *     WorkspaceDao#getByNamespace(String)}
   */
  public Page<WorkspaceImpl> getByNamespace(
      String namespace, boolean includeRuntimes, int maxItems, long skipCount)
      throws ServerException {
    requireNonNull(namespace, "Required non-null namespace");
    final Page<WorkspaceImpl> workspaces =
        workspaceDao.getByNamespace(namespace, maxItems, skipCount);
    for (WorkspaceImpl workspace : workspaces.getItems()) {
      normalizeState(workspace, includeRuntimes);
    }
    return workspaces;
  }

  /**
   * Updates an existing workspace with a new configuration.
   *
   * <p>
   *
   * <p>Replace strategy is used for workspace update, it means that existing workspace data will be
   * replaced with given {@code update}.
   *
   * @param update workspace update
   * @return updated instance of the workspace
   * @throws NullPointerException when either {@code workspaceId} or {@code update} is null
   * @throws NotFoundException when workspace with given id doesn't exist
   * @throws ConflictException when any conflict occurs (e.g Workspace with such name already exists
   *     in {@code namespace})
   * @throws ServerException when any other error occurs
   */
  public WorkspaceImpl updateWorkspace(String id, Workspace update)
      throws ConflictException, ServerException, NotFoundException, ValidationException {
    requireNonNull(id, "Required non-null workspace id");
    requireNonNull(update, "Required non-null workspace update");
    requireNonNull(update.getConfig(), "Required non-null workspace configuration update");
    validator.validateConfig(update.getConfig());
    validator.validateAttributes(update.getAttributes());

    WorkspaceImpl workspace = workspaceDao.get(id);
    workspace.setConfig(new WorkspaceConfigImpl(update.getConfig()));
    workspace.setAttributes(update.getAttributes());
    workspace.getAttributes().put(UPDATED_ATTRIBUTE_NAME, Long.toString(currentTimeMillis()));
    workspace.setTemporary(update.isTemporary());

    return normalizeState(workspaceDao.update(workspace), true);
  }

  /**
   * Removes workspace with specified identifier.
   *
   * <p>
   *
   * <p>Does not remove the workspace if it has the runtime, throws {@link ConflictException} in
   * this case. Won't throw any exception if workspace doesn't exist.
   *
   * @param workspaceId workspace id to remove workspace
   * @throws ConflictException when workspace has runtime
   * @throws ServerException when any server error occurs
   * @throws NullPointerException when {@code workspaceId} is null
   */
  public void removeWorkspace(String workspaceId) throws ConflictException, ServerException {
    requireNonNull(workspaceId, "Required non-null workspace id");
    if (runtimes.hasRuntime(workspaceId)) {
      throw new ConflictException(
          format("The workspace '%s' is currently running and cannot be removed.", workspaceId));
    }

    workspaceDao.remove(workspaceId);
    LOG.info("Workspace '{}' removed by user '{}'", workspaceId, sessionUserNameOrUndefined());
  }

  /**
   * Asynchronously starts certain workspace with specified environment and account.
   *
   * @param workspaceId identifier of workspace which should be started
   * @param envName name of environment or null, when default environment should be used
   * @param options possible startup options
   * @return starting workspace
   * @throws NullPointerException when {@code workspaceId} is null
   * @throws NotFoundException when workspace with given {@code workspaceId} doesn't exist
   * @throws ServerException when any other error occurs during workspace start
   */
  public WorkspaceImpl startWorkspace(
      String workspaceId, @Nullable String envName, @Nullable Map<String, String> options)
      throws NotFoundException, ServerException, ConflictException {
    requireNonNull(workspaceId, "Required non-null workspace id");
    final WorkspaceImpl workspace = workspaceDao.get(workspaceId);
    startAsync(workspace, envName, options);
    return normalizeState(workspace, true);
  }

  /**
   * Asynchronously starts workspace from the given configuration.
   *
   * @param config workspace configuration from which workspace is created and started
   * @param namespace workspace name is unique in this namespace
   * @return starting workspace
   * @throws NullPointerException when {@code workspaceId} is null
   * @throws NotFoundException when workspace with given {@code workspaceId} doesn't exist
   * @throws ServerException when any other error occurs during workspace start
   */
  public WorkspaceImpl startWorkspace(
      WorkspaceConfig config, String namespace, boolean isTemporary, Map<String, String> options)
      throws ServerException, NotFoundException, ConflictException, ValidationException {
    requireNonNull(config, "Required non-null configuration");
    requireNonNull(namespace, "Required non-null namespace");
    validator.validateConfig(config);
    final WorkspaceImpl workspace =
        doCreateWorkspace(
            config, accountManager.getByName(namespace), Collections.emptyMap(), isTemporary);
    startAsync(workspace, workspace.getConfig().getDefaultEnv(), options);
    return normalizeState(workspace, true);
  }

  /**
   * Asynchronously stops the workspace.
   *
   * @param workspaceId the id of the workspace to stop
   * @throws ServerException when any server error occurs
   * @throws NullPointerException when {@code workspaceId} is null
   * @throws NotFoundException when workspace {@code workspaceId} doesn't have runtime
   */
  public void stopWorkspace(String workspaceId, Map<String, String> options)
      throws ServerException, NotFoundException, ConflictException {

    requireNonNull(workspaceId, "Required non-null workspace id");
    final WorkspaceImpl workspace = normalizeState(workspaceDao.get(workspaceId), true);
    checkWorkspaceIsRunningOrStarting(workspace);
    if (!workspace.isTemporary()) {
      workspace.getAttributes().put(UPDATED_ATTRIBUTE_NAME, Long.toString(currentTimeMillis()));
      workspaceDao.update(workspace);
    }

    runtimes
        .stopAsync(workspace, options)
        .whenComplete(
            (aVoid, throwable) -> {
              if (workspace.isTemporary()) {
                removeWorkspaceQuietly(workspace);
              }
            });
  }

  public Set<String> getSupportedRecipes() {
    return runtimes.getSupportedRecipes();
  }

  /** Asynchronously starts given workspace. */
  private void startAsync(WorkspaceImpl workspace, String envName, Map<String, String> options)
      throws ConflictException, NotFoundException, ServerException {
    if (envName != null && !workspace.getConfig().getEnvironments().containsKey(envName)) {
      throw new NotFoundException(
          format(
              "Workspace '%s:%s' doesn't contain environment '%s'",
              workspace.getNamespace(), workspace.getConfig().getName(), envName));
    }
    workspace.getAttributes().put(UPDATED_ATTRIBUTE_NAME, Long.toString(currentTimeMillis()));
    workspaceDao.update(workspace);
    final String env = firstNonNull(envName, workspace.getConfig().getDefaultEnv());

    runtimes
        .startAsync(workspace, env, firstNonNull(options, Collections.emptyMap()))
        .exceptionally(
            ex -> {
              if (workspace.isTemporary()) {
                removeWorkspaceQuietly(workspace);
              }
              return null;
            });
  }

  private void checkWorkspaceIsRunningOrStarting(WorkspaceImpl workspace) throws ConflictException {
    if (workspace.getStatus() != RUNNING && workspace.getStatus() != STARTING) {
      throw new ConflictException(
          format(
              "Could not stop the workspace '%s/%s' because its status is '%s'.",
              workspace.getNamespace(), workspace.getConfig().getName(), workspace.getStatus()));
    }
  }

  private void removeWorkspaceQuietly(Workspace workspace) {
    try {
      workspaceDao.remove(workspace.getId());
    } catch (ServerException x) {
      LOG.error("Unable to remove temporary workspace '{}'", workspace.getId());
    }
  }

  private String sessionUserNameOrUndefined() {
    final Subject subject = EnvironmentContext.getCurrent().getSubject();
    if (!subject.isAnonymous()) {
      return subject.getUserName();
    }
    return "undefined";
  }

  private WorkspaceImpl normalizeState(WorkspaceImpl workspace, boolean includeRuntimes)
      throws ServerException {
    if (includeRuntimes) {
      runtimes.injectRuntime(workspace);
    } else {
      workspace.setStatus(runtimes.getStatus(workspace.getId()));
    }
    return workspace;
  }

  private WorkspaceImpl doCreateWorkspace(
      WorkspaceConfig config, Account account, Map<String, String> attributes, boolean isTemporary)
      throws NotFoundException, ServerException, ConflictException {
    WorkspaceImpl workspace =
        WorkspaceImpl.builder()
            .generateId()
            .setConfig(config)
            .setAccount(account)
            .setAttributes(attributes)
            .setTemporary(isTemporary)
            .setStatus(STOPPED)
            .build();
    workspace.getAttributes().put(CREATED_ATTRIBUTE_NAME, Long.toString(currentTimeMillis()));

    workspaceDao.create(workspace);
    LOG.info(
        "Workspace '{}/{}' with id '{}' created by user '{}'",
        account.getName(),
        workspace.getConfig().getName(),
        workspace.getId(),
        sessionUserNameOrUndefined());
    eventService.publish(new WorkspaceCreatedEvent(workspace));
    return workspace;
  }

  private WorkspaceImpl getByKey(String key) throws NotFoundException, ServerException {

    int lastColonIndex = key.indexOf(":");
    int lastSlashIndex = key.lastIndexOf("/");
    if (lastSlashIndex == -1 && lastColonIndex == -1) {
      // key is id
      return workspaceDao.get(key);
    }

    final String namespace;
    final String wsName;
    if (lastColonIndex == 0) {
      // no namespace, use current user namespace
      namespace = EnvironmentContext.getCurrent().getSubject().getUserName();
      wsName = key.substring(1);
    } else if (lastColonIndex > 0) {
      wsName = key.substring(lastColonIndex + 1);
      namespace = key.substring(0, lastColonIndex);
    } else {
      namespace = key.substring(0, lastSlashIndex);
      wsName = key.substring(lastSlashIndex + 1);
    }
    return workspaceDao.get(wsName, namespace);
  }
}

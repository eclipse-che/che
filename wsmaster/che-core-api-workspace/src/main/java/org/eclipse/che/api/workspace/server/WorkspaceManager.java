/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.Boolean.parseBoolean;
import static java.lang.String.format;
import static java.lang.System.currentTimeMillis;
import static java.time.Instant.now;
import static java.util.Objects.requireNonNull;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.RUNNING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STARTING;
import static org.eclipse.che.api.core.model.workspace.WorkspaceStatus.STOPPED;
import static org.eclipse.che.api.workspace.shared.Constants.CREATED_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.ERROR_MESSAGE_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.LAST_ACTIVE_INFRASTRUCTURE_NAMESPACE;
import static org.eclipse.che.api.workspace.shared.Constants.LAST_ACTIVITY_TIME;
import static org.eclipse.che.api.workspace.shared.Constants.REMOVE_WORKSPACE_AFTER_STOP;
import static org.eclipse.che.api.workspace.shared.Constants.STOPPED_ABNORMALLY_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.STOPPED_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.UPDATED_ATTRIBUTE_NAME;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_GENERATE_NAME_CHARS_APPEND;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE;

import com.google.inject.Inject;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
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
import org.eclipse.che.api.core.model.workspace.devfile.Devfile;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.user.server.PreferenceManager;
import org.eclipse.che.api.workspace.server.devfile.FileContentProvider;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileFormatException;
import org.eclipse.che.api.workspace.server.devfile.validator.DevfileIntegrityValidator;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.MetadataImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.NamespaceResolutionContext;
import org.eclipse.che.api.workspace.server.spi.WorkspaceDao;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.api.workspace.shared.event.WorkspaceCreatedEvent;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.annotation.Traced;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.tracing.TracingTags;
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

  private final WorkspaceDao workspaceDao;
  private final WorkspaceRuntimes runtimes;
  private final AccountManager accountManager;
  private final EventService eventService;
  private final PreferenceManager preferenceManager;
  private final WorkspaceValidator validator;
  private final DevfileIntegrityValidator devfileIntegrityValidator;

  @Inject
  public WorkspaceManager(
      WorkspaceDao workspaceDao,
      WorkspaceRuntimes runtimes,
      EventService eventService,
      AccountManager accountManager,
      PreferenceManager preferenceManager,
      WorkspaceValidator validator,
      DevfileIntegrityValidator devfileIntegrityValidator) {
    this.workspaceDao = workspaceDao;
    this.runtimes = runtimes;
    this.accountManager = accountManager;
    this.eventService = eventService;
    this.preferenceManager = preferenceManager;
    this.validator = validator;
    this.devfileIntegrityValidator = devfileIntegrityValidator;
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
  @Traced
  public WorkspaceImpl createWorkspace(
      WorkspaceConfig config, String namespace, Map<String, String> attributes)
      throws ServerException, NotFoundException, ConflictException, ValidationException {
    requireNonNull(config, "Required non-null config");
    requireNonNull(namespace, "Required non-null namespace");
    validator.validateConfig(config);
    validator.validateAttributes(attributes);

    WorkspaceImpl workspace =
        doCreateWorkspace(config, accountManager.getByName(namespace), attributes, false);
    TracingTags.WORKSPACE_ID.set(workspace.getId());
    return workspace;
  }

  /**
   * Creates a workspace out of a devfile.
   *
   * <p>The devfile should have been validated using the {@link
   * DevfileIntegrityValidator#validateDevfile(Devfile)}. This method does rest of the validation
   * and actually creates the workspace.
   *
   * @param devfile the devfile describing the workspace
   * @param namespace workspace name is unique in this namespace
   * @param attributes workspace instance attributes
   * @param contentProvider the content provider to use for resolving content references in the
   *     devfile
   * @return new workspace instance
   * @throws NullPointerException when either {@code config} or {@code namespace} is null
   * @throws NotFoundException when account with given id was not found
   * @throws ConflictException when any conflict occurs (e.g Workspace with such name already exists
   *     for {@code owner})
   * @throws ServerException when any other error occurs
   * @throws ValidationException when incoming configuration or attributes are not valid
   */
  @Traced
  public WorkspaceImpl createWorkspace(
      Devfile devfile,
      String namespace,
      Map<String, String> attributes,
      FileContentProvider contentProvider)
      throws ServerException, NotFoundException, ConflictException, ValidationException {
    requireNonNull(devfile, "Required non-null devfile");
    requireNonNull(namespace, "Required non-null namespace");
    validator.validateAttributes(attributes);

    devfile = generateNameIfNeeded(devfile);

    try {
      devfileIntegrityValidator.validateContentReferences(devfile, contentProvider);
    } catch (DevfileFormatException e) {
      throw new ValidationException(e.getMessage(), e);
    }

    WorkspaceImpl workspace =
        doCreateWorkspace(devfile, accountManager.getByName(namespace), attributes, false);
    TracingTags.WORKSPACE_ID.set(workspace.getId());
    return workspace;
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
   * <p>Returned workspaces have either {@link WorkspaceStatus#STOPPED} status or status defined by
   * their runtime instances(if those exist).
   *
   * @param user the id of the user
   * @param includeRuntimes if <code>true</code>, will fetch runtime info for workspaces. If <code>
   *     false</code>, will not fetch runtime info.
   * @return the list of workspaces or empty list if user can't read any workspace
   * @throws NullPointerException when {@code user} is null
   * @throws ServerException when any server error occurs while getting workspaces with {@link
   *     WorkspaceDao#getWorkspaces(String, int, long)}
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
   *     WorkspaceDao#getByNamespace(String, int, long)}
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
    checkArgument(
        update.getConfig() != null ^ update.getDevfile() != null,
        "Required non-null workspace configuration or devfile update but not both");
    if (update.getConfig() != null) {
      validator.validateConfig(update.getConfig());
    }
    WorkspaceImpl workspace = workspaceDao.get(id);

    validator.validateUpdateAttributes(workspace.getAttributes(), update.getAttributes());

    if (workspace.getConfig() != null) {
      workspace.setConfig(new WorkspaceConfigImpl(update.getConfig()));
    }
    if (workspace.getDevfile() != null) {
      workspace.setDevfile(new DevfileImpl(update.getDevfile()));
    }

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
  @Traced
  public void removeWorkspace(String workspaceId) throws ConflictException, ServerException {
    TracingTags.WORKSPACE_ID.set(workspaceId);

    requireNonNull(workspaceId, "Required non-null workspace id");
    if (runtimes.hasRuntime(workspaceId)) {
      throw new ConflictException(
          format("The workspace '%s' is currently running and cannot be removed.", workspaceId));
    }

    Optional<WorkspaceImpl> workspaceOpt = workspaceDao.remove(workspaceId);

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
      workspace.getAttributes().put(STOPPED_ATTRIBUTE_NAME, Long.toString(currentTimeMillis()));
      workspace.getAttributes().put(STOPPED_ABNORMALLY_ATTRIBUTE_NAME, Boolean.toString(false));
      workspaceDao.update(workspace);
    }
    String namespace = workspace.getAttributes().get(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE);
    final String owner = workspace.getRuntime().getOwner();

    runtimes
        .stopAsync(workspace, options)
        .whenComplete(
            (aVoid, throwable) -> {
              if (workspace.isTemporary()
                  || parseBoolean(options.get(REMOVE_WORKSPACE_AFTER_STOP))) {
                removeWorkspaceQuietly(workspace.getId());
              }
              try {
                if (runtimes.getActive(owner).isEmpty()) {
                  recordLastWorkspaceStoppedTime(namespace, owner);
                }
              } catch (ServerException | InfrastructureException e) {
                LOG.error(e.getMessage(), e);
              }
            });
  }

  private void recordLastWorkspaceStoppedTime(String namespace, String owner) {
    try {
      Map<String, String> preferences = preferenceManager.find(owner);
      String currentTime = Long.toString(now().getEpochSecond());
      preferences.put(LAST_ACTIVITY_TIME, currentTime);
      preferences.put(LAST_ACTIVE_INFRASTRUCTURE_NAMESPACE, namespace);
      preferenceManager.update(owner, preferences);
    } catch (ServerException e) {
      LOG.error(e.getMessage(), e);
    }
  }

  private void cleanLastWorkspaceStoppedTime(String owner) {
    try {
      preferenceManager.remove(
          owner, Arrays.asList(LAST_ACTIVITY_TIME, LAST_ACTIVE_INFRASTRUCTURE_NAMESPACE));
    } catch (ServerException e) {
      LOG.error(e.getMessage(), e);
    }
  }

  /** Returns a set of supported recipe types */
  public Set<String> getSupportedRecipes() {
    return runtimes.getSupportedRecipes();
  }

  /** Asynchronously starts given workspace. */
  private void startAsync(
      WorkspaceImpl workspace, @Nullable String envName, Map<String, String> options)
      throws ConflictException, NotFoundException, ServerException {

    try {
      runtimes.validate(workspace, envName);
    } catch (ValidationException e) {
      throw new ConflictException(e.getMessage(), e);
    }

    // handle the situation where a workspace created by a previous Che version doesn't have a
    // namespace stored for it. We use the legacy-aware method to figure out the namespace to
    // correctly capture the workspaces which have PVCs already in a namespace defined by the legacy
    // configuration variable.
    String targetNamespace =
        workspace.getAttributes().get(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE);
    if (isNullOrEmpty(targetNamespace)) {
      try {
        targetNamespace = runtimes.evalInfrastructureNamespace(buildResolutionContext(workspace));
        workspace
            .getAttributes()
            .put(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, targetNamespace);
      } catch (InfrastructureException e) {
        throw new ServerException(e);
      }
    }

    if (!runtimes.isInfrastructureNamespaceValid(targetNamespace)) {
      try {
        targetNamespace = runtimes.evalInfrastructureNamespace(buildResolutionContext(workspace));

        if (targetNamespace == null || !runtimes.isInfrastructureNamespaceValid(targetNamespace)) {
          throw new ServerException(
              format(
                  "The workspace would be started in a namespace/project"
                      + " '%s', which is not a valid namespace/project name.",
                  targetNamespace));
        }

        workspace
            .getAttributes()
            .put(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, targetNamespace);
      } catch (InfrastructureException e) {
        throw new ServerException(e);
      }
    }

    workspace.getAttributes().put(UPDATED_ATTRIBUTE_NAME, Long.toString(currentTimeMillis()));
    workspaceDao.update(workspace);

    runtimes
        .startAsync(workspace, envName, firstNonNull(options, Collections.emptyMap()))
        .thenAccept(aVoid -> handleStartupSuccess(workspace.getId()))
        .exceptionally(
            ex -> {
              if (workspace.isTemporary()) {
                removeWorkspaceQuietly(workspace.getId());
              } else {
                handleStartupError(workspace.getId(), ex.getCause());
              }
              return null;
            });
  }

  private NamespaceResolutionContext buildResolutionContext(WorkspaceImpl workspace) {
    Subject currentSubject = EnvironmentContext.getCurrent().getSubject();
    return new NamespaceResolutionContext(
        workspace.getId(), currentSubject.getUserId(), currentSubject.getUserName());
  }

  /** Returns first non-null argument or null if both are null. */
  private <T> T firstNonNull(T first, T second) {
    return first != null ? first : second;
  }

  private void checkWorkspaceIsRunningOrStarting(WorkspaceImpl workspace) throws ConflictException {
    if (workspace.getStatus() != RUNNING && workspace.getStatus() != STARTING) {
      throw new ConflictException(
          format(
              "Could not stop the workspace '%s/%s' because its status is '%s'.",
              workspace.getNamespace(), workspace.getName(), workspace.getStatus()));
    }
  }

  private void removeWorkspaceQuietly(String workspaceId) {
    try {
      workspaceDao.remove(workspaceId);
    } catch (ServerException x) {
      LOG.error(
          "Unable to remove temporary workspace '{}'. Error message was: {}",
          workspaceId,
          x.getMessage());
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

  private void handleStartupError(String workspaceId, Throwable t) {
    try {
      // we need to reload the workspace because the runtimes might have updated it
      WorkspaceImpl workspace = getWorkspace(workspaceId);
      workspace
          .getAttributes()
          .put(
              ERROR_MESSAGE_ATTRIBUTE_NAME,
              t instanceof RuntimeException ? t.getCause().getMessage() : t.getMessage());
      workspace.getAttributes().put(STOPPED_ATTRIBUTE_NAME, Long.toString(currentTimeMillis()));
      workspace.getAttributes().put(STOPPED_ABNORMALLY_ATTRIBUTE_NAME, Boolean.toString(true));
      workspaceDao.update(workspace);
    } catch (NotFoundException | ServerException | ConflictException e) {
      LOG.warn(
          String.format(
              "Cannot set error status of the workspace %s. Error is: %s",
              workspaceId, e.getMessage()));
    }
  }

  private void handleStartupSuccess(String workspaceId) {
    try {
      // we need to reload the workspace because the runtimes might have updated it
      WorkspaceImpl workspace = getWorkspace(workspaceId);

      workspace.getAttributes().remove(STOPPED_ATTRIBUTE_NAME);
      workspace.getAttributes().remove(STOPPED_ABNORMALLY_ATTRIBUTE_NAME);
      workspace.getAttributes().remove(ERROR_MESSAGE_ATTRIBUTE_NAME);

      workspaceDao.update(workspace);

      cleanLastWorkspaceStoppedTime(workspace.getRuntime().getOwner());
    } catch (NotFoundException | ServerException | ConflictException e) {
      LOG.warn(
          String.format(
              "Cannot clear error status status of the workspace %s. Error is: %s",
              workspaceId, e.getMessage()));
    }
  }

  /**
   * Gets total count of all workspaces
   *
   * @return workspaces count
   * @throws ServerException when any error occurs
   */
  public long getWorkspacesTotalCount() throws ServerException {
    return workspaceDao.getWorkspacesTotalCount();
  }

  private WorkspaceImpl doCreateWorkspace(
      WorkspaceConfig config, Account account, Map<String, String> attributes, boolean isTemporary)
      throws ConflictException, ServerException {
    return doCreateWorkspace(config, null, account, attributes, isTemporary);
  }

  private WorkspaceImpl doCreateWorkspace(
      Devfile devfile, Account account, Map<String, String> attributes, boolean isTemporary)
      throws ConflictException, ServerException {
    return doCreateWorkspace(null, devfile, account, attributes, isTemporary);
  }

  private WorkspaceImpl doCreateWorkspace(
      WorkspaceConfig config,
      Devfile devfile,
      Account account,
      Map<String, String> attributes,
      boolean isTemporary)
      throws ConflictException, ServerException {
    WorkspaceImpl workspace =
        WorkspaceImpl.builder()
            .generateId()
            .setAccount(account)
            .setConfig(config)
            .setDevfile(devfile)
            .setAttributes(attributes)
            .setTemporary(isTemporary)
            .setStatus(STOPPED)
            .build();
    workspace.getAttributes().put(CREATED_ATTRIBUTE_NAME, Long.toString(currentTimeMillis()));

    String targetNamespace =
        workspace.getAttributes().get(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE);
    if (isNullOrEmpty(targetNamespace)) {
      try {
        targetNamespace = runtimes.evalInfrastructureNamespace(buildResolutionContext(workspace));
        workspace
            .getAttributes()
            .put(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, targetNamespace);
      } catch (InfrastructureException e) {
        throw new ServerException(e);
      }
    }

    if (targetNamespace == null || !runtimes.isInfrastructureNamespaceValid(targetNamespace)) {
      throw new ServerException(
          format(
              "The workspace would be started in a namespace/project"
                  + " '%s', which is not a valid namespace/project name.",
              targetNamespace));
    }

    workspaceDao.create(workspace);
    LOG.info(
        "Workspace '{}/{}' with id '{}' created by user '{}'",
        account.getName(),
        workspace.getName(),
        workspace.getId(),
        sessionUserNameOrUndefined());
    eventService.publish(new WorkspaceCreatedEvent(workspace));
    return workspace;
  }

  /**
   * If 'generateName' is defined and 'name' is not, we generate name using 'generateName' as a
   * prefix following {@link Constants#WORKSPACE_GENERATE_NAME_CHARS_APPEND} random characters and
   * set it to 'name'.
   */
  private Devfile generateNameIfNeeded(Devfile origDevfile) {
    if (origDevfile.getMetadata() != null) {
      MetadataImpl metadata = new MetadataImpl(origDevfile.getMetadata());
      if (metadata.getName() == null && metadata.getGenerateName() != null) {
        metadata.setName(
            NameGenerator.generate(
                metadata.getGenerateName(), WORKSPACE_GENERATE_NAME_CHARS_APPEND));
        DevfileImpl devfileWithGeneratedName = new DevfileImpl(origDevfile);
        devfileWithGeneratedName.setMetadata(metadata);
        return devfileWithGeneratedName;
      }
    }
    return origDevfile;
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

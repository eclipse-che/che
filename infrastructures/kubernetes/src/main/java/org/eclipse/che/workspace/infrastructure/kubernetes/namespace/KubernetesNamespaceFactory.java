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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta.DEFAULT_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta.PHASE_ATTRIBUTE;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.client.KubernetesClientException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.inject.Named;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.NamespaceResolutionContext;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.inject.ConfigurationException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.server.impls.KubernetesNamespaceMetaImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSharedPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helps to create {@link KubernetesNamespace} instances.
 *
 * @author Anton Korneta
 */
@Singleton
public class KubernetesNamespaceFactory {

  private static final Logger LOG = LoggerFactory.getLogger(KubernetesNamespaceFactory.class);

  private static final Map<String, Function<NamespaceResolutionContext, String>>
      NAMESPACE_NAME_PLACEHOLDERS = new HashMap<>();

  private static final String USERNAME_PLACEHOLDER = "<username>";
  private static final String USERID_PLACEHOLDER = "<userid>";
  private static final String WORKSPACEID_PLACEHOLDER = "<workspaceid>";

  static {
    NAMESPACE_NAME_PLACEHOLDERS.put(USERNAME_PLACEHOLDER, NamespaceResolutionContext::getUserName);
    NAMESPACE_NAME_PLACEHOLDERS.put(USERID_PLACEHOLDER, NamespaceResolutionContext::getUserId);
    NAMESPACE_NAME_PLACEHOLDERS.put(
        WORKSPACEID_PLACEHOLDER, NamespaceResolutionContext::getWorkspaceId);
  }

  private final String defaultNamespaceName;
  private final boolean allowUserDefinedNamespaces;

  private final String legacyNamespaceName;
  private final String serviceAccountName;
  private final String clusterRoleName;
  private final KubernetesClientFactory clientFactory;
  private final UserManager userManager;
  protected final KubernetesSharedPool sharedPool;

  @Inject
  public KubernetesNamespaceFactory(
      @Nullable @Named("che.infra.kubernetes.namespace") String legacyNamespaceName,
      @Nullable @Named("che.infra.kubernetes.service_account_name") String serviceAccountName,
      @Nullable @Named("che.infra.kubernetes.cluster_role_name") String clusterRoleName,
      @Nullable @Named("che.infra.kubernetes.namespace.default") String defaultNamespaceName,
      @Named("che.infra.kubernetes.namespace.allow_user_defined")
          boolean allowUserDefinedNamespaces,
      KubernetesClientFactory clientFactory,
      UserManager userManager,
      KubernetesSharedPool sharedPool)
      throws ConfigurationException {
    this.userManager = userManager;
    this.legacyNamespaceName = legacyNamespaceName;
    this.serviceAccountName = serviceAccountName;
    this.clusterRoleName = clusterRoleName;
    this.clientFactory = clientFactory;
    this.defaultNamespaceName = defaultNamespaceName;
    this.allowUserDefinedNamespaces = allowUserDefinedNamespaces;
    this.sharedPool = sharedPool;

    if (isNullOrEmpty(defaultNamespaceName)) {
      throw new ConfigurationException("che.infra.kubernetes.namespace.default must be configured");
    }
  }

  private boolean hasPlaceholders(String namespaceName) {
    return namespaceName != null
        && NAMESPACE_NAME_PLACEHOLDERS.keySet().stream().anyMatch(namespaceName::contains);
  }

  /**
   * Creates a Kubernetes namespace for the specified workspace.
   *
   * <p>Namespace won't be prepared. This method should be used only in case workspace recovering.
   *
   * @param workspaceId identifier of the workspace
   * @return created namespace
   */
  public KubernetesNamespace access(String workspaceId, String namespace) {
    return doCreateNamespaceAccess(workspaceId, namespace);
  }

  @VisibleForTesting
  KubernetesNamespace doCreateNamespaceAccess(String workspaceId, String name) {
    return new KubernetesNamespace(clientFactory, sharedPool.getExecutor(), name, workspaceId);
  }

  /**
   * Checks if the current user is able to use the specified namespace for their new workspaces.
   *
   * @param namespaceName namespace name to check
   * @throws ValidationException if the specified namespace is not permitted for the current user
   */
  public void checkIfNamespaceIsAllowed(String namespaceName) throws ValidationException {
    if (allowUserDefinedNamespaces) {
      // any namespace name is allowed but workspace start may fail
      return;
    }

    String defaultNamespace =
        evalPlaceholders(defaultNamespaceName, EnvironmentContext.getCurrent().getSubject(), null);
    if (!namespaceName.equals(defaultNamespace)) {
      throw new ValidationException(
          format(
              "User defined namespaces are not allowed. Only the default namespace '%s' is available.",
              defaultNamespaceName));
    }
  }

  /** Returns list of k8s namespaces names where a user is able to run workspaces. */
  public List<KubernetesNamespaceMeta> list() throws InfrastructureException {
    if (!allowUserDefinedNamespaces) {
      return singletonList(getDefaultNamespace());
    }

    // if user defined namespaces are allowed - fetch all available
    List<KubernetesNamespaceMeta> namespaces = fetchNamespaces();

    provisionDefaultNamespace(namespaces);

    return namespaces;
  }

  /**
   * Returns default namespace, it's based on existing namespace if there is such or just object
   * holder if there is no such namespace on cluster.
   */
  private KubernetesNamespaceMeta getDefaultNamespace() throws InfrastructureException {
    // the default namespace must be configured if user defined are not allowed
    // so return only it
    NamespaceResolutionContext resolutionCtx =
        new NamespaceResolutionContext(
            // workspace id is not know at this stage.
            // It's good enough to have <workspaceid> placeholder after evaluating
            null,
            EnvironmentContext.getCurrent().getSubject().getUserId(),
            EnvironmentContext.getCurrent().getSubject().getUserName());
    String evaluatedName = evaluateNamespaceName(resolutionCtx);

    Optional<KubernetesNamespaceMeta> defaultNamespaceOpt = fetchNamespace(evaluatedName);

    KubernetesNamespaceMeta defaultNamespace =
        defaultNamespaceOpt
            // if the predefined namespace does not exist - return dummy info and it will be created
            // during the first workspace start
            .orElseGet(() -> new KubernetesNamespaceMetaImpl(evaluatedName));

    defaultNamespace.getAttributes().put(DEFAULT_ATTRIBUTE, "true");
    return defaultNamespace;
  }

  /**
   * Provision default namespace into the specified list. If default namespace is already there -
   * just provision the corresponding attributes to it.
   *
   * @param namespaces list where default namespace should be provisioned
   */
  private void provisionDefaultNamespace(List<KubernetesNamespaceMeta> namespaces) {
    String evaluatedName =
        evalPlaceholders(defaultNamespaceName, EnvironmentContext.getCurrent().getSubject(), null);

    Optional<KubernetesNamespaceMeta> defaultNamespaceOpt =
        namespaces.stream().filter(n -> evaluatedName.equals(n.getName())).findAny();
    KubernetesNamespaceMeta defaultNamespace;
    if (defaultNamespaceOpt.isPresent()) {
      defaultNamespace = defaultNamespaceOpt.get();
    } else {
      defaultNamespace = new KubernetesNamespaceMetaImpl(evaluatedName);
      namespaces.add(defaultNamespace);
    }

    defaultNamespace.getAttributes().put(DEFAULT_ATTRIBUTE, "true");
  }

  /**
   * Fetches the specified namespace from a cluster.
   *
   * @param name name of namespace that should be fetched.
   * @return optional with kubernetes namespace meta
   * @throws InfrastructureException when any error occurs during namespace fetching
   */
  protected Optional<KubernetesNamespaceMeta> fetchNamespace(String name)
      throws InfrastructureException {
    try {
      Namespace namespace = clientFactory.create().namespaces().withName(name).get();
      if (namespace == null) {
        return Optional.empty();
      } else {
        return Optional.of(asNamespaceMeta(namespace));
      }
    } catch (KubernetesClientException e) {
      throw new InfrastructureException(
          "Error occurred when tried to fetch default namespace. Cause: " + e.getMessage(), e);
    }
  }

  /**
   * Fetched namespace from a k8s cluster.
   *
   * @return list with available k8s namespace metas.
   * @throws InfrastructureException when any error occurs during namespaces fetching
   */
  protected List<KubernetesNamespaceMeta> fetchNamespaces() throws InfrastructureException {
    try {
      return clientFactory
          .create()
          .namespaces()
          .list()
          .getItems()
          .stream()
          .map(this::asNamespaceMeta)
          .collect(Collectors.toList());
    } catch (KubernetesClientException e) {
      throw new InfrastructureException(
          "Error occurred when tried to list all available namespaces. Cause: " + e.getMessage(),
          e);
    }
  }

  private KubernetesNamespaceMeta asNamespaceMeta(Namespace namespace) {
    Map<String, String> attributes = new HashMap<>(2);
    if (namespace.getStatus() != null && namespace.getStatus().getPhase() != null) {
      attributes.put(PHASE_ATTRIBUTE, namespace.getStatus().getPhase());
    }
    return new KubernetesNamespaceMetaImpl(namespace.getMetadata().getName(), attributes);
  }

  /**
   * Tells the caller whether the namespace that is being prepared for the provided workspace
   * runtime identity can be created or is expected to already be present.
   *
   * <p>Note that this method cannot be reduced to merely checking if user-defined namespaces are
   * allowed or not (and depending on prior validation using the {@link
   * #checkIfNamespaceIsAllowed(String)} method during the workspace creation) because workspace
   * start is a) async from workspace creation and the underlying namespaces might have disappeared
   * and b) can be called during workspace recovery, where we don't even have the current user in
   * the context.
   *
   * @param identity the identity of the workspace runtime
   * @return true if the namespace can be created, false if the namespace is expected to already
   *     exist
   * @throws InfrastructureException on failure
   */
  protected boolean canCreateNamespace(RuntimeIdentity identity) throws InfrastructureException {
    if (allowUserDefinedNamespaces) {
      return true;
    } else {
      // we need to make sure that the provided namespace is indeed the one provided by our
      // configuration
      User owner;
      try {
        owner = userManager.getById(identity.getOwnerId());
      } catch (NotFoundException | ServerException e) {
        throw new InfrastructureException(
            "Failed to resolve workspace owner. Cause: " + e.getMessage(), e);
      }

      String requiredNamespace = identity.getInfrastructureNamespace();

      NamespaceResolutionContext resolutionContext =
          new NamespaceResolutionContext(
              identity.getWorkspaceId(), identity.getOwnerId(), owner.getName());

      String resolvedDefaultNamespace = evalPlaceholders(defaultNamespaceName, resolutionContext);

      return resolvedDefaultNamespace.equals(requiredNamespace);
    }
  }

  /**
   * Tells the caller whether the namespace that is being prepared, should be marked as managed or
   * not.
   *
   * @param identity the runtime identity of the workspace
   * @return true if the workspace namespace should be marked managed, false otherwise
   */
  protected boolean shouldMarkNamespaceManaged(RuntimeIdentity identity) {
    // when infra namespace contains workspaceId that is generated
    // it mean that Che Server provides unique namespace for each workspace
    // and nothing else except workspace should be run there
    // Che Server also removes such namespace after workspace is removed
    return identity.getInfrastructureNamespace().contains(identity.getWorkspaceId());
  }

  public KubernetesNamespace getOrCreate(RuntimeIdentity identity) throws InfrastructureException {
    KubernetesNamespace namespace = get(identity);

    namespace.prepare(shouldMarkNamespaceManaged(identity), canCreateNamespace(identity));

    if (!isNullOrEmpty(serviceAccountName)) {
      KubernetesWorkspaceServiceAccount workspaceServiceAccount =
          doCreateServiceAccount(namespace.getWorkspaceId(), namespace.getName());
      workspaceServiceAccount.prepare();
    }

    return namespace;
  }

  public KubernetesNamespace get(RuntimeIdentity identity) throws InfrastructureException {
    String workspaceId = identity.getWorkspaceId();
    String namespaceName = identity.getInfrastructureNamespace();
    return doCreateNamespaceAccess(workspaceId, namespaceName);
  }

  /** Gets a namespace the workspace is deployed to. */
  public KubernetesNamespace get(Workspace workspace) throws InfrastructureException {
    return doCreateNamespaceAccess(workspace.getId(), getNamespaceName(workspace));
  }

  /** Returns a namespace name where workspace is assigned to. */
  protected String getNamespaceName(Workspace workspace) throws InfrastructureException {
    String namespace = workspace.getAttributes().get(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE);
    if (namespace == null) {
      // it seems to be legacy workspace since the namespace is not stored in workspace attributes
      // it's needed to evaluate that with current user and workspace id
      NamespaceResolutionContext resolutionCtx =
          new NamespaceResolutionContext(
              workspace.getId(),
              EnvironmentContext.getCurrent().getSubject().getUserId(),
              EnvironmentContext.getCurrent().getSubject().getUserName());
      namespace = evaluateLegacyNamespaceName(resolutionCtx);

      LOG.warn(
          "Workspace '{}' doesn't have an explicit namespace assigned."
              + " The legacy namespace resolution resolved it to '{}'.",
          workspace.getId(),
          namespace);
    }

    if (!NamespaceNameValidator.isValid(namespace)) {
      // At a certain unfortunate past version of Che, we stored invalid namespace names.
      // At this point in time, we're trying to work with an existing workspace that never could
      // started OR has been running since before that unfortunate version. In both cases, going
      // back to the default namespace name is the most safe bet we can make.

      // but of course, our attempt will be futile if we're running in a context that doesn't know
      // the current user.
      Subject subj = EnvironmentContext.getCurrent().getSubject();
      if (!subj.isAnonymous()) {
        NamespaceResolutionContext resolutionCtx =
            new NamespaceResolutionContext(workspace.getId(), subj.getUserId(), subj.getUserName());

        String defaultNamespace = evaluateNamespaceName(resolutionCtx);

        LOG.warn(
            "The namespace '{}' of the workspace '{}' is not valid. Trying to recover"
                + " from this situation using a default namespace which resolved to '{}'.",
            namespace,
            workspace.getId(),
            defaultNamespace);

        namespace = defaultNamespace;
      } else {
        // log a warning including a stacktrace to be able to figure out from where we got here...
        LOG.warn(
            "The namespace '{}' of the workspace '{}' is not valid but we currently don't have"
                + " an active user to try an recover from this situation. We're letting the parent"
                + " workflow continue, but it may fail at some later point in time because of"
                + " the incorrect namespace name in use.",
            namespace,
            workspace.getId(),
            new Throwable());
      }

      // ok, we tried to recover the namespace but nothing helped.
    }

    return namespace;
  }

  public String evaluateLegacyNamespaceName(NamespaceResolutionContext resolutionCtx)
      throws InfrastructureException {
    String namespace = resolveLegacyNamespaceName(resolutionCtx);

    if (!NamespaceNameValidator.isValid(namespace) || !checkNamespaceExists(namespace)) {
      namespace = evaluateNamespaceName(resolutionCtx);
    }

    return namespace;
  }

  /**
   * Evaluates namespace according to the specified context.
   *
   * <p>Kubernetes infrastructure use checks is evaluated legacy namespace exists, if it does - use
   * it. Otherwise evaluated new default namespace name;
   *
   * @param resolutionCtx context for namespace evaluation
   * @return evaluated namespace name
   * @throws InfrastructureException when there legacy namespace doesn't exist and default namespace
   *     is not configured
   * @throws InfrastructureException when any exception occurs during evaluation
   */
  public String evaluateNamespaceName(NamespaceResolutionContext resolutionCtx)
      throws InfrastructureException {
    String namespace = evalPlaceholders(defaultNamespaceName, resolutionCtx);

    LOG.debug(
        "Evaluated the namespace for workspace {} using the namespace default to {}",
        resolutionCtx.getWorkspaceId(),
        namespace);

    return namespace;
  }

  public void deleteIfManaged(Workspace workspace) throws InfrastructureException {
    KubernetesNamespace namespace = get(workspace);
    namespace.deleteIfManaged();
  }

  private String resolveLegacyNamespaceName(NamespaceResolutionContext resolutionCtx) {
    String effectiveOldLogicNamespace =
        isNullOrEmpty(legacyNamespaceName) ? WORKSPACEID_PLACEHOLDER : legacyNamespaceName;

    return evalPlaceholders(effectiveOldLogicNamespace, resolutionCtx);
  }

  protected boolean checkNamespaceExists(String namespaceName) throws InfrastructureException {
    try {
      return clientFactory.create().namespaces().withName(namespaceName).get() != null;
    } catch (KubernetesClientException e) {
      if (e.getCode() == 403) {
        // 403 means that the project does not exist
        // or a user really is not permitted to access it which is Che Server misconfiguration
        return false;
      } else {
        throw new InfrastructureException(
            format(
                "Error occurred while trying to fetch the namespace '%s'. Cause: %s",
                namespaceName, e.getMessage()),
            e);
      }
    }
  }

  protected String evalPlaceholders(String namespace, Subject currentUser, String workspaceId) {
    return evalPlaceholders(
        namespace,
        new NamespaceResolutionContext(
            workspaceId, currentUser.getUserId(), currentUser.getUserName()));
  }

  protected String evalPlaceholders(String namespace, NamespaceResolutionContext ctx) {
    checkArgument(!isNullOrEmpty(namespace));
    String evaluated = namespace;
    for (Entry<String, Function<NamespaceResolutionContext, String>> placeHolder :
        NAMESPACE_NAME_PLACEHOLDERS.entrySet()) {

      String key = placeHolder.getKey();
      String value = placeHolder.getValue().apply(ctx);

      if (value != null) {
        evaluated = evaluated.replaceAll(key, value);
      }
    }
    return evaluated;
  }

  @VisibleForTesting
  KubernetesWorkspaceServiceAccount doCreateServiceAccount(
      String workspaceId, String namespaceName) {
    return new KubernetesWorkspaceServiceAccount(
        workspaceId, namespaceName, serviceAccountName, clusterRoleName, clientFactory);
  }

  protected String getServiceAccountName() {
    return serviceAccountName;
  }

  protected String getClusterRoleName() {
    return clusterRoleName;
  }
}

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
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.inject.ConfigurationException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.server.impls.KubernetesNamespaceMetaImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta;
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

  private static final Map<String, Function<PlaceholderResolutionContext, String>>
      NAMESPACE_NAME_PLACEHOLDERS = new HashMap<>();

  static {
    NAMESPACE_NAME_PLACEHOLDERS.put("<username>", ctx -> ctx.user.getUserName());
    NAMESPACE_NAME_PLACEHOLDERS.put("<userid>", ctx -> ctx.user.getUserId());
    NAMESPACE_NAME_PLACEHOLDERS.put("<workspaceid>", ctx -> ctx.workspaceId);
  }

  private final String defaultNamespaceName;
  private final boolean allowUserDefinedNamespaces;

  private final String namespaceName;
  private final boolean isStatic;
  private final String serviceAccountName;
  private final String clusterRoleName;
  private final KubernetesClientFactory clientFactory;
  private final WorkspaceManager workspaceManager;

  @Inject
  public KubernetesNamespaceFactory(
      @Nullable @Named("che.infra.kubernetes.namespace") String namespaceName,
      @Nullable @Named("che.infra.kubernetes.service_account_name") String serviceAccountName,
      @Nullable @Named("che.infra.kubernetes.cluster_role_name") String clusterRoleName,
      @Nullable @Named("che.infra.kubernetes.namespace.default") String defaultNamespaceName,
      @Named("che.infra.kubernetes.namespace.allow_user_defined")
          boolean allowUserDefinedNamespaces,
      KubernetesClientFactory clientFactory,
      WorkspaceManager workspaceManager)
      throws ConfigurationException {
    this.namespaceName = namespaceName;
    this.isStatic = !isNullOrEmpty(namespaceName) && hasNoPlaceholders(namespaceName);
    this.serviceAccountName = serviceAccountName;
    this.clusterRoleName = clusterRoleName;
    this.clientFactory = clientFactory;
    this.defaultNamespaceName = defaultNamespaceName;
    this.allowUserDefinedNamespaces = allowUserDefinedNamespaces;
    this.workspaceManager = workspaceManager;

    // This will disappear once we support user selection of workspaces...
    if (allowUserDefinedNamespaces) {
      LOG.warn(
          "'che.infra.kubernetes.namespace.allow_user_defined' is not supported yet. It currently has no"
              + " effect.");
    }

    // right now allowUserDefinedNamespaces can't be true, but eventually we will implement it.
    // noinspection ConstantConditions
    if (isNullOrEmpty(defaultNamespaceName) && !allowUserDefinedNamespaces) {
      throw new ConfigurationException(
          "che.infra.kubernetes.namespace.default or "
              + "che.infra.kubernetes.namespace.allow_user_defined must be configured");
    }
  }

  private boolean hasNoPlaceholders(String namespaceName) {
    return NAMESPACE_NAME_PLACEHOLDERS.keySet().stream().noneMatch(namespaceName::contains);
  }

  /**
   * True if namespace is the same (static) for all workspaces. False if each workspace will be
   * provided with a new namespace or provided for each user when using placeholders.
   */
  public boolean isNamespaceStatic() {
    return isStatic;
  }

  /**
   * Creates a Kubernetes namespace for the specified workspace.
   *
   * <p>Namespace won't be prepared. This method should be used only in case workspace recovering.
   *
   * @param workspaceId identifier of the workspace
   * @return created namespace
   */
  public KubernetesNamespace create(String workspaceId, String namespace) {
    return doCreateNamespace(workspaceId, namespace);
  }

  @VisibleForTesting
  KubernetesNamespace doCreateNamespace(String workspaceId, String name) {
    return new KubernetesNamespace(clientFactory, name, workspaceId);
  }

  /** Returns list of k8s namespaces names where a user is able to run workspaces. */
  public List<KubernetesNamespaceMeta> list() throws InfrastructureException {
    if (!allowUserDefinedNamespaces) {
      return singletonList(getDefaultNamespace());
    }

    // if user defined namespaces are allowed - fetch all available
    List<KubernetesNamespaceMeta> namespaces = fetchNamespaces();

    // propagate default namespace if it's configured
    if (!isNullOrEmpty(defaultNamespaceName)) {
      provisionDefaultNamespace(namespaces);
    }
    return namespaces;
  }

  /**
   * Returns default namespace, it's based on existing namespace if there is such or just object
   * holder if there is no such namespace on cluster.
   */
  private KubernetesNamespaceMeta getDefaultNamespace() throws InfrastructureException {
    // the default namespace must be configured if user defined are not allowed
    // so return only it
    String evaluatedName =
        evalPlaceholders(defaultNamespaceName, EnvironmentContext.getCurrent().getSubject(), null);

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
   * Creates a Kubernetes namespace for the specified workspace.
   *
   * <p>The namespace name will be chosen according to a configuration, and it will be prepared
   * (created if necessary).
   *
   * @param workspaceId identifier of the workspace
   * @return created namespace
   * @throws InfrastructureException if any exception occurs during namespace preparing
   */
  public KubernetesNamespace create(String workspaceId) throws InfrastructureException {
    final String namespaceName;
    try {
      namespaceName = evalNamespaceName(workspaceId, EnvironmentContext.getCurrent().getSubject());
    } catch (NotFoundException | ServerException | ConflictException | ValidationException e) {
      throw new InfrastructureException(
          format("Failed to determine the namespace to put the workspace %s to", workspaceId), e);
    }

    KubernetesNamespace namespace = doCreateNamespace(workspaceId, namespaceName);
    namespace.prepare();

    if (!isNamespaceStatic() && !isNullOrEmpty(serviceAccountName)) {
      // prepare service account for workspace only if account name is configured
      // and project is not predefined
      // since predefined project should be prepared during Che deployment
      KubernetesWorkspaceServiceAccount workspaceServiceAccount =
          doCreateServiceAccount(workspaceId, namespaceName);
      workspaceServiceAccount.prepare();
    }

    return namespace;
  }

  protected String evalNamespaceName(String workspaceId, Subject currentUser)
      throws NotFoundException, ServerException, InfrastructureException, ConflictException,
          ValidationException {
    // This, my friend, is a hack of magnificent proportions put forth as a result of dire time
    // constraints imposed on the tears shedding developer writing these unfortunate lines.
    // The effort required to propagate the full workspace, including the attributes (or some
    // alternative thereof) from the callers (all of which happen to already possess the
    // information) down to this sad place is too effing much to do with confidence in the
    // couple of days left until the release. Let's pretend we will have time to fix this later
    // in the better times...
    Workspace wkspc = workspaceManager.getWorkspace(workspaceId);
    String ns = wkspc.getAttributes().get(Constants.WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE);

    if (ns != null) {
      return ns;
    } else {
      String effectiveOldLogicNamespace =
          isNullOrEmpty(namespaceName) ? "<workspaceid>" : namespaceName;
      String namespace = evalPlaceholders(effectiveOldLogicNamespace, currentUser, workspaceId);

      if (!checkNamespaceExists(namespace)) {
        // ok, the namespace pointed to by the legacy config doesn't exist.. that means there can be
        // no damage done by storing the workspace in the namespace designated by the new way of
        // doing things...

        if (isNullOrEmpty(defaultNamespaceName)) {
          throw new InfrastructureException(
              format(
                  "'che.infra.kubernetes.namespace.default' is not"
                      + " defined and no explicit namespace configured for workspace %s",
                  workspaceId));
        }

        namespace = evalPlaceholders(defaultNamespaceName, currentUser, workspaceId);
      }

      // Now, believe it or not, the horror continues - since the callers are as of now unaware
      // of the namespaces being stored within the workspace, we need to do it all here. Hopefully,
      // one day, when the callers (and workspace manager in particular) support workspace namespace
      // selection, things will make more sense again because this logic will have to move up a
      // layer or two and become infrastructure independent.
      wkspc.getAttributes().put(Constants.WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, namespace);
      workspaceManager.updateWorkspace(workspaceId, wkspc);

      return namespace;
    }
  }

  protected boolean checkNamespaceExists(String namespaceName) throws InfrastructureException {
    return clientFactory.create().namespaces().withName(namespaceName).get() != null;
  }

  protected String evalPlaceholders(String namespace, Subject currentUser, String workspaceId) {
    checkArgument(!isNullOrEmpty(namespace));
    String evaluated = namespace;
    PlaceholderResolutionContext ctx = new PlaceholderResolutionContext(currentUser, workspaceId);
    for (Entry<String, Function<PlaceholderResolutionContext, String>> placeHolder :
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

  private static final class PlaceholderResolutionContext {
    final Subject user;
    final String workspaceId;

    private PlaceholderResolutionContext(Subject user, String workspaceId) {
      this.user = user;
      this.workspaceId = workspaceId;
    }
  }
}

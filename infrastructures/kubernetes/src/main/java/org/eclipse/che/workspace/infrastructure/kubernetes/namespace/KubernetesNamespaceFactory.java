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
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.inject.ConfigurationException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.server.impls.KubernetesNamespaceMetaImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta;

/**
 * Helps to create {@link KubernetesNamespace} instances.
 *
 * @author Anton Korneta
 */
@Singleton
public class KubernetesNamespaceFactory {

  private static final Map<String, Function<Subject, String>> NAMESPACE_NAME_PLACEHOLDERS =
      new HashMap<>();

  static {
    NAMESPACE_NAME_PLACEHOLDERS.put("<username>", Subject::getUserName);
    NAMESPACE_NAME_PLACEHOLDERS.put("<userid>", Subject::getUserId);
  }

  private final String defaultNamespaceName;
  private final boolean allowUserDefinedNamespaces;

  private final String namespaceName;
  private final boolean isPredefined;
  private final String serviceAccountName;
  private final String clusterRoleName;
  private final KubernetesClientFactory clientFactory;

  @Inject
  public KubernetesNamespaceFactory(
      @Nullable @Named("che.infra.kubernetes.namespace") String namespaceName,
      @Nullable @Named("che.infra.kubernetes.service_account_name") String serviceAccountName,
      @Nullable @Named("che.infra.kubernetes.cluster_role_name") String clusterRoleName,
      @Nullable @Named("che.infra.kubernetes.namespace.default") String defaultNamespaceName,
      @Named("che.infra.kubernetes.namespace.allow_user_defined")
          boolean allowUserDefinedNamespaces,
      KubernetesClientFactory clientFactory)
      throws ConfigurationException {
    this.namespaceName = namespaceName;
    this.isPredefined = !isNullOrEmpty(namespaceName) && hasNoPlaceholders(this.namespaceName);
    this.serviceAccountName = serviceAccountName;
    this.clusterRoleName = clusterRoleName;
    this.clientFactory = clientFactory;
    this.defaultNamespaceName = defaultNamespaceName;
    this.allowUserDefinedNamespaces = allowUserDefinedNamespaces;
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
   * True if namespace is predefined for all workspaces. False if each workspace will be provided
   * with a new namespace or provided for each user when using placeholders.
   */
  public boolean isPredefined() {
    return isPredefined;
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
        evalDefaultNamespaceName(
            defaultNamespaceName, EnvironmentContext.getCurrent().getSubject());

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
        evalDefaultNamespaceName(
            defaultNamespaceName, EnvironmentContext.getCurrent().getSubject());

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
    final String namespaceName =
        evalNamespaceName(workspaceId, EnvironmentContext.getCurrent().getSubject());
    KubernetesNamespace namespace = doCreateNamespace(workspaceId, namespaceName);
    namespace.prepare();

    if (!isPredefined() && !isNullOrEmpty(serviceAccountName)) {
      // prepare service account for workspace only if account name is configured
      // and project is not predefined
      // since predefined project should be prepared during Che deployment
      KubernetesWorkspaceServiceAccount workspaceServiceAccount =
          doCreateServiceAccount(workspaceId, namespaceName);
      workspaceServiceAccount.prepare();
    }

    return namespace;
  }

  protected String evalNamespaceName(String workspaceId, Subject currentUser) {
    if (isPredefined) {
      return this.namespaceName;
    } else if (isNullOrEmpty(this.namespaceName)) {
      return workspaceId;
    } else {
      String tmpNamespaceName = this.namespaceName;
      for (String placeholder : NAMESPACE_NAME_PLACEHOLDERS.keySet()) {
        tmpNamespaceName =
            tmpNamespaceName.replaceAll(
                placeholder, NAMESPACE_NAME_PLACEHOLDERS.get(placeholder).apply(currentUser));
      }
      return tmpNamespaceName;
    }
  }

  protected String evalDefaultNamespaceName(String defaultNamespace, Subject currentUser) {
    checkArgument(!isNullOrEmpty(defaultNamespace));
    String evaluated = defaultNamespace;
    for (Entry<String, Function<Subject, String>> placeHolder :
        NAMESPACE_NAME_PLACEHOLDERS.entrySet()) {
      evaluated =
          evaluated.replaceAll(placeHolder.getKey(), placeHolder.getValue().apply(currentUser));
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

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

import static java.util.Collections.singletonList;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.eclipse.che.api.user.server.PreferenceManager;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.CheServerKubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSharedPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class implements the logic for creating the roles and role bindings for the workspace
 * service account. Because of the differences between Kubernetes and OpenShift we need to use a lot
 * of generic params.
 *
 * @param <Client> the type of the client to use
 * @param <R> the Role type
 * @param <B> the RoleBinding type
 */
public abstract class AbstractWorkspaceServiceAccount<
    Client extends KubernetesClient, R extends HasMetadata, B extends HasMetadata> {
  private static final Logger LOG = LoggerFactory.getLogger(AbstractWorkspaceServiceAccount.class);
  public static final String EXEC_ROLE_NAME = "exec";
  public static final String VIEW_ROLE_NAME = "workspace-view";
  public static final String METRICS_ROLE_NAME = "workspace-metrics";

  protected final String namespace;
  protected final String serviceAccountName;
  private final ClientFactory<Client> clientFactory;
  private final String workspaceId;
  private final Set<String> clusterRoleNames;
  private final Function<
          Client, MixedOperation<R, ? extends KubernetesResourceList<R>, ? extends Resource<R>>>
      roles;
  private final Function<
          Client, MixedOperation<B, ? extends KubernetesResourceList<B>, ? extends Resource<B>>>
      roleBindings;

  protected AbstractWorkspaceServiceAccount(
      String workspaceId,
      String namespace,
      String serviceAccountName,
      Set<String> clusterRoleNames,
      ClientFactory<Client> clientFactory,
      Function<
              Client, MixedOperation<R, ? extends KubernetesResourceList<R>, ? extends Resource<R>>>
          roles,
      Function<
              Client, MixedOperation<B, ? extends KubernetesResourceList<B>, ? extends Resource<B>>>
          roleBindings) {
    this.workspaceId = workspaceId;
    this.namespace = namespace;
    this.serviceAccountName = serviceAccountName;
    this.clusterRoleNames = clusterRoleNames;
    this.clientFactory = clientFactory;
    this.roles = roles;
    this.roleBindings = roleBindings;
  }

  /**
   * Make sure that workspace service account exists and has `view` and `exec` role bindings, as
   * well as create workspace-view and exec roles in namespace scope
   *
   * <p>Do NOT make any changes to the service account if it already exists in the namespace to
   * preserve its configuration done by someone else.
   *
   * @throws InfrastructureException when any exception occurred
   */
  public void prepare() throws InfrastructureException {
    Client k8sClient = clientFactory.create(workspaceId);
    if (k8sClient.serviceAccounts().inNamespace(namespace).withName(serviceAccountName).get()
        == null) {
      createWorkspaceServiceAccount(k8sClient);
      createImplicitRolesWithBindings(k8sClient);
      createExplicitClusterRoleBindings(k8sClient);
    }
  }

  /**
   * Creates implicit Roles and RoleBindings for workspace ServiceAccount that we need to have fully
   * working workspaces with this SA.
   *
   * <p>creates {@code <sa>-exec} and {@code <sa>-view}
   */
  private void createImplicitRolesWithBindings(Client k8sClient) {
    // exec role
    createRoleWithBinding(
        k8sClient,
        EXEC_ROLE_NAME,
        singletonList("pods/exec"),
        singletonList(""),
        singletonList("create"),
        serviceAccountName + "-exec");

    // view role
    createRoleWithBinding(
        k8sClient,
        VIEW_ROLE_NAME,
        Arrays.asList("pods", "services"),
        singletonList(""),
        singletonList("list"),
        serviceAccountName + "-view");

    // metrics role
    createRoleWithBinding(
        k8sClient,
        METRICS_ROLE_NAME,
        Arrays.asList("pods", "nodes"),
        singletonList("metrics.k8s.io"),
        Arrays.asList("list", "get", "watch"),
        serviceAccountName + "-metrics");
  }

  private void createRoleWithBinding(
      Client k8sClient,
      String roleName,
      List<String> resources,
      List<String> apiGroups,
      List<String> verbs,
      String bindingName) {
    createRole(k8sClient, roleName, resources, apiGroups, verbs);
    //noinspection unchecked
    roleBindings
        .apply(k8sClient)
        .inNamespace(namespace)
        .createOrReplace(createRoleBinding(roleName, bindingName, false));
  }

  /**
   * Creates workspace ServiceAccount ClusterRoleBindings that are defined in
   * 'che.infra.kubernetes.workspace_sa_cluster_roles' property.
   *
   * @see KubernetesNamespaceFactory#KubernetesNamespaceFactory(String, String, String, String,
   *     boolean, boolean, boolean, String, String, KubernetesClientFactory,
   *     CheServerKubernetesClientFactory, UserManager, PreferenceManager, KubernetesSharedPool)
   */
  private void createExplicitClusterRoleBindings(Client k8sClient) {
    // If the user specified an additional cluster roles for the workspace,
    // create a role binding for them too
    int idx = 0;
    for (String clusterRoleName : this.clusterRoleNames) {
      if (k8sClient.rbac().clusterRoles().withName(clusterRoleName).get() != null) {
        //noinspection unchecked
        roleBindings
            .apply(k8sClient)
            .inNamespace(namespace)
            .createOrReplace(
                createRoleBinding(clusterRoleName, serviceAccountName + "-cluster" + idx++, true));
      } else {
        LOG.warn(
            "Unable to find the cluster role {}. Skip creating custom role binding.",
            clusterRoleName);
      }
    }
  }

  /**
   * Builds a new role in the configured namespace but does not persist it.
   *
   * @param name the name of the role
   * @param resources the resources the role grants access to
   * @param verbs the verbs the role allows
   * @return the role object for the given type of Client
   */
  protected abstract R buildRole(
      String name, List<String> resources, List<String> apiGroups, List<String> verbs);

  /**
   * Builds a new role binding but does not persist it.
   *
   * @param roleName the name of the role to bind to
   * @param bindingName the name of the binding
   * @param clusterRole whether the binding is for a cluster role or to a role in the namespace
   * @return
   */
  protected abstract B createRoleBinding(String roleName, String bindingName, boolean clusterRole);

  private void createWorkspaceServiceAccount(Client k8sClient) {
    k8sClient
        .serviceAccounts()
        .inNamespace(namespace)
        .createOrReplace(
            new ServiceAccountBuilder()
                .withAutomountServiceAccountToken(true)
                .withNewMetadata()
                .withName(serviceAccountName)
                .endMetadata()
                .build());
  }

  private void createRole(
      Client k8sClient,
      String name,
      List<String> resources,
      List<String> apiGroups,
      List<String> verbs) {
    if (roles.apply(k8sClient).inNamespace(namespace).withName(name).get() == null) {
      R role = buildRole(name, resources, apiGroups, verbs);
      roles.apply(k8sClient).inNamespace(namespace).create(role);
    }
  }

  public interface ClientFactory<C extends KubernetesClient> {

    C create(String workspaceId) throws InfrastructureException;
  }
}

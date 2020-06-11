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

import io.fabric8.kubernetes.api.model.Doneable;
import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.api.model.rbac.RoleBinding;
import io.fabric8.kubernetes.api.model.rbac.RoleBindingBuilder;
import io.fabric8.kubernetes.api.model.rbac.SubjectBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
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
  private static final Logger LOG =
      LoggerFactory.getLogger(KubernetesWorkspaceServiceAccount.class);
  public static final String EXEC_ROLE_NAME = "exec";
  public static final String VIEW_ROLE_NAME = "workspace-view";

  protected final String namespace;
  protected final String serviceAccountName;
  private final ClientFactory<Client> clientFactory;
  private final String workspaceId;
  private final Set<String> clusterRoleNames;
  private final Function<
          Client,
          MixedOperation<
              R,
              ? extends KubernetesResourceList<R>,
              ? extends Doneable<R>,
              ? extends Resource<R, ? extends Doneable<R>>>>
      roles;
  private final Function<
          Client,
          MixedOperation<
              B,
              ? extends KubernetesResourceList<B>,
              ? extends Doneable<B>,
              ? extends Resource<B, ? extends Doneable<B>>>>
      roleBindings;

  protected AbstractWorkspaceServiceAccount(
      String workspaceId,
      String namespace,
      String serviceAccountName,
      Set<String> clusterRoleNames,
      ClientFactory<Client> clientFactory,
      Function<
              Client,
              MixedOperation<
                  R,
                  ? extends KubernetesResourceList<R>,
                  ? extends Doneable<R>,
                  ? extends Resource<R, ? extends Doneable<R>>>>
          roles,
      Function<
              Client,
              MixedOperation<
                  B,
                  ? extends KubernetesResourceList<B>,
                  ? extends Doneable<B>,
                  ? extends Resource<B, ? extends Doneable<B>>>>
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
    } else {
      return;
    }

    createRole(k8sClient, EXEC_ROLE_NAME);
    createRole(k8sClient, VIEW_ROLE_NAME);

    //noinspection unchecked
    roleBindings
        .apply(k8sClient)
        .inNamespace(namespace)
        .createOrReplace(createRoleBinding(EXEC_ROLE_NAME, serviceAccountName + "-exec", false));

    //noinspection unchecked
    roleBindings
        .apply(k8sClient)
        .inNamespace(namespace)
        .createOrReplace(createRoleBinding(VIEW_ROLE_NAME, serviceAccountName + "-view", false));

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
  protected abstract R buildRole(String name, List<String> resources, List<String> verbs);

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
        .createOrReplaceWithNew()
        .withAutomountServiceAccountToken(true)
        .withNewMetadata()
        .withName(serviceAccountName)
        .endMetadata()
        .done();
  }

  private void createRole(Client k8sClient, String name) {
    if (roles.apply(k8sClient).inNamespace(namespace).withName(name).get() == null) {
      R role = buildRole(name, singletonList("pods/exec"), singletonList("create"));
      roles.apply(k8sClient).inNamespace(namespace).create(role);
    }
  }

  private RoleBinding createCustomRoleBinding(String clusterRoleName, int order) {
    return new RoleBindingBuilder()
        .withNewMetadata()
        .withName(serviceAccountName + "-cluster" + order)
        .withNamespace(namespace)
        .endMetadata()
        .withNewRoleRef()
        .withKind("ClusterRole")
        .withName(clusterRoleName)
        .endRoleRef()
        .withSubjects(
            new SubjectBuilder()
                .withKind("ServiceAccount")
                .withName(serviceAccountName)
                .withNamespace(namespace)
                .build())
        .build();
  }

  public interface ClientFactory<C extends KubernetesClient> {
    C create(String workspaceId) throws InfrastructureException;
  }
}

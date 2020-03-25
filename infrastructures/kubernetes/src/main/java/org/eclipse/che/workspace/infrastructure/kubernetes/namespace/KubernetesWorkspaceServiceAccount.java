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

import static com.google.common.base.Strings.isNullOrEmpty;

import io.fabric8.kubernetes.api.model.rbac.PolicyRuleBuilder;
import io.fabric8.kubernetes.api.model.rbac.Role;
import io.fabric8.kubernetes.api.model.rbac.RoleBinding;
import io.fabric8.kubernetes.api.model.rbac.RoleBindingBuilder;
import io.fabric8.kubernetes.api.model.rbac.RoleBuilder;
import io.fabric8.kubernetes.api.model.rbac.SubjectBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Holds logic for preparing workspace service account.
 *
 * <p>It checks that required service account, roles and role bindings exist and creates if needed.
 *
 * @author Sergii Leshchenko
 */
public class KubernetesWorkspaceServiceAccount {

  private static final Logger LOG =
      LoggerFactory.getLogger(KubernetesWorkspaceServiceAccount.class);

  private final String namespace;
  private final String serviceAccountName;
  private final KubernetesClientFactory clientFactory;
  private final String workspaceId;
  private final String clusterRoleName;

  public KubernetesWorkspaceServiceAccount(
      String workspaceId,
      String namespace,
      String serviceAccountName,
      String clusterRoleName,
      KubernetesClientFactory clientFactory) {
    this.workspaceId = workspaceId;
    this.namespace = namespace;
    this.serviceAccountName = serviceAccountName;
    this.clusterRoleName = clusterRoleName;
    this.clientFactory = clientFactory;
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
  void prepare() throws InfrastructureException {
    KubernetesClient k8sClient = clientFactory.create(workspaceId);

    if (k8sClient.serviceAccounts().inNamespace(namespace).withName(serviceAccountName).get()
        == null) {
      createWorkspaceServiceAccount(k8sClient);
    } else {
      return;
    }

    String execRoleName = "exec";
    if (k8sClient.rbac().clusterRoles().inNamespace(namespace).withName(execRoleName).get()
        == null) {
      createExecRole(k8sClient, execRoleName);
    }

    String viewRoleName = "workspace-view";
    if (k8sClient.rbac().clusterRoles().inNamespace(namespace).withName(viewRoleName).get()
        == null) {
      createViewRole(k8sClient, viewRoleName);
    }

    k8sClient.rbac().roleBindings().inNamespace(namespace).createOrReplace(createExecRoleBinding());
    k8sClient.rbac().roleBindings().inNamespace(namespace).createOrReplace(createViewRoleBinding());

    // If the user specified an additional cluster role for the workspace,
    // create a role binding for it too
    if (!isNullOrEmpty(this.clusterRoleName)) {
      if (k8sClient.rbac().clusterRoles().withName(this.clusterRoleName).get() != null) {
        k8sClient
            .rbac()
            .roleBindings()
            .inNamespace(namespace)
            .createOrReplace(createCustomRoleBinding(this.clusterRoleName));
      } else {
        LOG.warn(
            "Unable to find the cluster role {}. Skip creating custom role binding.",
            this.clusterRoleName);
      }
    }
  }

  private void createWorkspaceServiceAccount(KubernetesClient k8sClient) {
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

  private void createExecRole(KubernetesClient k8sClient, String name) {
    Role execRole =
        new RoleBuilder()
            .withNewMetadata()
            .withName(name)
            .endMetadata()
            .withRules(
                new PolicyRuleBuilder()
                    .withResources("pods/exec")
                    .withApiGroups("")
                    .withVerbs("create")
                    .build())
            .build();
    k8sClient.rbac().roles().inNamespace(namespace).create(execRole);
  }

  private void createViewRole(KubernetesClient k8sClient, String name) {
    Role viewRole =
        new RoleBuilder()
            .withNewMetadata()
            .withName(name)
            .endMetadata()
            .withRules(
                new PolicyRuleBuilder()
                    .withResources("pods", "services")
                    .withApiGroups("")
                    .withVerbs("list")
                    .build())
            .build();
    k8sClient.rbac().roles().inNamespace(namespace).create(viewRole);
  }

  private RoleBinding createViewRoleBinding() {
    return new RoleBindingBuilder()
        .withNewMetadata()
        .withName(serviceAccountName + "-view")
        .withNamespace(namespace)
        .endMetadata()
        .withNewRoleRef()
        .withKind("Role")
        .withName("workspace-view")
        .endRoleRef()
        .withSubjects(
            new SubjectBuilder()
                .withKind("ServiceAccount")
                .withName(serviceAccountName)
                .withNamespace(namespace)
                .build())
        .build();
  }

  private RoleBinding createExecRoleBinding() {
    return new RoleBindingBuilder()
        .withNewMetadata()
        .withName(serviceAccountName + "-exec")
        .withNamespace(namespace)
        .endMetadata()
        .withNewRoleRef()
        .withKind("Role")
        .withName("exec")
        .endRoleRef()
        .withSubjects(
            new SubjectBuilder()
                .withKind("ServiceAccount")
                .withName(serviceAccountName)
                .withNamespace(namespace)
                .build())
        .build();
  }

  private RoleBinding createCustomRoleBinding(String clusterRoleName) {
    return new RoleBindingBuilder()
        .withNewMetadata()
        .withName(serviceAccountName + "-custom")
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
}

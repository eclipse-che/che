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
package org.eclipse.che.workspace.infrastructure.openshift.project;

import io.fabric8.kubernetes.api.model.ObjectReferenceBuilder;
import io.fabric8.openshift.api.model.PolicyRuleBuilder;
import io.fabric8.openshift.api.model.Role;
import io.fabric8.openshift.api.model.RoleBinding;
import io.fabric8.openshift.api.model.RoleBindingBuilder;
import io.fabric8.openshift.api.model.RoleBuilder;
import io.fabric8.openshift.client.OpenShiftClient;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;

/**
 * Holds logic for preparing workspace service account.
 *
 * <p>It checks that required service account, roles and role bindings exist and creates if needed.
 *
 * @author Sergii Leshchenko
 * @see
 *     org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesWorkspaceServiceAccount
 */
class OpenShiftWorkspaceServiceAccount {

  private final String projectName;
  private final String serviceAccountName;
  private final OpenShiftClientFactory clientFactory;
  private final String workspaceId;

  OpenShiftWorkspaceServiceAccount(
      String workspaceId,
      String projectName,
      String serviceAccountName,
      OpenShiftClientFactory clientFactory) {
    this.workspaceId = workspaceId;
    this.projectName = projectName;
    this.serviceAccountName = serviceAccountName;
    this.clientFactory = clientFactory;
  }

  /**
   * Make sure that workspace service account exists and has `view` and `exec` role bindings, as
   * well as create workspace-view and exec roles in namespace scope
   *
   * @throws InfrastructureException when any exception occurred
   */
  void prepare() throws InfrastructureException {
    OpenShiftClient osClient = clientFactory.createOC(workspaceId);

    if (osClient.serviceAccounts().inNamespace(projectName).withName(serviceAccountName).get()
        == null) {
      createWorkspaceServiceAccount(osClient);
    }

    String execRoleName = "exec";
    if (osClient.roles().inNamespace(projectName).withName(execRoleName).get() == null) {
      createExecRole(osClient, execRoleName);
    }

    String viewRoleName = "workspace-view";
    if (osClient.roles().inNamespace(projectName).withName(viewRoleName).get() == null) {
      createViewRole(osClient, viewRoleName);
    }

    osClient.roleBindings().inNamespace(projectName).createOrReplace(createExecRoleBinding());
    osClient.roleBindings().inNamespace(projectName).createOrReplace(createViewRoleBinding());
  }

  private void createWorkspaceServiceAccount(OpenShiftClient osClient) {
    osClient
        .serviceAccounts()
        .inNamespace(projectName)
        .createOrReplaceWithNew()
        .withAutomountServiceAccountToken(true)
        .withNewMetadata()
        .withName(serviceAccountName)
        .endMetadata()
        .done();
  }

  private void createExecRole(OpenShiftClient osClient, String name) {
    Role execRole =
        new RoleBuilder()
            .withNewMetadata()
            .withName(name)
            .endMetadata()
            .withRules(
                new PolicyRuleBuilder().withResources("pods/exec").withVerbs("create").build())
            .build();
    osClient.roles().inNamespace(projectName).create(execRole);
  }

  private void createViewRole(OpenShiftClient osClient, String name) {
    Role viewRole =
        new RoleBuilder()
            .withNewMetadata()
            .withName(name)
            .endMetadata()
            .withRules(
                new PolicyRuleBuilder().withResources("pods", "services").withVerbs("list").build())
            .build();
    osClient.roles().inNamespace(projectName).create(viewRole);
  }

  private RoleBinding createViewRoleBinding() {
    return new RoleBindingBuilder()
        .withNewMetadata()
        .withName(serviceAccountName + "-view")
        .withNamespace(projectName)
        .endMetadata()
        .withNewRoleRef()
        .withName("workspace-view")
        .withNamespace(projectName)
        .endRoleRef()
        .withSubjects(
            new ObjectReferenceBuilder()
                .withKind("ServiceAccount")
                .withName(serviceAccountName)
                .build())
        .build();
  }

  private RoleBinding createExecRoleBinding() {
    return new RoleBindingBuilder()
        .withNewMetadata()
        .withName(serviceAccountName + "-exec")
        .withNamespace(projectName)
        .endMetadata()
        .withNewRoleRef()
        .withName("exec")
        .withNamespace(projectName)
        .endRoleRef()
        .withSubjects(
            new ObjectReferenceBuilder()
                .withKind("ServiceAccount")
                .withName(serviceAccountName)
                .build())
        .build();
  }
}

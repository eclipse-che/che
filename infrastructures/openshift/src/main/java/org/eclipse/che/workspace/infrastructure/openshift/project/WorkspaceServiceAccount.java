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
 */
class WorkspaceServiceAccount {

  private final String projectName;
  private final String name;
  private final OpenShiftClientFactory clientFactory;
  private final String workspaceId;

  WorkspaceServiceAccount(
      String workspaceId,
      String projectName,
      String serviceAccountName,
      OpenShiftClientFactory clientFactory) {
    this.workspaceId = workspaceId;
    this.projectName = projectName;
    this.name = serviceAccountName;
    this.clientFactory = clientFactory;
  }

  /**
   * Make sure that workspace service account exists and has `view` and `exec` role bindings.
   *
   * <p>Note that `view` role is used from cluster scope and `exec` role is created in the current
   * namespace if does not exit.
   *
   * @throws InfrastructureException when any exception occurred
   */
  void prepare() throws InfrastructureException {
    OpenShiftClient osClient = clientFactory.createOC(workspaceId);

    if (osClient.serviceAccounts().inNamespace(projectName).withName(name).get() == null) {
      createWorkspaceServiceAccount(osClient);
    }

    String execRoleName = "exec";
    if (osClient.roles().inNamespace(projectName).withName(execRoleName).get() == null) {
      createExecRole(osClient, execRoleName);
    }

    String execRoleBindingName = name + "-exec";
    if (osClient.roleBindings().inNamespace(projectName).withName(execRoleBindingName).get()
        == null) {
      createExecRoleBinding(osClient, execRoleBindingName);
    }

    String viewRoleBindingName = name + "-view";
    if (osClient.roleBindings().inNamespace(projectName).withName(viewRoleBindingName).get()
        == null) {
      createViewRoleBinding(osClient, viewRoleBindingName);
    }
  }

  private void createWorkspaceServiceAccount(OpenShiftClient osClient) {
    osClient
        .serviceAccounts()
        .inNamespace(projectName)
        .createOrReplaceWithNew()
        .withAutomountServiceAccountToken(true)
        .withNewMetadata()
        .withName(name)
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

  private void createViewRoleBinding(OpenShiftClient osClient, String name) {
    osClient
        .roleBindings()
        .inNamespace(projectName)
        .createNew()
        .withNewMetadata()
        .withName(name)
        .endMetadata()
        .withNewRoleRef()
        .withName("view")
        .endRoleRef()
        .withSubjects(
            new ObjectReferenceBuilder().withKind("ServiceAccount").withName(name).build())
        .done();
  }

  private void createExecRoleBinding(OpenShiftClient osClient, String name) {
    osClient
        .roleBindings()
        .inNamespace(projectName)
        .createNew()
        .withNewMetadata()
        .withName(name)
        .endMetadata()
        .withNewRoleRef()
        .withName("exec")
        .withNamespace(projectName)
        .endRoleRef()
        .withSubjects(
            new ObjectReferenceBuilder().withKind("ServiceAccount").withName(name).build())
        .done();
  }
}

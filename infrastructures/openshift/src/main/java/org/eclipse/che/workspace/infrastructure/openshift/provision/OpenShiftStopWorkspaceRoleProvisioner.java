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
package org.eclipse.che.workspace.infrastructure.openshift.provision;

import io.fabric8.kubernetes.api.model.ObjectReferenceBuilder;
import io.fabric8.openshift.api.model.PolicyRuleBuilder;
import io.fabric8.openshift.api.model.Role;
import io.fabric8.openshift.api.model.RoleBinding;
import io.fabric8.openshift.api.model.RoleBindingBuilder;
import io.fabric8.openshift.api.model.RoleBuilder;
import io.fabric8.openshift.client.OpenShiftClient;
import javax.inject.Inject;
import javax.inject.Named;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.environment.CheInstallationLocation;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class creates the necessary role and rolebindings to allow the che serviceaccount to stop
 * user workspaces.
 *
 * @author Tom George
 */
public class OpenShiftStopWorkspaceRoleProvisioner {

  private final OpenShiftClientFactory clientFactory;
  private final String installationLocation;
  private final boolean stopWorkspaceRoleEnabled;

  private static final Logger LOG =
      LoggerFactory.getLogger(OpenShiftStopWorkspaceRoleProvisioner.class);

  @Inject
  public OpenShiftStopWorkspaceRoleProvisioner(
      OpenShiftClientFactory clientFactory,
      CheInstallationLocation installationLocation,
      @Named("che.workspace.stop.role.enabled") boolean stopWorkspaceRoleEnabled)
      throws InfrastructureException {
    this.clientFactory = clientFactory;
    this.installationLocation = installationLocation.getInstallationLocationNamespace();
    this.stopWorkspaceRoleEnabled = stopWorkspaceRoleEnabled;
  }

  public void provision(String projectName) throws InfrastructureException {
    if (stopWorkspaceRoleEnabled && installationLocation != null) {
      OpenShiftClient osClient = clientFactory.createOC();
      String stopWorkspacesRoleName = "workspace-stop";
      if (osClient.roles().inNamespace(projectName).withName(stopWorkspacesRoleName).get()
          == null) {
        osClient
            .roles()
            .inNamespace(projectName)
            .createOrReplace(createStopWorkspacesRole(stopWorkspacesRoleName));
      }
      osClient
          .roleBindings()
          .inNamespace(projectName)
          .createOrReplace(createStopWorkspacesRoleBinding(projectName));
    } else {
      LOG.warn(
          "Stop workspace Role and RoleBinding will not be provisioned to the '{}' namespace. 'che.workspace.stop.role.enabled' property is set to '{}'",
          installationLocation,
          stopWorkspaceRoleEnabled);
    }
  }

  protected Role createStopWorkspacesRole(String name) {
    return new RoleBuilder()
        .withNewMetadata()
        .withName(name)
        .endMetadata()
        .withRules(
            new PolicyRuleBuilder()
                .withApiGroups("")
                .withResources("pods")
                .withVerbs("get", "list", "watch", "delete")
                .build(),
            new PolicyRuleBuilder()
                .withApiGroups("")
                .withResources("configmaps", "services", "secrets")
                .withVerbs("delete", "list", "get")
                .build(),
            new PolicyRuleBuilder()
                .withApiGroups("route.openshift.io")
                .withResources("routes")
                .withVerbs("delete", "list")
                .build(),
            new PolicyRuleBuilder()
                .withApiGroups("apps")
                .withResources("deployments", "replicasets")
                .withVerbs("delete", "list", "get", "patch")
                .build())
        .build();
  }

  protected RoleBinding createStopWorkspacesRoleBinding(String projectName) {
    return new RoleBindingBuilder()
        .withNewMetadata()
        .withName("che-workspace-stop")
        .withNamespace(projectName)
        .endMetadata()
        .withNewRoleRef()
        .withName("workspace-stop")
        .withNamespace(projectName)
        .endRoleRef()
        .withSubjects(
            new ObjectReferenceBuilder()
                .withKind("ServiceAccount")
                .withName("che")
                .withNamespace(installationLocation)
                .build())
        .build();
  }
}

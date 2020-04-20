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
package org.eclipse.che.workspace.infrastructure.openshift.provision;

import com.google.inject.Inject;
import io.fabric8.kubernetes.api.model.ObjectReferenceBuilder;
import io.fabric8.openshift.api.model.*;
import io.fabric8.openshift.client.OpenShiftClient;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.eclipse.che.workspace.infrastructure.openshift.environment.OpenShiftCheInstallationLocation;
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

  private static final Logger LOG = LoggerFactory.getLogger(OpenShiftCheInstallationLocation.class);

  @Inject
  public OpenShiftStopWorkspaceRoleProvisioner(
      OpenShiftClientFactory clientFactory, OpenShiftCheInstallationLocation installationLocation) {
    this.clientFactory = clientFactory;
    this.installationLocation = installationLocation.getInstallationLocationNamespace();
  }

  public void provision(String projectName) throws InfrastructureException {
    if (installationLocation != null) {
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
          "Could not determine Che installation location. Did not provision stop workspace Role and RoleBinding.");
    }
  }

  protected OpenshiftRole createStopWorkspacesRole(String name) {
    return new OpenshiftRoleBuilder()
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

  protected OpenshiftRoleBinding createStopWorkspacesRoleBinding(String projectName) {
    return new OpenshiftRoleBindingBuilder()
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

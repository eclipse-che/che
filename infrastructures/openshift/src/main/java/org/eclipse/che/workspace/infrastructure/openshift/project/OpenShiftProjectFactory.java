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

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta.PHASE_ATTRIBUTE;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.openshift.api.model.Project;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.inject.Named;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.server.impls.KubernetesNamespaceMetaImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.eclipse.che.workspace.infrastructure.openshift.Constants;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientConfigFactory;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Helps to create {@link OpenShiftProject} instances.
 *
 * @author Anton Korneta
 */
@Singleton
public class OpenShiftProjectFactory extends KubernetesNamespaceFactory {
  private static final Logger LOG = LoggerFactory.getLogger(OpenShiftProjectFactory.class);

  private final OpenShiftClientFactory clientFactory;

  @Inject
  public OpenShiftProjectFactory(
      @Nullable @Named("che.infra.openshift.project") String projectName,
      @Nullable @Named("che.infra.kubernetes.service_account_name") String serviceAccountName,
      @Nullable @Named("che.infra.kubernetes.cluster_role_name") String clusterRoleName,
      @Nullable @Named("che.infra.kubernetes.namespace.default") String defaultNamespaceName,
      @Named("che.infra.kubernetes.namespace.allow_user_defined")
          boolean allowUserDefinedNamespaces,
      OpenShiftClientFactory clientFactory,
      OpenShiftClientConfigFactory clientConfigFactory) {
    super(
        projectName,
        serviceAccountName,
        clusterRoleName,
        defaultNamespaceName,
        allowUserDefinedNamespaces,
        clientFactory);
    if (allowUserDefinedNamespaces && !clientConfigFactory.isPersonalized()) {
      LOG.warn(
          "Users are allowed to list projects but Che server is configured with a service account. "
              + "All users will receive the same list of projects. Consider configuring OpenShift "
              + "OAuth to personalize credentials that will be used for cluster access.");
    }
    this.clientFactory = clientFactory;
  }

  /**
   * Creates a OpenShift project for the specified workspace.
   *
   * <p>The project name will be chosen according to a configuration, and it will be prepared
   * (created if necessary).
   *
   * @param workspaceId identifier of the workspace
   * @return created project
   * @throws InfrastructureException if any exception occurs during project preparing
   */
  public OpenShiftProject create(String workspaceId) throws InfrastructureException {
    final String projectName =
        evalNamespaceName(workspaceId, EnvironmentContext.getCurrent().getSubject());
    OpenShiftProject osProject = doCreateProject(workspaceId, projectName);
    osProject.prepare();

    if (!isPredefined() && !isNullOrEmpty(getServiceAccountName())) {
      // prepare service account for workspace only if account name is configured
      // and project is not predefined
      // since predefined project should be prepared during Che deployment
      OpenShiftWorkspaceServiceAccount osWorkspaceServiceAccount =
          doCreateServiceAccount(workspaceId, projectName);
      osWorkspaceServiceAccount.prepare();
    }

    return osProject;
  }

  /**
   * Creates a kubernetes namespace for the specified workspace.
   *
   * <p>Project won't be prepared. This method should be used only in case workspace recovering.
   *
   * @param workspaceId identifier of the workspace
   * @return created namespace
   */
  public OpenShiftProject create(String workspaceId, String projectName) {
    return doCreateProject(workspaceId, projectName);
  }

  @VisibleForTesting
  OpenShiftProject doCreateProject(String workspaceId, String name) {
    return new OpenShiftProject(clientFactory, name, workspaceId);
  }

  @VisibleForTesting
  OpenShiftWorkspaceServiceAccount doCreateServiceAccount(String workspaceId, String projectName) {
    return new OpenShiftWorkspaceServiceAccount(
        workspaceId, projectName, getServiceAccountName(), getClusterRoleName(), clientFactory);
  }

  @Override
  protected Optional<KubernetesNamespaceMeta> fetchNamespace(String name)
      throws InfrastructureException {
    try {
      Project project = clientFactory.createOC().projects().withName(name).get();
      return Optional.of(asNamespaceMeta(project));
    } catch (KubernetesClientException e) {
      if (e.getCode() == 403) {
        // 403 means that the project does not exist
        // or a user really is not permitted to access it which is Che Server misconfiguration
        return Optional.empty();
      } else {
        throw new InfrastructureException(
            "Error occurred when tried to fetch default project. Cause: " + e.getMessage(), e);
      }
    }
  }

  @Override
  protected List<KubernetesNamespaceMeta> fetchNamespaces() throws InfrastructureException {
    try {
      return clientFactory
          .createOC()
          .projects()
          .list()
          .getItems()
          .stream()
          .map(this::asNamespaceMeta)
          .collect(Collectors.toList());
    } catch (KubernetesClientException e) {
      throw new InfrastructureException(
          "Error occurred when tried to list all available projects. Cause: " + e.getMessage(), e);
    }
  }

  private KubernetesNamespaceMeta asNamespaceMeta(io.fabric8.openshift.api.model.Project project) {
    Map<String, String> attributes = new HashMap<>(4);
    ObjectMeta metadata = project.getMetadata();
    Map<String, String> annotations = metadata.getAnnotations();
    String displayName = annotations.get(Constants.PROJECT_DISPLAY_NAME_ANNOTATION);
    if (displayName != null) {
      attributes.put(Constants.PROJECT_DISPLAY_NAME_ATTRIBUTE, displayName);
    }
    String description = annotations.get(Constants.PROJECT_DESCRIPTION_ANNOTATION);
    if (description != null) {
      attributes.put(Constants.PROJECT_DESCRIPTION_ATTRIBUTE, description);
    }

    if (project.getStatus() != null && project.getStatus().getPhase() != null) {
      attributes.put(PHASE_ATTRIBUTE, project.getStatus().getPhase());
    }
    return new KubernetesNamespaceMetaImpl(metadata.getName(), attributes);
  }
}

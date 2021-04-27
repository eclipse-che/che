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
import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta.DEFAULT_ATTRIBUTE;
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
import org.eclipse.che.api.core.model.workspace.Workspace;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.user.server.PreferenceManager;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.NamespaceResolutionContext;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.workspace.infrastructure.kubernetes.CheServerKubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.server.impls.KubernetesNamespaceMetaImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSharedPool;
import org.eclipse.che.workspace.infrastructure.openshift.Constants;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientConfigFactory;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.eclipse.che.workspace.infrastructure.openshift.provision.OpenShiftStopWorkspaceRoleProvisioner;
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
  private final CheServerKubernetesClientFactory cheClientFactory;
  private final OpenShiftStopWorkspaceRoleProvisioner stopWorkspaceRoleProvisioner;

  private final String oAuthIdentityProvider;

  @Inject
  public OpenShiftProjectFactory(
      @Nullable @Named("che.infra.openshift.project") String projectName,
      @Nullable @Named("che.infra.kubernetes.service_account_name") String serviceAccountName,
      @Nullable @Named("che.infra.kubernetes.workspace_sa_cluster_roles") String clusterRoleNames,
      @Nullable @Named("che.infra.kubernetes.namespace.default") String defaultNamespaceName,
      @Named("che.infra.kubernetes.namespace.allow_user_defined")
          boolean allowUserDefinedNamespaces,
      @Named("che.infra.kubernetes.namespace.creation_allowed") boolean namespaceCreationAllowed,
      @Named("che.infra.kubernetes.namespace.label") boolean labelProjects,
      @Named("che.infra.kubernetes.namespace.labels") String projectLabels,
      @Named("che.infra.kubernetes.namespace.annotations") String projectAnnotations,
      OpenShiftClientFactory clientFactory,
      CheServerKubernetesClientFactory cheClientFactory,
      OpenShiftClientConfigFactory clientConfigFactory,
      OpenShiftStopWorkspaceRoleProvisioner stopWorkspaceRoleProvisioner,
      UserManager userManager,
      PreferenceManager preferenceManager,
      KubernetesSharedPool sharedPool,
      @Nullable @Named("che.infra.openshift.oauth_identity_provider")
          String oAuthIdentityProvider) {
    super(
        projectName,
        serviceAccountName,
        clusterRoleNames,
        defaultNamespaceName,
        allowUserDefinedNamespaces,
        namespaceCreationAllowed,
        labelProjects,
        projectLabels,
        projectAnnotations,
        clientFactory,
        cheClientFactory,
        userManager,
        preferenceManager,
        sharedPool);
    if (allowUserDefinedNamespaces && !clientConfigFactory.isPersonalized()) {
      LOG.warn(
          "Users are allowed to list projects but Che server is configured with a service account. "
              + "All users will receive the same list of projects. Consider configuring OpenShift "
              + "OAuth to personalize credentials that will be used for cluster access.");
    }
    this.clientFactory = clientFactory;
    this.cheClientFactory = cheClientFactory;
    this.stopWorkspaceRoleProvisioner = stopWorkspaceRoleProvisioner;
    this.oAuthIdentityProvider = oAuthIdentityProvider;
  }

  public OpenShiftProject getOrCreate(RuntimeIdentity identity) throws InfrastructureException {
    OpenShiftProject osProject = get(identity);

    osProject.prepare(canCreateNamespace(identity), labelNamespaces ? namespaceLabels : emptyMap());

    if (!isNullOrEmpty(getServiceAccountName())) {
      OpenShiftWorkspaceServiceAccount osWorkspaceServiceAccount =
          doCreateServiceAccount(osProject.getWorkspaceId(), osProject.getName());
      osWorkspaceServiceAccount.prepare();
    }

    if (!isNullOrEmpty(oAuthIdentityProvider)) {
      stopWorkspaceRoleProvisioner.provision(osProject.getName());
    }
    return osProject;
  }

  @Override
  public OpenShiftProject get(Workspace workspace) throws InfrastructureException {
    return doCreateProjectAccess(workspace.getId(), getNamespaceName(workspace));
  }

  public OpenShiftProject get(RuntimeIdentity identity) throws InfrastructureException {
    return doCreateProjectAccess(identity.getWorkspaceId(), identity.getInfrastructureNamespace());
  }

  @Override
  public void deleteIfManaged(Workspace workspace) throws InfrastructureException {
    OpenShiftProject osProject = get(workspace);
    if (isWorkspaceNamespaceManaged(osProject.getName(), workspace)) {
      osProject.delete();
    }
  }

  @Override
  protected boolean checkNamespaceExists(String namespaceName) throws InfrastructureException {
    return fetchNamespaceObject(namespaceName).isPresent();
  }

  /**
   * Creates a kubernetes namespace for the specified workspace.
   *
   * <p>Project won't be prepared. This method should be used only in case workspace recovering.
   *
   * @param workspaceId identifier of the workspace
   * @return created namespace
   */
  public OpenShiftProject access(String workspaceId, String projectName) {
    return doCreateProjectAccess(workspaceId, projectName);
  }

  @VisibleForTesting
  OpenShiftProject doCreateProjectAccess(String workspaceId, String name) {
    return new OpenShiftProject(
        clientFactory, cheClientFactory, sharedPool.getExecutor(), name, workspaceId);
  }

  @VisibleForTesting
  OpenShiftWorkspaceServiceAccount doCreateServiceAccount(String workspaceId, String projectName) {
    return new OpenShiftWorkspaceServiceAccount(
        workspaceId, projectName, getServiceAccountName(), getClusterRoleNames(), clientFactory);
  }

  @Override
  protected Optional<KubernetesNamespaceMeta> fetchNamespace(String name)
      throws InfrastructureException {
    return fetchNamespaceObject(name).map(this::asNamespaceMeta);
  }

  private Optional<Project> fetchNamespaceObject(String name) throws InfrastructureException {
    try {
      Project project = clientFactory.createOC().projects().withName(name).get();
      return Optional.ofNullable(project);
    } catch (KubernetesClientException e) {
      if (e.getCode() == 403) {
        // 403 means that the project does not exist
        // or a user really is not permitted to access it which is Che Server misconfiguration
        return Optional.empty();
      } else {
        throw new InfrastructureException(
            format("Error while trying to fetch the project '%s'. Cause: %s", name, e.getMessage()),
            e);
      }
    }
  }

  protected List<KubernetesNamespaceMeta> findPreparedNamespaces(
      NamespaceResolutionContext namespaceCtx) throws InfrastructureException {
    try {
      List<Project> workspaceProjects =
          clientFactory.createOC().projects().withLabels(namespaceLabels).list().getItems();
      if (!workspaceProjects.isEmpty()) {
        Map<String, String> evaluatedAnnotations = evaluateAnnotationPlaceholders(namespaceCtx);
        return workspaceProjects
            .stream()
            .filter(p -> matchesAnnotations(p, evaluatedAnnotations))
            .map(this::asNamespaceMeta)
            .collect(Collectors.toList());
      } else {
        return emptyList();
      }
    } catch (KubernetesClientException kce) {
      if (kce.getCode() == 403) {
        LOG.warn(
            "Trying to fetch projects with labels '{}', but failed for lack of permissions. Cause: '{}'",
            namespaceLabels,
            kce.getMessage());
        return emptyList();
      } else {
        throw new InfrastructureException(
            "Error occurred when tried to list all available projects. Cause: " + kce.getMessage(),
            kce);
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
      if (e.getCode() == 403) {
        LOG.warn(
            "Trying to fetch all namespaces, but failed for lack of permissions. Cause: {}",
            e.getMessage());
        return emptyList();
      } else {
        throw new InfrastructureException(
            "Error occurred when tried to list all available projects. Cause: " + e.getMessage(),
            e);
      }
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
    attributes.put(DEFAULT_ATTRIBUTE, "true");
    return new KubernetesNamespaceMetaImpl(metadata.getName(), attributes);
  }
}

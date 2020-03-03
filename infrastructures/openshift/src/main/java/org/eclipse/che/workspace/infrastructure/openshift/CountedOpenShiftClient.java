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
package org.eclipse.che.workspace.infrastructure.openshift;

import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.ParameterMixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.openshift.api.model.Build;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.BuildConfigList;
import io.fabric8.openshift.api.model.BuildList;
import io.fabric8.openshift.api.model.ClusterRoleBinding;
import io.fabric8.openshift.api.model.ClusterRoleBindingList;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigList;
import io.fabric8.openshift.api.model.DoneableBuild;
import io.fabric8.openshift.api.model.DoneableBuildConfig;
import io.fabric8.openshift.api.model.DoneableClusterRoleBinding;
import io.fabric8.openshift.api.model.DoneableDeploymentConfig;
import io.fabric8.openshift.api.model.DoneableGroup;
import io.fabric8.openshift.api.model.DoneableImageStream;
import io.fabric8.openshift.api.model.DoneableImageStreamTag;
import io.fabric8.openshift.api.model.DoneableOAuthAccessToken;
import io.fabric8.openshift.api.model.DoneableOAuthAuthorizeToken;
import io.fabric8.openshift.api.model.DoneableOAuthClient;
import io.fabric8.openshift.api.model.DoneablePolicy;
import io.fabric8.openshift.api.model.DoneablePolicyBinding;
import io.fabric8.openshift.api.model.DoneableProject;
import io.fabric8.openshift.api.model.DoneableRole;
import io.fabric8.openshift.api.model.DoneableRoleBinding;
import io.fabric8.openshift.api.model.DoneableRoute;
import io.fabric8.openshift.api.model.DoneableSecurityContextConstraints;
import io.fabric8.openshift.api.model.DoneableTemplate;
import io.fabric8.openshift.api.model.DoneableUser;
import io.fabric8.openshift.api.model.Group;
import io.fabric8.openshift.api.model.GroupList;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.ImageStreamList;
import io.fabric8.openshift.api.model.ImageStreamTag;
import io.fabric8.openshift.api.model.ImageStreamTagList;
import io.fabric8.openshift.api.model.OAuthAccessToken;
import io.fabric8.openshift.api.model.OAuthAccessTokenList;
import io.fabric8.openshift.api.model.OAuthAuthorizeToken;
import io.fabric8.openshift.api.model.OAuthAuthorizeTokenList;
import io.fabric8.openshift.api.model.OAuthClient;
import io.fabric8.openshift.api.model.OAuthClientList;
import io.fabric8.openshift.api.model.Policy;
import io.fabric8.openshift.api.model.PolicyBinding;
import io.fabric8.openshift.api.model.PolicyBindingList;
import io.fabric8.openshift.api.model.PolicyList;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.api.model.ProjectList;
import io.fabric8.openshift.api.model.Role;
import io.fabric8.openshift.api.model.RoleBinding;
import io.fabric8.openshift.api.model.RoleBindingList;
import io.fabric8.openshift.api.model.RoleList;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteList;
import io.fabric8.openshift.api.model.SecurityContextConstraints;
import io.fabric8.openshift.api.model.SecurityContextConstraintsList;
import io.fabric8.openshift.api.model.Template;
import io.fabric8.openshift.api.model.TemplateList;
import io.fabric8.openshift.api.model.User;
import io.fabric8.openshift.api.model.UserList;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.dsl.BuildConfigResource;
import io.fabric8.openshift.client.dsl.BuildResource;
import io.fabric8.openshift.client.dsl.CreateableLocalSubjectAccessReview;
import io.fabric8.openshift.client.dsl.CreateableSubjectAccessReview;
import io.fabric8.openshift.client.dsl.DeployableScalableResource;
import io.fabric8.openshift.client.dsl.ProjectRequestOperation;
import io.fabric8.openshift.client.dsl.SubjectAccessReviewOperation;
import io.fabric8.openshift.client.dsl.TemplateResource;
import io.micrometer.core.instrument.Counter;
import java.net.URL;
import org.eclipse.che.workspace.infrastructure.kubernetes.CountedKubernetesClient;

/**
 * Wrapper over {@link OpenShiftClient} that counts all client invocations to the {@link Counter}.
 */
public class CountedOpenShiftClient extends CountedKubernetesClient implements OpenShiftClient {

  private final OpenShiftClient client;

  /** @param client to wrap */
  public CountedOpenShiftClient(OpenShiftClient client, Runnable invoked) {
    super(client, invoked);
    this.client = client;
  }

  @Override
  public URL getOpenshiftUrl() {
    invoked.run();
    return client.getOpenshiftUrl();
  }

  @Override
  public MixedOperation<
          Build, BuildList, DoneableBuild, BuildResource<Build, DoneableBuild, String, LogWatch>>
      builds() {
    invoked.run();
    return client.builds();
  }

  @Override
  public MixedOperation<
          BuildConfig,
          BuildConfigList,
          DoneableBuildConfig,
          BuildConfigResource<BuildConfig, DoneableBuildConfig, Void, Build>>
      buildConfigs() {
    invoked.run();
    return client.buildConfigs();
  }

  @Override
  public MixedOperation<
          DeploymentConfig,
          DeploymentConfigList,
          DoneableDeploymentConfig,
          DeployableScalableResource<DeploymentConfig, DoneableDeploymentConfig>>
      deploymentConfigs() {
    invoked.run();
    return client.deploymentConfigs();
  }

  @Override
  public NonNamespaceOperation<Group, GroupList, DoneableGroup, Resource<Group, DoneableGroup>>
      groups() {
    invoked.run();
    return client.groups();
  }

  @Override
  public MixedOperation<
          ImageStream,
          ImageStreamList,
          DoneableImageStream,
          Resource<ImageStream, DoneableImageStream>>
      imageStreams() {
    invoked.run();
    return client.imageStreams();
  }

  @Override
  public MixedOperation<
          ImageStreamTag,
          ImageStreamTagList,
          DoneableImageStreamTag,
          Resource<ImageStreamTag, DoneableImageStreamTag>>
      imageStreamTags() {
    invoked.run();
    return client.imageStreamTags();
  }

  @Override
  public NonNamespaceOperation<
          OAuthAccessToken,
          OAuthAccessTokenList,
          DoneableOAuthAccessToken,
          Resource<OAuthAccessToken, DoneableOAuthAccessToken>>
      oAuthAccessTokens() {
    invoked.run();
    return client.oAuthAccessTokens();
  }

  @Override
  public NonNamespaceOperation<
          OAuthAuthorizeToken,
          OAuthAuthorizeTokenList,
          DoneableOAuthAuthorizeToken,
          Resource<OAuthAuthorizeToken, DoneableOAuthAuthorizeToken>>
      oAuthAuthorizeTokens() {
    invoked.run();
    return client.oAuthAuthorizeTokens();
  }

  @Override
  public NonNamespaceOperation<
          OAuthClient,
          OAuthClientList,
          DoneableOAuthClient,
          Resource<OAuthClient, DoneableOAuthClient>>
      oAuthClients() {
    invoked.run();
    return client.oAuthClients();
  }

  @Override
  public MixedOperation<Policy, PolicyList, DoneablePolicy, Resource<Policy, DoneablePolicy>>
      policies() {
    invoked.run();
    return client.policies();
  }

  @Override
  public MixedOperation<
          PolicyBinding,
          PolicyBindingList,
          DoneablePolicyBinding,
          Resource<PolicyBinding, DoneablePolicyBinding>>
      policyBindings() {
    invoked.run();
    return client.policyBindings();
  }

  @Override
  public NonNamespaceOperation<
          Project, ProjectList, DoneableProject, Resource<Project, DoneableProject>>
      projects() {
    invoked.run();
    return client.projects();
  }

  @Override
  public ProjectRequestOperation projectrequests() {
    invoked.run();
    return client.projectrequests();
  }

  @Override
  public MixedOperation<Role, RoleList, DoneableRole, Resource<Role, DoneableRole>> roles() {
    invoked.run();
    return client.roles();
  }

  @Override
  public MixedOperation<
          RoleBinding,
          RoleBindingList,
          DoneableRoleBinding,
          Resource<RoleBinding, DoneableRoleBinding>>
      roleBindings() {
    invoked.run();
    return client.roleBindings();
  }

  @Override
  public MixedOperation<Route, RouteList, DoneableRoute, Resource<Route, DoneableRoute>> routes() {
    invoked.run();
    return client.routes();
  }

  @Override
  public ParameterMixedOperation<
          Template,
          TemplateList,
          DoneableTemplate,
          TemplateResource<Template, KubernetesList, DoneableTemplate>>
      templates() {
    invoked.run();
    return client.templates();
  }

  @Override
  public NonNamespaceOperation<User, UserList, DoneableUser, Resource<User, DoneableUser>> users() {
    invoked.run();
    return client.users();
  }

  @Override
  public NonNamespaceOperation<
          SecurityContextConstraints,
          SecurityContextConstraintsList,
          DoneableSecurityContextConstraints,
          Resource<SecurityContextConstraints, DoneableSecurityContextConstraints>>
      securityContextConstraints() {
    invoked.run();
    return client.securityContextConstraints();
  }

  @Override
  public SubjectAccessReviewOperation<
          CreateableSubjectAccessReview, CreateableLocalSubjectAccessReview>
      subjectAccessReviews() {
    invoked.run();
    return client.subjectAccessReviews();
  }

  @Override
  public MixedOperation<
          ClusterRoleBinding,
          ClusterRoleBindingList,
          DoneableClusterRoleBinding,
          Resource<ClusterRoleBinding, DoneableClusterRoleBinding>>
      clusterRoleBindings() {
    invoked.run();
    return client.clusterRoleBindings();
  }

  @Override
  public User currentUser() {
    invoked.run();
    return client.currentUser();
  }

  @Override
  public boolean supportsOpenShiftAPIGroup(String s) {
    invoked.run();
    return client.supportsOpenShiftAPIGroup(s);
  }
}

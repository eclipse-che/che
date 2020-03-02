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

  /**
   * @param client to wrap
   * @param invocationCounter every method call will be recorder by this counter
   */
  public CountedOpenShiftClient(OpenShiftClient client, Counter invocationCounter) {
    super(client, invocationCounter);
    this.client = client;
  }

  @Override
  public URL getOpenshiftUrl() {
    invocationCounter.increment();
    return client.getOpenshiftUrl();
  }

  @Override
  public MixedOperation<
          Build, BuildList, DoneableBuild, BuildResource<Build, DoneableBuild, String, LogWatch>>
      builds() {
    invocationCounter.increment();
    return client.builds();
  }

  @Override
  public MixedOperation<
          BuildConfig,
          BuildConfigList,
          DoneableBuildConfig,
          BuildConfigResource<BuildConfig, DoneableBuildConfig, Void, Build>>
      buildConfigs() {
    invocationCounter.increment();
    return client.buildConfigs();
  }

  @Override
  public MixedOperation<
          DeploymentConfig,
          DeploymentConfigList,
          DoneableDeploymentConfig,
          DeployableScalableResource<DeploymentConfig, DoneableDeploymentConfig>>
      deploymentConfigs() {
    invocationCounter.increment();
    return client.deploymentConfigs();
  }

  @Override
  public NonNamespaceOperation<Group, GroupList, DoneableGroup, Resource<Group, DoneableGroup>>
      groups() {
    invocationCounter.increment();
    return client.groups();
  }

  @Override
  public MixedOperation<
          ImageStream,
          ImageStreamList,
          DoneableImageStream,
          Resource<ImageStream, DoneableImageStream>>
      imageStreams() {
    invocationCounter.increment();
    return client.imageStreams();
  }

  @Override
  public MixedOperation<
          ImageStreamTag,
          ImageStreamTagList,
          DoneableImageStreamTag,
          Resource<ImageStreamTag, DoneableImageStreamTag>>
      imageStreamTags() {
    invocationCounter.increment();
    return client.imageStreamTags();
  }

  @Override
  public NonNamespaceOperation<
          OAuthAccessToken,
          OAuthAccessTokenList,
          DoneableOAuthAccessToken,
          Resource<OAuthAccessToken, DoneableOAuthAccessToken>>
      oAuthAccessTokens() {
    invocationCounter.increment();
    return client.oAuthAccessTokens();
  }

  @Override
  public NonNamespaceOperation<
          OAuthAuthorizeToken,
          OAuthAuthorizeTokenList,
          DoneableOAuthAuthorizeToken,
          Resource<OAuthAuthorizeToken, DoneableOAuthAuthorizeToken>>
      oAuthAuthorizeTokens() {
    invocationCounter.increment();
    return client.oAuthAuthorizeTokens();
  }

  @Override
  public NonNamespaceOperation<
          OAuthClient,
          OAuthClientList,
          DoneableOAuthClient,
          Resource<OAuthClient, DoneableOAuthClient>>
      oAuthClients() {
    invocationCounter.increment();
    return client.oAuthClients();
  }

  @Override
  public MixedOperation<Policy, PolicyList, DoneablePolicy, Resource<Policy, DoneablePolicy>>
      policies() {
    invocationCounter.increment();
    return client.policies();
  }

  @Override
  public MixedOperation<
          PolicyBinding,
          PolicyBindingList,
          DoneablePolicyBinding,
          Resource<PolicyBinding, DoneablePolicyBinding>>
      policyBindings() {
    invocationCounter.increment();
    return client.policyBindings();
  }

  @Override
  public NonNamespaceOperation<
          Project, ProjectList, DoneableProject, Resource<Project, DoneableProject>>
      projects() {
    invocationCounter.increment();
    return client.projects();
  }

  @Override
  public ProjectRequestOperation projectrequests() {
    invocationCounter.increment();
    return client.projectrequests();
  }

  @Override
  public MixedOperation<Role, RoleList, DoneableRole, Resource<Role, DoneableRole>> roles() {
    invocationCounter.increment();
    return client.roles();
  }

  @Override
  public MixedOperation<
          RoleBinding,
          RoleBindingList,
          DoneableRoleBinding,
          Resource<RoleBinding, DoneableRoleBinding>>
      roleBindings() {
    invocationCounter.increment();
    return client.roleBindings();
  }

  @Override
  public MixedOperation<Route, RouteList, DoneableRoute, Resource<Route, DoneableRoute>> routes() {
    invocationCounter.increment();
    return client.routes();
  }

  @Override
  public ParameterMixedOperation<
          Template,
          TemplateList,
          DoneableTemplate,
          TemplateResource<Template, KubernetesList, DoneableTemplate>>
      templates() {
    invocationCounter.increment();
    return client.templates();
  }

  @Override
  public NonNamespaceOperation<User, UserList, DoneableUser, Resource<User, DoneableUser>> users() {
    invocationCounter.increment();
    return client.users();
  }

  @Override
  public NonNamespaceOperation<
          SecurityContextConstraints,
          SecurityContextConstraintsList,
          DoneableSecurityContextConstraints,
          Resource<SecurityContextConstraints, DoneableSecurityContextConstraints>>
      securityContextConstraints() {
    invocationCounter.increment();
    return client.securityContextConstraints();
  }

  @Override
  public SubjectAccessReviewOperation<
          CreateableSubjectAccessReview, CreateableLocalSubjectAccessReview>
      subjectAccessReviews() {
    invocationCounter.increment();
    return client.subjectAccessReviews();
  }

  @Override
  public MixedOperation<
          ClusterRoleBinding,
          ClusterRoleBindingList,
          DoneableClusterRoleBinding,
          Resource<ClusterRoleBinding, DoneableClusterRoleBinding>>
      clusterRoleBindings() {
    invocationCounter.increment();
    return client.clusterRoleBindings();
  }

  @Override
  public User currentUser() {
    invocationCounter.increment();
    return client.currentUser();
  }

  @Override
  public boolean supportsOpenShiftAPIGroup(String s) {
    invocationCounter.increment();
    return client.supportsOpenShiftAPIGroup(s);
  }
}

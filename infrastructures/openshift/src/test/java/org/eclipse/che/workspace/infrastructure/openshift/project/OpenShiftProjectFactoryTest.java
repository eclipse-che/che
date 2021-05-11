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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta.DEFAULT_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta.PHASE_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.openshift.Constants.PROJECT_DESCRIPTION_ANNOTATION;
import static org.eclipse.che.workspace.infrastructure.openshift.Constants.PROJECT_DESCRIPTION_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.openshift.Constants.PROJECT_DISPLAY_NAME_ANNOTATION;
import static org.eclipse.che.workspace.infrastructure.openshift.Constants.PROJECT_DISPLAY_NAME_ATTRIBUTE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.Status;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.api.model.ProjectBuilder;
import io.fabric8.openshift.api.model.ProjectList;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.dsl.ProjectOperation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.user.server.PreferenceManager;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.NamespaceResolutionContext;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.inject.ConfigurationException;
import org.eclipse.che.workspace.infrastructure.kubernetes.CheServerKubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSharedPool;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientConfigFactory;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.eclipse.che.workspace.infrastructure.openshift.provision.OpenShiftStopWorkspaceRoleProvisioner;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link OpenShiftProjectFactory}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class OpenShiftProjectFactoryTest {

  private static final String USER_ID = "userid";
  private static final String USER_NAME = "username";
  private static final String NO_OAUTH_IDENTITY_PROVIDER = null;
  private static final String OAUTH_IDENTITY_PROVIDER = "openshift-v4";
  private static final String NAMESPACE_LABEL_NAME = "component";
  private static final String NAMESPACE_LABELS = NAMESPACE_LABEL_NAME + "=workspace";
  private static final String NAMESPACE_ANNOTATION_NAME = "owner";
  private static final String NAMESPACE_ANNOTATIONS = NAMESPACE_ANNOTATION_NAME + "=<username>";

  @Mock private OpenShiftClientConfigFactory configFactory;
  @Mock private OpenShiftClientFactory clientFactory;
  @Mock private CheServerKubernetesClientFactory cheClientFactory;
  @Mock private OpenShiftStopWorkspaceRoleProvisioner stopWorkspaceRoleProvisioner;
  @Mock private WorkspaceManager workspaceManager;
  @Mock private UserManager userManager;
  @Mock private PreferenceManager preferenceManager;
  @Mock private KubernetesSharedPool pool;

  @Mock private ProjectOperation projectOperation;

  @Mock private Resource<Project> projectResource;

  @Mock private OpenShiftClient osClient;

  private OpenShiftProjectFactory projectFactory;

  @Mock private FilterWatchListDeletable<Project, ProjectList> projectListResource;

  @Mock private ProjectList projectList;

  @BeforeMethod
  public void setUp() throws Exception {
    lenient().when(clientFactory.createOC()).thenReturn(osClient);
    lenient().when(osClient.projects()).thenReturn(projectOperation);

    lenient()
        .when(workspaceManager.getWorkspace(any()))
        .thenReturn(WorkspaceImpl.builder().setId("1").setAttributes(emptyMap()).build());

    lenient().when(projectOperation.withName(any())).thenReturn(projectResource);
    lenient().when(projectResource.get()).thenReturn(mock(Project.class));

    lenient().when(projectOperation.withLabels(any())).thenReturn(projectListResource);
    lenient().when(projectListResource.list()).thenReturn(projectList);
    lenient().when(projectList.getItems()).thenReturn(emptyList());

    lenient()
        .when(userManager.getById(USER_ID))
        .thenReturn(new UserImpl(USER_ID, "test@mail.com", USER_NAME));

    EnvironmentContext.setCurrent(new EnvironmentContext());
  }

  @Test
  public void shouldNotThrowExceptionIfDefaultNamespaceIsSpecifiedOnCheckingIfNamespaceIsAllowed()
      throws Exception {
    projectFactory =
        new OpenShiftProjectFactory(
            "legacy",
            "",
            null,
            "defaultNs",
            false,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            configFactory,
            stopWorkspaceRoleProvisioner,
            userManager,
            preferenceManager,
            pool,
            NO_OAUTH_IDENTITY_PROVIDER);

    projectFactory.checkIfNamespaceIsAllowed("defaultNs");
  }

  @Test
  public void
      shouldNotThrowExceptionIfNonDefaultNamespaceIsSpecifiedAndUserDefinedAreAllowedOnCheckingIfNamespaceIsAllowed()
          throws Exception {
    projectFactory =
        new OpenShiftProjectFactory(
            "legacy",
            "",
            null,
            "defaultNs",
            true,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            configFactory,
            stopWorkspaceRoleProvisioner,
            userManager,
            preferenceManager,
            pool,
            NO_OAUTH_IDENTITY_PROVIDER);

    projectFactory.checkIfNamespaceIsAllowed("any-namespace");
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "User defined namespaces are not allowed. Only the default namespace 'defaultNs' is available.")
  public void
      shouldThrowExceptionIfNonDefaultNamespaceIsSpecifiedAndUserDefinedAreNotAllowedOnCheckingIfNamespaceIsAllowed()
          throws Exception {
    projectFactory =
        new OpenShiftProjectFactory(
            "legacy",
            "",
            null,
            "defaultNs",
            false,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            configFactory,
            stopWorkspaceRoleProvisioner,
            userManager,
            preferenceManager,
            pool,
            NO_OAUTH_IDENTITY_PROVIDER);

    projectFactory.checkIfNamespaceIsAllowed("any-namespace");
  }

  @Test(
      expectedExceptions = ConfigurationException.class,
      expectedExceptionsMessageRegExp = "che.infra.kubernetes.namespace.default must be configured")
  public void
      shouldThrowExceptionIfNoDefaultNamespaceIsConfiguredAndUserDefinedNamespacesAreNotAllowed()
          throws Exception {
    projectFactory =
        new OpenShiftProjectFactory(
            "projectName",
            "",
            null,
            null,
            false,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            configFactory,
            stopWorkspaceRoleProvisioner,
            userManager,
            preferenceManager,
            pool,
            NO_OAUTH_IDENTITY_PROVIDER);
  }

  @Test
  public void shouldReturnPreparedNamespacesWhenFound() throws InfrastructureException {
    // given
    List<Project> projects =
        Arrays.asList(
            createProject(
                "ns1", "project1", "desc1", "Active", Map.of(NAMESPACE_ANNOTATION_NAME, "jondoe")),
            createProject(
                "ns3",
                "project3",
                "desc3",
                "Active",
                Map.of(NAMESPACE_ANNOTATION_NAME, "some_other_user")),
            createProject(
                "ns2", "project2", "desc2", "Active", Map.of(NAMESPACE_ANNOTATION_NAME, "jondoe")));
    doReturn(projects).when(projectList).getItems();

    projectFactory =
        new OpenShiftProjectFactory(
            "predefined",
            "",
            "",
            "che-default",
            false,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            configFactory,
            stopWorkspaceRoleProvisioner,
            userManager,
            preferenceManager,
            pool,
            NO_OAUTH_IDENTITY_PROVIDER);
    EnvironmentContext.getCurrent().setSubject(new SubjectImpl("jondoe", "123", null, false));

    // when
    List<KubernetesNamespaceMeta> availableNamespaces = projectFactory.list();

    // then
    assertEquals(availableNamespaces.size(), 2);
    assertEquals(availableNamespaces.get(0).getName(), "ns1");
    assertEquals(availableNamespaces.get(1).getName(), "ns2");
  }

  @Test
  public void shouldNotThrowAnExceptionWhenNotAllowedToListNamespaces() throws Exception {
    // given
    Project p = createProject("ns1", "project1", "desc1", "Active");
    doThrow(new KubernetesClientException("Not allowed.", 403, new Status()))
        .when(projectList)
        .getItems();
    prepareNamespaceToBeFoundByName("che-default", p);

    projectFactory =
        new OpenShiftProjectFactory(
            "predefined",
            "",
            "",
            "che-default",
            false,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            configFactory,
            stopWorkspaceRoleProvisioner,
            userManager,
            preferenceManager,
            pool,
            NO_OAUTH_IDENTITY_PROVIDER);
    EnvironmentContext.getCurrent().setSubject(new SubjectImpl("jondoe", "123", null, false));

    // when
    List<KubernetesNamespaceMeta> availableNamespaces = projectFactory.list();

    // then
    assertEquals(availableNamespaces.get(0).getName(), "ns1");
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwAnExceptionWhenErrorListingNamespaces() throws Exception {
    // given
    doThrow(new KubernetesClientException("Not allowed.", 500, new Status()))
        .when(projectList)
        .getItems();

    projectFactory =
        new OpenShiftProjectFactory(
            "predefined",
            "",
            "",
            "che-default",
            false,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            configFactory,
            stopWorkspaceRoleProvisioner,
            userManager,
            preferenceManager,
            pool,
            NO_OAUTH_IDENTITY_PROVIDER);

    // when
    projectFactory.list();

    // then throw
  }

  @Test
  public void shouldReturnDefaultProjectWhenItExistsAndUserDefinedIsNotAllowed() throws Exception {
    prepareNamespaceToBeFoundByName(
        "che-default",
        new ProjectBuilder()
            .withNewMetadata()
            .withName("che-default")
            .withAnnotations(
                ImmutableMap.of(
                    PROJECT_DISPLAY_NAME_ANNOTATION,
                    "Default Che Project",
                    PROJECT_DESCRIPTION_ANNOTATION,
                    "some description"))
            .endMetadata()
            .withNewStatus()
            .withPhase("Active")
            .endStatus()
            .build());

    projectFactory =
        new OpenShiftProjectFactory(
            "predefined",
            "",
            null,
            "che-default",
            false,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            configFactory,
            stopWorkspaceRoleProvisioner,
            userManager,
            preferenceManager,
            pool,
            NO_OAUTH_IDENTITY_PROVIDER);

    List<KubernetesNamespaceMeta> availableNamespaces = projectFactory.list();
    assertEquals(availableNamespaces.size(), 1);
    KubernetesNamespaceMeta defaultNamespace = availableNamespaces.get(0);
    assertEquals(defaultNamespace.getName(), "che-default");
    assertEquals(defaultNamespace.getAttributes().get(DEFAULT_ATTRIBUTE), "true");
    assertEquals(
        defaultNamespace.getAttributes().get(PROJECT_DISPLAY_NAME_ATTRIBUTE),
        "Default Che Project");
    assertEquals(
        defaultNamespace.getAttributes().get(PROJECT_DESCRIPTION_ATTRIBUTE), "some description");
    assertEquals(defaultNamespace.getAttributes().get(PHASE_ATTRIBUTE), "Active");
  }

  @Test
  public void shouldReturnDefaultProjectWhenItDoesNotExistAndUserDefinedIsNotAllowed()
      throws Exception {
    throwOnTryToGetProjectByName(
        "che-default", new KubernetesClientException("forbidden", 403, null));

    projectFactory =
        new OpenShiftProjectFactory(
            "predefined",
            "",
            null,
            "che-default",
            false,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            configFactory,
            stopWorkspaceRoleProvisioner,
            userManager,
            preferenceManager,
            pool,
            NO_OAUTH_IDENTITY_PROVIDER);

    List<KubernetesNamespaceMeta> availableNamespaces = projectFactory.list();
    assertEquals(availableNamespaces.size(), 1);
    KubernetesNamespaceMeta defaultNamespace = availableNamespaces.get(0);
    assertEquals(defaultNamespace.getName(), "che-default");
    assertEquals(defaultNamespace.getAttributes().get(DEFAULT_ATTRIBUTE), "true");
    assertNull(
        defaultNamespace
            .getAttributes()
            .get(PHASE_ATTRIBUTE)); // no phase - means such project does not exist
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Error while trying to fetch the project 'che-default'. Cause: connection refused")
  public void shouldThrowExceptionWhenFailedToGetInfoAboutDefaultNamespace() throws Exception {
    throwOnTryToGetProjectByName(
        "che-default", new KubernetesClientException("connection refused"));

    projectFactory =
        new OpenShiftProjectFactory(
            "predefined",
            "",
            null,
            "che-default",
            false,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            configFactory,
            stopWorkspaceRoleProvisioner,
            userManager,
            preferenceManager,
            pool,
            NO_OAUTH_IDENTITY_PROVIDER);

    projectFactory.list();
  }

  @Test
  public void shouldReturnListOfExistingProjectsAlongWithDefaultIfUserDefinedIsAllowed()
      throws Exception {
    prepareListedProjects(
        Arrays.asList(
            createProject("my-for-ws", "Project for Workspaces", "some description", "Active"),
            createProject("default", "Default Che Project", "some description", "Active")));

    projectFactory =
        new OpenShiftProjectFactory(
            "predefined",
            "",
            null,
            "default",
            true,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            configFactory,
            stopWorkspaceRoleProvisioner,
            userManager,
            preferenceManager,
            pool,
            NO_OAUTH_IDENTITY_PROVIDER);

    List<KubernetesNamespaceMeta> availableNamespaces = projectFactory.list();

    assertEquals(availableNamespaces.size(), 2);
    KubernetesNamespaceMeta forWS = availableNamespaces.get(0);
    assertEquals(forWS.getName(), "my-for-ws");
    assertEquals(
        forWS.getAttributes().get(PROJECT_DISPLAY_NAME_ATTRIBUTE), "Project for Workspaces");
    assertEquals(forWS.getAttributes().get(PROJECT_DESCRIPTION_ATTRIBUTE), "some description");
    assertEquals(forWS.getAttributes().get(PHASE_ATTRIBUTE), "Active");
    assertNull(forWS.getAttributes().get(DEFAULT_ATTRIBUTE));

    KubernetesNamespaceMeta defaultNamespace = availableNamespaces.get(1);
    assertEquals(defaultNamespace.getName(), "default");
    assertEquals(
        defaultNamespace.getAttributes().get(PROJECT_DISPLAY_NAME_ATTRIBUTE),
        "Default Che Project");
    assertEquals(
        defaultNamespace.getAttributes().get(PROJECT_DESCRIPTION_ATTRIBUTE), "some description");
    assertEquals(defaultNamespace.getAttributes().get(PHASE_ATTRIBUTE), "Active");
    assertEquals(defaultNamespace.getAttributes().get(DEFAULT_ATTRIBUTE), "true");
  }

  @Test
  public void shouldReturnListOfExistingProjectsAlongWithNonExistingDefaultIfUserDefinedIsAllowed()
      throws Exception {
    prepareListedProjects(singletonList(createProject("my-for-ws", "", "", "Active")));

    projectFactory =
        new OpenShiftProjectFactory(
            "predefined",
            "",
            null,
            "default",
            true,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            configFactory,
            stopWorkspaceRoleProvisioner,
            userManager,
            preferenceManager,
            pool,
            NO_OAUTH_IDENTITY_PROVIDER);

    List<KubernetesNamespaceMeta> availableNamespaces = projectFactory.list();
    assertEquals(availableNamespaces.size(), 2);
    KubernetesNamespaceMeta forWS = availableNamespaces.get(0);
    assertEquals(forWS.getName(), "my-for-ws");
    assertEquals(forWS.getAttributes().get(PHASE_ATTRIBUTE), "Active");
    assertNull(forWS.getAttributes().get(DEFAULT_ATTRIBUTE));

    KubernetesNamespaceMeta defaultNamespace = availableNamespaces.get(1);
    assertEquals(defaultNamespace.getName(), "default");
    assertEquals(defaultNamespace.getAttributes().get(DEFAULT_ATTRIBUTE), "true");
    assertNull(
        defaultNamespace
            .getAttributes()
            .get(PHASE_ATTRIBUTE)); // no phase - means such namespace does not exist
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Error occurred when tried to list all available projects. Cause: connection refused")
  public void shouldThrowExceptionWhenFailedToGetNamespaces() throws Exception {
    throwOnTryToGetProjectsList(new KubernetesClientException("connection refused"));
    projectFactory =
        new OpenShiftProjectFactory(
            "predefined",
            "",
            null,
            "default-ns",
            true,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            configFactory,
            stopWorkspaceRoleProvisioner,
            userManager,
            preferenceManager,
            pool,
            NO_OAUTH_IDENTITY_PROVIDER);

    projectFactory.list();
  }

  @Test
  public void shouldRequireNamespacePriorExistenceIfDifferentFromDefaultAndUserDefinedIsNotAllowed()
      throws Exception {
    // There is only one scenario where this can happen. The workspace was created and started in
    // some default namespace. Then server was reconfigured to use a different default namespace
    // AND the namespace of the workspace was MANUALLY deleted in the cluster. In this case, we
    // should NOT try to re-create the namespace because it would be created in a namespace that
    // is not configured. We DO allow it to start if the namespace still exists though.

    // given
    projectFactory =
        spy(
            new OpenShiftProjectFactory(
                "predefined",
                "",
                null,
                "new-default",
                false,
                true,
                true,
                NAMESPACE_LABELS,
                NAMESPACE_ANNOTATIONS,
                clientFactory,
                cheClientFactory,
                configFactory,
                stopWorkspaceRoleProvisioner,
                userManager,
                preferenceManager,
                pool,
                NO_OAUTH_IDENTITY_PROVIDER));
    OpenShiftProject toReturnProject = mock(OpenShiftProject.class);
    doReturn(toReturnProject).when(projectFactory).doCreateProjectAccess(any(), any());

    // when
    RuntimeIdentity identity =
        new RuntimeIdentityImpl("workspace123", null, USER_ID, "old-default");
    OpenShiftProject project = projectFactory.getOrCreate(identity);

    // then
    assertEquals(toReturnProject, project);
    verify(projectFactory, never()).doCreateServiceAccount(any(), any());
    verify(toReturnProject).prepare(eq(false), any());
  }

  @Test
  public void shouldPrepareWorkspaceServiceAccountIfItIsConfiguredAndProjectIsNotPredefined()
      throws Exception {
    // given
    projectFactory =
        spy(
            new OpenShiftProjectFactory(
                "",
                "serviceAccount",
                null,
                "<workspaceid>",
                false,
                true,
                true,
                NAMESPACE_LABELS,
                NAMESPACE_ANNOTATIONS,
                clientFactory,
                cheClientFactory,
                configFactory,
                stopWorkspaceRoleProvisioner,
                userManager,
                preferenceManager,
                pool,
                NO_OAUTH_IDENTITY_PROVIDER));
    OpenShiftProject toReturnProject = mock(OpenShiftProject.class);
    when(toReturnProject.getWorkspaceId()).thenReturn("workspace123");
    when(toReturnProject.getName()).thenReturn("workspace123");
    doReturn(toReturnProject).when(projectFactory).doCreateProjectAccess(any(), any());

    OpenShiftWorkspaceServiceAccount serviceAccount = mock(OpenShiftWorkspaceServiceAccount.class);
    doReturn(serviceAccount).when(projectFactory).doCreateServiceAccount(any(), any());

    // when
    RuntimeIdentity identity =
        new RuntimeIdentityImpl("workspace123", null, USER_ID, "workspace123");
    projectFactory.getOrCreate(identity);

    // then
    verify(projectFactory).doCreateServiceAccount("workspace123", "workspace123");
    verify(serviceAccount).prepare();
  }

  @Test
  public void shouldCallStopWorkspaceRoleProvisionWhenIdentityProviderIsDefined() throws Exception {
    projectFactory =
        spy(
            new OpenShiftProjectFactory(
                "",
                "serviceAccount",
                null,
                "<workspaceid>",
                false,
                true,
                true,
                NAMESPACE_LABELS,
                NAMESPACE_ANNOTATIONS,
                clientFactory,
                cheClientFactory,
                configFactory,
                stopWorkspaceRoleProvisioner,
                userManager,
                preferenceManager,
                pool,
                OAUTH_IDENTITY_PROVIDER));
    OpenShiftProject toReturnProject = mock(OpenShiftProject.class);
    when(toReturnProject.getWorkspaceId()).thenReturn("workspace123");
    when(toReturnProject.getName()).thenReturn("workspace123");
    doReturn(toReturnProject).when(projectFactory).doCreateProjectAccess(any(), any());

    OpenShiftWorkspaceServiceAccount serviceAccount = mock(OpenShiftWorkspaceServiceAccount.class);
    doReturn(serviceAccount).when(projectFactory).doCreateServiceAccount(any(), any());

    // when
    RuntimeIdentity identity =
        new RuntimeIdentityImpl("workspace123", null, USER_ID, "workspace123");
    projectFactory.getOrCreate(identity);

    // then
    verify(projectFactory).doCreateServiceAccount("workspace123", "workspace123");
    verify(serviceAccount).prepare();
    verify(stopWorkspaceRoleProvisioner, times(1)).provision("workspace123");
  }

  @Test
  public void shouldNotCallStopWorkspaceRoleProvisionWhenIdentityProviderIsDefined()
      throws Exception {
    projectFactory =
        spy(
            new OpenShiftProjectFactory(
                "",
                "serviceAccount",
                null,
                "<workspaceid>",
                false,
                true,
                true,
                NAMESPACE_LABELS,
                NAMESPACE_ANNOTATIONS,
                clientFactory,
                cheClientFactory,
                configFactory,
                stopWorkspaceRoleProvisioner,
                userManager,
                preferenceManager,
                pool,
                NO_OAUTH_IDENTITY_PROVIDER));
    OpenShiftProject toReturnProject = mock(OpenShiftProject.class);
    when(toReturnProject.getWorkspaceId()).thenReturn("workspace123");
    when(toReturnProject.getName()).thenReturn("workspace123");
    doReturn(toReturnProject).when(projectFactory).doCreateProjectAccess(any(), any());

    OpenShiftWorkspaceServiceAccount serviceAccount = mock(OpenShiftWorkspaceServiceAccount.class);
    doReturn(serviceAccount).when(projectFactory).doCreateServiceAccount(any(), any());

    // when
    RuntimeIdentity identity =
        new RuntimeIdentityImpl("workspace123", null, USER_ID, "workspace123");
    projectFactory.getOrCreate(identity);

    // then
    verify(projectFactory).doCreateServiceAccount("workspace123", "workspace123");
    verify(serviceAccount).prepare();
    verify(stopWorkspaceRoleProvisioner, times(0)).provision("workspace123");
  }

  @Test
  public void testEvalNamespaceNameWhenPreparedNamespacesFound() throws InfrastructureException {
    List<Project> projects =
        Arrays.asList(
            createProject(
                "ns1", "project1", "desc1", "Active", Map.of(NAMESPACE_ANNOTATION_NAME, "jondoe")),
            createProject(
                "ns3",
                "project3",
                "desc3",
                "Active",
                Map.of(NAMESPACE_ANNOTATION_NAME, "some_other_user")),
            createProject(
                "ns2", "project2", "desc2", "Active", Map.of(NAMESPACE_ANNOTATION_NAME, "jondoe")));
    doReturn(projects).when(projectList).getItems();

    projectFactory =
        new OpenShiftProjectFactory(
            "legacy",
            "",
            "",
            "defaultNs",
            false,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            configFactory,
            stopWorkspaceRoleProvisioner,
            userManager,
            preferenceManager,
            pool,
            NO_OAUTH_IDENTITY_PROVIDER);

    String namespace =
        projectFactory.evaluateNamespaceName(
            new NamespaceResolutionContext("workspace123", "user123", "jondoe"));

    assertEquals(namespace, "ns1");
  }

  @Test
  public void testUsernamePlaceholderInLabelsIsNotEvaluated() throws InfrastructureException {
    List<Project> projects =
        singletonList(
            createProject(
                "ns1", "project1", "desc1", "Active", Map.of(NAMESPACE_ANNOTATION_NAME, "jondoe")));
    doReturn(projects).when(projectList).getItems();

    projectFactory =
        new OpenShiftProjectFactory(
            "predefined",
            "",
            null,
            "che-default",
            false,
            true,
            true,
            "try_placeholder_here=<username>",
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            configFactory,
            stopWorkspaceRoleProvisioner,
            userManager,
            preferenceManager,
            pool,
            NO_OAUTH_IDENTITY_PROVIDER);
    EnvironmentContext.getCurrent().setSubject(new SubjectImpl("jondoe", "123", null, false));
    projectFactory.list();

    verify(projectOperation).withLabels(Map.of("try_placeholder_here", "<username>"));
  }

  private void prepareNamespaceToBeFoundByName(String name, Project project) throws Exception {
    @SuppressWarnings("unchecked")
    Resource<Project> getProjectByNameOperation = mock(Resource.class);
    when(projectOperation.withName(name)).thenReturn(getProjectByNameOperation);

    when(getProjectByNameOperation.get()).thenReturn(project);
  }

  private void throwOnTryToGetProjectByName(String name, KubernetesClientException e)
      throws Exception {
    @SuppressWarnings("unchecked")
    Resource<Project> getProjectByNameOperation = mock(Resource.class);
    when(projectOperation.withName(name)).thenReturn(getProjectByNameOperation);

    when(getProjectByNameOperation.get()).thenThrow(e);
  }

  private void prepareListedProjects(List<Project> projects) throws Exception {
    @SuppressWarnings("unchecked")
    ProjectList projectList = mock(ProjectList.class);
    when(projectOperation.list()).thenReturn(projectList);

    when(projectList.getItems()).thenReturn(projects);
  }

  private void throwOnTryToGetProjectsList(Throwable e) throws Exception {
    when(projectOperation.list()).thenThrow(e);
  }

  private Project createProject(String name, String displayName, String description, String phase) {
    return createProject(name, displayName, description, phase, emptyMap());
  }

  private Project createProject(
      String name,
      String displayName,
      String description,
      String phase,
      Map<String, String> extraAnnotations) {
    Map<String, String> annotations = new HashMap<>();
    if (displayName != null) {
      annotations.put(PROJECT_DISPLAY_NAME_ANNOTATION, displayName);
    }
    if (description != null) {
      annotations.put(PROJECT_DESCRIPTION_ANNOTATION, description);
    }
    if (extraAnnotations != null) {
      annotations.putAll(extraAnnotations);
    }

    return new ProjectBuilder()
        .withNewMetadata()
        .withName(name)
        .withAnnotations(annotations)
        .endMetadata()
        .withNewStatus()
        .withNewPhase(phase)
        .endStatus()
        .build();
  }
}

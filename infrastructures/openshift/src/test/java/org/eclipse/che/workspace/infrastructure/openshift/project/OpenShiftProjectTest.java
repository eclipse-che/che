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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.DoneableServiceAccount;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.Status;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.openshift.api.model.DoneableProjectRequest;
import io.fabric8.openshift.api.model.Project;
import io.fabric8.openshift.api.model.ProjectBuilder;
import io.fabric8.openshift.api.model.ProjectRequestFluent.MetadataNested;
import io.fabric8.openshift.client.OpenShiftClient;
import io.fabric8.openshift.client.dsl.ProjectRequestOperation;
import java.util.Map;
import java.util.concurrent.Executor;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.workspace.infrastructure.kubernetes.CheServerKubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesConfigsMaps;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesDeployments;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesIngresses;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesPersistentVolumeClaims;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesSecrets;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesServices;
import org.eclipse.che.workspace.infrastructure.openshift.OpenShiftClientFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link OpenShiftProject}
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class OpenShiftProjectTest {

  public static final String PROJECT_NAME = "testProject";
  public static final String WORKSPACE_ID = "workspace123";

  @Mock private KubernetesDeployments deployments;
  @Mock private KubernetesServices services;
  @Mock private OpenShiftRoutes routes;
  @Mock private KubernetesPersistentVolumeClaims pvcs;
  @Mock private KubernetesIngresses ingresses;
  @Mock private KubernetesSecrets secrets;
  @Mock private KubernetesConfigsMaps configsMaps;
  @Mock private OpenShiftClientFactory clientFactory;
  @Mock private CheServerKubernetesClientFactory cheClientFactory;
  @Mock private Executor executor;
  @Mock private OpenShiftClient openShiftClient;
  @Mock private KubernetesClient kubernetesClient;
  @Mock private Resource<ServiceAccount> serviceAccountResource;

  private OpenShiftProject openShiftProject;

  @BeforeMethod
  public void setUp() throws Exception {
    lenient().when(clientFactory.create(anyString())).thenReturn(kubernetesClient);
    lenient().when(clientFactory.createOC()).thenReturn(openShiftClient);
    lenient().when(clientFactory.createOC(anyString())).thenReturn(openShiftClient);
    lenient().when(openShiftClient.adapt(OpenShiftClient.class)).thenReturn(openShiftClient);

    final MixedOperation mixedOperation = mock(MixedOperation.class);
    final NonNamespaceOperation namespaceOperation = mock(NonNamespaceOperation.class);
    lenient().doReturn(mixedOperation).when(kubernetesClient).serviceAccounts();
    lenient().when(mixedOperation.inNamespace(anyString())).thenReturn(namespaceOperation);
    lenient().when(namespaceOperation.withName(anyString())).thenReturn(serviceAccountResource);
    lenient().when(serviceAccountResource.get()).thenReturn(mock(ServiceAccount.class));

    openShiftProject =
        new OpenShiftProject(
            clientFactory,
            cheClientFactory,
            WORKSPACE_ID,
            PROJECT_NAME,
            deployments,
            services,
            routes,
            pvcs,
            ingresses,
            secrets,
            configsMaps);
  }

  @Test
  public void testOpenShiftProjectPreparingWhenProjectExists() throws Exception {
    // given
    MetadataNested projectMeta = prepareProjectRequest();
    prepareNamespaceGet(PROJECT_NAME);

    prepareProject(PROJECT_NAME);
    OpenShiftProject project =
        new OpenShiftProject(clientFactory, cheClientFactory, executor, PROJECT_NAME, WORKSPACE_ID);

    // when
    project.prepare(true, Map.of());

    // then
    verify(projectMeta, never()).withName(PROJECT_NAME);
  }

  @Test
  public void testOpenShiftProjectPreparingWhenProjectDoesNotExist() throws Exception {
    // given
    MetadataNested projectMetadata = prepareProjectRequest();
    prepareNamespaceGet(PROJECT_NAME);

    Resource resource = prepareProjectResource(PROJECT_NAME);
    doThrow(new KubernetesClientException("error", 403, null)).when(resource).get();
    OpenShiftProject project =
        new OpenShiftProject(clientFactory, cheClientFactory, executor, PROJECT_NAME, WORKSPACE_ID);

    // when
    openShiftProject.prepare(true, Map.of());

    // then
    verify(projectMetadata).withName(PROJECT_NAME);
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwsExceptionIfNamespaceDoesntExistAndNotAllowedToCreateIt() throws Exception {
    // given
    Resource resource = prepareProjectResource(PROJECT_NAME);
    doThrow(new KubernetesClientException("error", 403, null)).when(resource).get();
    OpenShiftProject project =
        new OpenShiftProject(clientFactory, cheClientFactory, executor, PROJECT_NAME, WORKSPACE_ID);

    // when
    project.prepare(false, Map.of());

    // then
    // exception is thrown
  }

  @Test
  public void testOpenShiftProjectCleaningUp() throws Exception {
    // when
    openShiftProject.cleanUp();

    verify(routes).delete();
    verify(services).delete();
    verify(deployments).delete();
    verify(secrets).delete();
    verify(configsMaps).delete();
  }

  @Test
  public void testOpenShiftProjectCleaningUpIfExceptionsOccurs() throws Exception {
    doThrow(new InfrastructureException("err1.")).when(services).delete();
    doThrow(new InfrastructureException("err2.")).when(deployments).delete();

    InfrastructureException error = null;
    // when
    try {
      openShiftProject.cleanUp();

    } catch (InfrastructureException e) {
      error = e;
    }

    // then
    assertNotNull(error);
    String message = error.getMessage();
    assertEquals(message, "Error(s) occurs while cleaning up the namespace. err1. err2.");
    verify(routes).delete();
  }

  @Test
  public void testDeletesExistingProject() throws Exception {
    // given
    OpenShiftProject project =
        new OpenShiftProject(clientFactory, cheClientFactory, executor, PROJECT_NAME, WORKSPACE_ID);
    Resource resource = prepareProjectResource(PROJECT_NAME);

    // when
    project.delete();

    // then
    verify(resource).delete();
  }

  @Test
  public void testDoesntFailIfDeletedProjectDoesntExist() throws Exception {
    // given
    OpenShiftProject project =
        new OpenShiftProject(clientFactory, cheClientFactory, executor, PROJECT_NAME, WORKSPACE_ID);
    Resource resource = prepareProjectResource(PROJECT_NAME);
    when(resource.get()).thenThrow(new KubernetesClientException("err", 404, null));
    when(resource.delete()).thenThrow(new KubernetesClientException("err", 404, null));

    // when
    project.delete();

    // then
    verify(resource).delete();
    // and no exception is thrown
  }

  @Test
  public void testDoesntFailIfDeletedProjectIsBeingDeleted() throws Exception {
    // given
    OpenShiftProject project =
        new OpenShiftProject(clientFactory, cheClientFactory, executor, PROJECT_NAME, WORKSPACE_ID);
    Resource resource = prepareProjectResource(PROJECT_NAME);
    when(resource.delete()).thenThrow(new KubernetesClientException("err", 409, null));

    // when
    project.delete();

    // then
    verify(resource).delete();
    // and no exception is thrown
  }

  @Test
  public void testLabelNamespace() throws InfrastructureException {
    // given
    prepareProject(PROJECT_NAME);
    prepareNamespaceGet(PROJECT_NAME);
    OpenShiftProject openShiftProject =
        new OpenShiftProject(clientFactory, cheClientFactory, executor, PROJECT_NAME, WORKSPACE_ID);

    KubernetesClient cheKubeClient = mock(KubernetesClient.class);
    doReturn(cheKubeClient).when(cheClientFactory).create();

    NonNamespaceOperation nonNamespaceOperation = mock(NonNamespaceOperation.class);
    doReturn(nonNamespaceOperation).when(cheKubeClient).namespaces();

    ArgumentCaptor<Namespace> namespaceArgumentCaptor = ArgumentCaptor.forClass(Namespace.class);
    doAnswer(a -> a.getArgument(0))
        .when(nonNamespaceOperation)
        .createOrReplace(namespaceArgumentCaptor.capture());

    Map<String, String> labels = Map.of("label.with.this", "yes", "and.this", "of courese");

    // when
    openShiftProject.prepare(true, labels);

    // then
    Namespace updatedNamespace = namespaceArgumentCaptor.getValue();
    assertTrue(
        updatedNamespace.getMetadata().getLabels().entrySet().containsAll(labels.entrySet()));
    assertEquals(updatedNamespace.getMetadata().getName(), PROJECT_NAME);
  }

  @Test
  public void testDontTryToLabelNamespaceIfAlreadyLabeled() throws InfrastructureException {
    // given
    Map<String, String> labels = Map.of("label.with.this", "yes", "and.this", "of courese");

    prepareProject(PROJECT_NAME);
    Namespace namespace = prepareNamespaceGet(PROJECT_NAME);
    namespace.getMetadata().setLabels(labels);
    OpenShiftProject openShiftProject =
        new OpenShiftProject(clientFactory, cheClientFactory, executor, PROJECT_NAME, WORKSPACE_ID);

    KubernetesClient cheKubeClient = mock(KubernetesClient.class);
    doReturn(cheKubeClient).when(cheClientFactory).create();

    NonNamespaceOperation nonNamespaceOperation = mock(NonNamespaceOperation.class);
    doReturn(nonNamespaceOperation).when(cheKubeClient).namespaces();

    doAnswer(a -> a.getArgument(0))
        .when(nonNamespaceOperation)
        .createOrReplace(any(Namespace.class));

    // when
    openShiftProject.prepare(true, labels);

    // then
    assertTrue(namespace.getMetadata().getLabels().entrySet().containsAll(labels.entrySet()));
    verify(nonNamespaceOperation, never()).createOrReplace(any());
  }

  @Test
  public void testDoNotFailWhenNoPermissionsToUpdateNamespace() throws InfrastructureException {
    // given
    Map<String, String> labels = Map.of("label.with.this", "yes", "and.this", "of courese");

    prepareProject(PROJECT_NAME);
    prepareNamespaceGet(PROJECT_NAME);
    OpenShiftProject openShiftProject =
        new OpenShiftProject(clientFactory, cheClientFactory, executor, PROJECT_NAME, WORKSPACE_ID);

    KubernetesClient cheKubeClient = mock(KubernetesClient.class);
    lenient().doReturn(cheKubeClient).when(cheClientFactory).create();

    NonNamespaceOperation nonNamespaceOperation = mock(NonNamespaceOperation.class);
    lenient().doReturn(nonNamespaceOperation).when(cheKubeClient).namespaces();

    ArgumentCaptor<Namespace> namespaceArgumentCaptor = ArgumentCaptor.forClass(Namespace.class);
    lenient()
        .doThrow(new KubernetesClientException("No permissions.", 403, new Status()))
        .when(nonNamespaceOperation)
        .createOrReplace(namespaceArgumentCaptor.capture());

    // when
    openShiftProject.prepare(true, labels);

    // then
    Namespace updatedNamespace = namespaceArgumentCaptor.getValue();
    assertTrue(
        updatedNamespace.getMetadata().getLabels().entrySet().containsAll(labels.entrySet()));
    assertEquals(updatedNamespace.getMetadata().getName(), PROJECT_NAME);
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void testFailWhenFailToUpdateNamespace() throws InfrastructureException {
    // given
    Map<String, String> labels = Map.of("label.with.this", "yes", "and.this", "of courese");

    prepareProject(PROJECT_NAME);
    Namespace namespace = prepareNamespaceGet(PROJECT_NAME);
    OpenShiftProject openShiftProject =
        new OpenShiftProject(clientFactory, cheClientFactory, executor, PROJECT_NAME, WORKSPACE_ID);

    KubernetesClient cheKubeClient = mock(KubernetesClient.class);
    lenient().doReturn(cheKubeClient).when(cheClientFactory).create();

    NonNamespaceOperation nonNamespaceOperation = mock(NonNamespaceOperation.class);
    lenient().doReturn(nonNamespaceOperation).when(cheKubeClient).namespaces();

    lenient()
        .doThrow(new KubernetesClientException("Some other error", 500, new Status()))
        .when(nonNamespaceOperation)
        .createOrReplace(any(Namespace.class));

    // when
    openShiftProject.prepare(true, labels);

    // then
    verify(nonNamespaceOperation).createOrReplace(namespace);
  }

  private MetadataNested prepareProjectRequest() {
    ProjectRequestOperation projectRequestOperation = mock(ProjectRequestOperation.class);
    DoneableProjectRequest projectRequest = mock(DoneableProjectRequest.class);
    MetadataNested metadataNested = mock(MetadataNested.class);

    lenient().doReturn(projectRequestOperation).when(openShiftClient).projectrequests();
    lenient().doReturn(projectRequest).when(projectRequestOperation).createNew();
    lenient().doReturn(metadataNested).when(projectRequest).withNewMetadata();
    lenient().doReturn(metadataNested).when(metadataNested).withName(anyString());
    lenient().doReturn(projectRequest).when(metadataNested).endMetadata();
    return metadataNested;
  }

  private Resource prepareProjectResource(String projectName) {
    Resource projectResource = mock(Resource.class);

    NonNamespaceOperation projectOperation = mock(NonNamespaceOperation.class);
    doReturn(projectResource).when(projectOperation).withName(projectName);
    doReturn(projectOperation).when(openShiftClient).projects();

    when(projectResource.get())
        .thenReturn(
            new ProjectBuilder().withNewMetadata().withName(projectName).endMetadata().build());

    openShiftClient.projects().withName(projectName).get();
    return projectResource;
  }

  private Namespace prepareNamespaceGet(String namespaceName) {
    Namespace namespace =
        new NamespaceBuilder().withNewMetadata().withName(namespaceName).endMetadata().build();

    NonNamespaceOperation nsOperation = mock(NonNamespaceOperation.class);
    doReturn(nsOperation).when(openShiftClient).namespaces();

    Resource nsResource = mock(Resource.class);
    doReturn(nsResource).when(nsOperation).withName(namespaceName);

    doReturn(namespace).when(nsResource).get();

    return namespace;
  }

  private Project prepareProject(String projectName) {
    Project project =
        new ProjectBuilder().withNewMetadata().withName(projectName).endMetadata().build();
    Resource projectResource = prepareProjectResource(projectName);
    doReturn(project).when(projectResource).get();
    return project;
  }
}

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
package org.eclipse.che.workspace.infrastructure.kubernetes.namespace;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta.DEFAULT_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta.PHASE_ATTRIBUTE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.DoneableNamespace;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.Arrays;
import java.util.List;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl.WorkspaceImplBuilder;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.inject.ConfigurationException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSharedPool;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link KubernetesNamespaceFactory}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class KubernetesNamespaceFactoryTest {

  private static final String USER_ID = "userid";
  private static final String USER_NAME = "username";

  @Mock private KubernetesSharedPool pool;
  @Mock private KubernetesClientFactory clientFactory;

  @Mock private KubernetesClient k8sClient;
  @Mock private UserManager userManager;

  @Mock
  private NonNamespaceOperation<
          Namespace, NamespaceList, DoneableNamespace, Resource<Namespace, DoneableNamespace>>
      namespaceOperation;

  @Mock private Resource<Namespace, DoneableNamespace> namespaceResource;

  private KubernetesNamespaceFactory namespaceFactory;

  @BeforeMethod
  public void setUp() throws Exception {
    lenient().when(clientFactory.create()).thenReturn(k8sClient);
    lenient().when(k8sClient.namespaces()).thenReturn(namespaceOperation);
    lenient().when(namespaceOperation.withName(any())).thenReturn(namespaceResource);
    lenient().when(namespaceResource.get()).thenReturn(mock(Namespace.class));

    lenient()
        .when(userManager.getById(USER_ID))
        .thenReturn(new UserImpl(USER_ID, "test@mail.com", USER_NAME));
  }

  @AfterMethod
  public void tearDown() {
    EnvironmentContext.reset();
  }

  @Test
  public void shouldNotThrowExceptionIfDefaultNamespaceIsSpecifiedOnCheckingIfNamespaceIsAllowed()
      throws Exception {
    namespaceFactory =
        new KubernetesNamespaceFactory(
            "legacy", "", "", "defaultNs", false, clientFactory, userManager, pool);

    namespaceFactory.checkIfNamespaceIsAllowed("defaultNs");
  }

  @Test
  public void
      shouldNotThrowExceptionIfNonDefaultNamespaceIsSpecifiedAndUserDefinedAreAllowedOnCheckingIfNamespaceIsAllowed()
          throws Exception {
    namespaceFactory =
        new KubernetesNamespaceFactory(
            "legacy", "", "", "defaultNs", true, clientFactory, userManager, pool);

    namespaceFactory.checkIfNamespaceIsAllowed("any-namespace");
  }

  @Test(
      expectedExceptions = ValidationException.class,
      expectedExceptionsMessageRegExp =
          "User defined namespaces are not allowed. Only the default namespace 'defaultNs' is available.")
  public void
      shouldThrowExceptionIfNonDefaultNamespaceIsSpecifiedAndUserDefinedAreNotAllowedOnCheckingIfNamespaceIsAllowed()
          throws Exception {
    namespaceFactory =
        new KubernetesNamespaceFactory(
            "legacy", "", "", "defaultNs", false, clientFactory, userManager, pool);

    namespaceFactory.checkIfNamespaceIsAllowed("any-namespace");
  }

  @Test(
      expectedExceptions = ConfigurationException.class,
      expectedExceptionsMessageRegExp = "che.infra.kubernetes.namespace.default must be configured")
  public void shouldThrowExceptionIfNoDefaultNamespaceIsConfigured() throws Exception {
    namespaceFactory =
        new KubernetesNamespaceFactory(
            "predefined", "", "", null, false, clientFactory, userManager, pool);
  }

  @Test
  public void shouldReturnDefaultNamespaceWhenItExistsAndUserDefinedIsNotAllowed()
      throws Exception {
    prepareNamespaceToBeFoundByName(
        "che-default",
        new NamespaceBuilder()
            .withNewMetadata()
            .withName("che-default")
            .endMetadata()
            .withNewStatus()
            .withNewPhase("Active")
            .endStatus()
            .build());
    namespaceFactory =
        new KubernetesNamespaceFactory(
            "predefined", "", "", "che-default", false, clientFactory, userManager, pool);

    List<KubernetesNamespaceMeta> availableNamespaces = namespaceFactory.list();
    assertEquals(availableNamespaces.size(), 1);
    KubernetesNamespaceMeta defaultNamespace = availableNamespaces.get(0);
    assertEquals(defaultNamespace.getName(), "che-default");
    assertEquals(defaultNamespace.getAttributes().get(DEFAULT_ATTRIBUTE), "true");
    assertEquals(defaultNamespace.getAttributes().get(PHASE_ATTRIBUTE), "Active");
  }

  @Test
  public void shouldReturnDefaultNamespaceWhenItDoesNotExistAndUserDefinedIsNotAllowed()
      throws Exception {
    prepareNamespaceToBeFoundByName("che-default", null);

    namespaceFactory =
        new KubernetesNamespaceFactory(
            "predefined", "", "", "che-default", false, clientFactory, userManager, pool);

    List<KubernetesNamespaceMeta> availableNamespaces = namespaceFactory.list();
    assertEquals(availableNamespaces.size(), 1);
    KubernetesNamespaceMeta defaultNamespace = availableNamespaces.get(0);
    assertEquals(defaultNamespace.getName(), "che-default");
    assertEquals(defaultNamespace.getAttributes().get(DEFAULT_ATTRIBUTE), "true");
    assertNull(
        defaultNamespace
            .getAttributes()
            .get(PHASE_ATTRIBUTE)); // no phase - means such namespace does not exist
  }

  @Test(
      expectedExceptions = InfrastructureException.class,
      expectedExceptionsMessageRegExp =
          "Error occurred when tried to fetch default namespace. Cause: connection refused")
  public void shouldThrowExceptionWhenFailedToGetInfoAboutDefaultNamespace() throws Exception {
    namespaceFactory =
        new KubernetesNamespaceFactory(
            "predefined", "", "", "che", false, clientFactory, userManager, pool);
    throwOnTryToGetNamespaceByName("che", new KubernetesClientException("connection refused"));

    namespaceFactory.list();
  }

  @Test
  public void shouldReturnListOfExistingNamespacesAlongWithDefaultIfUserDefinedIsAllowed()
      throws Exception {
    prepareListedNamespaces(
        Arrays.asList(
            createNamespace("my-for-ws", "Active"), createNamespace("default", "Active")));

    namespaceFactory =
        new KubernetesNamespaceFactory(
            "predefined", "", "", "default", true, clientFactory, userManager, pool);

    List<KubernetesNamespaceMeta> availableNamespaces = namespaceFactory.list();

    assertEquals(availableNamespaces.size(), 2);
    KubernetesNamespaceMeta forWS = availableNamespaces.get(0);
    assertEquals(forWS.getName(), "my-for-ws");
    assertEquals(forWS.getAttributes().get(PHASE_ATTRIBUTE), "Active");
    assertNull(forWS.getAttributes().get(DEFAULT_ATTRIBUTE));

    KubernetesNamespaceMeta defaultNamespace = availableNamespaces.get(1);
    assertEquals(defaultNamespace.getName(), "default");
    assertEquals(defaultNamespace.getAttributes().get(PHASE_ATTRIBUTE), "Active");
    assertEquals(defaultNamespace.getAttributes().get(DEFAULT_ATTRIBUTE), "true");
  }

  @Test
  public void
      shouldReturnListOfExistingNamespacesAlongWithNonExistingDefaultIfUserDefinedIsAllowed()
          throws Exception {
    prepareListedNamespaces(singletonList(createNamespace("my-for-ws", "Active")));

    namespaceFactory =
        new KubernetesNamespaceFactory(
            "predefined", "", "", "default", true, clientFactory, userManager, pool);

    List<KubernetesNamespaceMeta> availableNamespaces = namespaceFactory.list();
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
          "Error occurred when tried to list all available namespaces. Cause: connection refused")
  public void shouldThrowExceptionWhenFailedToGetNamespaces() throws Exception {
    namespaceFactory =
        new KubernetesNamespaceFactory(
            "predefined", "", "", "default_ns", true, clientFactory, userManager, pool);
    throwOnTryToGetNamespacesList(new KubernetesClientException("connection refused"));

    namespaceFactory.list();
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
    namespaceFactory =
        spy(
            new KubernetesNamespaceFactory(
                "predefined", "", "", "new-default", false, clientFactory, userManager, pool));
    KubernetesNamespace toReturnNamespace = mock(KubernetesNamespace.class);
    doReturn(toReturnNamespace).when(namespaceFactory).doCreateNamespaceAccess(any(), any());

    // when
    RuntimeIdentity identity =
        new RuntimeIdentityImpl("workspace123", null, USER_ID, "old-default");
    KubernetesNamespace namespace = namespaceFactory.getOrCreate(identity);

    // then
    assertEquals(toReturnNamespace, namespace);
    verify(namespaceFactory, never()).doCreateServiceAccount(any(), any());
    verify(toReturnNamespace).prepare(eq(false));
  }

  @Test
  public void shouldPrepareWorkspaceServiceAccountIfItIsConfiguredAndNamespaceIsNotPredefined()
      throws Exception {
    // given
    namespaceFactory =
        spy(
            new KubernetesNamespaceFactory(
                "",
                "serviceAccount",
                "",
                "<workspaceid>",
                false,
                clientFactory,
                userManager,
                pool));
    KubernetesNamespace toReturnNamespace = mock(KubernetesNamespace.class);
    when(toReturnNamespace.getWorkspaceId()).thenReturn("workspace123");
    when(toReturnNamespace.getName()).thenReturn("workspace123");
    doReturn(toReturnNamespace).when(namespaceFactory).doCreateNamespaceAccess(any(), any());

    KubernetesWorkspaceServiceAccount serviceAccount =
        mock(KubernetesWorkspaceServiceAccount.class);
    doReturn(serviceAccount).when(namespaceFactory).doCreateServiceAccount(any(), any());

    // when
    RuntimeIdentity identity =
        new RuntimeIdentityImpl("workspace123", null, USER_ID, "workspace123");
    namespaceFactory.getOrCreate(identity);

    // then
    verify(namespaceFactory).doCreateServiceAccount("workspace123", "workspace123");
    verify(serviceAccount).prepare();
  }

  @Test
  public void
      testEvalNamespaceUsesNamespaceDefaultIfWorkspaceDoesntRecordNamespaceAndLegacyNamespaceDoesntExist()
          throws Exception {
    namespaceFactory =
        new KubernetesNamespaceFactory(
            "blabol-<userid>-<username>-<userid>-<username>--",
            "",
            "",
            "che-<userid>",
            false,
            clientFactory,
            userManager,
            pool);

    when(namespaceResource.get()).thenReturn(null);

    WorkspaceImpl workspace =
        new WorkspaceImplBuilder().setId("workspace123").setAttributes(emptyMap()).build();
    EnvironmentContext.getCurrent().setSubject(new SubjectImpl("jondoe", "123", null, false));
    String namespace = namespaceFactory.getNamespaceName(workspace);

    assertEquals(namespace, "che-123");
  }

  @Test
  public void
      testEvalNamespaceUsesLegacyNamespaceIfWorkspaceDoesntRecordNamespaceAndLegacyNamespaceExists()
          throws Exception {

    namespaceFactory =
        new KubernetesNamespaceFactory(
            "blabol-<userid>-<username>-<userid>-<username>",
            "",
            "",
            "che-<userid>",
            false,
            clientFactory,
            userManager,
            pool);

    WorkspaceImpl workspace = new WorkspaceImplBuilder().build();

    EnvironmentContext.getCurrent().setSubject(new SubjectImpl("jondoe", "123", null, false));

    String namespace = namespaceFactory.getNamespaceName(workspace);

    assertEquals(namespace, "blabol-123-jondoe-123-jondoe");
  }

  @Test
  public void testEvalNamespaceUsesWorkspaceRecordedNamespaceIfWorkspaceRecordsIt()
      throws Exception {

    namespaceFactory =
        new KubernetesNamespaceFactory(
            "blabol-<userid>-<username>-<userid>-<username>",
            "",
            "",
            "che-<userid>",
            false,
            clientFactory,
            userManager,
            pool);

    WorkspaceImpl workspace =
        new WorkspaceImplBuilder()
            .setAttributes(
                ImmutableMap.of(
                    Constants.WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, "wkspcnmspc"))
            .build();

    String namespace = namespaceFactory.getNamespaceName(workspace);

    assertEquals(namespace, "wkspcnmspc");
  }

  @Test
  public void testEvalNamespaceTreatsWorkspaceRecordedNamespaceLiterally() throws Exception {

    namespaceFactory =
        new KubernetesNamespaceFactory(
            "blabol-<userid>-<username>-<userid>-<username>",
            "",
            "",
            "che-<userid>",
            false,
            clientFactory,
            userManager,
            pool);

    WorkspaceImpl workspace =
        new WorkspaceImplBuilder()
            .setAttributes(
                ImmutableMap.of(Constants.WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, "<userid>"))
            .build();

    String namespace = namespaceFactory.getNamespaceName(workspace);

    // this is an invalid name, but that is not a purpose of this test.
    assertEquals(namespace, "<userid>");
  }

  private void prepareNamespaceToBeFoundByName(String name, Namespace namespace) throws Exception {
    @SuppressWarnings("unchecked")
    Resource<Namespace, DoneableNamespace> getNamespaceByNameOperation = mock(Resource.class);
    when(namespaceOperation.withName(name)).thenReturn(getNamespaceByNameOperation);

    when(getNamespaceByNameOperation.get()).thenReturn(namespace);
  }

  private void throwOnTryToGetNamespaceByName(String namespaceName, Throwable e) throws Exception {
    @SuppressWarnings("unchecked")
    Resource<Namespace, DoneableNamespace> getNamespaceByNameOperation = mock(Resource.class);
    when(namespaceOperation.withName(namespaceName)).thenReturn(getNamespaceByNameOperation);

    when(getNamespaceByNameOperation.get()).thenThrow(e);
  }

  private void prepareListedNamespaces(List<Namespace> namespaces) throws Exception {
    @SuppressWarnings("unchecked")
    NamespaceList namespaceList = mock(NamespaceList.class);
    when(namespaceOperation.list()).thenReturn(namespaceList);

    when(namespaceList.getItems()).thenReturn(namespaces);
  }

  private void throwOnTryToGetNamespacesList(Throwable e) throws Exception {
    when(namespaceOperation.list()).thenThrow(e);
  }

  private Namespace createNamespace(String name, String phase) {
    return new NamespaceBuilder()
        .withNewMetadata()
        .withName(name)
        .endMetadata()
        .withNewStatus()
        .withNewPhase(phase)
        .endStatus()
        .build();
  }
}

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

import static java.lang.String.format;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
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
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.fail;

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
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.inject.ConfigurationException;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests {@link KubernetesNamespaceFactory}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class KubernetesNamespaceFactoryTest {
  @Mock private KubernetesClientFactory clientFactory;

  @Mock private KubernetesClient k8sClient;
  @Mock private WorkspaceManager workspaceManager;

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
    lenient()
        .when(workspaceManager.getWorkspace(any()))
        .thenReturn(WorkspaceImpl.builder().setId("1").setAttributes(emptyMap()).build());

    lenient().when(namespaceOperation.withName(any())).thenReturn(namespaceResource);
    lenient().when(namespaceResource.get()).thenReturn(mock(Namespace.class));
  }

  @Test(
      expectedExceptions = ConfigurationException.class,
      expectedExceptionsMessageRegExp =
          "che.infra.kubernetes.namespace.default or "
              + "che.infra.kubernetes.namespace.allow_user_defined must be configured")
  public void
      shouldThrowExceptionIfNoDefaultNamespaceIsConfiguredAndUserDefinedNamespacesAreNotAllowed()
          throws Exception {
    namespaceFactory =
        new KubernetesNamespaceFactory(
            "predefined", "", "", null, false, clientFactory, workspaceManager);
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
            .withNewStatus("Active")
            .build());
    namespaceFactory =
        new KubernetesNamespaceFactory(
            "predefined", "", "", "che-default", false, clientFactory, workspaceManager);

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
            "predefined", "", "", "che-default", false, clientFactory, workspaceManager);

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
  public void shouldThrownExceptionWhenFailedToGetInfoAboutDefaultNamespace() throws Exception {
    namespaceFactory =
        new KubernetesNamespaceFactory(
            "predefined", "", "", "che", false, clientFactory, workspaceManager);
    throwOnTryToGetNamespaceByName("che", new KubernetesClientException("connection refused"));

    namespaceFactory.list();
  }

  @Test
  public void shouldReturnListOfExistingNamespacesIfUserDefinedIsAllowed() throws Exception {
    prepareListedNamespaces(
        Arrays.asList(
            createNamespace("my-for-ws", "Active"),
            createNamespace("experimental", "Terminating")));

    namespaceFactory =
        new KubernetesNamespaceFactory(
            "predefined", "", "", null, true, clientFactory, workspaceManager);

    List<KubernetesNamespaceMeta> availableNamespaces = namespaceFactory.list();
    assertEquals(availableNamespaces.size(), 2);
    KubernetesNamespaceMeta forWS = availableNamespaces.get(0);
    assertEquals(forWS.getName(), "my-for-ws");
    assertEquals(forWS.getAttributes().get(PHASE_ATTRIBUTE), "Active");
    assertNull(forWS.getAttributes().get(DEFAULT_ATTRIBUTE));

    KubernetesNamespaceMeta experimental = availableNamespaces.get(1);
    assertEquals(experimental.getName(), "experimental");
    assertEquals(experimental.getAttributes().get(PHASE_ATTRIBUTE), "Terminating");
  }

  @Test
  public void shouldReturnListOfExistingNamespacesAlongWithDefaultIfUserDefinedIsAllowed()
      throws Exception {
    prepareListedNamespaces(
        Arrays.asList(
            createNamespace("my-for-ws", "Active"), createNamespace("default", "Active")));

    namespaceFactory =
        new KubernetesNamespaceFactory(
            "predefined", "", "", "default", true, clientFactory, workspaceManager);

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
            "predefined", "", "", "default", true, clientFactory, workspaceManager);

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
  public void shouldThrownExceptionWhenFailedToGetNamespaces() throws Exception {
    namespaceFactory =
        new KubernetesNamespaceFactory(
            "predefined", "", "", null, true, clientFactory, workspaceManager);
    throwOnTryToGetNamespacesList(new KubernetesClientException("connection refused"));

    namespaceFactory.list();
  }

  @DataProvider
  public static Object[][] creatingNamespaceConditions() {
    // Whether or not the factory potentially creates a namespace depends on the 3 conditions:
    // 1) the value of the legacy property 'che.infra.kubernetes.namespace'
    // 2) legacy property pointing to the existing namespace
    // 3) value of the new property 'che.infra.kubernetes.namespace.default'
    // the output is either true, false, or error (represented as a null here)

    // this is the truth table we need to follow
    // ...namespace    | ...namespace exists | ...namespace.default | creating?
    // no-placeholders |       no            |       null           | error
    // no-placeholders |       no            |   no-placeholders    | no
    // no-placeholders |       no            |    placeholders      | yes
    // no-placeholders |      yes            |       null           | no
    // no-placeholders |      yes            |   no-placeholders    | no
    // no-placeholders |      yes            |    placeholders      | no
    //  placeholders   |       no            |        null          | error
    //  placeholders   |       no            |   no-placeholders    | no
    //  placeholders   |       no            |    placeholders      | yes
    //  placeholders   |      yes            |        null          | yes
    //  placeholders   |      yes            |   no-placeholders    | yes
    //  placeholders   |      yes            |    placeholders      | yes

    // additionally, we want to test that if the legacy property is null, it behaves exactly
    // the same as having a placeholder, because it should default to <workspaceid>
    return new Object[][] {
      new Object[] {"some", false, null, null},
      new Object[] {"some", false, "some", false},
      new Object[] {"some", false, "<userid>", true},
      new Object[] {"some", true, null, false},
      new Object[] {"some", true, "some", false},
      new Object[] {"some", true, "<userid>", false},
      new Object[] {"<workspaceid>", false, null, null},
      new Object[] {"<workspaceid>", false, "some", false},
      new Object[] {"<workspaceid>", false, "<userid>", true},
      new Object[] {"<workspaceid>", true, null, true},
      new Object[] {"<workspaceid>", true, "some", true},
      new Object[] {"<workspaceid>", true, "<userid>", true},
      new Object[] {null, false, null, null},
      new Object[] {null, false, "some", false},
      new Object[] {null, false, "<userid>", true},
      new Object[] {null, true, null, true},
      new Object[] {null, true, "some", true},
      new Object[] {null, true, "<userid>", true},
    };
  }

  @Test(dataProvider = "creatingNamespaceConditions")
  public void shouldDetermineWhenNamespaceCanBeCreated(
      String legacyProperty,
      boolean legacyNamespaceExists,
      String namespaceProperty,
      Boolean expectedOutcome) {

    // given
    namespaceFactory =
        new KubernetesNamespaceFactory(
            legacyProperty, "", "", namespaceProperty, true, clientFactory, workspaceManager);

    Namespace existingLegacyNamespace = legacyNamespaceExists ? mock(Namespace.class) : null;
    when(namespaceResource.get()).thenReturn(existingLegacyNamespace);

    // when
    Boolean result;
    try {
      result = namespaceFactory.isCreatingNamespace("123");
    } catch (InfrastructureException e) {
      // this can happen and we test for it below...
      result = null;
    }

    // then
    assertEquals(result, expectedOutcome);
  }

  @Test(dataProvider = "creatingNamespaceConditions")
  public void testNotManagingNamespacesWheneverNotCreatingThem(
      String legacyProperty,
      boolean legacyNamespaceExists,
      String namespaceProperty,
      Boolean expectedCreating)
      throws InfrastructureException {

    // it is possible that we are creating namespaces that we are not fully managing, e.g. <user*>
    // namespaces are created but not fully deleted afterwards. We just clean them.
    // However, whenever a namespace is NOT being created, we should never claim we're managing the
    // namespace.
    // This is what this test asserts.

    // given
    namespaceFactory =
        new KubernetesNamespaceFactory(
            legacyProperty, "", "", namespaceProperty, true, clientFactory, workspaceManager);

    Namespace existingLegacyNamespace = legacyNamespaceExists ? mock(Namespace.class) : null;
    when(namespaceResource.get()).thenReturn(existingLegacyNamespace);

    // when
    boolean creating;
    try {
      creating = namespaceFactory.isCreatingNamespace("123");
    } catch (InfrastructureException e) {
      // if we can't determine whether we're potentially creating a namespace, we shouldn't claim
      // we're managing it
      if (expectedCreating != null) {
        fail("Shouldn't have failed.");
      }
      creating = false;
    }
    boolean managing = namespaceFactory.isManagingNamespace("123");

    // then
    if (!creating) {
      assertFalse(
          managing,
          format(
              "legacyProp=%s, legacyExists=%s, namespaceProp=%s, expectedCreating=%s",
              legacyProperty, legacyNamespaceExists, namespaceProperty, expectedCreating));
    }
  }

  @Test
  public void shouldCreateAndPrepareNamespaceWithPredefinedValueIfItIsNotEmpty() throws Exception {
    // given
    namespaceFactory =
        spy(
            new KubernetesNamespaceFactory(
                "predefined", "", "", "che", false, clientFactory, workspaceManager));
    KubernetesNamespace toReturnNamespace = mock(KubernetesNamespace.class);
    doReturn(toReturnNamespace).when(namespaceFactory).doCreateNamespace(any(), any());

    // when
    KubernetesNamespace namespace = namespaceFactory.create("workspace123");

    // then
    assertEquals(toReturnNamespace, namespace);
    verify(namespaceFactory).doCreateNamespace("workspace123", "predefined");
    verify(toReturnNamespace).prepare();
  }

  @Test
  public void shouldCreateAndPrepareNamespaceWithWorkspaceIdAsNameIfConfiguredNameIsNotPredefined()
      throws Exception {
    // given
    namespaceFactory =
        spy(
            new KubernetesNamespaceFactory(
                "", "", "", "che", false, clientFactory, workspaceManager));
    KubernetesNamespace toReturnNamespace = mock(KubernetesNamespace.class);
    doReturn(toReturnNamespace).when(namespaceFactory).doCreateNamespace(any(), any());

    // when
    KubernetesNamespace namespace = namespaceFactory.create("workspace123");

    // then
    assertEquals(toReturnNamespace, namespace);
    verify(namespaceFactory).doCreateNamespace("workspace123", "workspace123");
    verify(toReturnNamespace).prepare();
  }

  @Test
  public void
      shouldCreateNamespaceAndDoNotPrepareNamespaceOnCreatingNamespaceWithWorkspaceIdAndNameSpecified()
          throws Exception {
    // given
    namespaceFactory =
        spy(
            new KubernetesNamespaceFactory(
                "", "", "", "che", false, clientFactory, workspaceManager));
    KubernetesNamespace toReturnNamespace = mock(KubernetesNamespace.class);
    doReturn(toReturnNamespace).when(namespaceFactory).doCreateNamespace(any(), any());

    // when
    KubernetesNamespace namespace = namespaceFactory.create("workspace123", "name");

    // then
    assertEquals(toReturnNamespace, namespace);
    verify(namespaceFactory).doCreateNamespace("workspace123", "name");
    verify(toReturnNamespace, never()).prepare();
  }

  @Test
  public void shouldPrepareWorkspaceServiceAccountIfItIsConfiguredAndNamespaceIsNotPredefined()
      throws Exception {
    // given
    namespaceFactory =
        spy(
            new KubernetesNamespaceFactory(
                "", "serviceAccount", "", "<workspaceid>", false, clientFactory, workspaceManager));
    KubernetesNamespace toReturnNamespace = mock(KubernetesNamespace.class);
    doReturn(toReturnNamespace).when(namespaceFactory).doCreateNamespace(any(), any());

    KubernetesWorkspaceServiceAccount serviceAccount =
        mock(KubernetesWorkspaceServiceAccount.class);
    doReturn(serviceAccount).when(namespaceFactory).doCreateServiceAccount(any(), any());

    // when
    namespaceFactory.create("workspace123");

    // then
    verify(namespaceFactory).doCreateServiceAccount("workspace123", "workspace123");
    verify(serviceAccount).prepare();
  }

  @Test
  public void shouldNotPrepareWorkspaceServiceAccountIfItIsConfiguredAndProjectIsPredefined()
      throws Exception {
    // given
    namespaceFactory =
        spy(
            new KubernetesNamespaceFactory(
                "namespace",
                "serviceAccount",
                "clusterRole",
                "che",
                false,
                clientFactory,
                workspaceManager));
    KubernetesNamespace toReturnNamespace = mock(KubernetesNamespace.class);
    doReturn(toReturnNamespace).when(namespaceFactory).doCreateNamespace(any(), any());

    KubernetesWorkspaceServiceAccount serviceAccount =
        mock(KubernetesWorkspaceServiceAccount.class);
    doReturn(serviceAccount).when(namespaceFactory).doCreateServiceAccount(any(), any());

    // when
    namespaceFactory.create("workspace123");

    // then
    verify(namespaceFactory, never()).doCreateServiceAccount(any(), any());
  }

  @Test
  public void shouldNotPrepareWorkspaceServiceAccountIfItIsNotConfiguredAndProjectIsNotPredefined()
      throws Exception {
    // given
    namespaceFactory =
        spy(
            new KubernetesNamespaceFactory(
                "", "", "", "che", false, clientFactory, workspaceManager));
    KubernetesNamespace toReturnNamespace = mock(KubernetesNamespace.class);
    doReturn(toReturnNamespace).when(namespaceFactory).doCreateNamespace(any(), any());

    KubernetesWorkspaceServiceAccount serviceAccount =
        mock(KubernetesWorkspaceServiceAccount.class);
    doReturn(serviceAccount).when(namespaceFactory).doCreateServiceAccount(any(), any());

    // when
    namespaceFactory.create("workspace123");

    // then
    verify(namespaceFactory, never()).doCreateServiceAccount(any(), any());
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
            workspaceManager);

    when(namespaceResource.get()).thenReturn(null);

    String namespace =
        namespaceFactory.evalNamespaceName(null, new SubjectImpl("JonDoe", "123", null, false));

    assertEquals(namespace, "che-123");
  }

  @Test
  public void
      testEvalNamespaceUsesLegacyNamespaceIfWorkspaceDoesntRecordNamespaceAndLegacyNamespaceExists()
          throws Exception {

    namespaceFactory =
        new KubernetesNamespaceFactory(
            "blabol-<userid>-<username>-<userid>-<username>--",
            "",
            "",
            "che-<userid>",
            false,
            clientFactory,
            workspaceManager);

    String namespace =
        namespaceFactory.evalNamespaceName(null, new SubjectImpl("JonDoe", "123", null, false));

    assertEquals(namespace, "blabol-123-JonDoe-123-JonDoe--");
  }

  @Test
  public void testEvalNamespaceUsesWorkspaceRecordedNamespaceIfWorkspaceRecordsIt()
      throws Exception {

    namespaceFactory =
        new KubernetesNamespaceFactory(
            "blabol-<userid>-<username>-<userid>-<username>--",
            "",
            "",
            "che-<userid>",
            false,
            clientFactory,
            workspaceManager);

    when(workspaceManager.getWorkspace(eq("42")))
        .thenReturn(
            WorkspaceImpl.builder()
                .setId("42")
                .setAttributes(
                    singletonMap(
                        Constants.WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, "wkspcnmspc"))
                .build());

    String namespace =
        namespaceFactory.evalNamespaceName("42", new SubjectImpl("JonDoe", "123", null, false));

    assertEquals(namespace, "wkspcnmspc");
  }

  @Test
  public void testEvalNamespaceTreatsWorkspaceRecordedNamespaceLiterally() throws Exception {

    namespaceFactory =
        new KubernetesNamespaceFactory(
            "blabol-<userid>-<username>-<userid>-<username>--",
            "",
            "",
            "che-<userid>",
            false,
            clientFactory,
            workspaceManager);

    when(workspaceManager.getWorkspace(eq("42")))
        .thenReturn(
            WorkspaceImpl.builder()
                .setId("42")
                .setAttributes(
                    singletonMap(
                        Constants.WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, "<userid>"))
                .build());

    String namespace =
        namespaceFactory.evalNamespaceName("42", new SubjectImpl("JonDoe", "123", null, false));

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
        .withNewStatus(phase)
        .build();
  }
}

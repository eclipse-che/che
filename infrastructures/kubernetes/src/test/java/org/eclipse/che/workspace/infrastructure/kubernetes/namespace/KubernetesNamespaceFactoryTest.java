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
import static org.eclipse.che.api.workspace.shared.Constants.WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta.DEFAULT_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta.PHASE_ATTRIBUTE;
import static org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory.NAMESPACE_TEMPLATE_ATTRIBUTE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.google.common.collect.ImmutableMap;
import io.fabric8.kubernetes.api.model.DoneableNamespace;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.NamespaceBuilder;
import io.fabric8.kubernetes.api.model.NamespaceList;
import io.fabric8.kubernetes.api.model.ServiceAccountList;
import io.fabric8.kubernetes.api.model.Status;
import io.fabric8.kubernetes.api.model.rbac.Role;
import io.fabric8.kubernetes.api.model.rbac.RoleBindingList;
import io.fabric8.kubernetes.api.model.rbac.RoleList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.server.mock.KubernetesServer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.eclipse.che.api.core.ValidationException;
import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.user.server.PreferenceManager;
import org.eclipse.che.api.user.server.UserManager;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.workspace.server.model.impl.RuntimeIdentityImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl.WorkspaceImplBuilder;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.api.workspace.server.spi.NamespaceResolutionContext;
import org.eclipse.che.api.workspace.shared.Constants;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.inject.ConfigurationException;
import org.eclipse.che.workspace.infrastructure.kubernetes.CheServerKubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta;
import org.eclipse.che.workspace.infrastructure.kubernetes.util.KubernetesSharedPool;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.testng.MockitoTestNGListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.collections.Sets;

/**
 * Tests {@link KubernetesNamespaceFactory}.
 *
 * @author Sergii Leshchenko
 */
@Listeners(MockitoTestNGListener.class)
public class KubernetesNamespaceFactoryTest {

  private static final String USER_ID = "userid";
  private static final String USER_NAME = "username";
  private static final String NAMESPACE_LABEL_NAME = "component";
  private static final String NAMESPACE_LABELS = NAMESPACE_LABEL_NAME + "=workspace";
  private static final String NAMESPACE_ANNOTATION_NAME = "owner";
  private static final String NAMESPACE_ANNOTATIONS = NAMESPACE_ANNOTATION_NAME + "=<username>";

  @Mock private KubernetesSharedPool pool;
  @Mock private KubernetesClientFactory clientFactory;
  @Mock private CheServerKubernetesClientFactory cheClientFactory;
  private KubernetesClient k8sClient;
  @Mock private UserManager userManager;
  @Mock private PreferenceManager preferenceManager;
  @Mock Appender mockedAppender;

  @Mock
  private NonNamespaceOperation<
          Namespace, NamespaceList, DoneableNamespace, Resource<Namespace, DoneableNamespace>>
      namespaceOperation;

  @Mock private Resource<Namespace, DoneableNamespace> namespaceResource;

  private KubernetesServer serverMock;

  private KubernetesNamespaceFactory namespaceFactory;

  @Mock
  private FilterWatchListDeletable<Namespace, NamespaceList, Boolean, Watch, Watcher<Namespace>>
      namespaceListResource;

  @Mock private NamespaceList namespaceList;

  @BeforeMethod
  public void setUp() throws Exception {
    serverMock = new KubernetesServer(true, true);
    serverMock.before();
    k8sClient = spy(serverMock.getClient());
    lenient().when(clientFactory.create()).thenReturn(k8sClient);
    lenient().when(k8sClient.namespaces()).thenReturn(namespaceOperation);

    lenient().when(namespaceOperation.withName(any())).thenReturn(namespaceResource);
    lenient().when(namespaceResource.get()).thenReturn(mock(Namespace.class));

    lenient().doReturn(namespaceListResource).when(namespaceOperation).withLabels(anyMap());
    lenient().when(namespaceListResource.list()).thenReturn(namespaceList);
    lenient().when(namespaceList.getItems()).thenReturn(Collections.emptyList());

    lenient()
        .when(userManager.getById(USER_ID))
        .thenReturn(new UserImpl(USER_ID, "test@mail.com", USER_NAME));
  }

  @AfterMethod
  public void tearDown() {
    EnvironmentContext.reset();
    serverMock.after();
  }

  @Test
  public void shouldNotThrowExceptionIfDefaultNamespaceIsSpecifiedOnCheckingIfNamespaceIsAllowed()
      throws Exception {
    namespaceFactory =
        new KubernetesNamespaceFactory(
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
            userManager,
            preferenceManager,
            pool);

    namespaceFactory.checkIfNamespaceIsAllowed("defaultNs");
  }

  @Test
  public void
      shouldNotThrowExceptionIfNonDefaultNamespaceIsSpecifiedAndUserDefinedAreAllowedOnCheckingIfNamespaceIsAllowed()
          throws Exception {
    namespaceFactory =
        new KubernetesNamespaceFactory(
            "legacy",
            "",
            "",
            "defaultNs",
            true,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            userManager,
            preferenceManager,
            pool);

    namespaceFactory.checkIfNamespaceIsAllowed("any-namespace");
  }

  @Test
  public void shouldLookAtStoredNamespacesOnCheckingIfNamespaceIsAllowed() throws Exception {

    Map<String, String> prefs = new HashMap<>();
    prefs.put(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, "any-namespace");
    prefs.put(NAMESPACE_TEMPLATE_ATTRIBUTE, "defaultNs");

    when(preferenceManager.find(anyString())).thenReturn(prefs);

    namespaceFactory =
        new KubernetesNamespaceFactory(
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
            userManager,
            preferenceManager,
            pool);

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
            userManager,
            preferenceManager,
            pool);

    namespaceFactory.checkIfNamespaceIsAllowed("any-namespace");
  }

  @Test(
      expectedExceptions = ConfigurationException.class,
      expectedExceptionsMessageRegExp = "che.infra.kubernetes.namespace.default must be configured")
  public void shouldThrowExceptionIfNoDefaultNamespaceIsConfigured() {
    namespaceFactory =
        new KubernetesNamespaceFactory(
            "predefined",
            "",
            "",
            null,
            false,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            userManager,
            preferenceManager,
            pool);
  }

  @Test
  public void shouldReturnPreparedNamespacesWhenFound() throws InfrastructureException {
    // given
    List<Namespace> namespaces =
        Arrays.asList(
            new NamespaceBuilder()
                .withNewMetadata()
                .withName("ns1")
                .withAnnotations(Map.of(NAMESPACE_ANNOTATION_NAME, "jondoe"))
                .endMetadata()
                .withNewStatus()
                .withNewPhase("Active")
                .endStatus()
                .build(),
            new NamespaceBuilder()
                .withNewMetadata()
                .withName("ns2")
                .withAnnotations(Map.of(NAMESPACE_ANNOTATION_NAME, "jondoe"))
                .endMetadata()
                .withNewStatus()
                .withNewPhase("Active")
                .endStatus()
                .build(),
            new NamespaceBuilder()
                .withNewMetadata()
                .withName("ns3")
                .withAnnotations(Map.of(NAMESPACE_ANNOTATION_NAME, "some_other_user"))
                .endMetadata()
                .withNewStatus()
                .withNewPhase("Active")
                .endStatus()
                .build());
    doReturn(namespaces).when(namespaceList).getItems();

    namespaceFactory =
        new KubernetesNamespaceFactory(
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
            userManager,
            preferenceManager,
            pool);
    EnvironmentContext.getCurrent().setSubject(new SubjectImpl("jondoe", "123", null, false));

    // when
    List<KubernetesNamespaceMeta> availableNamespaces = namespaceFactory.list();

    // then
    assertEquals(availableNamespaces.size(), 2);
    verify(namespaceOperation).withLabels(Map.of(NAMESPACE_LABEL_NAME, "workspace"));
    assertEquals(availableNamespaces.get(0).getName(), "ns1");
    assertEquals(availableNamespaces.get(1).getName(), "ns2");
  }

  @Test
  public void shouldNotThrowAnExceptionWhenNotAllowedToListNamespaces() throws Exception {
    // given
    Namespace ns =
        new NamespaceBuilder()
            .withNewMetadata()
            .withName("ns1")
            .endMetadata()
            .withNewStatus()
            .withNewPhase("Active")
            .endStatus()
            .build();
    doThrow(new KubernetesClientException("Not allowed.", 403, new Status()))
        .when(namespaceList)
        .getItems();
    prepareNamespaceToBeFoundByName("che-default", ns);

    namespaceFactory =
        new KubernetesNamespaceFactory(
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
            userManager,
            preferenceManager,
            pool);
    EnvironmentContext.getCurrent().setSubject(new SubjectImpl("jondoe", "123", null, false));

    // when
    List<KubernetesNamespaceMeta> availableNamespaces = namespaceFactory.list();

    // then
    assertEquals(availableNamespaces.get(0).getName(), "ns1");
  }

  @Test(expectedExceptions = InfrastructureException.class)
  public void throwAnExceptionWhenErrorListingNamespaces() throws Exception {
    // given
    doThrow(new KubernetesClientException("Not allowed.", 500, new Status()))
        .when(namespaceList)
        .getItems();

    namespaceFactory =
        new KubernetesNamespaceFactory(
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
            userManager,
            preferenceManager,
            pool);

    // when
    namespaceFactory.list();

    // then throw
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
            userManager,
            preferenceManager,
            pool);

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
            userManager,
            preferenceManager,
            pool);

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
            "predefined",
            "",
            "",
            "che",
            false,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            userManager,
            preferenceManager,
            pool);
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
            "predefined",
            "",
            "",
            "default",
            true,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            userManager,
            preferenceManager,
            pool);

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
            "predefined",
            "",
            "",
            "default",
            true,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            userManager,
            preferenceManager,
            pool);

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
            "predefined",
            "",
            "",
            "default_ns",
            true,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            userManager,
            preferenceManager,
            pool);
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
                "predefined",
                "",
                "",
                "new-default",
                false,
                true,
                true,
                NAMESPACE_LABELS,
                NAMESPACE_ANNOTATIONS,
                clientFactory,
                cheClientFactory,
                userManager,
                preferenceManager,
                pool));
    KubernetesNamespace toReturnNamespace = mock(KubernetesNamespace.class);
    doReturn(toReturnNamespace).when(namespaceFactory).doCreateNamespaceAccess(any(), any());

    // when
    RuntimeIdentity identity =
        new RuntimeIdentityImpl("workspace123", null, USER_ID, "old-default");
    KubernetesNamespace namespace = namespaceFactory.getOrCreate(identity);

    // then
    assertEquals(toReturnNamespace, namespace);
    verify(namespaceFactory, never()).doCreateServiceAccount(any(), any());
    verify(toReturnNamespace).prepare(eq(false), any());
  }

  @Test
  public void shouldReturnDefaultNamespaceWhenCreatingIsNotIsNotAllowed() throws Exception {
    // given
    namespaceFactory =
        spy(
            new KubernetesNamespaceFactory(
                "predefined",
                "",
                "",
                "new-default",
                true,
                false,
                true,
                NAMESPACE_LABELS,
                NAMESPACE_ANNOTATIONS,
                clientFactory,
                cheClientFactory,
                userManager,
                preferenceManager,
                pool));
    KubernetesNamespace toReturnNamespace = mock(KubernetesNamespace.class);
    doReturn(toReturnNamespace).when(namespaceFactory).doCreateNamespaceAccess(any(), any());

    // when
    RuntimeIdentity identity =
        new RuntimeIdentityImpl("workspace123", null, USER_ID, "old-default");
    KubernetesNamespace namespace = namespaceFactory.getOrCreate(identity);

    // then
    assertEquals(toReturnNamespace, namespace);
    verify(namespaceFactory, never()).doCreateServiceAccount(any(), any());
    verify(toReturnNamespace).prepare(eq(false), any());
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
                true,
                true,
                NAMESPACE_LABELS,
                NAMESPACE_ANNOTATIONS,
                clientFactory,
                cheClientFactory,
                userManager,
                preferenceManager,
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
  public void shouldBindToAllConfiguredClusterRoles() throws Exception {
    // given
    namespaceFactory =
        spy(
            new KubernetesNamespaceFactory(
                "",
                "serviceAccount",
                "cr2, cr3",
                "<workspaceid>",
                false,
                true,
                true,
                NAMESPACE_LABELS,
                NAMESPACE_ANNOTATIONS,
                clientFactory,
                cheClientFactory,
                userManager,
                preferenceManager,
                pool));
    KubernetesNamespace toReturnNamespace = mock(KubernetesNamespace.class);
    when(toReturnNamespace.getWorkspaceId()).thenReturn("workspace123");
    when(toReturnNamespace.getName()).thenReturn("workspace123");
    doReturn(toReturnNamespace).when(namespaceFactory).doCreateNamespaceAccess(any(), any());
    when(clientFactory.create(any())).thenReturn(k8sClient);

    // pre-create the cluster roles
    Stream.of("cr1", "cr2", "cr3")
        .forEach(
            cr ->
                k8sClient
                    .rbac()
                    .clusterRoles()
                    .createOrReplaceWithNew()
                    .withNewMetadata()
                    .withName(cr)
                    .endMetadata()
                    .done());

    // when
    RuntimeIdentity identity =
        new RuntimeIdentityImpl("workspace123", null, USER_ID, "workspace123");
    namespaceFactory.getOrCreate(identity);

    // then
    verify(namespaceFactory).doCreateServiceAccount("workspace123", "workspace123");

    ServiceAccountList sas = k8sClient.serviceAccounts().inNamespace("workspace123").list();
    assertEquals(sas.getItems().size(), 1);
    assertEquals(sas.getItems().get(0).getMetadata().getName(), "serviceAccount");

    RoleList roles = k8sClient.rbac().roles().inNamespace("workspace123").list();
    assertEquals(
        Sets.newHashSet("workspace-view", "exec"),
        roles.getItems().stream().map(r -> r.getMetadata().getName()).collect(Collectors.toSet()));

    RoleBindingList bindings = k8sClient.rbac().roleBindings().inNamespace("workspace123").list();
    assertEquals(
        bindings
            .getItems()
            .stream()
            .map(r -> r.getMetadata().getName())
            .collect(Collectors.toSet()),
        Sets.newHashSet(
            "serviceAccount-cluster0",
            "serviceAccount-cluster1",
            "serviceAccount-view",
            "serviceAccount-exec"));
  }

  @Test
  public void shouldCreateExecAndViewRolesAndBindings() throws Exception {
    // given
    namespaceFactory =
        spy(
            new KubernetesNamespaceFactory(
                "",
                "serviceAccount",
                "",
                "<workspaceid>",
                false,
                true,
                true,
                NAMESPACE_LABELS,
                NAMESPACE_ANNOTATIONS,
                clientFactory,
                cheClientFactory,
                userManager,
                preferenceManager,
                pool));
    KubernetesNamespace toReturnNamespace = mock(KubernetesNamespace.class);
    when(toReturnNamespace.getWorkspaceId()).thenReturn("workspace123");
    when(toReturnNamespace.getName()).thenReturn("workspace123");
    doReturn(toReturnNamespace).when(namespaceFactory).doCreateNamespaceAccess(any(), any());
    when(clientFactory.create(any())).thenReturn(k8sClient);

    // when
    RuntimeIdentity identity =
        new RuntimeIdentityImpl("workspace123", null, USER_ID, "workspace123");
    namespaceFactory.getOrCreate(identity);

    // then
    verify(namespaceFactory).doCreateServiceAccount("workspace123", "workspace123");

    ServiceAccountList sas = k8sClient.serviceAccounts().inNamespace("workspace123").list();
    assertEquals(sas.getItems().size(), 1);
    assertEquals(sas.getItems().get(0).getMetadata().getName(), "serviceAccount");

    RoleList roles = k8sClient.rbac().roles().inNamespace("workspace123").list();
    assertEquals(
        Sets.newHashSet("workspace-view", "exec"),
        roles.getItems().stream().map(r -> r.getMetadata().getName()).collect(Collectors.toSet()));
    Role role1 = roles.getItems().get(0);
    Role role2 = roles.getItems().get(1);

    assertFalse(
        role1.getRules().containsAll(role2.getRules())
            && role2.getRules().containsAll(role1.getRules()),
        "exec and view roles should not be the same");

    RoleBindingList bindings = k8sClient.rbac().roleBindings().inNamespace("workspace123").list();
    assertEquals(
        Sets.newHashSet("serviceAccount-view", "serviceAccount-exec"),
        bindings
            .getItems()
            .stream()
            .map(r -> r.getMetadata().getName())
            .collect(Collectors.toSet()));
  }

  @Test
  public void testNullClusterRolesResultsInEmptySet() {
    namespaceFactory =
        new KubernetesNamespaceFactory(
            "blabol-<userid>-<username>-<userid>-<username>--",
            "",
            null,
            "che-<userid>",
            false,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            userManager,
            preferenceManager,
            pool);
    assertTrue(namespaceFactory.getClusterRoleNames().isEmpty());
  }

  @Test
  public void testClusterRolesProperlyParsed() {
    namespaceFactory =
        new KubernetesNamespaceFactory(
            "blabol-<userid>-<username>-<userid>-<username>--",
            "",
            "  one,two, three ,,five  ",
            "che-<userid>",
            false,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            userManager,
            preferenceManager,
            pool);
    Set<String> expected = Sets.newHashSet("one", "two", "three", "five");
    assertTrue(namespaceFactory.getClusterRoleNames().containsAll(expected));
    assertTrue(expected.containsAll(namespaceFactory.getClusterRoleNames()));
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
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            userManager,
            preferenceManager,
            pool);

    when(namespaceResource.get()).thenReturn(null);

    WorkspaceImpl workspace =
        new WorkspaceImplBuilder().setId("workspace123").setAttributes(emptyMap()).build();
    EnvironmentContext.getCurrent().setSubject(new SubjectImpl("jondoe", "123", null, false));
    String namespace = namespaceFactory.getNamespaceName(workspace);

    assertEquals(namespace, "che-123");
  }

  @Test
  public void testEvalNamespaceUsesNamespaceFromUserPreferencesIfExist() throws Exception {
    namespaceFactory =
        new KubernetesNamespaceFactory(
            "blabol-<userid>-<username>-<userid>-<username>--",
            "",
            "",
            "che-<userid>",
            true,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            userManager,
            preferenceManager,
            pool);

    Map<String, String> prefs = new HashMap<>();
    prefs.put(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, "che-123");
    prefs.put(NAMESPACE_TEMPLATE_ATTRIBUTE, "che-<userid>");

    when(preferenceManager.find(anyString())).thenReturn(prefs);
    String namespace =
        namespaceFactory.evaluateNamespaceName(
            new NamespaceResolutionContext("workspace123", "user123", "jondoe"));

    assertEquals(namespace, "che-123");
  }

  @Test
  public void testEvalNamespaceSkipsNamespaceFromUserPreferencesIfTemplateChanged()
      throws Exception {
    namespaceFactory =
        new KubernetesNamespaceFactory(
            "blabol-<userid>-<username>-<userid>-<username>--",
            "",
            "",
            "che-<userid>-<username>",
            true,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            userManager,
            preferenceManager,
            pool);

    Map<String, String> prefs = new HashMap<>();
    // returned but ignored
    prefs.put(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, "che-123");
    prefs.put(NAMESPACE_TEMPLATE_ATTRIBUTE, "che-<userid>");

    when(preferenceManager.find(anyString())).thenReturn(prefs);
    String namespace =
        namespaceFactory.evaluateNamespaceName(
            new NamespaceResolutionContext("workspace123", "user123", "jondoe"));

    assertEquals(namespace, "che-user123-jondoe");
  }

  @Test
  public void testEvalNamespaceSkipsNamespaceFromUserPreferencesIfUserAllowedPropertySetFalse()
      throws Exception {
    namespaceFactory =
        new KubernetesNamespaceFactory(
            "blabol-<userid>-<username>-<userid>-<username>--",
            "",
            "",
            "che-<userid>-<username>",
            false,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            userManager,
            preferenceManager,
            pool);

    Map<String, String> prefs = new HashMap<>();
    // returned but ignored
    prefs.put(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, "che-123");
    prefs.put(NAMESPACE_TEMPLATE_ATTRIBUTE, "che-<userid>");

    when(preferenceManager.find(anyString())).thenReturn(prefs);
    String namespace =
        namespaceFactory.evaluateNamespaceName(
            new NamespaceResolutionContext("workspace123", "user123", "jondoe"));

    assertEquals(namespace, "che-user123-jondoe");
  }

  @Test
  public void testEvalNamespaceSkipsNamespaceFromUserPreferencesIfTemplateContainsWorkspaceId()
      throws Exception {
    namespaceFactory =
        new KubernetesNamespaceFactory(
            "blabol-<userid>-<username>-<userid>-<username>--",
            "",
            "",
            "che-<workspaceid>-<username>",
            true,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            userManager,
            preferenceManager,
            pool);

    Map<String, String> prefs = new HashMap<>();
    // returned but ignored
    prefs.put(WORKSPACE_INFRASTRUCTURE_NAMESPACE_ATTRIBUTE, "che-123");
    prefs.put(NAMESPACE_TEMPLATE_ATTRIBUTE, "che-<workspaceid>-<username>");

    String namespace =
        namespaceFactory.evaluateNamespaceName(
            new NamespaceResolutionContext("workspace123", "user123", "jondoe"));

    assertEquals(namespace, "che-workspace123-jondoe");
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
            true,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            userManager,
            preferenceManager,
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
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            userManager,
            preferenceManager,
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
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            userManager,
            preferenceManager,
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

  @Test
  public void testEvalNamespaceNameWhenPreparedNamespacesFound() throws InfrastructureException {
    List<Namespace> namespaces =
        Arrays.asList(
            new NamespaceBuilder()
                .withNewMetadata()
                .withName("ns1")
                .withAnnotations(Map.of(NAMESPACE_ANNOTATION_NAME, "jondoe"))
                .endMetadata()
                .withNewStatus()
                .withNewPhase("Active")
                .endStatus()
                .build(),
            new NamespaceBuilder()
                .withNewMetadata()
                .withName("ns2")
                .withAnnotations(Map.of(NAMESPACE_ANNOTATION_NAME, "jondoe"))
                .endMetadata()
                .withNewStatus()
                .withNewPhase("Active")
                .endStatus()
                .build());
    doReturn(namespaces).when(namespaceList).getItems();

    namespaceFactory =
        new KubernetesNamespaceFactory(
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
            userManager,
            preferenceManager,
            pool);

    String namespace =
        namespaceFactory.evaluateNamespaceName(
            new NamespaceResolutionContext("workspace123", "user123", "jondoe"));

    assertEquals(namespace, "ns1");
  }

  @Test
  public void testUsernamePlaceholderInLabelsIsNotEvaluated() throws InfrastructureException {
    List<Namespace> namespaces =
        singletonList(
            new NamespaceBuilder()
                .withNewMetadata()
                .withName("ns1")
                .withAnnotations(Map.of(NAMESPACE_ANNOTATION_NAME, "jondoe"))
                .endMetadata()
                .withNewStatus()
                .withNewPhase("Active")
                .endStatus()
                .build());
    doReturn(namespaces).when(namespaceList).getItems();

    namespaceFactory =
        new KubernetesNamespaceFactory(
            "legacy",
            "",
            "",
            "defaultNs",
            false,
            true,
            true,
            "try_placeholder_here=<username>",
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            userManager,
            preferenceManager,
            pool);
    EnvironmentContext.getCurrent().setSubject(new SubjectImpl("jondoe", "123", null, false));
    namespaceFactory.list();

    verify(namespaceOperation).withLabels(Map.of("try_placeholder_here", "<username>"));
  }

  @Test(dataProvider = "invalidUsernames")
  public void normalizeTest(String raw, String expected) {
    namespaceFactory =
        new KubernetesNamespaceFactory(
            "",
            "",
            "",
            "che-<userid>",
            false,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            userManager,
            preferenceManager,
            pool);
    assertEquals(expected, namespaceFactory.normalizeNamespaceName(raw));
  }

  @Test
  public void normalizeLengthTest() {
    namespaceFactory =
        new KubernetesNamespaceFactory(
            "",
            "",
            "",
            "che-<userid>",
            false,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            userManager,
            preferenceManager,
            pool);

    assertEquals(
        63,
        namespaceFactory
            .normalizeNamespaceName(
                "looooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooooong")
            .length());
  }

  @Test
  public void shouldPrintWarnMessageAboutLegacyNamespaceName() {

    // run your test
    namespaceFactory =
        new KubernetesNamespaceFactory(
            "legacy",
            "",
            "",
            "<userid>-che",
            false,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            userManager,
            preferenceManager,
            pool);

    assertEqualsMessage(
        "The 'che.infra.kubernetes.namespace' configuration parameter has been deprecated and is subject to removal in future releases. The current value is: `{}`. Legacy workspaces located in this namespace may become unreachable in future releases. Please refer to the documentation about possible next steps.");
  }

  @Test
  public void shouldPrintWarnMessageAboutMissingRequiredPlaceholders() {

    // run your test
    namespaceFactory =
        new KubernetesNamespaceFactory(
            null,
            "",
            "",
            "che-che",
            false,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            userManager,
            preferenceManager,
            pool);

    assertEqualsMessage(
        "Namespace strategies other than 'per user' have been deprecated and are subject to removal in future releases. Using the {} placeholder is required in the 'che.infra.kubernetes.namespace.default' parameter. The current value is: `{}`.");
  }

  @Test
  public void shouldPrintWarnMessageAboutLegacyNamespaceNamePlaceholders() {

    // run your test
    namespaceFactory =
        new KubernetesNamespaceFactory(
            null,
            "",
            "",
            "<workspaceid>-che",
            false,
            true,
            true,
            NAMESPACE_LABELS,
            NAMESPACE_ANNOTATIONS,
            clientFactory,
            cheClientFactory,
            userManager,
            preferenceManager,
            pool);

    assertEqualsMessage(
        "'che.infra.kubernetes.namespace.default' configuration parameter with `{}` placeholder has been deprecated and is subject to removal in future releases. Current value is: `{}`. Please refer to the documentation about possible next steps.");
  }

  public void assertEqualsMessage(String message) {
    // verify using ArgumentCaptor
    ArgumentCaptor<Appender> argumentCaptor = ArgumentCaptor.forClass(Appender.class);
    Mockito.verify(mockedAppender).doAppend(argumentCaptor.capture());

    // assert against argumentCaptor.getAllValues()
    Assert.assertEquals(1, argumentCaptor.getAllValues().size());
    Assert.assertEquals(
        ((LoggingEvent) argumentCaptor.getAllValues().get(0)).getMessage(), message);
  }

  @BeforeMethod
  public void addMockedAppender() {
    ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(KubernetesNamespaceFactory.class))
        .addAppender(mockedAppender);
  }

  @AfterMethod
  public void detachMockedAppender() {
    // remove the mock appender from static context
    ((ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME))
        .detachAppender(mockedAppender);
  }

  @DataProvider
  public static Object[][] invalidUsernames() {
    return new Object[][] {
      new Object[] {"gmail@foo.bar", "gmail-foo-bar"},
      new Object[] {"_fef_123-ah_*zz**", "fef-123-ah-zz"},
      new Object[] {"a-b#-hello", "a-b-hello"},
      new Object[] {"a---------b", "a-b"},
      new Object[] {"--ab--", "ab"}
    };
  }

  private void prepareNamespaceToBeFoundByLabel(String username, List<Namespace> namespaces) {
    //
    // lenient().doReturn(namespaceListResource).when(namespaceOperation).withLabels(Map.of(NAMESPACE_LABEL_NAME, username));
    doReturn(namespaceList).when(namespaceListResource).list();
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

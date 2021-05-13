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
package org.eclipse.che.api.factory.server.scm.kubernetes;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.factory.server.scm.kubernetes.KubernetesGitCredentialManager.ANNOTATION_CHE_USERID;
import static org.eclipse.che.api.factory.server.scm.kubernetes.KubernetesGitCredentialManager.ANNOTATION_SCM_URL;
import static org.eclipse.che.api.factory.server.scm.kubernetes.KubernetesGitCredentialManager.ANNOTATION_SCM_USERNAME;
import static org.eclipse.che.api.factory.server.scm.kubernetes.KubernetesGitCredentialManager.DEFAULT_SECRET_ANNOTATIONS;
import static org.eclipse.che.api.factory.server.scm.kubernetes.KubernetesGitCredentialManager.NAME_PATTERN;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.api.model.SecretList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.FilterWatchListDeletable;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.factory.server.scm.PersonalAccessToken;
import org.eclipse.che.commons.lang.NameGenerator;
import org.eclipse.che.workspace.infrastructure.kubernetes.KubernetesClientFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.server.impls.KubernetesNamespaceMetaImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.KubernetesNamespaceMeta;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class KubernetesGitCredentialManagerTest {

  @Mock private KubernetesNamespaceFactory namespaceFactory;
  @Mock private KubernetesClientFactory clientFactory;
  @Mock private KubernetesClient kubeClient;

  @Mock private MixedOperation<Secret, SecretList, Resource<Secret>> secretsMixedOperation;

  @Mock NonNamespaceOperation<Secret, SecretList, Resource<Secret>> nonNamespaceOperation;

  @Mock private FilterWatchListDeletable<Secret, SecretList> filterWatchDeletable;

  @Mock private SecretList secretList;

  KubernetesGitCredentialManager kubernetesGitCredentialManager;

  @BeforeMethod
  protected void init() {
    kubernetesGitCredentialManager =
        new KubernetesGitCredentialManager(namespaceFactory, clientFactory);
    assertNotNull(this.kubernetesGitCredentialManager);
  }

  @Test
  public void testCreateAndSaveNewGitCredential() throws Exception {
    KubernetesNamespaceMeta meta = new KubernetesNamespaceMetaImpl("test");
    when(namespaceFactory.list()).thenReturn(Collections.singletonList(meta));

    when(clientFactory.create()).thenReturn(kubeClient);
    when(kubeClient.secrets()).thenReturn(secretsMixedOperation);
    when(secretsMixedOperation.inNamespace(eq(meta.getName()))).thenReturn(nonNamespaceOperation);
    when(nonNamespaceOperation.withLabels(anyMap())).thenReturn(filterWatchDeletable);
    when(filterWatchDeletable.list()).thenReturn(secretList);
    when(secretList.getItems()).thenReturn(emptyList());
    ArgumentCaptor<Secret> captor = ArgumentCaptor.forClass(Secret.class);

    PersonalAccessToken token =
        new PersonalAccessToken(
            "https://bitbucket.com",
            "cheUser",
            "username",
            "userId",
            "token-name",
            "tid-23434",
            "token123");

    // when
    kubernetesGitCredentialManager.createOrReplace(token);
    // then
    verify(nonNamespaceOperation).createOrReplace(captor.capture());
    Secret createdSecret = captor.getValue();
    assertNotNull(createdSecret);
    assertEquals(
        new String(Base64.getDecoder().decode(createdSecret.getData().get("credentials"))),
        "https://username:token123@bitbucket.com");
    assertTrue(createdSecret.getMetadata().getName().startsWith(NAME_PATTERN));
    assertFalse(createdSecret.getMetadata().getName().contains(token.getScmUserName()));
  }

  @Test
  public void testUpdateTokenInExistingCredential() throws Exception {
    KubernetesNamespaceMeta namespaceMeta = new KubernetesNamespaceMetaImpl("test");
    PersonalAccessToken token =
        new PersonalAccessToken(
            "https://bitbucket.com:5648",
            "cheUser",
            "username",
            "userId",
            "token-name",
            "tid-23434",
            "token123");

    Map<String, String> annotations = new HashMap<>(DEFAULT_SECRET_ANNOTATIONS);

    annotations.put(ANNOTATION_SCM_URL, token.getScmProviderUrl() + "/");
    annotations.put(ANNOTATION_SCM_USERNAME, token.getScmUserName());
    annotations.put(ANNOTATION_CHE_USERID, token.getCheUserId());
    ObjectMeta objectMeta =
        new ObjectMetaBuilder()
            .withName(NameGenerator.generate(NAME_PATTERN, 5))
            .withAnnotations(annotations)
            .build();
    Secret existing =
        new SecretBuilder()
            .withMetadata(objectMeta)
            .withData(Map.of("credentials", "foo 123"))
            .build();

    when(namespaceFactory.list()).thenReturn(Collections.singletonList(namespaceMeta));

    when(clientFactory.create()).thenReturn(kubeClient);
    when(kubeClient.secrets()).thenReturn(secretsMixedOperation);
    when(secretsMixedOperation.inNamespace(eq(namespaceMeta.getName())))
        .thenReturn(nonNamespaceOperation);
    when(nonNamespaceOperation.withLabels(anyMap())).thenReturn(filterWatchDeletable);
    when(filterWatchDeletable.list()).thenReturn(secretList);
    when(secretList.getItems()).thenReturn(singletonList(existing));

    // when
    kubernetesGitCredentialManager.createOrReplace(token);
    // then
    ArgumentCaptor<Secret> captor = ArgumentCaptor.forClass(Secret.class);
    verify(nonNamespaceOperation).createOrReplace(captor.capture());
    Secret createdSecret = captor.getValue();
    assertNotNull(createdSecret);
    assertEquals(
        new String(Base64.getDecoder().decode(createdSecret.getData().get("credentials"))),
        "https://username:token123@bitbucket.com:5648");
    assertEquals(createdSecret.getMetadata().getName(), objectMeta.getName());
  }
}

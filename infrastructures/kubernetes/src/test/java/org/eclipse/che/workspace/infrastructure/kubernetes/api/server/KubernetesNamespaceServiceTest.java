/*
 * Copyright (c) 2012-2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.workspace.infrastructure.kubernetes.api.server;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.response.Response;
import java.util.Collections;
import java.util.List;
import org.eclipse.che.api.core.rest.CheJsonProvider;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.server.impls.KubernetesNamespaceMetaImpl;
import org.eclipse.che.workspace.infrastructure.kubernetes.api.shared.dto.KubernetesNamespaceMetaDto;
import org.eclipse.che.workspace.infrastructure.kubernetes.namespace.KubernetesNamespaceFactory;
import org.everrest.assured.EverrestJetty;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link KubernetesNamespaceService}
 *
 * @author Sergii Leshchenko
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class KubernetesNamespaceServiceTest {

  @SuppressWarnings("unused") // is declared for deploying by everrest-assured
  private CheJsonProvider jsonProvider = new CheJsonProvider(Collections.emptySet());

  @Mock private KubernetesNamespaceFactory namespaceFactory;

  @InjectMocks private KubernetesNamespaceService service;

  @Test
  public void shouldReturnNamespaces() throws Exception {
    KubernetesNamespaceMetaImpl namespaceMeta =
        new KubernetesNamespaceMetaImpl(
            "ws-namespace", ImmutableMap.of("phase", "active", "default", "true"));
    when(namespaceFactory.list()).thenReturn(singletonList(namespaceMeta));

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/kubernetes/namespace");

    assertEquals(response.getStatusCode(), 200);
    List<KubernetesNamespaceMetaDto> namespaces =
        unwrapDtoList(response, KubernetesNamespaceMetaDto.class);
    assertEquals(namespaces.size(), 1);
    assertEquals(new KubernetesNamespaceMetaImpl(namespaces.get(0)), namespaceMeta);
    verify(namespaceFactory).list();
  }

  private static <T> List<T> unwrapDtoList(Response response, Class<T> dtoClass) {
    return DtoFactory.getInstance().createListDtoFromJson(response.body().print(), dtoClass);
  }
}

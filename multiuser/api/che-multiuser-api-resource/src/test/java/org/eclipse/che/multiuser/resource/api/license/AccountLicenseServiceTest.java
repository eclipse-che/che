/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.multiuser.resource.api.license;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Collections.singletonList;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import com.jayway.restassured.response.Response;
import java.util.HashSet;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.CheJsonProvider;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.resource.shared.dto.AccountLicenseDto;
import org.eclipse.che.multiuser.resource.shared.dto.ProvidedResourcesDto;
import org.eclipse.che.multiuser.resource.shared.dto.ResourceDto;
import org.eclipse.che.multiuser.resource.spi.impl.AccountLicenseImpl;
import org.everrest.assured.EverrestJetty;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.eclipse.che.multiuser.resource.api.license.AccountLicenseService}
 *
 * @author Sergii Leschenko
 */
@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class AccountLicenseServiceTest {
  @SuppressWarnings("unused") // is declared for deploying by everrest-assured
  private ApiExceptionMapper mapper;

  @SuppressWarnings("unused") // is declared for deploying by everrest-assured
  private CheJsonProvider jsonProvider = new CheJsonProvider(new HashSet<>());

  @Mock private AccountLicenseManager accountLicenseManager;

  @InjectMocks private AccountLicenseService service;

  @Test
  public void shouldGetLicense() throws Exception {
    // given
    final ResourceDto testResource =
        DtoFactory.newDto(ResourceDto.class).withType("test").withAmount(1234).withUnit("mb");

    final AccountLicenseDto toFetch =
        DtoFactory.newDto(AccountLicenseDto.class)
            .withAccountId("account123")
            .withResourcesDetails(
                singletonList(
                    DtoFactory.newDto(ProvidedResourcesDto.class)
                        .withId("resource123")
                        .withProviderId("provider")
                        .withOwner("account123")
                        .withStartTime(123L)
                        .withEndTime(321L)
                        .withResources(singletonList(testResource))))
            .withTotalResources(singletonList(testResource));

    // when
    when(accountLicenseManager.getByAccount(eq("account123")))
        .thenReturn(new AccountLicenseImpl(toFetch));

    // then
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .when()
            .expect()
            .statusCode(200)
            .get(SECURE_PATH + "/license/account/account123");

    final AccountLicenseDto fetchedLicense =
        DtoFactory.getInstance()
            .createDtoFromJson(response.body().print(), AccountLicenseDto.class);
    assertEquals(fetchedLicense, toFetch);
    verify(accountLicenseManager).getByAccount("account123");
  }
}

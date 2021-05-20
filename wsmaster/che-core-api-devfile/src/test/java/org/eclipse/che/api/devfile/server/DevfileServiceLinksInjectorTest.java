/*
 * Copyright (c) 2012-2020 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.devfile.server;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.devfile.shared.Constants;
import org.eclipse.che.api.devfile.shared.dto.UserDevfileDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.core.impl.uri.UriBuilderImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

@Listeners(MockitoTestNGListener.class)
public class DevfileServiceLinksInjectorTest {
  private static final String URI_BASE = "http://localhost:8080";
  private static final String SERVICE_PATH = "/devfile";

  @Mock ServiceContext context;

  @BeforeMethod
  public void setUp() {
    final UriBuilder uriBuilder = new UriBuilderImpl();
    uriBuilder.uri(URI_BASE);

    when(context.getBaseUriBuilder()).thenReturn(uriBuilder);
  }

  @Test
  public void shouldInjectLinks() {
    // given
    final UserDevfileDto userDevfileDto = DtoFactory.newDto(UserDevfileDto.class).withId("id123");
    DevfileServiceLinksInjector linksInjector = new DevfileServiceLinksInjector();
    // when
    final UserDevfileDto withLinks = linksInjector.injectLinks(userDevfileDto, context);
    // then
    assertEquals(withLinks.getLinks().size(), 1);
    assertNotNull(withLinks.getLink(Constants.LINK_REL_SELF));
    assertEquals(withLinks.getLinks().get(0).getMethod(), HttpMethod.GET);
    assertEquals(withLinks.getLinks().get(0).getHref(), URI_BASE + SERVICE_PATH + "/id123");
    assertEquals(withLinks.getLinks().get(0).getProduces(), MediaType.APPLICATION_JSON);
  }
}

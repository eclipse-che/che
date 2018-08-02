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
package org.eclipse.che.multiuser.organization.api;

import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import javax.ws.rs.core.UriBuilder;
import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.multiuser.organization.shared.Constants;
import org.eclipse.che.multiuser.organization.shared.dto.OrganizationDto;
import org.everrest.core.impl.uri.UriBuilderImpl;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.eclipse.che.multiuser.organization.api.OrganizationLinksInjector}
 *
 * @author Sergii Leschenko
 */
@Listeners(MockitoTestNGListener.class)
public class OrganizationLinksInjectorTest {
  private static final String URI_BASE = "http://localhost:8080";

  @Mock ServiceContext context;

  OrganizationLinksInjector organizationLinksInjector = new OrganizationLinksInjector();

  @BeforeMethod
  public void setUp() {
    final UriBuilder uriBuilder = new UriBuilderImpl();
    uriBuilder.uri(URI_BASE);

    when(context.getBaseUriBuilder()).thenReturn(uriBuilder);
  }

  @Test
  public void shouldInjectLinks() {
    final OrganizationDto organization = DtoFactory.newDto(OrganizationDto.class).withId("org123");

    final OrganizationDto withLinks = organizationLinksInjector.injectLinks(organization, context);

    assertEquals(withLinks.getLinks().size(), 2);
    assertNotNull(withLinks.getLink(Constants.LINK_REL_SELF));
    assertNotNull(withLinks.getLink(Constants.LINK_REL_SUBORGANIZATIONS));
  }
}

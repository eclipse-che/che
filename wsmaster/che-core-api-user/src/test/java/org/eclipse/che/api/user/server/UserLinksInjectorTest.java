/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.user.server;

import com.google.common.collect.Sets;

import org.eclipse.che.api.core.rest.ServiceContext;
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.core.impl.uri.UriBuilderImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link UserLinksInjector}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(MockitoTestNGListener.class)
public class UserLinksInjectorTest {

    @Mock
    private ServiceContext serviceContext;

    @InjectMocks
    private UserLinksInjector linksInjector;

    @BeforeMethod
    public void setUpContext() {
        final UriBuilderImpl uriBuilder = new UriBuilderImpl();
        uriBuilder.uri("http://localhost:8080");
        when(serviceContext.getServiceUriBuilder()).thenReturn(uriBuilder);
        when(serviceContext.getBaseUriBuilder()).thenReturn(uriBuilder);
    }

    @Test
    public void shouldInjectLinks() throws Exception {
        final UserDto userDto = DtoFactory.newDto(UserDto.class)
                                          .withId("user123")
                                          .withEmail("user@codenvy.com")
                                          .withName("user");

        linksInjector.injectLinks(userDto, serviceContext);

        // [rel, method] pairs links
        final Set<Pair<String, String>> links = userDto.getLinks()
                                                       .stream()
                                                       .map(link -> Pair.of(link.getMethod(), link.getRel()))
                                                       .collect(Collectors.toSet());
        final Set<Pair<String, String>> expectedLinks
                = new HashSet<>(asList(Pair.of("GET", Constants.LINK_REL_SELF),
                                       Pair.of("GET", Constants.LINK_REL_CURRENT_USER),
                                       Pair.of("GET", Constants.LINK_REL_PROFILE),
                                       Pair.of("GET", Constants.LINK_REL_CURRENT_USER_SETTINGS),
                                       Pair.of("GET", Constants.LINK_REL_PREFERENCES),
                                       Pair.of("POST", Constants.LINK_REL_CURRENT_USER_PASSWORD)));

        assertEquals(links, expectedLinks, "Difference " + Sets.symmetricDifference(links, expectedLinks) + "\n");
    }
}

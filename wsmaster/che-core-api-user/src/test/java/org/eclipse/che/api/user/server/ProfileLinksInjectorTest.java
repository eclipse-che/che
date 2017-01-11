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
import org.eclipse.che.api.user.shared.dto.ProfileDto;
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
 * Tests for {@link ProfileLinksInjector}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners(MockitoTestNGListener.class)
public class ProfileLinksInjectorTest {

    @Mock
    private ServiceContext serviceContext;

    @InjectMocks
    private ProfileLinksInjector linksInjector;

    @BeforeMethod
    public void setUpContext() {
        final UriBuilderImpl uriBuilder = new UriBuilderImpl();
        uriBuilder.uri("http://localhost:8080");
        when(serviceContext.getServiceUriBuilder()).thenReturn(uriBuilder);
        when(serviceContext.getBaseUriBuilder()).thenReturn(uriBuilder);
    }

    @Test
    public void shouldInjectProfileLinks() throws Exception {
        final ProfileDto profileDto = DtoFactory.newDto(ProfileDto.class)
                                                .withUserId("user123")
                                                .withEmail("user@codenvy.com");

        linksInjector.injectLinks(profileDto, serviceContext);

        // [rel, method] pairs links
        final Set<Pair<String, String>> links = profileDto.getLinks()
                                                          .stream()
                                                          .map(link -> Pair.of(link.getMethod(), link.getRel()))
                                                          .collect(Collectors.toSet());
        final Set<Pair<String, String>> expectedLinks
                = new HashSet<>(asList(Pair.of("GET", Constants.LINK_REL_SELF),
                                       Pair.of("GET", Constants.LINK_REL_CURRENT_PROFILE),
                                       Pair.of("PUT", Constants.LINK_REL_PROFILE_ATTRIBUTES),
                                       Pair.of("PUT", Constants.LINK_REL_CURRENT_PROFILE_ATTRIBUTES),
                                       Pair.of("DELETE", Constants.LINK_REL_CURRENT_PROFILE_ATTRIBUTES)));

        assertEquals(links, expectedLinks, "Difference " + Sets.symmetricDifference(links, expectedLinks) + "\n");
    }
}

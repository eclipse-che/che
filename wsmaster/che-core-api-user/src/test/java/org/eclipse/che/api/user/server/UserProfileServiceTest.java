/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.user.server;

import sun.security.acl.PrincipalImpl;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.Profile;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.user.server.dao.UserProfileDao;
import org.eclipse.che.api.user.shared.dto.ProfileDescriptor;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.subject.Subject;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.ContainerRequest;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.tools.DependencySupplierImpl;
import org.everrest.core.tools.ResourceLauncher;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.eclipse.che.api.user.server.Constants.LINK_REL_UPDATE_CURRENT_USER_PROFILE;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_GET_CURRENT_USER_PROFILE;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_GET_USER_PROFILE_BY_ID;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_UPDATE_PREFERENCES;
import static org.eclipse.che.api.user.server.Constants.LINK_REL_UPDATE_USER_PROFILE_BY_ID;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests for {@link UserProfileService}
 *
 * @author Max Shaposhnik
 * @author Eugene Voevodin
 */
@Listeners(value = {MockitoTestNGListener.class})
public class UserProfileServiceTest {

    private static final String BASE_URI     = "http://localhost/service";
    private static final String SERVICE_PATH = BASE_URI + "/profile";

    @Mock
    private UserProfileDao     profileDao;
    @Mock
    private UserDao            userDao;
    @Mock
    private PreferenceDao      preferenceDao;
    @Mock
    private EventService       eventService;
    @Mock
    private User               testUser;
    @Mock
    private UriInfo            uriInfo;
    @Mock
    private EnvironmentContext environmentContext;
    @Mock
    private SecurityContext    securityContext;
    private ResourceLauncher   launcher;
    private UserProfileService service;

    @BeforeMethod
    public void setUp() throws Exception {
        final ResourceBinderImpl resources = new ResourceBinderImpl();
        resources.addResource(UserProfileService.class, null);
        final DependencySupplierImpl dependencies = new DependencySupplierImpl();
        dependencies.addComponent(UserDao.class, userDao);
        dependencies.addComponent(UserProfileDao.class, profileDao);
        dependencies.addComponent(PreferenceDao.class, preferenceDao);
        dependencies.addComponent(EventService.class, eventService);
        final URI uri = new URI(BASE_URI);
        final ContainerRequest req = new ContainerRequest(null, uri, uri, null, null, securityContext);
        final ApplicationContextImpl contextImpl = new ApplicationContextImpl(req, null, ProviderBinder.getInstance());
        contextImpl.setDependencySupplier(dependencies);
        ApplicationContextImpl.setCurrent(contextImpl);
        final ApplicationProviderBinder binder = new ApplicationProviderBinder();
        binder.addExceptionMapper(ApiExceptionMapper.class);
        final EverrestProcessor processor = new EverrestProcessor(resources,
                                                                  binder,
                                                                  dependencies,
                                                                  new EverrestConfiguration(),
                                                                  null);
        launcher = new ResourceLauncher(processor);
        service = (UserProfileService)resources.getMatchedResource("/profile", new ArrayList<String>())
                                               .getInstance(ApplicationContextImpl.getCurrent());
        //setup testUser
        final String id = "user123abc456def";
        final String email = "user@testuser.com";
        when(testUser.getEmail()).thenReturn(email);
        when(testUser.getId()).thenReturn(id);
        when(environmentContext.get(SecurityContext.class)).thenReturn(securityContext);
        when(securityContext.getUserPrincipal()).thenReturn(new PrincipalImpl(email));
        when(userDao.getByAlias(email)).thenReturn(testUser);
        when(userDao.getById(id)).thenReturn(testUser);
        org.eclipse.che.commons.env.EnvironmentContext.getCurrent().setSubject(new Subject() {
            @Override
            public String getUserName() {
                return testUser.getEmail();
            }

            @Override
            public boolean isMemberOf(String s) {
                return false;
            }

            @Override
            public boolean hasPermission(String domain, String instance, String action) {
                return false;
            }

            @Override
            public void checkPermission(String domain, String instance, String action) throws ForbiddenException {
            }

            @Override
            public String getToken() {
                return null;
            }

            @Override
            public String getUserId() {
                return testUser.getId();
            }

            @Override
            public boolean isTemporary() {
                return false;
            }
        });
    }

    @Test
    public void shouldBeAbleToGetCurrentProfile() throws Exception {
        final Profile current = new Profile().withId(testUser.getId()).withUserId(testUser.getId());
        when(profileDao.getById(current.getId())).thenReturn(current);

        final ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH, null);

        assertEquals(response.getStatus(), OK.getStatusCode());
        final ProfileDescriptor descriptor = (ProfileDescriptor)response.getEntity();
        assertEquals(descriptor.getId(), current.getId());
        assertEquals(descriptor.getUserId(), current.getUserId());
        assertEquals(descriptor.getAttributes().get("email"), testUser.getEmail());
    }

    @Test
    public void shouldBeAbleToGetPreferences() throws Exception {
        final Map<String, String> preferences = new HashMap<>(8);
        preferences.put("test1", "test1");
        preferences.put("test2", "test2");
        preferences.put("test3", "test3");
        when(preferenceDao.getPreferences(testUser.getId())).thenReturn(preferences);

        final ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH + "/prefs", null);

        assertEquals(response.getStatus(), OK.getStatusCode());
        assertEquals(response.getEntity(), preferences);
    }

    @Test
    public void shouldBeAbleToRemoveAttributes() throws Exception {
        final Map<String, String> attributes = new HashMap<>(8);
        attributes.put("test", "test");
        attributes.put("test1", "test");
        attributes.put("test2", "test");
        final Profile profile = new Profile().withId(testUser.getId()).withAttributes(attributes);
        when(profileDao.getById(profile.getId())).thenReturn(profile);

        final ContainerResponse response = makeRequest(HttpMethod.DELETE, SERVICE_PATH + "/attributes", asList("test", "test2"));

        assertEquals(response.getStatus(), NO_CONTENT.getStatusCode());
        verify(profileDao, times(1)).update(profile);
        assertEquals(attributes.size(), 1);
        assertNotNull(attributes.get("test1"));
    }

    @Test
    public void shouldRemoveAllAttributesIfNullWasSent() throws Exception {
        final Map<String, String> attributes = new HashMap<>(8);
        attributes.put("test", "test");
        attributes.put("test1", "test");
        attributes.put("test2", "test");
        final Profile profile = new Profile().withId(testUser.getId()).withAttributes(attributes);
        when(profileDao.getById(profile.getId())).thenReturn(profile);

        final ContainerResponse response = makeRequest(HttpMethod.DELETE, SERVICE_PATH + "/attributes", null);

        assertEquals(response.getStatus(), NO_CONTENT.getStatusCode());
        verify(profileDao, times(1)).update(profile);
        assertTrue(attributes.isEmpty());
    }

    @Test
    public void shouldBeAbleToUpdatePreferences() throws Exception {
        final Map<String, String> preferences = new HashMap<>(8);
        preferences.put("test1", "test1");
        preferences.put("test2", "test2");
        preferences.put("test3", "test3");
        when(preferenceDao.getPreferences(testUser.getId())).thenReturn(preferences);
        final Map<String, String> update = singletonMap("test1", "new_value");

        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH + "/prefs", update);

        preferences.putAll(update);
        assertEquals(response.getStatus(), OK.getStatusCode());
        assertEquals(response.getEntity(), preferences);
        verify(preferenceDao).setPreferences(testUser.getId(), preferences);
    }

    @Test
    public void shouldThrowExceptionIfPreferencesUpdateIsNull() throws Exception {
        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH + "/prefs", null);

        assertEquals(response.getStatus(), 409);
    }

    @Test
    public void shouldThrowExceptionIfPreferencesUpdateIsEmpty() throws Exception {
        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH + "/prefs", emptyMap());

        assertEquals(response.getStatus(), 409);
    }

    @Test
    public void shouldBeAbleToRemovePreferences() throws Exception {
        final Map<String, String> preferences = new HashMap<>(8);
        preferences.put("test1", "test1");
        preferences.put("test2", "test2");
        preferences.put("test3", "test3");
        when(preferenceDao.getPreferences(testUser.getId())).thenReturn(preferences);

        final ContainerResponse response = makeRequest(HttpMethod.DELETE, SERVICE_PATH + "/prefs", singletonList("test1"));

        assertEquals(response.getStatus(), NO_CONTENT.getStatusCode());
        assertNull(preferences.get("test1"));
        verify(preferenceDao).setPreferences(testUser.getId(), preferences);
    }

    @Test
    public void shouldRemoveAllPreferencesIfNullWasSend() throws Exception {
        final ContainerResponse response = makeRequest(HttpMethod.DELETE, SERVICE_PATH + "/prefs", null);

        assertEquals(response.getStatus(), NO_CONTENT.getStatusCode());
        verify(preferenceDao).remove(testUser.getId());
    }

    @Test
    public void shouldBeAbleToGetProfileById() throws Exception {
        final Profile profile = new Profile().withId(testUser.getId())
                                             .withUserId(testUser.getId());
        when(profileDao.getById(profile.getId())).thenReturn(profile);

        final ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH + "/" + profile.getId(), null);

        assertEquals(response.getStatus(), OK.getStatusCode());
        final ProfileDescriptor descriptor = (ProfileDescriptor)response.getEntity();
        assertEquals(descriptor.getUserId(), profile.getId());
        assertEquals(descriptor.getId(), profile.getId());
        assertEquals(descriptor.getAttributes().get("email"), testUser.getEmail());
    }

    @Test
    public void shouldBeAbleToUpdateCurrentProfileAttributes() throws Exception {
        final Profile profile = new Profile().withId(testUser.getId())
                                             .withAttributes(new HashMap<>(singletonMap("existed", "old")));
        when(profileDao.getById(profile.getId())).thenReturn(profile);
        final Map<String, String> attributes = new HashMap<>(4);
        attributes.put("existed", "new");
        attributes.put("new", "value");

        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH, attributes);

        assertEquals(response.getStatus(), OK.getStatusCode());
        verify(profileDao, times(1)).update(profile);
        verify(eventService).publish(any());
        assertEquals(((ProfileDescriptor)response.getEntity()).getAttributes(), attributes);
    }

    @Test
    public void shouldThrowExceptionIfAttributesUpdateForCurrentProfileIsNull() throws Exception {
        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH, null);

        assertEquals(response.getStatus(), 409);
    }

    @Test
    public void shouldThrowExceptionIfAttributesUpdateForCurrentProfileIsEmpty() throws Exception {
        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH, emptyMap());

        assertEquals(response.getStatus(), 409);
    }

    @Test
    public void shouldThrowExceptionIfAttributesUpdateForSpecificProfileIsNull() throws Exception {
        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH + "/any_profile_id", null);

        assertEquals(response.getStatus(), 409);
    }

    @Test
    public void shouldThrowExceptionIfAttributesUpdateForSpecificProfileIsEmpty() throws Exception {
        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH + "/any_profile_id", emptyMap());

        assertEquals(response.getStatus(), 409);
    }

    @Test
    public void shouldBeAbleToUpdateProfileById() throws Exception {
        final Profile profile = new Profile().withId(testUser.getId())
                                             .withUserId(testUser.getId())
                                             .withAttributes(new HashMap<>(singletonMap("existed", "old")));
        when(profileDao.getById(testUser.getId())).thenReturn(profile);
        final Map<String, String> attributes = new HashMap<>(4);
        attributes.put("existed", "new");
        attributes.put("new", "value");

        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH + "/" + profile.getId(), attributes);

        assertEquals(response.getStatus(), OK.getStatusCode());
        assertEquals(((ProfileDescriptor)response.getEntity()).getAttributes(), attributes);
        verify(profileDao, times(1)).update(profile);
        verify(eventService).publish(any());
    }

    @Test
    public void testLinksForUser() {
        final Profile profile = new Profile().withId(testUser.getId());
        when(securityContext.isUserInRole("user")).thenReturn(true);

        final Set<String> expectedRels = new HashSet<>(asList(LINK_REL_GET_CURRENT_USER_PROFILE,
                                                              LINK_REL_UPDATE_CURRENT_USER_PROFILE,
                                                              LINK_REL_GET_USER_PROFILE_BY_ID,
                                                              LINK_REL_UPDATE_PREFERENCES));

        assertEquals(asRels(service.toDescriptor(profile, securityContext).getLinks()), expectedRels);
    }

    @Test
    public void testLinksForSystemAdmin() {
        final Profile profile = new Profile().withId(testUser.getId());
        when(securityContext.isUserInRole("system/admin")).thenReturn(true);

        final Set<String> expectedRels = new HashSet<>(asList(LINK_REL_UPDATE_USER_PROFILE_BY_ID,
                                                              LINK_REL_GET_USER_PROFILE_BY_ID));

        assertEquals(asRels(service.toDescriptor(profile, securityContext).getLinks()), expectedRels);
    }

    @Test
    public void testLinksForSystemManager() {
        final Profile profile = new Profile().withId(testUser.getId());
        when(securityContext.isUserInRole("system/manager")).thenReturn(true);

        assertEquals(asRels(service.toDescriptor(profile, securityContext).getLinks()), singleton(LINK_REL_GET_USER_PROFILE_BY_ID));
    }

    private Set<String> asRels(List<Link> links) {
        final Set<String> rels = new HashSet<>();
        for (Link link : links) {
            rels.add(link.getRel());
        }
        return rels;
    }

    private ContainerResponse makeRequest(String method, String path, Object entity) throws Exception {
        Map<String, List<String>> headers = null;
        byte[] data = null;
        if (entity != null) {
            headers = new HashMap<>();
            headers.put(HttpHeaders.CONTENT_TYPE, singletonList(MediaType.APPLICATION_JSON));
            data = JsonHelper.toJson(entity).getBytes();
        }
        return launcher.service(method, path, BASE_URI, headers, data, null, environmentContext);
    }
}

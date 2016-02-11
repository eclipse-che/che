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

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.user.server.dao.PreferenceDao;
import org.eclipse.che.api.user.server.dao.Profile;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.server.dao.UserDao;
import org.eclipse.che.api.user.server.dao.UserProfileDao;
import org.eclipse.che.api.user.shared.dto.UserDescriptor;
import org.eclipse.che.api.user.shared.dto.UserInRoleDescriptor;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.core.impl.ApplicationContextImpl;
import org.everrest.core.impl.ApplicationProviderBinder;
import org.everrest.core.impl.ContainerResponse;
import org.everrest.core.impl.EnvironmentContext;
import org.everrest.core.impl.EverrestConfiguration;
import org.everrest.core.impl.EverrestProcessor;
import org.everrest.core.impl.ProviderBinder;
import org.everrest.core.impl.ResourceBinderImpl;
import org.everrest.core.impl.uri.UriBuilderImpl;
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
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Tests for {@link UserService}
 *
 * @author Eugene Veovodin
 * @author Max Shaposhnik
 */
@Listeners(value = {MockitoTestNGListener.class})
public class UserServiceTest {

    private final String BASE_URI     = "http://localhost/service";
    private final String SERVICE_PATH = BASE_URI + "/user";

    @Mock
    UserProfileDao     profileDao;
    @Mock
    UserDao            userDao;
    @Mock
    TokenValidator     tokenValidator;
    @Mock
    UriInfo            uriInfo;
    @Mock
    EnvironmentContext environmentContext;
    @Mock
    PreferenceDao      preferenceDao;
    @Mock
    SecurityContext    securityContext;

    UserService userService;

    ResourceLauncher launcher;

    @BeforeMethod
    public void setUp() throws Exception {
        ResourceBinderImpl resources = new ResourceBinderImpl();
        DependencySupplierImpl dependencies = new DependencySupplierImpl();
        dependencies.addComponent(UserProfileDao.class, profileDao);
        dependencies.addComponent(UserDao.class, userDao);
        dependencies.addComponent(TokenValidator.class, tokenValidator);
        dependencies.addComponent(PreferenceDao.class, preferenceDao);

        userService = new UserService(userDao, profileDao, preferenceDao, tokenValidator, true);
        final Field uriField = userService.getClass()
                                          .getSuperclass()
                                          .getDeclaredField("uriInfo");
        uriField.setAccessible(true);
        uriField.set(userService, uriInfo);

        resources.addResource(userService, null);

        EverrestProcessor processor = new EverrestProcessor(resources,
                                                            new ApplicationProviderBinder(),
                                                            dependencies,
                                                            new EverrestConfiguration(),
                                                            null);
        launcher = new ResourceLauncher(processor);
        ProviderBinder providerBinder = ProviderBinder.getInstance();
        providerBinder.addExceptionMapper(ApiExceptionMapper.class);
        ApplicationContextImpl.setCurrent(new ApplicationContextImpl(null, null, providerBinder));
        //set up user
        final User user = createUser();
        when(environmentContext.get(SecurityContext.class)).thenReturn(securityContext);

        when(uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());

        org.eclipse.che.commons.env.EnvironmentContext.getCurrent().setUser(new org.eclipse.che.commons.user.User() {

            @Override
            public String getName() {
                return user.getEmail();
            }

            @Override
            public boolean isMemberOf(String s) {
                return false;
            }

            @Override
            public String getToken() {
                return null;
            }

            @Override
            public String getId() {
                return user.getId();
            }

            @Override
            public boolean isTemporary() {
                return false;
            }
        });
    }

    @Test
    public void shouldBeAbleToCreateNewUser() throws Exception {
        final String userEmail = "test@email.com";
        final String token = "test_token";
        when(tokenValidator.validateToken(token)).thenReturn(userEmail);

        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH + "/create?token=" + token, null);

        assertEquals(response.getStatus(), CREATED.getStatusCode());
        final UserDescriptor user = (UserDescriptor)response.getEntity();
        assertEquals(user.getEmail(), userEmail);
        assertEquals(user.getPassword(), "<none>");
        verify(userDao).create(any(User.class));
        verify(profileDao).create(any(Profile.class));
    }

    @Test
    public void shouldBeAbleToCreateNewUserForSystemAdmin() throws Exception {
        final UserDescriptor newUser = DtoFactory.getInstance()
                                                 .createDto(UserDescriptor.class)
                                                 .withName("test")
                                                 .withPassword("password123")
                                                 .withEmail("test@mail.com");
        when(securityContext.isUserInRole("system/admin")).thenReturn(true);

        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH + "/create", newUser);

        assertEquals(response.getStatus(), CREATED.getStatusCode());
        final UserDescriptor descriptor = (UserDescriptor)response.getEntity();
        assertEquals(descriptor.getName(), newUser.getName());
        assertEquals(descriptor.getPassword(), "<none>");
        verify(userDao).create(any(User.class));
        verify(profileDao).create(any(Profile.class));
    }

    @Test
    public void shouldNotBeAbleToCreateNewUserWithoutSystemAdminRoleIfDeniedUserSelfCreation() throws Exception {
        when(securityContext.isUserInRole("system/admin")).thenReturn(false);
        final Field uriField = userService.getClass()
                                          .getDeclaredField("userSelfCreationAllowed");
        uriField.setAccessible(true);
        uriField.set(userService, false);

        final String userEmail = "test@email.com";
        final String token = "test_token";
        when(tokenValidator.validateToken(token)).thenReturn(userEmail);

        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH + "/create?token=" + token, null);

        assertEquals(response.getStatus(), FORBIDDEN.getStatusCode());
        verify(userDao, never()).create(any(User.class));
        verify(profileDao, never()).create(any(Profile.class));
    }

    @Test
    public void shouldBeAbleToCreateNewUserWithSystemAdminRoleIfDeniedUserSelfCreation() throws Exception {
        when(securityContext.isUserInRole("system/admin")).thenReturn(true);
        final Field uriField = userService.getClass()
                                          .getDeclaredField("userSelfCreationAllowed");
        uriField.setAccessible(true);
        uriField.set(userService, false);

        final UserDescriptor newUser = DtoFactory.getInstance()
                                                 .createDto(UserDescriptor.class)
                                                 .withName("test")
                                                 .withEmail("test@mail.com");

        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH + "/create", newUser);

        assertEquals(response.getStatus(), CREATED.getStatusCode());
        final UserDescriptor user = (UserDescriptor)response.getEntity();
        assertEquals(user.getName(), newUser.getName());
        assertEquals(user.getPassword(), "<none>");
        verify(userDao).create(any(User.class));
        verify(profileDao).create(any(Profile.class));
    }

    @Test
    public void shouldThrowForbiddenExceptionWhenCreatingUserWithInvalidPassword() throws Exception {
        final UserDescriptor newUser = DtoFactory.getInstance()
                                                 .createDto(UserDescriptor.class)
                                                 .withName("test")
                                                 .withPassword("password");
        when(securityContext.isUserInRole("system/admin")).thenReturn(true);

        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH + "/create", newUser);

        assertEquals(response.getStatus(), BAD_REQUEST.getStatusCode());
        verify(userDao, never()).create(any(User.class));
        verify(profileDao, never()).create(any(Profile.class));
    }

    @Test
    public void shouldGeneratedPasswordWhenCreatingUserAndItIsMissing() throws Exception {
        final UserDescriptor newUser = DtoFactory.getInstance()
                                                 .createDto(UserDescriptor.class)
                                                 .withName("test")
                                                 .withEmail("test@mail.com");
        when(securityContext.isUserInRole("system/admin")).thenReturn(true);

        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH + "/create", newUser);

        final UserDescriptor descriptor = (UserDescriptor)response.getEntity();
        assertEquals(descriptor.getName(), newUser.getName());
        assertEquals(descriptor.getPassword(), "<none>");
        verify(userDao).create(any(User.class));
        verify(profileDao).create(any(Profile.class));
    }

    @Test
    public void shouldThrowUnauthorizedExceptionWhenCreatingUserBasedOnTokenAndItIsNull() throws Exception {
        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH + "/create", null);

        assertEquals(response.getStatus(), UNAUTHORIZED.getStatusCode());
        verify(userDao, never()).create(any(User.class));
        verify(profileDao, never()).create(any(Profile.class));
    }

    @Test
    public void shouldThrowForbiddenExceptionWhenCreatingUserBasedOnEntityWhichIsNull() throws Exception {
        when(securityContext.isUserInRole("system/admin")).thenReturn(true);

        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH + "/create", null);

        assertEquals(response.getStatus(), BAD_REQUEST.getStatusCode());
        verify(userDao, never()).create(any(User.class));
        verify(profileDao, never()).create(any(Profile.class));
    }

    @Test
    public void shouldThrowForbiddenExceptionWhenCreatingUserBasedOnEntityWhichContainsNullEmail() throws Exception {
        final UserDescriptor newUser = DtoFactory.getInstance().createDto(UserDescriptor.class);
        when(securityContext.isUserInRole("system/admin")).thenReturn(true);

        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH + "/create", newUser);

        assertEquals(response.getStatus(), BAD_REQUEST.getStatusCode());
        verify(userDao, never()).create(any(User.class));
        verify(profileDao, never()).create(any(Profile.class));
    }

    @Test
    public void shouldBeAbleToGetCurrentUser() throws Exception {
        final User user = createUser();

        final ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH, null);

        assertEquals(response.getStatus(), OK.getStatusCode());
        final UserDescriptor descriptor = (UserDescriptor)response.getEntity();
        assertEquals(descriptor.getId(), user.getId());
        assertEquals(descriptor.getEmail(), user.getEmail());
        assertEquals(descriptor.getAliases(), user.getAliases());
    }

    @Test
    public void shouldBeAbleToGetUserById() throws Exception {
        final User user = createUser();

        final ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH + "/" + user.getId(), null);

        assertEquals(response.getStatus(), OK.getStatusCode());
        final UserDescriptor descriptor = (UserDescriptor)response.getEntity();
        assertEquals(descriptor.getId(), user.getId());
        assertEquals(descriptor.getEmail(), user.getEmail());
        assertEquals(descriptor.getAliases(), user.getAliases());
    }

    @Test
    public void shouldBeAbleToGetUserByEmail() throws Exception {
        final User user = createUser();

        final ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH + "?alias=" + user.getEmail(), null);

        assertEquals(response.getStatus(), OK.getStatusCode());
        final UserDescriptor descriptor = (UserDescriptor)response.getEntity();
        assertEquals(descriptor.getId(), user.getId());
        assertEquals(descriptor.getEmail(), user.getEmail());
        assertEquals(descriptor.getAliases(), user.getAliases());
    }

    @Test
    public void shouldBeAbleToUpdateUserPassword() throws Exception {
        final User user = createUser();
        final String newPassword = "validPass123";
        final Map<String, List<String>> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, singletonList(MediaType.APPLICATION_FORM_URLENCODED));

        final ContainerResponse response = launcher.service(HttpMethod.POST,
                                                            SERVICE_PATH + "/password",
                                                            BASE_URI,
                                                            headers,
                                                            ("password=" + newPassword).getBytes(),
                                                            null,
                                                            environmentContext);

        assertEquals(response.getStatus(), NO_CONTENT.getStatusCode());
        verify(userDao).update(user.withPassword(newPassword));
    }

    @Test
    public void shouldFailUpdatePasswordContainsOnlyLetters() throws Exception {
        final User user = createUser();
        final String newPassword = "password";
        final Map<String, List<String>> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, singletonList(MediaType.APPLICATION_FORM_URLENCODED));

        final ContainerResponse response = launcher.service(HttpMethod.POST,
                                                            SERVICE_PATH + "/password",
                                                            BASE_URI,
                                                            headers,
                                                            ("password=" + newPassword).getBytes(),
                                                            null,
                                                            environmentContext);

        assertEquals(response.getStatus(), BAD_REQUEST.getStatusCode());
        verify(userDao, never()).update(user.withPassword(newPassword));
    }

    @Test
    public void shouldFailUpdatePasswordContainsOnlyDigits() throws Exception {
        final User user = createUser();
        final String newPassword = "12345678";
        final Map<String, List<String>> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, singletonList(MediaType.APPLICATION_FORM_URLENCODED));

        final ContainerResponse response = launcher.service(HttpMethod.POST,
                                                            SERVICE_PATH + "/password",
                                                            BASE_URI,
                                                            headers,
                                                            ("password=" + newPassword).getBytes(),
                                                            null,
                                                            environmentContext);

        assertEquals(response.getStatus(), BAD_REQUEST.getStatusCode());
        verify(userDao, never()).update(user.withPassword(newPassword));
    }

    @Test
    public void shouldFailUpdatePasswordWhichLessEightChar() throws Exception {
        final User user = createUser();
        final String newPassword = "abc123";
        final Map<String, List<String>> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, singletonList(MediaType.APPLICATION_FORM_URLENCODED));

        final ContainerResponse response = launcher.service(HttpMethod.POST,
                                                            SERVICE_PATH + "/password",
                                                            BASE_URI,
                                                            headers,
                                                            ("password=" + newPassword).getBytes(),
                                                            null,
                                                            environmentContext);

        assertEquals(response.getStatus(), BAD_REQUEST.getStatusCode());
        verify(userDao, never()).update(user.withPassword(newPassword));
    }

    @Test
    public void shouldBeAbleToRemoveUser() throws Exception {
        final User testUser = createUser();

        final ContainerResponse response = makeRequest(HttpMethod.DELETE, SERVICE_PATH + "/" + testUser.getId(), null);

        assertEquals(response.getStatus(), NO_CONTENT.getStatusCode());
        verify(userDao).remove(testUser.getId());
    }


    /**
     * Check we have a valid user which has the 'user' role
     */
    @Test
    public void checkUserWithDefaultScope() throws Exception {
        when(securityContext.isUserInRole("user")).thenReturn(true);

        final ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH + "/inrole?role=user", null);

        assertEquals(response.getStatus(), OK.getStatusCode());
        final UserInRoleDescriptor userInRoleDescriptor = (UserInRoleDescriptor)response.getEntity();

        assertNotNull(userInRoleDescriptor);
        assertEquals(userInRoleDescriptor.getIsInRole(), true);
        assertEquals(userInRoleDescriptor.getScope(), "system");
        assertEquals(userInRoleDescriptor.getScopeId(), "");
    }


    /**
     * Check we have a valid user which has the 'user' role with 'system' scope
     */
    @Test
    public void checkUserWithSystemScope() throws Exception {
        when(securityContext.isUserInRole("user")).thenReturn(true);

        final ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH + "/inrole?role=user&scope=system", null);

        assertEquals(response.getStatus(), OK.getStatusCode());
        final UserInRoleDescriptor userInRoleDescriptor = (UserInRoleDescriptor)response.getEntity();

        assertNotNull(userInRoleDescriptor);
        assertEquals(userInRoleDescriptor.getIsInRole(), true);
        assertEquals(userInRoleDescriptor.getScope(), "system");
        assertEquals(userInRoleDescriptor.getScopeId(), "");
    }


    /**
     * Check the current user has the temp_user role
     *
     * @throws Exception
     */
    @Test
    public void checkTempUserWithSystemScope() throws Exception {
        when(securityContext.isUserInRole("temp_user")).thenReturn(true);

        final ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH + "/inrole?role=temp_user&scope=system", null);

        assertEquals(response.getStatus(), OK.getStatusCode());
        final UserInRoleDescriptor userInRoleDescriptor = (UserInRoleDescriptor)response.getEntity();

        assertNotNull(userInRoleDescriptor);
        assertEquals(userInRoleDescriptor.getIsInRole(), true);
        assertEquals(userInRoleDescriptor.getScope(), "system");
        assertEquals(userInRoleDescriptor.getScopeId(), "");
    }

    /**
     * Check admin user is 'true' for isUserInRole' with admin role
     *
     * @throws Exception
     */
    @Test
    public void checkUserIsAdminWithDefaultScope() throws Exception {
        when(securityContext.isUserInRole("system/admin")).thenReturn(true);

        final ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH + "/inrole?role=admin", null);

        assertEquals(response.getStatus(), OK.getStatusCode());
        final UserInRoleDescriptor userInRoleDescriptor = (UserInRoleDescriptor)response.getEntity();

        assertNotNull(userInRoleDescriptor);
        assertEquals(userInRoleDescriptor.getIsInRole(), true);
        assertEquals(userInRoleDescriptor.getScope(), "system");
        assertEquals(userInRoleDescriptor.getScopeId(), "");
    }

    /**
     * Check admin user is 'false' for isUserInRole' with admin role
     *
     * @throws Exception
     */
    @Test
    public void checkUserIsNotAdmin() throws Exception {
        when(securityContext.isUserInRole("system/admin")).thenReturn(false);

        final ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH + "/inrole?role=admin", null);

        assertEquals(response.getStatus(), OK.getStatusCode());
        final UserInRoleDescriptor userInRoleDescriptor = (UserInRoleDescriptor)response.getEntity();

        assertNotNull(userInRoleDescriptor);
        assertEquals(userInRoleDescriptor.getIsInRole(), false);
        assertEquals(userInRoleDescriptor.getScope(), "system");
        assertEquals(userInRoleDescriptor.getScopeId(), "");
    }


    /**
     * Check admin user is 'true' for isUserInRole' with manager role
     *
     * @throws Exception
     */
    @Test
    public void checkUserIsManagerWithProvidedScope() throws Exception {
        when(securityContext.isUserInRole("system/manager")).thenReturn(true);

        final ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH + "/inrole?role=manager&scope=system", null);

        assertEquals(response.getStatus(), OK.getStatusCode());
        final UserInRoleDescriptor userInRoleDescriptor = (UserInRoleDescriptor)response.getEntity();

        assertNotNull(userInRoleDescriptor);
        assertEquals(userInRoleDescriptor.getIsInRole(), true);
        assertEquals(userInRoleDescriptor.getScope(), "system");
        assertEquals(userInRoleDescriptor.getScopeId(), "");
    }

    @Test
    public void shouldNotBeAbleToCreateUserWithoutEmailBySystemAdmin() throws Exception {
        final UserDescriptor newUser = DtoFactory.getInstance()
                                                 .createDto(UserDescriptor.class)
                                                 .withName("user")
                                                 .withPassword("password");
        when(securityContext.isUserInRole("system/admin")).thenReturn(true);

        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH + "/create", newUser);

        assertEquals(response.getStatus(), BAD_REQUEST.getStatusCode());
    }

    @Test
    public void shouldBeAbleToGetSettingOfUserService() throws Exception {
        final ContainerResponse response = makeRequest(HttpMethod.GET, SERVICE_PATH + "/settings", null);

        assertEquals(response.getStatus(), OK.getStatusCode());
        @SuppressWarnings("unchecked")
        final Map<String, String> settings = (Map<String, String>)response.getEntity();
        assertEquals(settings.size(), 1);
        assertEquals(settings.get(UserService.USER_SELF_CREATION_ALLOWED), "true");
    }

    private User createUser() throws NotFoundException, ServerException {
        final User testUser = new User().withId("test_id")
                                        .withEmail("test@email");
        when(userDao.getById(testUser.getId())).thenReturn(testUser);
        when(userDao.getByAlias(testUser.getEmail())).thenReturn(testUser);
        return testUser;
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

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

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.user.server.dao.User;
import org.eclipse.che.api.user.shared.dto.UserDescriptor;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.subject.Subject;
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
import javax.ws.rs.core.UriInfo;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;
import static javax.ws.rs.core.Response.Status.NO_CONTENT;
import static javax.ws.rs.core.Response.Status.OK;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

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
    TokenValidator     tokenValidator;
    @Mock
    UserNameValidator  userNameValidator;
    @Mock
    UriInfo            uriInfo;
    @Mock
    EnvironmentContext environmentContext;
    @Mock
    UserManager        userManager;

    UserService userService;

    ResourceLauncher launcher;

    @BeforeMethod
    public void setUp() throws Exception {
        ResourceBinderImpl resources = new ResourceBinderImpl();
        DependencySupplierImpl dependencies = new DependencySupplierImpl();
        dependencies.addComponent(UserManager.class, userManager);
        dependencies.addComponent(TokenValidator.class, tokenValidator);

        userService = new UserService(userManager, tokenValidator, userNameValidator, true);
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

        when(uriInfo.getBaseUriBuilder()).thenReturn(new UriBuilderImpl());

        org.eclipse.che.commons.env.EnvironmentContext.getCurrent().setSubject(new Subject() {

            @Override
            public String getUserName() {
                return user.getEmail();
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
                return user.getId();
            }

            @Override
            public boolean isTemporary() {
                return false;
            }
        });
    }

    @Test(enabled = false)
    //TODO Should be fixed within https://jira.codenvycorp.com/browse/CHE-1078
    public void shouldBeAbleToCreateNewUser() throws Exception {
        final User userByToken = new User().withEmail("test@email.com").withName("test");
        final String userEmail = "test@email.com";
        final String userName = "test";
        final String token = "test_token";
        when(tokenValidator.validateToken(token)).thenReturn(userByToken);

        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH + "/create?token=" + token, null);

        assertEquals(response.getStatus(), CREATED.getStatusCode());
        final UserDescriptor user = (UserDescriptor)response.getEntity();
        assertEquals(user.getEmail(), userEmail);
        assertEquals(user.getName(), userName);
        assertEquals(user.getPassword(), "<none>");
        verify(userManager).create(any(User.class), eq(false));
    }

    @Test(enabled = false)
    //TODO Should be fixed within https://jira.codenvycorp.com/browse/CHE-1078
    public void shouldBeAbleToCreateNewUserWithEmail() throws Exception {
        final String name = "name";
        final String email = "test_user@email.com";
        final UserDescriptor newUser = DtoFactory.getInstance()
                                                 .createDto(UserDescriptor.class)
                                                 .withName(name)
                                                 .withEmail(email);

        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH + "/create", newUser);

        assertEquals(response.getStatus(), CREATED.getStatusCode());
        final UserDescriptor descriptor = (UserDescriptor)response.getEntity();
        assertEquals(descriptor.getName(), name);
        assertEquals(descriptor.getEmail(), email);
    }

    @Test(enabled = false)
    //TODO Should be fixed within https://jira.codenvycorp.com/browse/CHE-1078
    public void shouldBeAbleToCreateNewUserWithUserDto() throws Exception {
        final UserDescriptor newUser = DtoFactory.getInstance()
                                                 .createDto(UserDescriptor.class)
                                                 .withName("test")
                                                 .withPassword("password123")
                                                 .withEmail("test@mail.com");

        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH + "/create", newUser);

        assertEquals(response.getStatus(), CREATED.getStatusCode());
        final UserDescriptor descriptor = (UserDescriptor)response.getEntity();
        assertEquals(descriptor.getName(), newUser.getName());
        assertEquals(descriptor.getPassword(), "<none>");
        verify(userManager).create(any(User.class), eq(false));
    }

    @Test
    public void shouldThrowBadRequestExceptionWhenCreatingUserWithInvalidUsername() throws Exception {
        final UserDescriptor newUser = DtoFactory.getInstance()
                                                 .createDto(UserDescriptor.class)
                                                 .withName("test-123@gmail.com")
                                                 .withPassword("password");

        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH + "/create", newUser);

        assertEquals(response.getStatus(), BAD_REQUEST.getStatusCode());
        verify(userManager, never()).create(any(User.class), anyBoolean());
    }

    @Test
    public void shouldThrowForbiddenExceptionWhenCreatingUserWithInvalidPassword() throws Exception {
        final UserDescriptor newUser = DtoFactory.getInstance()
                                                 .createDto(UserDescriptor.class)
                                                 .withName("test")
                                                 .withPassword("password");

        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH + "/create", newUser);

        assertEquals(response.getStatus(), BAD_REQUEST.getStatusCode());
        verify(userManager, never()).create(any(User.class), anyBoolean());
    }

    @Test
    public void shouldThrowForbiddenExceptionWhenCreatingUserBasedOnEntityWhichIsNull() throws Exception {

        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH + "/create", null);

        assertEquals(response.getStatus(), BAD_REQUEST.getStatusCode());
        verify(userManager, never()).create(any(User.class), anyBoolean());
    }

    @Test
    public void shouldThrowForbiddenExceptionWhenCreatingUserBasedOnEntityWhichContainsNullEmail() throws Exception {
        final UserDescriptor newUser = DtoFactory.getInstance().createDto(UserDescriptor.class);

        final ContainerResponse response = makeRequest(HttpMethod.POST, SERVICE_PATH + "/create", newUser);

        assertEquals(response.getStatus(), BAD_REQUEST.getStatusCode());
        verify(userManager, never()).create(any(User.class), anyBoolean());
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
        verify(userManager).update(user.withPassword(newPassword));
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
        verify(userManager, never()).update(user.withPassword(newPassword));
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
        verify(userManager, never()).update(user.withPassword(newPassword));
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
        verify(userManager, never()).update(user.withPassword(newPassword));
    }

    @Test
    public void shouldBeAbleToRemoveUser() throws Exception {
        final User testUser = createUser();

        final ContainerResponse response = makeRequest(HttpMethod.DELETE, SERVICE_PATH + "/" + testUser.getId(), null);

        assertEquals(response.getStatus(), NO_CONTENT.getStatusCode());
        verify(userManager).remove(testUser.getId());
    }

    @Test
    public void shouldNotBeAbleToCreateUserWithoutEmailBySystemAdmin() throws Exception {
        final UserDescriptor newUser = DtoFactory.getInstance()
                                                 .createDto(UserDescriptor.class)
                                                 .withName("user")
                                                 .withPassword("password");

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
        when(userManager.getById(testUser.getId())).thenReturn(testUser);
        when(userManager.getByAlias(testUser.getEmail())).thenReturn(testUser);
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

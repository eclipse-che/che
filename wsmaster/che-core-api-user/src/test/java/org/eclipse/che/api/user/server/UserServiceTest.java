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

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jayway.restassured.response.Response;

import org.eclipse.che.account.api.AccountManager;
import org.eclipse.che.account.spi.AccountValidator;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.model.user.User;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.user.server.model.impl.UserImpl;
import org.eclipse.che.api.user.shared.dto.UserDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Map;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Collections.emptyList;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.testng.Assert.assertEquals;

/**
 * Tests for {@link UserService}
 *
 * @author Eugene Veovodin
 * @author Max Shaposhnik
 */
@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class UserServiceTest {
    @SuppressWarnings("unused")
    private static final ApiExceptionMapper MAPPER  = new ApiExceptionMapper();
    @SuppressWarnings("unused")
    private static final EnvironmentFilter  FILTER  = new EnvironmentFilter();
    private static final Subject            SUBJECT = new SubjectImpl("user", "user123", "token", false);

    @Mock(answer = Answers.RETURNS_MOCKS)
    private UserManager          userManager;
    @Mock
    private AccountManager       accountManager;
    @Mock
    private TokenValidator       tokenValidator;
    @Mock
    private UserLinksInjector    linksInjector;
    private UserValidator        userValidator;
    @Captor
    private ArgumentCaptor<User> userCaptor;
    private UserService          userService;

    @BeforeMethod
    public void initService() {
        initMocks(this);

        userValidator = new UserValidator(new AccountValidator(accountManager));

        // Return the incoming instance when injectLinks is called
        when(linksInjector.injectLinks(any(), any())).thenAnswer(inv -> inv.getArguments()[0]);

        userService = new UserService(userManager, tokenValidator, userValidator, linksInjector, true);
    }

    @Test
    public void shouldCreateUserFromToken() throws Exception {
        when(tokenValidator.validateToken("token_value")).thenReturn(new UserImpl("id", "test@eclipse.org", "test"));

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .contentType("application/json")
                                         .post(SECURE_PATH + "/user?token=token_value");

        assertEquals(response.statusCode(), 201);
        verify(userManager).create(userCaptor.capture(), anyBoolean());
        final User user = userCaptor.getValue();
        assertEquals(user.getEmail(), "test@eclipse.org");
        assertEquals(user.getName(), "test");
    }

    @Test
    public void shouldCreateUserFromEntity() throws Exception {
        final UserDto newUser = newDto(UserDto.class).withName("test")
                                                     .withEmail("test@codenvy.com")
                                                     .withPassword("password12345");
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .body(newUser)
                                         .contentType("application/json")
                                         .post(SECURE_PATH + "/user");

        assertEquals(response.statusCode(), 201);
        verify(userManager).create(userCaptor.capture(), anyBoolean());
        final User user = userCaptor.getValue();
        assertEquals(user.getEmail(), "test@codenvy.com");
        assertEquals(user.getName(), "test");
        assertEquals(user.getPassword(), "password12345");
    }

    @Test
    public void shouldNotCreateUserFromEntityWhenPasswordIsNotValid() throws Exception {
        final UserDto newUser = newDto(UserDto.class).withName("test")
                                                     .withEmail("test@codenvy.com")
                                                     .withPassword("1");
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .body(newUser)
                                         .contentType("application/json")
                                         .post(SECURE_PATH + "/user");

        assertEquals(response.statusCode(), 400);
        assertEquals(unwrapError(response), "Password should contain at least 8 characters");
    }

    @Test
    public void shouldNotCreateUserIfTokenIsNotValid() throws Exception {
        when(tokenValidator.validateToken("token_value")).thenThrow(new ConflictException("error"));

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .contentType("application/json")
                                         .post(SECURE_PATH + "/user?token=token_value");

        assertEquals(response.statusCode(), 409);
        assertEquals(unwrapError(response), "error");
    }

    @Test
    public void shouldNotCreateUserFromEntityIfEntityIsNull() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .contentType("application/json")
                                         .post(SECURE_PATH + "/user");

        assertEquals(response.statusCode(), 400);
        assertEquals(unwrapError(response), "User required");
    }

    @Test
    public void shouldNotCreateUserFromEntityIfEmailIsNull() throws Exception {
        final UserDto newUser = newDto(UserDto.class).withName("test")
                                                     .withPassword("password12345");
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .body(newUser)
                                         .contentType("application/json")
                                         .post(SECURE_PATH + "/user");

        assertEquals(response.statusCode(), 400);
        assertEquals(unwrapError(response), "User email required");
    }

    @Test
    public void shouldNotCreateUserFromEntityIfNameIsNull() throws Exception {
        final UserDto newUser = newDto(UserDto.class).withEmail("test@codenvy.com")
                                                     .withPassword("password12345");
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .body(newUser)
                                         .contentType("application/json")
                                         .post(SECURE_PATH + "/user");

        assertEquals(response.statusCode(), 400);
        assertEquals(unwrapError(response), "User name required");
    }

    @Test
    public void shouldNotCreateUserFromEntityIfPasswordIsNotValid() throws Exception {
        final UserDto newUser = newDto(UserDto.class).withEmail("test@codenvy.com")
                                                     .withName("test")
                                                     .withPassword("1");
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .body(newUser)
                                         .contentType("application/json")
                                         .post(SECURE_PATH + "/user");

        assertEquals(response.statusCode(), 400);
        assertEquals(unwrapError(response), "Password should contain at least 8 characters");
    }

    @Test
    public void shouldBeAbleToGetCurrentUser() throws Exception {
        when(userManager.getById(SUBJECT.getUserId())).thenReturn(copySubject());

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/user");

        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void shouldBeAbleToGetUserById() throws Exception {
        final UserImpl testUser = copySubject();
        when(userManager.getById(SUBJECT.getUserId())).thenReturn(testUser);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/user/" + SUBJECT.getUserId());

        assertEquals(response.getStatusCode(), 200);
        final UserDto fetchedUser = unwrapDto(response, UserDto.class);
        assertEquals(fetchedUser.getId(), testUser.getId());
        assertEquals(fetchedUser.getName(), testUser.getName());
        assertEquals(fetchedUser.getEmail(), testUser.getEmail());
    }

    @Test
    public void shouldBeAbleToFindUserByEmail() throws Exception {
        final UserImpl testUser = copySubject();
        when(userManager.getByEmail(testUser.getEmail())).thenReturn(testUser);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/user/find?email=" + testUser.getEmail());

        assertEquals(response.getStatusCode(), 200);
        final UserDto fetchedUser = unwrapDto(response, UserDto.class);
        assertEquals(fetchedUser.getId(), testUser.getId());
        assertEquals(fetchedUser.getName(), testUser.getName());
        assertEquals(fetchedUser.getEmail(), testUser.getEmail());
    }

    @Test
    public void shouldBeAbleToFindUserByName() throws Exception {
        final UserImpl testUser = copySubject();
        when(userManager.getByName(testUser.getName())).thenReturn(testUser);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/user/find?name=" + testUser.getName());

        assertEquals(response.getStatusCode(), 200);
        final UserDto fetchedUser = unwrapDto(response, UserDto.class);
        assertEquals(fetchedUser.getId(), testUser.getId());
        assertEquals(fetchedUser.getName(), testUser.getName());
        assertEquals(fetchedUser.getEmail(), testUser.getEmail());
    }

    @Test
    public void shouldNotFindUserByNameOrEmailWhenBothSpecified() throws Exception {
        final UserImpl testUser = copySubject();
        when(userManager.getByName(testUser.getName())).thenReturn(testUser);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/user/find?" +
                                              "name=" + testUser.getName() +
                                              "&email=" + testUser.getEmail());

        assertEquals(response.getStatusCode(), 400);
        assertEquals(unwrapError(response), "Expected either user's email or name, while both values received");
    }

    @Test
    public void shouldNotFindUserByNameOrEmailWhenBothAreEmpty() throws Exception {
        final UserImpl testUser = copySubject();
        when(userManager.getByName(testUser.getName())).thenReturn(testUser);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/user/find");

        assertEquals(response.getStatusCode(), 400);
        assertEquals(unwrapError(response), "Missed user's email or name");
    }

    @Test
    public void shouldUpdatePassword() throws Exception {
        final UserImpl testUser = copySubject();
        when(userManager.getById(testUser.getId())).thenReturn(testUser);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/x-www-form-urlencoded")
                                         .body("password=password12345")
                                         .when()
                                         .post(SECURE_PATH + "/user/password");

        verify(userManager).update(userCaptor.capture());
        final User fetchedUser = userCaptor.getValue();
        assertEquals(fetchedUser.getPassword(), "password12345");
    }

    @Test
    public void shouldNotUpdatePasswordIfPasswordContainsOnlyDigits() throws Exception {
        final UserImpl testUser = copySubject();
        when(userManager.getById(testUser.getId())).thenReturn(testUser);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/x-www-form-urlencoded")
                                         .body("password=1234567890")
                                         .when()
                                         .post(SECURE_PATH + "/user/password");

        assertEquals(response.getStatusCode(), 400);
        assertEquals(unwrapError(response), "Password should contain letters and digits");
    }

    @Test
    public void shouldNotUpdatePasswordIfPasswordContainsLessThan8Chars() throws Exception {
        final UserImpl testUser = copySubject();
        when(userManager.getById(testUser.getId())).thenReturn(testUser);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/x-www-form-urlencoded")
                                         .body("password=0xf")
                                         .when()
                                         .post(SECURE_PATH + "/user/password");

        assertEquals(response.getStatusCode(), 400);
        assertEquals(unwrapError(response), "Password should contain at least 8 characters");
    }

    @Test
    public void shouldNotUpdatePasswordIfPasswordIsNull() throws Exception {
        final UserImpl testUser = copySubject();
        when(userManager.getById(testUser.getId())).thenReturn(testUser);

        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .contentType("application/x-www-form-urlencoded")
                                         .when()
                                         .post(SECURE_PATH + "/user/password");

        assertEquals(response.getStatusCode(), 400);
        assertEquals(unwrapError(response), "Password required");
    }

    @Test
    public void shouldRemoveUser() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .delete(SECURE_PATH + "/user/" + SUBJECT.getUserId());

        assertEquals(response.getStatusCode(), 204);
        verify(userManager).remove(SUBJECT.getUserId());
    }

    @Test
    public void shouldBeAbleToGetSettings() throws Exception {
        final Response response = given().auth()
                                         .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
                                         .when()
                                         .get(SECURE_PATH + "/user/settings");

        assertEquals(response.getStatusCode(), 200);
        final Map<String, String> settings = new Gson().fromJson(response.print(),
                                                                 new TypeToken<Map<String, String>>() {}.getType());
        assertEquals(settings, ImmutableMap.of("che.auth.user_self_creation", "true"));
    }

    @Filter
    public static class EnvironmentFilter implements RequestFilter {

        public void doFilter(GenericContainerRequest request) {
            EnvironmentContext.getCurrent().setSubject(SUBJECT);
        }
    }

    private static <T> T unwrapDto(Response response, Class<T> dtoClass) {
        return DtoFactory.getInstance().createDtoFromJson(response.body().print(), dtoClass);
    }

    private static String unwrapError(Response response) {
        return unwrapDto(response, ServiceError.class).getMessage();
    }

    private static UserImpl copySubject() {
        return new UserImpl(SUBJECT.getUserId(),
                            SUBJECT.getUserName() + "@codenvy.com",
                            SUBJECT.getUserName(),
                            null,
                            emptyList());
    }
}

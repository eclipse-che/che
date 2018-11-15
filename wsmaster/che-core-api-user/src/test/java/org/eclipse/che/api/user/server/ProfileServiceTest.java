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
package org.eclipse.che.api.user.server;

import static com.jayway.restassured.RestAssured.given;
import static java.util.Arrays.asList;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.jayway.restassured.response.Response;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.user.Profile;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.user.server.model.impl.ProfileImpl;
import org.eclipse.che.api.user.shared.dto.ProfileDto;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link ProfileService}.
 *
 * @author Yevhenii Voevodin
 */
@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class ProfileServiceTest {

  @SuppressWarnings("unused")
  private static final ApiExceptionMapper MAPPER = new ApiExceptionMapper();

  @SuppressWarnings("unused")
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  private static final Subject SUBJECT = new SubjectImpl("user", "user123", "token", false);

  @Mock(answer = Answers.RETURNS_MOCKS)
  private ProfileManager profileManager;

  @Mock private ProfileLinksInjector linksInjector;

  @Mock(answer = Answers.RETURNS_MOCKS)
  private UserManager userManager;

  @Captor private ArgumentCaptor<Profile> profileCaptor;

  @InjectMocks private ProfileService profileService;

  @BeforeMethod
  public void setUp() throws NotFoundException, ServerException {
    lenient()
        .when(linksInjector.injectLinks(any(), any()))
        .thenAnswer(inv -> inv.getArguments()[0]);

    lenient()
        .when(profileManager.getById(SUBJECT.getUserId()))
        .thenReturn(new ProfileImpl(SUBJECT.getUserId()));
  }

  @Test
  public void shouldGetCurrentProfile() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/profile");

    assertEquals(response.getStatusCode(), 200);
    final ProfileDto profileDto = unwrapDto(response, ProfileDto.class);
    assertEquals(profileDto.getUserId(), SUBJECT.getUserId());
  }

  @Test
  public void shouldGetProfileById() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/profile/" + SUBJECT.getUserId());

    assertEquals(response.getStatusCode(), 200);
    final ProfileDto profileDto = unwrapDto(response, ProfileDto.class);
    assertEquals(profileDto.getUserId(), SUBJECT.getUserId());
  }

  @Test
  public void shouldBeAbleToUpdateCurrentProfileAttributes() throws Exception {
    final ImmutableMap<String, String> attributes =
        ImmutableMap.of(
            "attr1", "value1",
            "attr2", "value2",
            "attr3", "value3");
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .contentType("application/json")
            .body(attributes)
            .put(SECURE_PATH + "/profile/attributes");

    assertEquals(response.getStatusCode(), 200);
    verify(profileManager).update(profileCaptor.capture());
    final Profile profile = profileCaptor.getValue();
    assertEquals(profile.getAttributes(), attributes);
  }

  @Test
  public void shouldNotUpdateCurrentProfileAttributesIfNothingSent() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .contentType("application/json")
            .put(SECURE_PATH + "/profile/attributes");

    assertEquals(response.getStatusCode(), 400);
  }

  @Test
  public void shouldBeAbleToUpdateAttributesOfSpecifiedProfile() throws Exception {
    final ImmutableMap<String, String> attributes =
        ImmutableMap.of(
            "attr1", "value1",
            "attr2", "value2",
            "attr3", "value3");
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .contentType("application/json")
            .body(attributes)
            .put(SECURE_PATH + "/profile/" + SUBJECT.getUserId() + "/attributes/");

    assertEquals(response.getStatusCode(), 200);
    verify(profileManager).update(profileCaptor.capture());
    final Profile profile = profileCaptor.getValue();
    assertEquals(profile.getAttributes(), attributes);
  }

  @Test
  public void shouldNotUpdateSpecifiedProfileAttributesIfNothingSent() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .contentType("application/json")
            .put(SECURE_PATH + "/profile/" + SUBJECT.getUserId() + "/attributes/");

    assertEquals(response.getStatusCode(), 400);
  }

  @Test
  public void shouldBeAbleToRemoveSpecifiedAttributes() throws Exception {
    when(profileManager.getById(SUBJECT.getUserId()))
        .thenReturn(
            new ProfileImpl(
                SUBJECT.getUserId(),
                ImmutableMap.of(
                    "attr1", "value1",
                    "attr2", "value2",
                    "attr3", "value3")));
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .contentType("application/json")
            .body(asList("attr1", "attr3"))
            .delete(SECURE_PATH + "/profile/attributes");

    assertEquals(response.getStatusCode(), 204);
    verify(profileManager).update(profileCaptor.capture());
    final Profile profile = profileCaptor.getValue();
    assertEquals(profile.getAttributes(), ImmutableMap.of("attr2", "value2"));
  }

  @Test
  public void shouldRemoveAllAttributeIfNoSpecified() throws Exception {
    when(profileManager.getById(SUBJECT.getUserId()))
        .thenReturn(
            new ProfileImpl(
                SUBJECT.getUserId(),
                ImmutableMap.of(
                    "attr1", "value1",
                    "attr2", "value2",
                    "attr3", "value3")));
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .contentType("application/json")
            .delete(SECURE_PATH + "/profile/attributes");

    assertEquals(response.getStatusCode(), 204);
    verify(profileManager).update(profileCaptor.capture());
    final Profile profile = profileCaptor.getValue();
    assertTrue(profile.getAttributes().isEmpty());
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
}

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
package org.eclipse.che.api.logger;

import static com.jayway.restassured.RestAssured.given;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

import com.jayway.restassured.response.Response;
import java.util.List;
import org.eclipse.che.api.core.rest.ApiExceptionMapper;
import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.logger.shared.dto.LoggerDto;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.eclipse.che.dto.server.DtoFactory;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Tests for {@link org.eclipse.che.api.logger.LoggerService}.
 *
 * @author Florent Benoit
 */
@Listeners(value = {EverrestJetty.class, MockitoTestNGListener.class})
public class LoggerServiceTest {

  @SuppressWarnings("unused")
  private static final ApiExceptionMapper MAPPER = new ApiExceptionMapper();

  private static final String NAMESPACE = "user";
  private static final String USER_ID = "user123";
  private static final String LOGGER_NAME = "org.eclipse.che.api-sample-logger";

  @SuppressWarnings("unused")
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  private LoggerService loggerService;

  @BeforeMethod
  public void setup() {
    loggerService = new LoggerService();
  }

  @Test
  public void shouldGetLogger() {

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/logger/ROOT");

    assertEquals(response.getStatusCode(), 200);
    LoggerDto remoteLoggerDto =
        DtoFactory.getInstance().createDtoFromJson(response.body().print(), LoggerDto.class);
    assertNotNull(remoteLoggerDto);
    assertEquals(remoteLoggerDto.getName(), "ROOT");
    assertEquals(remoteLoggerDto.getLevel(), "INFO");
  }

  @Test
  public void shouldGetNotFoundExceptionOnUnknownLogger() {

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/logger/unknown");

    assertEquals(response.getStatusCode(), 404);
    assertEquals(
        DtoFactory.getInstance()
            .createDtoFromJson(response.body().print(), ServiceError.class)
            .getMessage(),
        "The logger with name unknown is not existing.");
  }

  @Test
  public void shouldGetLoggers() throws Exception {

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/logger");

    assertEquals(response.getStatusCode(), 200);
    List<LoggerDto> loggers = unwrapDtoList(response, LoggerDto.class);
    LoggerDto rootLoggerDto = newDto(LoggerDto.class).withName("ROOT").withLevel("INFO");
    assertTrue(loggers.contains(rootLoggerDto));
  }

  @Test
  public void shouldCreateLogger() throws Exception {

    LoggerDto toCreateLoggerDto = newDto(LoggerDto.class).withName(LOGGER_NAME).withLevel("INFO");

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(toCreateLoggerDto)
            .when()
            .post(SECURE_PATH + "/logger/" + LOGGER_NAME);

    assertEquals(response.getStatusCode(), 200);

    List<LoggerDto> loggers = this.loggerService.getLoggers(0, 30);
    assertTrue(loggers.contains(toCreateLoggerDto));
  }

  @Test(dependsOnMethods = "shouldCreateLogger")
  public void shouldUpdateLogger() throws Exception {

    LoggerDto toUpdateLoggerDto = newDto(LoggerDto.class).withName(LOGGER_NAME).withLevel("DEBUG");

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType("application/json")
            .body(toUpdateLoggerDto)
            .when()
            .put(SECURE_PATH + "/logger/" + LOGGER_NAME);

    assertEquals(response.getStatusCode(), 200);

    List<LoggerDto> loggers = this.loggerService.getLoggers(0, 30);
    assertTrue(loggers.contains(toUpdateLoggerDto));
  }

  @Test(dependsOnMethods = "shouldCreateLogger")
  public void shouldGetLoggerPaginateSkip() {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/logger/?skipCount=100");

    assertEquals(response.getStatusCode(), 200);
    List<LoggerDto> loggers = unwrapDtoList(response, LoggerDto.class);
    assertEquals(loggers.size(), 0);
  }

  @Test(dependsOnMethods = "shouldCreateLogger")
  public void shouldGetLoggerPaginateLimit() {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/logger/?maxItems=1");

    assertEquals(response.getStatusCode(), 200);
    List<LoggerDto> loggers = unwrapDtoList(response, LoggerDto.class);
    assertEquals(loggers.size(), 1);
  }

  private static <T> List<T> unwrapDtoList(Response response, Class<T> dtoClass) {
    return DtoFactory.getInstance().createListDtoFromJson(response.body().print(), dtoClass);
  }

  @Filter
  public static class EnvironmentFilter implements RequestFilter {
    @Override
    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext.getCurrent()
          .setSubject(new SubjectImpl(NAMESPACE, USER_ID, "token", false));
    }
  }
}

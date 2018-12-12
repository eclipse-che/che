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
package org.eclipse.che.api.devfile.server;

import static com.jayway.restassured.RestAssured.given;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_NAME;
import static org.everrest.assured.JettyHttpServer.ADMIN_USER_PASSWORD;
import static org.everrest.assured.JettyHttpServer.SECURE_PATH;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jayway.restassured.http.ContentType;
import com.jayway.restassured.response.Response;
import java.io.IOException;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.workspace.server.WorkspaceLinksGenerator;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.everrest.assured.EverrestJetty;
import org.everrest.core.Filter;
import org.everrest.core.GenericContainerRequest;
import org.everrest.core.RequestFilter;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

@Listeners({EverrestJetty.class, MockitoTestNGListener.class})
public class DevfileServiceTest {

  @Mock private WorkspaceLinksGenerator linksGenerator;

  @Mock private WorkspaceManager workspaceManager;
  @Mock private EnvironmentContext environmentContext;
  @Mock private DevfileIntegrityValidator integrityValidator;
  private DevfileSchemaProvider schemaProvider = new DevfileSchemaProvider();
  private DevfileSchemaValidator validator;

  @SuppressWarnings("unused")
  private static final EnvironmentFilter FILTER = new EnvironmentFilter();

  private static final Subject SUBJECT = new SubjectImpl("user", "user123", "token", false);

  @SuppressWarnings("unused")
  private DevfileService devFileService;

  @BeforeMethod
  public void initService() {
    this.validator = spy(new DevfileSchemaValidator(schemaProvider));
    this.devFileService =
        new DevfileService(
            linksGenerator, validator, integrityValidator, schemaProvider, workspaceManager);
  }

  @Test
  public void shouldRetrieveSchema() throws Exception {
    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/devfile");

    assertEquals(response.getStatusCode(), 200);
    assertEquals(response.getBody().asString(), schemaProvider.getSchemaContent());
  }

  @Test
  public void shouldAcceptDevFileAndFindAvailableName() throws Exception {
    ArgumentCaptor<WorkspaceConfigImpl> captor = ArgumentCaptor.forClass(WorkspaceConfigImpl.class);
    EnvironmentContext.setCurrent(environmentContext);
    WorkspaceImpl ws = mock(WorkspaceImpl.class);
    when(workspaceManager.createWorkspace(any(), eq(SUBJECT.getUserName()), anyMap()))
        .thenReturn(createWorkspace(WorkspaceStatus.STOPPED));
    String yamlContent =
        Files.readFile(getClass().getClassLoader().getResourceAsStream("devfile.yaml"));
    when(workspaceManager.getWorkspace(anyString(), anyString()))
        .thenAnswer(
            invocation -> {
              String wsname = invocation.getArgument(0);
              if (wsname.equals("petclinic-dev-environment")
                  || wsname.equals("petclinic-dev-environment_1")) {
                return ws;
              }
              throw new NotFoundException("ws not found");
            });

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .contentType(ContentType.JSON)
            .body(yamlContent)
            .when()
            .post(SECURE_PATH + "/devfile");

    assertEquals(response.getStatusCode(), 201);
    verify(validator).validateBySchema(eq(yamlContent), eq(false));
    verify(workspaceManager).createWorkspace(captor.capture(), eq(SUBJECT.getUserName()), anyMap());
    assertEquals("petclinic-dev-environment_2", captor.getValue().getName());
  }

  @Test
  public void shouldCreateDevFileFromWorkspace() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper(new YAMLFactory());
    when(workspaceManager.getWorkspace(anyString()))
        .thenReturn(createWorkspace(WorkspaceStatus.STOPPED));

    final Response response =
        given()
            .auth()
            .basic(ADMIN_USER_NAME, ADMIN_USER_PASSWORD)
            .when()
            .get(SECURE_PATH + "/devfile/ws123456");

    assertEquals(response.getStatusCode(), 200);
    Devfile devFile = objectMapper.readValue(response.getBody().asString(), Devfile.class);
    assertNotNull(devFile);
  }

  private WorkspaceImpl createWorkspace(WorkspaceStatus status)
      throws IOException, JsonParseException {
    return WorkspaceImpl.builder()
        .setConfig(createConfig())
        .generateId()
        .setAccount(new AccountImpl("anyId", SUBJECT.getUserName(), "test"))
        .setStatus(status)
        .build();
  }

  private WorkspaceConfig createConfig() throws IOException, JsonParseException {
    String jsonContent =
        Files.readFile(getClass().getClassLoader().getResourceAsStream("workspace_config.json"));
    return JsonHelper.fromJson(jsonContent, WorkspaceConfigImpl.class, null);
  }

  @Filter
  public static class EnvironmentFilter implements RequestFilter {
    @Override
    public void doFilter(GenericContainerRequest request) {
      EnvironmentContext.getCurrent().setSubject(SUBJECT);
    }
  }
}

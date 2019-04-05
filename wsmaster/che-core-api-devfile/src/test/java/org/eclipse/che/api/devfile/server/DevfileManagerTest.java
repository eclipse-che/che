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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.devfile.server.convert.DevfileConverter;
import org.eclipse.che.api.devfile.server.exception.DevfileFormatException;
import org.eclipse.che.api.devfile.server.validator.DevfileIntegrityValidator;
import org.eclipse.che.api.devfile.server.validator.DevfileSchemaValidator;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ActionImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.CommandImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.ComponentImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.DevfileImpl;
import org.eclipse.che.api.workspace.server.model.impl.devfile.EndpointImpl;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

@Listeners(MockitoTestNGListener.class)
public class DevfileManagerTest {

  private static final Subject TEST_SUBJECT = new SubjectImpl("name", "id", "token", false);
  private static final String DEVFILE_YAML_CONTENT = "devfile yaml stub";

  @Mock private DevfileSchemaValidator schemaValidator;
  @Mock private DevfileIntegrityValidator integrityValidator;
  @Mock private DevfileConverter devfileConverter;
  @Mock private WorkspaceManager workspaceManager;
  @Mock private ObjectMapper objectMapper;

  @Mock private JsonNode devfileJsonNode;
  private DevfileImpl devfile;

  @InjectMocks private DevfileManager devfileManager;

  @BeforeMethod
  public void setUp() throws Exception {
    devfile = new DevfileImpl();

    lenient().when(schemaValidator.validateBySchema(any())).thenReturn(devfileJsonNode);
    lenient().when(objectMapper.treeToValue(any(), eq(DevfileImpl.class))).thenReturn(devfile);
  }

  @Test
  public void testValidateAndParse() throws Exception {
    // when
    DevfileImpl parsed = devfileManager.parse(DEVFILE_YAML_CONTENT);

    // then
    assertEquals(parsed, devfile);
    verify(schemaValidator).validateBySchema(DEVFILE_YAML_CONTENT);
    verify(objectMapper).treeToValue(devfileJsonNode, DevfileImpl.class);
    verify(integrityValidator).validateDevfile(devfile);
  }

  @Test
  public void testInitializingDevfileMapsAfterParsing() throws Exception {
    // given
    CommandImpl command = new CommandImpl();
    command.getActions().add(new ActionImpl());
    devfile.getCommands().add(command);

    ComponentImpl component = new ComponentImpl();
    component.getEndpoints().add(new EndpointImpl());
    devfile.getComponents().add(component);

    // when
    DevfileImpl parsed = devfileManager.parse(DEVFILE_YAML_CONTENT);

    // then
    assertNotNull(parsed.getCommands().get(0).getAttributes());
    assertNotNull(parsed.getComponents().get(0).getSelector());
    assertNotNull(parsed.getComponents().get(0).getEndpoints().get(0).getAttributes());
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "non valid")
  public void shouldThrowExceptionWhenExceptionOccurredDuringSchemaValidation() throws Exception {
    // given
    doThrow(new DevfileFormatException("non valid")).when(schemaValidator).validateBySchema(any());

    // when
    devfileManager.parse(DEVFILE_YAML_CONTENT);
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "non valid")
  public void shouldThrowExceptionWhenErrorOccurredDuringDevfileParsing() throws Exception {
    // given
    JsonProcessingException jsonException = mock(JsonProcessingException.class);
    when(jsonException.getMessage()).thenReturn("non valid");
    doThrow(jsonException).when(objectMapper).treeToValue(any(), any());

    // when
    devfileManager.parse(DEVFILE_YAML_CONTENT);
  }

  @Test
  public void shouldFindAvailableNameAndCreateWorkspace() throws Exception {
    // given
    ArgumentCaptor<WorkspaceConfigImpl> captor = ArgumentCaptor.forClass(WorkspaceConfigImpl.class);

    EnvironmentContext current = new EnvironmentContext();
    current.setSubject(TEST_SUBJECT);
    EnvironmentContext.setCurrent(current);

    when(workspaceManager.createWorkspace(any(WorkspaceConfig.class), anyString(), anyMap()))
        .thenReturn(createWorkspace(WorkspaceStatus.STOPPED));
    when(workspaceManager.getWorkspace(anyString(), anyString()))
        .thenAnswer(
            invocation -> {
              String wsname = invocation.getArgument(0);
              if ("petclinic-dev-environment".equals(wsname)
                  || "petclinic-dev-environment_1".equals(wsname)) {
                return mock(WorkspaceImpl.class);
              }
              throw new NotFoundException("ws not found");
            });
    WorkspaceConfigImpl wsConfig = mock(WorkspaceConfigImpl.class);
    when(wsConfig.getName()).thenReturn("petclinic-dev-environment");
    doReturn(new WorkspaceConfigImpl(wsConfig))
        .when(devfileConverter)
        .devFileToWorkspaceConfig(any(), any());
    FileContentProvider fileContentProvider = mock(FileContentProvider.class);

    // when
    devfileManager.createWorkspace(devfile, fileContentProvider);

    // then
    verify(workspaceManager).createWorkspace(captor.capture(), anyString(), anyMap());
    assertEquals("petclinic-dev-environment_2", captor.getValue().getName());
    verify(devfileConverter).devFileToWorkspaceConfig(eq(devfile), any());
  }

  private WorkspaceImpl createWorkspace(WorkspaceStatus status)
      throws IOException, JsonParseException {
    return WorkspaceImpl.builder()
        .generateId()
        .setConfig(createConfig())
        .setAccount(new AccountImpl("anyId", TEST_SUBJECT.getUserName(), "test"))
        .setStatus(status)
        .build();
  }

  private WorkspaceConfigImpl createConfig() throws IOException, JsonParseException {
    String jsonContent =
        Files.readFile(getClass().getClassLoader().getResourceAsStream("workspace_config.json"));
    return JsonHelper.fromJson(jsonContent, WorkspaceConfigImpl.class, null);
  }
}

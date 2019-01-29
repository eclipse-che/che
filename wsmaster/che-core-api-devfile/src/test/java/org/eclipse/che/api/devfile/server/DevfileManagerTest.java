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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertEquals;

import java.io.IOException;
import org.eclipse.che.account.spi.AccountImpl;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.model.workspace.WorkspaceStatus;
import org.eclipse.che.api.devfile.model.Devfile;
import org.eclipse.che.api.devfile.server.schema.DevfileSchemaProvider;
import org.eclipse.che.api.devfile.server.validator.DevfileIntegrityValidator;
import org.eclipse.che.api.devfile.server.validator.DevfileSchemaValidator;
import org.eclipse.che.api.workspace.server.WorkspaceManager;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigImpl;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceImpl;
import org.eclipse.che.commons.env.EnvironmentContext;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.commons.json.JsonParseException;
import org.eclipse.che.commons.subject.Subject;
import org.eclipse.che.commons.subject.SubjectImpl;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.testng.reporters.Files;

@Listeners(MockitoTestNGListener.class)
public class DevfileManagerTest {

  private DevfileSchemaValidator schemaValidator;
  private DevfileIntegrityValidator integrityValidator;
  private DevfileConverter devfileConverter;
  @Mock private WorkspaceManager workspaceManager;
  @Mock private DevfileEnvironmentFactory devfileEnvironmentFactory;

  private static final Subject TEST_SUBJECT = new SubjectImpl("name", "id", "token", false);

  private DevfileManager devfileManager;

  @BeforeMethod
  public void setUp() throws Exception {
    schemaValidator = spy(new DevfileSchemaValidator(new DevfileSchemaProvider()));
    integrityValidator = spy(new DevfileIntegrityValidator());
    devfileConverter = spy(new DevfileConverter(devfileEnvironmentFactory));
    devfileManager =
        new DevfileManager(schemaValidator, integrityValidator, devfileConverter, workspaceManager);
  }

  @Test
  public void testValidateAndConvert() throws Exception {
    String yamlContent =
        Files.readFile(getClass().getClassLoader().getResourceAsStream("devfile.yaml"));
    devfileManager.parse(yamlContent, true);
    verify(schemaValidator).validateBySchema(eq(yamlContent), eq(true));
    verify(integrityValidator).validateDevfile(any(Devfile.class));
  }

  @Test(
      expectedExceptions = DevfileFormatException.class,
      expectedExceptionsMessageRegExp = "Devfile schema validation failed. Errors: [\\w\\W]+")
  public void shouldThrowExceptionWhenUnconvertableContentProvided() throws Exception {
    String yamlContent =
        Files.readFile(getClass().getClassLoader().getResourceAsStream("devfile.yaml"))
            .concat("foos:");
    devfileManager.parse(yamlContent, true);
    verify(schemaValidator).validateBySchema(eq(yamlContent), eq(true));
    verifyNoMoreInteractions(integrityValidator);
  }

  @Test
  public void shouldFindAvailableNameAndCreateWorkspace() throws Exception {
    ArgumentCaptor<WorkspaceConfigImpl> captor = ArgumentCaptor.forClass(WorkspaceConfigImpl.class);
    EnvironmentContext current = new EnvironmentContext();
    current.setSubject(TEST_SUBJECT);
    EnvironmentContext.setCurrent(current);
    WorkspaceImpl ws = mock(WorkspaceImpl.class);
    when(workspaceManager.createWorkspace(any(), anyString(), anyMap()))
        .thenReturn(createWorkspace(WorkspaceStatus.STOPPED));
    when(workspaceManager.getWorkspace(anyString(), anyString()))
        .thenAnswer(
            invocation -> {
              String wsname = invocation.getArgument(0);
              if ("petclinic-dev-environment".equals(wsname)
                  || "petclinic-dev-environment_1".equals(wsname)) {
                return ws;
              }
              throw new NotFoundException("ws not found");
            });
    String yamlContent =
        Files.readFile(getClass().getClassLoader().getResourceAsStream("devfile.yaml"));
    Devfile devfile = devfileManager.parse(yamlContent, true);
    // when
    devfileManager.createWorkspace(devfile, null);
    // then
    verify(workspaceManager).createWorkspace(captor.capture(), anyString(), anyMap());
    assertEquals("petclinic-dev-environment_2", captor.getValue().getName());
  }

  private WorkspaceImpl createWorkspace(WorkspaceStatus status)
      throws IOException, JsonParseException {
    return WorkspaceImpl.builder()
        .setConfig(createConfig())
        .generateId()
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

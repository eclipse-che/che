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
package org.eclipse.che.api.workspace.server.stack;

import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

import java.util.ArrayList;
import java.util.Collections;
import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.workspace.server.WorkspaceValidator;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.stack.StackComponentDto;
import org.eclipse.che.api.workspace.shared.dto.stack.StackDto;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

/**
 * Test for {@link StackValidator}
 *
 * @author Mihail Kuznyetsov
 */
@Listeners(MockitoTestNGListener.class)
public class StackValidatorTest {

  @InjectMocks private StackValidator validator;

  @Mock private WorkspaceValidator wsValidator;

  @BeforeMethod
  public void setUp() throws Exception {
    doNothing().when(wsValidator).validateConfig(any(WorkspaceConfigDto.class));
  }

  @Test
  public void shouldcheck() throws Exception {
    validator.check(createStack());
  }

  @Test(
      expectedExceptions = BadRequestException.class,
      expectedExceptionsMessageRegExp = "Required non-null stack")
  public void shouldNotValidateIfStackIsNull() throws Exception {
    validator.check(null);
  }

  @Test(
      expectedExceptions = BadRequestException.class,
      expectedExceptionsMessageRegExp = "Required non-null and non-empty stack name")
  public void shouldNotValidateIfStackNameIsNull() throws Exception {
    validator.check(createStack().withName(null));
  }

  @Test(
      expectedExceptions = BadRequestException.class,
      expectedExceptionsMessageRegExp = "Required non-null and non-empty stack name")
  public void shouldNotValidateIfStackNameIsEmpty() throws Exception {
    validator.check(createStack().withName(""));
  }

  @Test
  public void shouldValidateIfStackScopeIsGeneral() throws Exception {
    validator.check(createStack().withScope("general"));
  }

  @Test
  public void shouldValidateIfStackScopeIsAdvanced() throws Exception {
    validator.check(createStack().withScope("advanced"));
  }

  @Test(
      expectedExceptions = BadRequestException.class,
      expectedExceptionsMessageRegExp = "Required non-null scope value: 'general' or 'advanced'")
  public void shouldNotValidateIfStackScopeIsNull() throws Exception {
    validator.check(createStack().withScope(null));
  }

  @Test(
      expectedExceptions = BadRequestException.class,
      expectedExceptionsMessageRegExp = "Required non-null scope value: 'general' or 'advanced'")
  public void shouldNotValidateIfStackScopeIsNotGeneralOrAdvanced() throws Exception {
    validator.check(createStack().withScope("not-valid"));
  }

  @Test(
      expectedExceptions = BadRequestException.class,
      expectedExceptionsMessageRegExp = "Workspace config required")
  public void shouldValidateIfSourceIsStackSourceAndWorkspaceConfigIsNull() throws Exception {
    validator.check(createStack().withWorkspaceConfig(null));
  }

  @Test
  public void shouldNotcheck() throws Exception {
    validator.check(createStack());
  }

  private static StackDto createStack() {
    return newDto(StackDto.class)
        .withId("stack123")
        .withName("Name")
        .withDescription("Description")
        .withScope("general")
        .withCreator("user123")
        .withTags(new ArrayList<>(Collections.singletonList("latest")))
        .withWorkspaceConfig(newDto(WorkspaceConfigDto.class))
        .withComponents(
            new ArrayList<>(
                Collections.singletonList(
                    newDto(StackComponentDto.class).withName("maven").withVersion("3.3.1"))));
  }
}

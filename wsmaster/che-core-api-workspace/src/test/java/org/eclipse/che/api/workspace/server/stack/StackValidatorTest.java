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
package org.eclipse.che.api.workspace.server.stack;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.eclipse.che.api.workspace.shared.dto.stack.StackComponentDto;
import org.eclipse.che.api.workspace.shared.dto.stack.StackDto;
import org.eclipse.che.api.workspace.shared.dto.stack.StackSourceDto;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.eclipse.che.dto.server.DtoFactory.newDto;

/**
 * Test for {@link StackValidator}
 *
 * @author Mihail Kuznyetsov
 */
public class StackValidatorTest {

    StackValidator validator;

    @BeforeMethod
    public void setUp() {
        validator = new StackValidator();
    }

    @Test
    public void shouldcheck() throws Exception {
        validator.check(createStack());
    }


    @Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = "Required non-null stack")
    public void shouldNotValidateIfStackIsNull() throws Exception {
        validator.check(null);
    }

    @Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = "Required non-null stack creator")
    public void shouldNotValidateIfStackCreatorIsNull() throws Exception {
        validator.check(createStack().withCreator(null));
    }

    @Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = "Required non-null and non-empty stack name")
    public void shouldNotValidateIfStackNameIsNull() throws Exception {
        validator.check(createStack().withName(null));
    }

    @Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = "Required non-null and non-empty stack name")
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

    @Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = "Required non-null scope value: 'general' or 'advanced'")
    public void shouldNotValidateIfStackScopeIsNull() throws Exception {
        validator.check(createStack().withScope(null));
    }

    @Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = "Required non-null scope value: 'general' or 'advanced'")
    public void shouldNotValidateIfStackScopeIsNotGeneralOrAdvanced() throws Exception {
        validator.check(createStack().withScope("not-valid"));
    }

    @Test
    public void shouldValidateIfSourceIsWorkspaceConfigAndStackSourceIsNull() throws Exception {
        validator.check(createStack().withSource(null));
    }

    @Test
    public void shouldValidateIfSourceIsStackSourceAndWorkspaceConfigIsNull() throws Exception {
        validator.check(createStack().withWorkspaceConfig(null));
    }

    @Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = "Stack source required. You must specify either 'workspaceConfig' or 'stackSource'")
    public void shouldNotValidateIfWorkspaceConfigAndSourceAreNull() throws Exception {
        validator.check(createStack().withSource(null).withWorkspaceConfig(null));
    }

    @Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = "Required non-null and non-empty tag list")
    public void shouldNotValidateIfStackTagsListIsNull() throws Exception {
        validator.check(createStack().withTags(null));
    }

    @Test(expectedExceptions = BadRequestException.class, expectedExceptionsMessageRegExp = "Required non-null and non-empty tag list")
    public void shouldNotValidateIfStackTagsListIsEmpty() throws Exception {
        validator.check(createStack().withTags(Collections.emptyList()));
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
                .withSource(newDto(StackSourceDto.class).withType("recipe").withOrigin("FROM codenvy/ubuntu_jdk8"))
                .withComponents(new ArrayList<>(
                        Collections.singletonList(newDto(StackComponentDto.class).withName("maven").withVersion("3.3.1"))));
    }
}

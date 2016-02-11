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
package org.eclipse.che.api.workspace.server;

import org.eclipse.che.api.core.BadRequestException;
import org.eclipse.che.api.core.model.workspace.WorkspaceConfig;
import org.eclipse.che.api.machine.shared.dto.MachineConfigDto;
import org.eclipse.che.api.machine.shared.dto.MachineSourceDto;
import org.eclipse.che.api.workspace.server.model.impl.WorkspaceConfigValidatorImpl;
import org.eclipse.che.api.workspace.shared.dto.EnvironmentDto;
import org.eclipse.che.api.workspace.shared.dto.WorkspaceConfigDto;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static java.util.Collections.singletonList;
import static org.eclipse.che.dto.server.DtoFactory.newDto;
import static org.testng.Assert.assertNull;
import static org.testng.AssertJUnit.assertNotNull;

/**
 * Tests for {@link org.eclipse.che.api.workspace.server.WorkspaceConfigValidator} and {@link WorkspaceConfigValidatorImpl}
 *
 * @author Alexander Reshetnyak
 */

public class WorkspaceConfigValidatorTest {

    private WorkspaceConfigValidator wsValidator;

    @BeforeClass
    public void prepare() throws Exception {
        wsValidator = new WorkspaceConfigValidatorImpl();
    }

    @Test
    public void shouldBeNotValidationHaveNotDevMachineInConfig() throws Exception {
        BadRequestException exResult = null;
        try {
            wsValidator.validate(createConfig("dev-workspace", false));
        } catch (BadRequestException e) {
            exResult = e;
        }
        assertNotNull(exResult);
    }

    @Test
    public void shouldBeValidationHaveDevMachineInConfig() throws Exception {
        Exception exResult = null;
        try {
            wsValidator.validate(createConfig("dev-workspace", true));
        } catch (BadRequestException e) {
            exResult = e;
        }
        assertNull(exResult);
    }

    @Test
    public void shouldNotBeValidationHaveDevMachineInConfigButWsNameNull() throws Exception {
        Exception exResult = null;
        try {
            wsValidator.validate(createConfig(null, true));
        } catch (BadRequestException e) {
            exResult = e;
        }
        assertNotNull(exResult);
    }

    @Test
    public void shouldNotBeValidationHaveNotDevMachineInConfigButWsNameNull() throws Exception {
        Exception exResult = null;
        try {
            wsValidator.validate(createConfig(null, false));
        } catch (BadRequestException e) {
            exResult = e;
        }
        assertNotNull(exResult);
    }

    @Test
    public void shouldNotBeValidationHaveNotDevMachineInConfigButWsNameNullWsNameNotCheck() throws Exception {
        Exception exResult = null;
        try {
            wsValidator.validateWithoutWorkspaceName(createConfig(null, false));
        } catch (BadRequestException e) {
            exResult = e;
        }
        assertNotNull(exResult);
    }

    @Test
    public void shouldBeValidationHaveDevMachineInConfigButWsNameNullWsNameNotCheck() throws Exception {
        Exception exResult = null;
        try {
            wsValidator.validateWithoutWorkspaceName(createConfig(null, true));
        } catch (BadRequestException e) {
            exResult = e;
        }
        assertNull(exResult);
    }

    @Test
    public void shouldNotBeValidationWsNameNull() throws Exception {
        Exception exResult = null;
        try {
            wsValidator.validateWorkspaceName(null);
        } catch (BadRequestException e) {
            exResult = e;
        }
        assertNotNull(exResult);
    }

    @Test
    public void shouldNotBeValidationWsNameShort() throws Exception {
        Exception exResult = null;
        try {
            wsValidator.validateWorkspaceName("ws");
        } catch (BadRequestException e) {
            exResult = e;
        }
        assertNotNull(exResult);
    }

    @Test
    public void shouldNotBeValidationWsNameLong() throws Exception {
        Exception exResult = null;
        try {
            wsValidator.validateWorkspaceName("ws01234567890123456789");
        } catch (BadRequestException e) {
            exResult = e;
        }
        assertNotNull(exResult);
    }

    @Test
    public void shouldBeValidationWsNameValid() throws Exception {
        Exception exResult = null;
        try {
            wsValidator.validateWorkspaceName("dev-workspace1");
        } catch (BadRequestException e) {
            exResult = e;
        }
        assertNull(exResult);
    }


    private static WorkspaceConfig createConfig(String wsName, boolean isDevMachine) {
        MachineConfigDto devMachine = newDto(MachineConfigDto.class).withDev(isDevMachine)
                                                                    .withName("dev-machine")
                                                                    .withType("docker")
                                                                    .withSource(newDto(MachineSourceDto.class).withLocation("location")
                                                                                                              .withType("recipe"));
        EnvironmentDto devEnv = newDto(EnvironmentDto.class).withName("dev-env")
                                                            .withMachineConfigs(new ArrayList<>(singletonList(devMachine)))
                                                            .withRecipe(null);
        return newDto(WorkspaceConfigDto.class).withName(wsName)
                                               .withEnvironments(new ArrayList<>(singletonList(devEnv)))
                                               .withDefaultEnv("dev-env");
    }
}

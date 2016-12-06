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
package org.eclipse.che.plugin.docker.machine.parser;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.core.model.workspace.Environment;
import org.eclipse.che.api.core.model.workspace.EnvironmentRecipe;
import org.eclipse.che.api.environment.server.model.CheServiceBuildContextImpl;
import org.eclipse.che.api.environment.server.model.CheServiceImpl;
import org.eclipse.che.api.environment.server.model.CheServicesEnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentImpl;
import org.eclipse.che.api.workspace.server.model.impl.EnvironmentRecipeImpl;
import org.eclipse.che.api.workspace.server.model.impl.ExtendedMachineImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.testng.AssertJUnit.assertEquals;

/**
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
@Listeners(MockitoTestNGListener.class)
public class DockerfileEnvironmentParserTest {

    private static final String DEFAULT_MACHINE_NAME = "dev-machine";
    private static final String DEFAULT_DOCKERFILE   = "FROM codenvy/ubuntu_jdk8\n";

    @Mock
    private Environment environment;
    @Mock
    private EnvironmentRecipe recipe;

    @InjectMocks
    public DockerfileEnvironmentParser parser;

    @Test
    public void shouldBeAbleToParseDockerfileEnvironmentFromContent() throws ServerException {
        Environment environment = createDockerfileEnvConfig(DEFAULT_DOCKERFILE, null, DEFAULT_MACHINE_NAME);

        CheServicesEnvironmentImpl expected = new CheServicesEnvironmentImpl();
        expected.getServices().put(DEFAULT_MACHINE_NAME, new CheServiceImpl().withBuild(
                new CheServiceBuildContextImpl().withDockerfileContent(DEFAULT_DOCKERFILE)));

        // when
        CheServicesEnvironmentImpl cheServicesEnvironment = parser.parse(environment);

        // then
        assertEquals(cheServicesEnvironment, expected);
    }

    @Test
    public void shouldBeAbleToParseDockerfileEnvironmentFromLocation() throws Exception {
        // given
        String recipeLocation = "http://localhost:8080/recipe/url";
        EnvironmentImpl environment = createDockerfileEnvConfig(null, recipeLocation, DEFAULT_MACHINE_NAME);

        CheServicesEnvironmentImpl expected = new CheServicesEnvironmentImpl();
        expected.getServices().put(DEFAULT_MACHINE_NAME, new CheServiceImpl().withBuild(
                new CheServiceBuildContextImpl().withContext(recipeLocation)));

        // when
        CheServicesEnvironmentImpl cheServicesEnvironment = parser.parse(environment);

        // then
        Assert.assertEquals(cheServicesEnvironment, expected);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Environment of type '.*' doesn't support multiple machines, but contains machines: .*")
    public void shouldThrowExceptionOnParseOfDockerfileEnvWithSeveralExtendedMachines() throws Exception {
        // given
        EnvironmentImpl environment = createDockerfileEnvConfig();
        environment.getMachines().put("anotherMachine", new ExtendedMachineImpl(emptyList(), emptyMap(), emptyMap()));

        // when
        parser.parse(environment);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Content type '.*' of recipe of environment is unsupported." +
                                            " Supported values are: text/x-dockerfile")
    public void shouldThrowExceptionOnParseOfDockerfileEnvWithNotSupportedContentType() throws Exception {
        // given
        EnvironmentImpl environment = createDockerfileEnvConfig();
        environment.getRecipe().setContentType("dockerfile");

        // when
        parser.parse(environment);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Dockerfile environment parser doesn't support recipe type 'dockerImage'")
    public void shouldThrowExceptionInCaseEnvironmentContainsNotSupportedRecipeType() throws ServerException {
        // given
        EnvironmentImpl environment = createDockerfileEnvConfig();
        environment.getRecipe().setType("dockerImage");

        // when
        parser.parse(environment);
    }

    private EnvironmentImpl createDockerfileEnvConfig() {
        return createDockerfileEnvConfig(DEFAULT_DOCKERFILE, null, DEFAULT_MACHINE_NAME);
    }

    private EnvironmentImpl createDockerfileEnvConfig(String recipeContent,
                                                             String recipeLocation,
                                                             String machineName) {
        return new EnvironmentImpl(new EnvironmentRecipeImpl("dockerfile",
                                                             "text/x-dockerfile",
                                                             recipeContent,
                                                             recipeLocation),
                                   singletonMap(machineName,
                                                new ExtendedMachineImpl(emptyList(),
                                                                        emptyMap(),
                                                                        emptyMap())));
    }
}

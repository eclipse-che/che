/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.workspace.infrastructure.docker.old.parser;

import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;

/**
 * @author Alexander Garagatyi
 * @author Alexander Andrienko
 */
@Listeners(MockitoTestNGListener.class)
public class DockerImageEnvironmentParserTest {
/*
    private static final String DEFAULT_MACHINE_NAME = "dev-machine";
    private static final String DEFAULT_DOCKER_IMAGE = "codenvy/ubuntu_jdk8";

    @Mock
    private Environment environment;
    @Mock
    private Recipe      recipe;

    @InjectMocks
    public DockerImageEnvironmentParser parser;

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Environment of type '.*' doesn't support multiple machines, but contains machines: .*")
    public void shouldThrowExceptionOnParseOfDockerimageEnvWithSeveralExtendedMachines() throws Exception {
        EnvironmentImpl environment = createDockerimageEnvConfig();
        environment.getMachines().put("anotherMachine", new ExtendedMachineImpl(emptyList(), emptyMap(), emptyMap()));

        // when
        parser.parse(environment);
    }

    @Test
    public void shouldBeAbleToParseDockerImageEnvironment() throws Exception {
        // given
        EnvironmentImpl environment = createDockerimageEnvConfig(DEFAULT_DOCKER_IMAGE, DEFAULT_MACHINE_NAME);

        CheServicesEnvironmentImpl expected = new CheServicesEnvironmentImpl();
        expected.getServices().put(DEFAULT_MACHINE_NAME, new CheServiceImpl().withImage(DEFAULT_DOCKER_IMAGE));

        // when
        CheServicesEnvironmentImpl cheServicesEnvironment = parser.parse(environment);

        // then
        assertEquals(cheServicesEnvironment, expected);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Docker image environment parser doesn't support recipe type 'dockerfile'")
    public void shouldReturnThrowExceptionInCaseEnvironmentContainsNotSupportedRecipeType() throws ServerException {
        // given
        EnvironmentImpl environment = createDockerimageEnvConfig(DEFAULT_DOCKER_IMAGE, DEFAULT_MACHINE_NAME);
        environment.getRecipe().setType("dockerfile");

        // when
        parser.parse(environment);
    }

    private static EnvironmentImpl createDockerimageEnvConfig() {
        return createDockerimageEnvConfig(DEFAULT_DOCKER_IMAGE, DEFAULT_MACHINE_NAME);
    }

    private static EnvironmentImpl createDockerimageEnvConfig(String image, String machineName) {
        return new EnvironmentImpl(new EnvironmentRecipeImpl("dockerimage",
                                                             null,
                                                             null,
                                                             image),
                                   singletonMap(machineName,
                                                new ExtendedMachineImpl(emptyList(),
                                                                        emptyMap(),
                                                                        emptyMap())));
    }*/
}

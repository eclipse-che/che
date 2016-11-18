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
package org.eclipse.che.plugin.docker.compose.yaml;

import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.machine.server.util.RecipeDownloader;
import org.eclipse.che.plugin.docker.compose.ComposeEnvironment;
import org.eclipse.che.plugin.docker.compose.ComposeServiceImpl;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.testng.MockitoTestNGListener;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * Test deserialization field {@link ComposeServiceImpl#build}
 * in the {@link ComposeEnvironmentParser}.
 *
 * @author Mario Loriedo
 */
@Listeners(MockitoTestNGListener.class)
public class BuildContextTest {

    @Mock
    private RecipeDownloader recipeDownloader;

    @InjectMocks
    private ComposeEnvironmentParser parser;

    @Test
    public void shouldParseBuildArgsWhenProvided() throws ServerException {
        // given
        String recipeContent = "services:\n" +
                               " dev-machine:\n" +
                               "  build:\n" +
                               "   context: .\n" +
                               "   args:\n" +
                               "    buildno: 1\n" +
                               "    password: secret\n";

        Map<String, String> expected = new HashMap<String, String>() {
            {
                put("buildno", "1");
                put("password", "secret");
            }
        };

        // when
        ComposeEnvironment composeEnvironment = parser.parse(recipeContent, "application/x-yaml");


        // then
        assertEquals(composeEnvironment.getServices().get("dev-machine").getBuild().getArgs(), expected);
    }

    @Test
    public void shouldNotParseBuildArgsWhenNotProvided() throws ServerException {
        // given
        String recipeContent = "services:\n" +
                               " dev-machine:\n" +
                               "  build:\n" +
                               "   context: .\n";

        // when
        ComposeEnvironment composeEnvironment = parser.parse(recipeContent, "application/x-yaml");

        // then
        assertEquals(Collections.emptyMap(), composeEnvironment.getServices().get("dev-machine").getBuild().getArgs());
    }
}

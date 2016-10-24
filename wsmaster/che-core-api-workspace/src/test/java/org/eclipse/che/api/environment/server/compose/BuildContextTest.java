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
package org.eclipse.che.api.environment.server.compose;

import org.eclipse.che.api.core.ServerException;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;

/**
 * @author Mario Loriedo
 */
public class BuildContextTest {

    ComposeFileParser parser = new ComposeFileParser();

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
        String recipeContentType = "application/x-yaml";

        Map<String,String> expected = new HashMap<String, String>() {
            {
                put("buildno","1");
                put("password","secret");
            }
        };

        // when
        ComposeEnvironmentImpl composeEnvironment = parser.parse(recipeContent, recipeContentType);


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
        String recipeContentType = "application/x-yaml";

        // when
        ComposeEnvironmentImpl composeEnvironment = parser.parse(recipeContent, recipeContentType);

        // then
        assertEquals(Collections.emptyMap(), composeEnvironment.getServices().get("dev-machine").getBuild().getArgs());
    }

}

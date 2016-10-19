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

import com.google.common.collect.ImmutableMap;
import org.eclipse.che.api.core.ServerException;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertEquals;

/**
 * @author Dmytro Nochevnov
 */
public class EnvironmentContextTest {

    ComposeFileParser parser = new ComposeFileParser();

    @Test
    public void shouldParseDictionaryTypeEnvironment() throws ServerException {
        // given
        String recipeContent = "services: \n"
                               + " dev-machine: \n"
                               + "  image: codenvy/ubuntu_jdk8\n"
                               + "  environment:\n"
                               + "    RACK_ENV: development\n"
                               + "    SHOW: 'true'";

        String recipeContentType = "application/x-yaml";

        Map<String,String> expected = ImmutableMap.of("RACK_ENV", "development",
                                                      "SHOW", "true");

        // when
        ComposeEnvironmentImpl composeEnvironment = parser.parse(recipeContent, recipeContentType);

        // then
        assertEquals(composeEnvironment.getServices().get("dev-machine").getEnvironment(), expected);
    }

    @Test
    public void shouldParseArrayTypeEnvironment() throws ServerException {
        // given
        String recipeContent = "services: \n"
                               + " dev-machine: \n"
                               + "  image: codenvy/ubuntu_jdk8\n"
                               + "  environment:\n"
                               + "   - MYSQL_ROOT_PASSWORD=root\n"
                               + "   - MYSQL_DATABASE=db";

        String recipeContentType = "application/x-yaml";

        Map<String,String> expected = ImmutableMap.of("MYSQL_ROOT_PASSWORD","root",
                                                      "MYSQL_DATABASE","db");

        // when
        ComposeEnvironmentImpl composeEnvironment = parser.parse(recipeContent, recipeContentType);

        // then
        assertEquals(composeEnvironment.getServices().get("dev-machine").getEnvironment(), expected);
    }

    @Test
    public void shouldParseEmptyEnvironment() throws ServerException {
        // given
        String recipeContent = "services: \n"
                               + " dev-machine: \n"
                               + "  image: codenvy/ubuntu_jdk8\n"
                               + "  environment:";

        String recipeContentType = "application/x-yaml";

        Map<String,String> expected = ImmutableMap.of();

        // when
        ComposeEnvironmentImpl composeEnvironment = parser.parse(recipeContent, recipeContentType);

        // then
        assertEquals(composeEnvironment.getServices().get("dev-machine").getEnvironment(), expected);
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
          expectedExceptionsMessageRegExp = "Parsing of environment configuration failed. Unsupported type 'class java.lang.Boolean'.*")
    public void shouldThrowErrorOnUnsupportedTypeOfEnvironment() throws ServerException {
        // given
        String recipeContent = "services: \n"
                               + " dev-machine: \n"
                               + "  image: codenvy/ubuntu_jdk8\n"
                               + "  environment:\n"
                               + "   true";

        String recipeContentType = "application/x-yaml";

        // when
        ComposeEnvironmentImpl composeEnvironment = parser.parse(recipeContent, recipeContentType);

        // then
        composeEnvironment.getServices().get("dev-machine").getEnvironment();
    }

    @Test(expectedExceptions = IllegalArgumentException.class,
        expectedExceptionsMessageRegExp = "Parsing of environment configuration failed. Unsupported value '\\[\\{MYSQL_ROOT_PASSWORD=root}]'.*")
    public void shouldThrowErrorOnUnsupportedFormatOfListEnvironment() throws ServerException {
        // given
        String recipeContent = "services: \n"
                               + " dev-machine: \n"
                               + "  image: codenvy/ubuntu_jdk8\n"
                               + "  environment:\n"
                               + "   - MYSQL_ROOT_PASSWORD: root\n";

        String recipeContentType = "application/x-yaml";

        // when
        ComposeEnvironmentImpl composeEnvironment = parser.parse(recipeContent, recipeContentType);

        // then
        composeEnvironment.getServices().get("dev-machine").getEnvironment();
    }

}

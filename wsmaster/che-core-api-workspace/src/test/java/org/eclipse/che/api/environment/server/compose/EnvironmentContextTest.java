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
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;

import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

/**
 * @author Dmytro Nochevnov
 */
public class EnvironmentContextTest {

    ComposeFileParser parser = new ComposeFileParser();

    @Test(dataProvider = "correctContentTestData")
    public void testCorrectContentParsing(String content, Map<String, String> expected) throws ServerException {
        // when
        ComposeEnvironmentImpl composeEnvironment = parser.parse(content, "application/x-yaml");

        // then
        assertEquals(composeEnvironment.getServices().get("dev-machine").getEnvironment(), expected);
    }

    @DataProvider
    public Object[][] correctContentTestData() {
        return new Object[][] {
            // dictionary type environment
            {"services: \n"
             + " dev-machine: \n"
             + "  image: codenvy/ubuntu_jdk8\n"
             + "  environment:\n"
             + "    RACK_ENV: development\n"
             + "    SHOW: 'true'",
             ImmutableMap.of("RACK_ENV", "development",
                             "SHOW", "true")
            },

            // dictionary format, value of variable is empty
            {"services: \n"
             + " dev-machine: \n"
             + "  image: codenvy/ubuntu_jdk8\n"
             + "  environment:\n"
             + "   MYSQL_ROOT_PASSWORD: ",
             ImmutableMap.of("MYSQL_ROOT_PASSWORD", "")
            },

            // dictionary format, value of variable contains colon sign
            {"services: \n"
             + " dev-machine: \n"
             + "  image: codenvy/ubuntu_jdk8\n"
             + "  environment:\n"
             + "   VAR : val:1",
             ImmutableMap.of("VAR", "val:1")
            },

            // array type environment
            {"services: \n"
             + " dev-machine: \n"
             + "  image: codenvy/ubuntu_jdk8\n"
             + "  environment:\n"
             + "   - MYSQL_ROOT_PASSWORD=root\n"
             + "   - MYSQL_DATABASE=db",
             ImmutableMap.of("MYSQL_ROOT_PASSWORD", "root",
                             "MYSQL_DATABASE", "db")
            },

            // array format, value of variable contains equal sign
            {"services: \n"
             + " dev-machine: \n"
             + "  image: codenvy/ubuntu_jdk8\n"
             + "  environment:\n"
             + "   - VAR=val=1",
             ImmutableMap.of("VAR", "val=1")
            },

            // array format, empty value of variable
            {"services: \n"
             + " dev-machine: \n"
             + "  image: codenvy/ubuntu_jdk8\n"
             + "  environment:\n"
             + "   - VAR= ",
             ImmutableMap.of("VAR", "")
            },

            // empty environment
            {"services: \n"
             + " dev-machine: \n"
             + "  image: codenvy/ubuntu_jdk8\n"
             + "  environment:",
             ImmutableMap.of()
            },
        };
    }

    @Test(dataProvider = "incorrectContentTestData")
    public void shouldThrowError(String content, String errorPattern) throws ServerException {
        try {
            parser.parse(content, "application/x-yaml");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().matches(errorPattern),
                       format("Actual error message \"%s\" doesn't match regex \"%s\" for content \"%s\"",
                              e.getMessage(),
                              errorPattern,
                              content));
            return;
        }

        fail(format("Content \"%s\" should throw IllegalArgumentException", content));
    }

    @DataProvider
    public Object[][] incorrectContentTestData() {
        return new Object[][] {
            // unsupported type of environment
            {"services: \n"
             + " dev-machine: \n"
             + "  image: codenvy/ubuntu_jdk8\n"
             + "  environment:\n"
             + "   true",
             "Parsing of environment configuration failed. Unsupported type 'class java.lang.Boolean'\\.(?s).*"
             },

            // unsupported format of list environment
            {"services: \n"
             + " dev-machine: \n"
             + "  image: codenvy/ubuntu_jdk8\n"
             + "  environment:\n"
             + "   - MYSQL_ROOT_PASSWORD: root\n",
             "Parsing of environment configuration failed. Unsupported value '\\[\\{MYSQL_ROOT_PASSWORD=root}]'\\.(?s).*"
             },

            // dictionary format, no colon in entry
            {"services: \n"
             + " dev-machine: \n"
             + "  image: codenvy/ubuntu_jdk8\n"
             + "  environment:\n"
             + "   MYSQL_ROOT_PASSWORD",
             "Parsing of environment configuration failed. Unsupported value 'MYSQL_ROOT_PASSWORD'\\.(?s).*"
            },

            // dictionary format, value of variable contains equal sign
            {"services: \n"
             + " dev-machine: \n"
             + "  image: codenvy/ubuntu_jdk8\n"
             + "  environment:\n"
             + "   VAR=val=1",
             "Parsing of environment configuration failed. Unsupported value 'VAR=val=1'\\.(?s).*"
            },

            // array format, no equal sign in entry
            {"services: \n"
             + " dev-machine: \n"
             + "  image: codenvy/ubuntu_jdk8\n"
             + "  environment:\n"
             + "   - MYSQL_ROOT_PASSWORD=root\n"
             + "   - MYSQL_DATABASE\n",
             "Parsing of environment configuration failed. Unsupported value 'MYSQL_DATABASE'\\.(?s).*"
            },
        };
    }


}

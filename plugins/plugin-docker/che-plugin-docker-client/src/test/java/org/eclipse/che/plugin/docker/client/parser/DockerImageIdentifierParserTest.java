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
package org.eclipse.che.plugin.docker.client.parser;

import org.eclipse.che.plugin.docker.client.DockerFileException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class DockerImageIdentifierParserTest {

    @Test(dataProvider = "validImages", timeOut = 500)
    public void shouldBeAbleToParseValidImage(String image, DockerImageIdentifier expectedImageIdentifier) throws Exception {
        final DockerImageIdentifier actualParsedIdentifier = DockerImageIdentifierParser.parse(image);

        if (!actualParsedIdentifier.equals(expectedImageIdentifier)) {
            System.out.println();
        }
        assertEquals(actualParsedIdentifier, expectedImageIdentifier);
    }

    @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Null argument value is forbidden")
    public void shouldThrowIllegalArgumentExceptionIfArgumentIsNull() throws Exception {
        DockerImageIdentifierParser.parse(null);
    }

    @Test(dataProvider = "invalidImages",
          expectedExceptions = DockerFileException.class,
          expectedExceptionsMessageRegExp = "Provided image reference is invalid",
          timeOut = 500)
    public void shouldThrowDockerFileExceptionIfArgumentDoesNotMatchRegexp(String image) throws Exception {
        DockerImageIdentifierParser.parse(image);
    }

    @DataProvider(name = "validImages")
    public static Object[][] validImages() {
        return new Object[][] {
                {"single_repo_component",
                 DockerImageIdentifier.builder()
                                      .setRepository("single_repo_component")
                         .build()},
                {"single_repo_component:latest",
                 DockerImageIdentifier.builder()
                                      .setRepository("single_repo_component")
                                      .setTag("latest")
                         .build()},
                {"single_repo_component:some_Tag.with.dots",
                 DockerImageIdentifier.builder()
                                      .setRepository("single_repo_component")
                                      .setTag("some_Tag.with.dots")
                         .build()},
                {"single_repo_component@sha256:acd122209878932bcdafafdFADCDBafaadacdbeEFAD",
                 DockerImageIdentifier.builder()
                                      .setRepository("single_repo_component")
                                      .setDigest("sha256:acd122209878932bcdafafdFADCDBafaadacdbeEFAD")
                         .build()},
                {"registry1-my-registries.com.ua/single_repo_component",
                 DockerImageIdentifier.builder()
                                      .setRepository("single_repo_component")
                                      .setRegistry("registry1-my-registries.com.ua")
                         .build()},
                {"registry1-my-registries.com.ua/single_repo_component:latest",
                 DockerImageIdentifier.builder()
                                      .setRepository("single_repo_component")
                                      .setRegistry("registry1-my-registries.com.ua")
                                      .setTag("latest")
                         .build()},
                {"registry1-my-registries.com.ua/single_repo_component:some.Tag-with_Different.Symbols",
                 DockerImageIdentifier.builder()
                                      .setRepository("single_repo_component")
                                      .setRegistry("registry1-my-registries.com.ua")
                                      .setTag("some.Tag-with_Different.Symbols")
                         .build()},
                {"registry1-my-registries.com.ua/single_repo_component@sha256:acd122209878932bcdafafdFADCDBafaadacdbeEFAD",
                 DockerImageIdentifier.builder()
                                      .setRepository("single_repo_component")
                                      .setRegistry("registry1-my-registries.com.ua")
                                      .setDigest("sha256:acd122209878932bcdafafdFADCDBafaadacdbeEFAD")
                         .build()},
                {"registry1-my-registries.com.ua:5050/single_repo_component",
                 DockerImageIdentifier.builder()
                                      .setRepository("single_repo_component")
                                      .setRegistry("registry1-my-registries.com.ua:5050")
                         .build()},
                {"registry1-my-registries.com.ua:50900/single_repo_component:latest",
                 DockerImageIdentifier.builder()
                                      .setRepository("single_repo_component")
                                      .setRegistry("registry1-my-registries.com.ua:50900")
                                      .setTag("latest")
                         .build()},
                {"registry1-my-registries.com.ua:81/single_repo_component:someTag",
                 DockerImageIdentifier.builder()
                                      .setRepository("single_repo_component")
                                      .setRegistry("registry1-my-registries.com.ua:81")
                                      .setTag("someTag")
                         .build()},
                {"registry1-my-registries.com.ua:80/single_repo_component:someTag",
                 DockerImageIdentifier.builder()
                                      .setRepository("single_repo_component")
                                      .setRegistry("registry1-my-registries.com.ua:80")
                                      .setTag("someTag")
                         .build()},
                {"registry1-my-registries.com.ua:443/single_repo_component:someTag",
                 DockerImageIdentifier.builder()
                                      .setRepository("single_repo_component")
                                      .setRegistry("registry1-my-registries.com.ua:443")
                                      .setTag("someTag")
                         .build()},
                {"registry1-my-registries.com.ua:22/single_repo_component@sha256:acd122209878932bcdafafdFADCDBafaadacdbeEFAD",
                 DockerImageIdentifier.builder()
                                      .setRepository("single_repo_component")
                                      .setRegistry("registry1-my-registries.com.ua:22")
                                      .setDigest("sha256:acd122209878932bcdafafdFADCDBafaadacdbeEFAD")
                         .build()},
                {"my_repo_component1/my_repo_component2",
                 DockerImageIdentifier.builder()
                                      .setRepository("my_repo_component1/my_repo_component2")
                         .build()},
                {"my_repo_component1/my_repo_component2:latest",
                 DockerImageIdentifier.builder()
                                      .setRepository("my_repo_component1/my_repo_component2")
                                      .setTag("latest")
                         .build()},
                {"my_repo_component1/my_repo_component2:someTag",
                 DockerImageIdentifier.builder()
                                      .setRepository("my_repo_component1/my_repo_component2")
                                      .setTag("someTag")
                         .build()},
                {"my_repo_component1/my_repo_component2@sha256:acd122209878932bcdafafdFADCDBafaadacdbeEFAD",
                 DockerImageIdentifier.builder()
                                      .setRepository("my_repo_component1/my_repo_component2")
                                      .setDigest("sha256:acd122209878932bcdafafdFADCDBafaadacdbeEFAD")
                         .build()},
                {"my.other.registry.com/my_repo_component1/my_repo_component2",
                 DockerImageIdentifier.builder()
                                      .setRepository("my_repo_component1/my_repo_component2")
                                      .setRegistry("my.other.registry.com")
                         .build()},
                {"my.other.registry.com/my_repo_component1/my_repo_component2:latest",
                 DockerImageIdentifier.builder()
                                      .setRepository("my_repo_component1/my_repo_component2")
                                      .setRegistry("my.other.registry.com")
                                      .setTag("latest")
                         .build()},
                {"my.other.registry.com/my_repo_component1/my_repo_component2:someTag",
                 DockerImageIdentifier.builder()
                                      .setRepository("my_repo_component1/my_repo_component2")
                                      .setRegistry("my.other.registry.com")
                                      .setTag("someTag")
                         .build()},
                {"my.other.registry.com/my_repo_component1/my_repo_component2@sha256:acd122209878932bcdafafdFADCDBafaadacdbeEFAD",
                 DockerImageIdentifier.builder()
                                      .setRepository("my_repo_component1/my_repo_component2")
                                      .setRegistry("my.other.registry.com")
                                      .setDigest("sha256:acd122209878932bcdafafdFADCDBafaadacdbeEFAD")
                         .build()},
                {"my.other.registry.com:80/my_repo_component1/my_repo_component2",
                 DockerImageIdentifier.builder()
                                      .setRepository("my_repo_component1/my_repo_component2")
                                      .setRegistry("my.other.registry.com:80")
                         .build()},
                {"my.other.registry.com:9080/my_repo_component1/my_repo_component2:latest",
                 DockerImageIdentifier.builder()
                                      .setRepository("my_repo_component1/my_repo_component2")
                                      .setRegistry("my.other.registry.com:9080")
                                      .setTag("latest")
                         .build()},
                {"my.other.registry.com:5000/my_repo_component1/my_repo_component2:someTag",
                 DockerImageIdentifier.builder()
                                      .setRepository("my_repo_component1/my_repo_component2")
                                      .setRegistry("my.other.registry.com:5000")
                                      .setTag("someTag")
                         .build()},
                {"my.other.registry.com:5000/my_repo_component1/my_repo_component2/my_repo_component3:someTag",
                 DockerImageIdentifier.builder()
                                      .setRepository("my_repo_component1/my_repo_component2/my_repo_component3")
                                      .setRegistry("my.other.registry.com:5000")
                                      .setTag("someTag")
                         .build()},
                {"my.other.registry.com:32800/my_repo_component1/my_repo_component2@sha256:acd122209878932bcdafafdFADCDBafaadacdbeEFAD",
                 DockerImageIdentifier.builder()
                                      .setRepository("my_repo_component1/my_repo_component2")
                                      .setRegistry("my.other.registry.com:32800")
                                      .setDigest("sha256:acd122209878932bcdafafdFADCDBafaadacdbeEFAD")
                         .build()},
                {"my_repo_component1/my_repo_component2@some-digest:adfbac09548AFCBDFACDAFdcedfedfedfdde",
                 DockerImageIdentifier.builder()
                                      .setRepository("my_repo_component1/my_repo_component2")
                                      .setDigest("some-digest:adfbac09548AFCBDFACDAFdcedfedfedfdde")
                         .build()},
                {"my_repo_component1/my_repo_component2@some_digest:adfbac09548AFCBDFACDAFdcedfedfedfdde",
                 DockerImageIdentifier.builder()
                                      .setRepository("my_repo_component1/my_repo_component2")
                                      .setDigest("some_digest:adfbac09548AFCBDFACDAFdcedfedfedfdde")
                         .build()},
                {"my_repo_component1/my_repo_component2@some+digest:adfbac09548AFCBDFACDAFdcedfedfedfdde",
                 DockerImageIdentifier.builder()
                                      .setRepository("my_repo_component1/my_repo_component2")
                                      .setDigest("some+digest:adfbac09548AFCBDFACDAFdcedfedfedfdde")
                         .build()},
                {"my_repo_component1/my_repo_component2@some.digest:adfbac09548AFCBDFACDAFdcedfedfedfdde",
                 DockerImageIdentifier.builder()
                                      .setRepository("my_repo_component1/my_repo_component2")
                                      .setDigest("some.digest:adfbac09548AFCBDFACDAFdcedfedfedfdde")
                         .build()}
        };
    }

    @DataProvider(name = "invalidImages")
    public static Object[][] invalidImages() {
        return new Object[][] {
                {"image/"},
                {"/image/"},
                {"/image"},
                {":image"},
                {":image:"},
                {"image:"},
                {"image@"},
                {"@image"},
                {"repo/image:tag@digest:aaaaaa098@digest"},
                {"repo/image:tag:tag1"},
                {"my.other.registry.com:-8080/my_repo_component1/my_repo_component2/my_repo_component3"}
        };
    }
}

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

    @Test(expectedExceptions = DockerFileException.class,
          expectedExceptionsMessageRegExp = "Null and empty argument value is forbidden")
    public void shouldThrowIllegalArgumentExceptionIfArgumentIsNull() throws Exception {
        DockerImageIdentifierParser.parse(null);
    }

    @Test(expectedExceptions = DockerFileException.class,
          expectedExceptionsMessageRegExp = "Null and empty argument value is forbidden")
    public void shouldThrowIllegalArgumentExceptionIfArgumentIsEmpty() throws Exception {
        DockerImageIdentifierParser.parse("");
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
                {"ubuntu",
                 DockerImageIdentifier.builder()
                                      .setRepository("ubuntu")
                         .build()},
                {"codenvy/ubuntu_jdk8",
                 DockerImageIdentifier.builder()
                                      .setRepository("codenvy/ubuntu_jdk8")
                        .build()},
                {"localhost:5000/ubuntu",
                 DockerImageIdentifier.builder()
                                      .setRepository("ubuntu")
                                      .setRegistry("localhost:5000")
                         .build()},
                {"myserver:5000/ubuntu",
                 DockerImageIdentifier.builder()
                                      .setRepository("ubuntu")
                                      .setRegistry("myserver:5000")
                         .build()},
                {"myserver.com/ubuntu",
                 DockerImageIdentifier.builder()
                                      .setRepository("ubuntu")
                                      .setRegistry("myserver.com")
                         .build()},
                {"myserver.com/codenvy/ubuntu",
                 DockerImageIdentifier.builder()
                                      .setRepository("codenvy/ubuntu")
                                      .setRegistry("myserver.com")
                         .build()},
                {"docker.io/ubuntu",
                 DockerImageIdentifier.builder()
                                      .setRepository("ubuntu")
                                      .setRegistry("docker.io")
                         .build()},
                {"docker.io/codenvy/ubuntu",
                 DockerImageIdentifier.builder()
                                      .setRepository("codenvy/ubuntu")
                                      .setRegistry("docker.io")
                         .build()},
                {"codenvy/agaragatyi/ubuntu_jdk8",
                 DockerImageIdentifier.builder()
                                      .setRepository("codenvy/agaragatyi/ubuntu_jdk8")
                         .build()},
                {"ubuntu:latest",
                 DockerImageIdentifier.builder()
                                      .setRepository("ubuntu")
                                      .setTag("latest")
                         .build()},
                {"ubuntu:8080",
                 DockerImageIdentifier.builder()
                                      .setRepository("ubuntu")
                                      .setTag("8080")
                         .build()},
                {"debian:t.com",
                 DockerImageIdentifier.builder()
                                      .setRepository("debian")
                                      .setTag("t.com")
                         .build()},
                {"ubuntu@sha256:acd122209878932bcdafafdFADCDBafaadacdbeEFAD",
                 DockerImageIdentifier.builder()
                                      .setRepository("ubuntu")
                                      .setDigest("sha256:acd122209878932bcdafafdFADCDBafaadacdbeEFAD")
                         .build()},
                {"registry1-my-registries.com.ua/ubuntu",
                 DockerImageIdentifier.builder()
                                      .setRepository("ubuntu")
                                      .setRegistry("registry1-my-registries.com.ua")
                         .build()},
                {"registry1-my-registries.com.ua/ubuntu:latest",
                 DockerImageIdentifier.builder()
                                      .setRepository("ubuntu")
                                      .setRegistry("registry1-my-registries.com.ua")
                                      .setTag("latest")
                         .build()},
                {"registry1-my-registries.com.ua/debian:some.Tag-with_Different.Symbols",
                 DockerImageIdentifier.builder()
                                      .setRepository("debian")
                                      .setRegistry("registry1-my-registries.com.ua")
                                      .setTag("some.Tag-with_Different.Symbols")
                         .build()},
                {"registry1-my-registries.com.ua/alpine@sha256:acd122209878932bcdafafdFADCDBafaadacdbeEFAD",
                 DockerImageIdentifier.builder()
                                      .setRepository("alpine")
                                      .setRegistry("registry1-my-registries.com.ua")
                                      .setDigest("sha256:acd122209878932bcdafafdFADCDBafaadacdbeEFAD")
                         .build()},
                {"registry1-my-registries.com.ua:5050/alpine_12",
                 DockerImageIdentifier.builder()
                                      .setRepository("alpine_12")
                                      .setRegistry("registry1-my-registries.com.ua:5050")
                         .build()},
                {"registry1-my-registries.com.ua:50900/debian:latest",
                 DockerImageIdentifier.builder()
                                      .setRepository("debian")
                                      .setRegistry("registry1-my-registries.com.ua:50900")
                                      .setTag("latest")
                         .build()},
                {"registry1-my-registries.com.ua:81/debian:someTag",
                 DockerImageIdentifier.builder()
                                      .setRepository("debian")
                                      .setRegistry("registry1-my-registries.com.ua:81")
                                      .setTag("someTag")
                         .build()},
                {"registry1-my-registries.com.ua:80/debian:someTag",
                 DockerImageIdentifier.builder()
                                      .setRepository("debian")
                                      .setRegistry("registry1-my-registries.com.ua:80")
                                      .setTag("someTag")
                         .build()},
                {"registry1-my-registries.com.ua:443/alpine:someTag",
                 DockerImageIdentifier.builder()
                                      .setRepository("alpine")
                                      .setRegistry("registry1-my-registries.com.ua:443")
                                      .setTag("someTag")
                         .build()},
                {"registry1-my-registries.com.ua:22/ubuntu@sha256:acd122209878932bcdafafdFADCDBafaadacdbeEFAD",
                 DockerImageIdentifier.builder()
                                      .setRepository("ubuntu")
                                      .setRegistry("registry1-my-registries.com.ua:22")
                                      .setDigest("sha256:acd122209878932bcdafafdFADCDBafaadacdbeEFAD")
                         .build()},
                {"eclipse/che",
                 DockerImageIdentifier.builder()
                                      .setRepository("eclipse/che")
                         .build()},
                {"eclipse/che:latest",
                 DockerImageIdentifier.builder()
                                      .setRepository("eclipse/che")
                                      .setTag("latest")
                         .build()},
                {"eclipse/che:someTag",
                 DockerImageIdentifier.builder()
                                      .setRepository("eclipse/che")
                                      .setTag("someTag")
                         .build()},
                {"eclipse/che@sha256:acd122209878932bcdafafdFADCDBafaadacdbeEFAD",
                 DockerImageIdentifier.builder()
                                      .setRepository("eclipse/che")
                                      .setDigest("sha256:acd122209878932bcdafafdFADCDBafaadacdbeEFAD")
                         .build()},
                {"my.other.registry.com/eclipse/che",
                 DockerImageIdentifier.builder()
                                      .setRepository("eclipse/che")
                                      .setRegistry("my.other.registry.com")
                         .build()},
                {"my.other.registry.com/eclipse/che:latest",
                 DockerImageIdentifier.builder()
                                      .setRepository("eclipse/che")
                                      .setRegistry("my.other.registry.com")
                                      .setTag("latest")
                         .build()},
                {"my.other.registry.com/eclipse/che:someTag",
                 DockerImageIdentifier.builder()
                                      .setRepository("eclipse/che")
                                      .setRegistry("my.other.registry.com")
                                      .setTag("someTag")
                         .build()},
                {"my.other.registry.com/eclipse/che@sha256:acd122209878932bcdafafdFADCDBafaadacdbeEFAD",
                 DockerImageIdentifier.builder()
                                      .setRepository("eclipse/che")
                                      .setRegistry("my.other.registry.com")
                                      .setDigest("sha256:acd122209878932bcdafafdFADCDBafaadacdbeEFAD")
                         .build()},
                {"my.other.registry.com:80/eclipse/che",
                 DockerImageIdentifier.builder()
                                      .setRepository("eclipse/che")
                                      .setRegistry("my.other.registry.com:80")
                         .build()},
                {"my.other.registry.com:9080/eclipse/che:latest",
                 DockerImageIdentifier.builder()
                                      .setRepository("eclipse/che")
                                      .setRegistry("my.other.registry.com:9080")
                                      .setTag("latest")
                         .build()},
                {"my.other.registry.com:5000/eclipse/che:someTag",
                 DockerImageIdentifier.builder()
                                      .setRepository("eclipse/che")
                                      .setRegistry("my.other.registry.com:5000")
                                      .setTag("someTag")
                         .build()},
                {"my.other.registry.com:5000/eclipse/che/something:someTag",
                 DockerImageIdentifier.builder()
                                      .setRepository("eclipse/che/something")
                                      .setRegistry("my.other.registry.com:5000")
                                      .setTag("someTag")
                         .build()},
                {"my.other.registry.com:32800/eclipse/che@sha256:acd122209878932bcdafafdFADCDBafaadacdbeEFAD",
                 DockerImageIdentifier.builder()
                                      .setRepository("eclipse/che")
                                      .setRegistry("my.other.registry.com:32800")
                                      .setDigest("sha256:acd122209878932bcdafafdFADCDBafaadacdbeEFAD")
                         .build()},
                {"eclipse/che@some-digest:adfbac09548AFCBDFACDAFdcedfedfedfdde",
                 DockerImageIdentifier.builder()
                                      .setRepository("eclipse/che")
                                      .setDigest("some-digest:adfbac09548AFCBDFACDAFdcedfedfedfdde")
                         .build()},
                {"eclipse/che@some_digest:adfbac09548AFCBDFACDAFdcedfedfedfdde",
                 DockerImageIdentifier.builder()
                                      .setRepository("eclipse/che")
                                      .setDigest("some_digest:adfbac09548AFCBDFACDAFdcedfedfedfdde")
                         .build()},
                {"eclipse/che@some+digest:adfbac09548AFCBDFACDAFdcedfedfedfdde",
                 DockerImageIdentifier.builder()
                                      .setRepository("eclipse/che")
                                      .setDigest("some+digest:adfbac09548AFCBDFACDAFdcedfedfedfdde")
                         .build()},
                {"eclipse/che@some.digest:adfbac09548AFCBDFACDAFdcedfedfedfdde",
                 DockerImageIdentifier.builder()
                                      .setRepository("eclipse/che")
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
                {"my.other.registry.com:-8080/eclipse/che/some"}
        };
    }
}

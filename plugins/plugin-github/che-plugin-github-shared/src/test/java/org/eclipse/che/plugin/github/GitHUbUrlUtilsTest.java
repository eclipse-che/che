/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.github;

import org.eclipse.che.plugin.github.shared.GitHubUrlUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GitHUbUrlUtilsTest {

  private String HTTPS_URL = "https://github.com/che-samples/web-java-spring-petclinic";
  private String SOME_PATH = "che/samples/web-java-spring-petclinic";
  private String SSH_URL = "git@github.com:che-samples/web-java-spring-petclinic.git";
  private String BLOB_URL =
      "https://github.com/che-samples/web-java-spring-petclinic/blob/mysql/src/test/java/org/springframework"
          + "/samples/petclinic/service/ClinicServiceJdbcTests.java";
  private String FILE =
      "src/test/java/org/springframework"
          + "/samples/petclinic/service/ClinicServiceJdbcTests.java";

  @Test
  public void shouldConvertToHttps() {
    String https = GitHubUrlUtils.toHttpsIfNeed(SSH_URL);
    Assert.assertEquals(https, HTTPS_URL);
  }

  @Test
  public void shouldReturnSameUrl() {
    String https = GitHubUrlUtils.toHttpsIfNeed(HTTPS_URL);
    Assert.assertEquals(https, HTTPS_URL);
  }

  @Test
  public void blobUrl() {
    String blobUrl =
        GitHubUrlUtils.getBlobUrl(
            HTTPS_URL,
            "mysql",
            "src/test/java/org/springframework"
                + "/samples/petclinic/service/ClinicServiceJdbcTests.java");
    Assert.assertEquals(blobUrl, BLOB_URL);
  }

  @Test
  public void blobUrlWithLines() {
    String blobUrl =
        GitHubUrlUtils.getBlobUrl(
            HTTPS_URL,
            "mysql",
            "src/test/java/org/springframework"
                + "/samples/petclinic/service/ClinicServiceJdbcTests.java",
            10,
            20);
    System.out.println(blobUrl);
  }
}

/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.github;

import static org.testng.Assert.assertEquals;

import org.eclipse.che.plugin.github.shared.GitHubUrlUtils;
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
    assertEquals(https, HTTPS_URL);
  }

  @Test
  public void shouldReturnSameUrl() {
    String https = GitHubUrlUtils.toHttpsIfNeed(HTTPS_URL);
    assertEquals(https, HTTPS_URL);
  }

  @Test
  public void blobUrl() {
    String blobUrl =
        GitHubUrlUtils.getBlobUrl(
            HTTPS_URL,
            "mysql",
            "src/test/java/org/springframework"
                + "/samples/petclinic/service/ClinicServiceJdbcTests.java");
    assertEquals(blobUrl, BLOB_URL);
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
    assertEquals(blobUrl, BLOB_URL + "#L10-L20");
  }

  @Test
  public void getTreeUrl() {
    String rootGitRepoUrl = "https://github.com/eclipse/che/";
    String ref = "master";
    String path = "assembly/assembly-wsmaster-war";
    String getTreeUrl = GitHubUrlUtils.getTreeUrl(rootGitRepoUrl, ref, path);
    assertEquals(getTreeUrl, rootGitRepoUrl + "/tree/" + ref + "/" + path);
  }
}

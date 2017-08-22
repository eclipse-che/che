/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.client;

import static java.util.stream.Collectors.toList;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.eclipse.che.api.core.rest.HttpJsonResponse;
import org.eclipse.che.dto.server.JsonStringMapImpl;
import org.eclipse.che.plugin.github.shared.GitHubKey;

/** @author Mihail Kuznyetsov. */
@Singleton
public class TestGitHubServiceClient {
  private final HttpJsonRequestFactory requestFactory;

  @Inject
  public TestGitHubServiceClient(HttpJsonRequestFactory requestFactory) {
    this.requestFactory = requestFactory;
  }

  public void deletePublicKeys(final String username, final String password, final String keyTitle)
      throws Exception {
    List<GitHubKey> keys = getPublicKeys(username, password, keyTitle);
    for (GitHubKey key : keys) {
      requestFactory
          .fromUrl(key.getUrl())
          .setAuthorizationHeader(createBasicAuthHeader(username, password))
          .useDeleteMethod()
          .request();
    }
  }

  public void createPublicKey(final String username, final String password, final GitHubKey key)
      throws Exception {
    requestFactory
        .fromUrl("https://api.github.com/user/keys")
        .setAuthorizationHeader(createBasicAuthHeader(username, password))
        .usePostMethod()
        .setBody(key)
        .request();
  }

  public void uploadPublicKey(final String username, final String password, final String key)
      throws Exception {
    final String sshKeyTitle = "QA selenium test";

    GitHubKey publicSshKey = newDto(GitHubKey.class);
    publicSshKey.setTitle(sshKeyTitle);
    publicSshKey.setKey(key);

    deletePublicKeys(username, password, sshKeyTitle);
    createPublicKey(username, password, publicSshKey);
  }

  public List<GitHubKey> getPublicKeys(
      final String username, final String password, final String title) throws Exception {
    List<GitHubKey> keys =
        requestFactory
            .fromUrl("https://api.github.com/user/keys")
            .setAuthorizationHeader(createBasicAuthHeader(username, password))
            .useGetMethod()
            .request()
            .asList(GitHubKey.class);

    return keys.stream().filter(key -> title.equals(key.getTitle())).collect(toList());
  }

  public void hardResetHeadToCommit(
      final String repository, final String commitSha, final String username, final String password)
      throws Exception {
    ImmutableMap<String, Object> m = ImmutableMap.of("sha", commitSha, "force", true);

    String url =
        "https://api.github.com/repos/" + username + "/" + repository + "/git/refs/heads/master";
    requestFactory
        .fromUrl(url)
        .usePostMethod()
        .setAuthorizationHeader(createBasicAuthHeader(username, password))
        .setBody(new JsonStringMapImpl<Object>(m))
        .request();
  }

  @SuppressWarnings("unchecked")
  public List<String> getNumbersOfOpenedPullRequests(
      final String repository, final String username, final String password) throws Exception {
    String url = "https://api.github.com/repos/" + username + "/" + repository + "/pulls";
    HttpJsonResponse response =
        requestFactory
            .fromUrl(url)
            .useGetMethod()
            .setAuthorizationHeader(createBasicAuthHeader(username, password))
            .request();
    List<Map<String, String>> prs =
        response.as(List.class, new TypeToken<List<Map<String, String>>>() {}.getType());
    return prs.stream()
        .filter(g -> g.get("state").equals("open"))
        .map(g -> g.get("number"))
        .collect(toList());
  }

  public void closePullRequest(
      final String repo, final String number, final String username, final String password)
      throws Exception {
    String url = "https://api.github.com/repos/" + username + "/" + repo + "/pulls/" + number;
    requestFactory
        .fromUrl(url)
        .usePostMethod()
        .setAuthorizationHeader(createBasicAuthHeader(username, password))
        .setBody(ImmutableMap.of("state", "close"))
        .request();
  }

  public void deleteBranch(
      final String repository,
      final String branchName,
      final String username,
      final String password)
      throws Exception {
    String url =
        "https://api.github.com/repos/"
            + username
            + "/"
            + repository
            + "/git/refs/heads/"
            + branchName;
    requestFactory
        .fromUrl(url)
        .useDeleteMethod()
        .setAuthorizationHeader(createBasicAuthHeader(username, password))
        .request();
  }

  public void deleteRepo(final String repository, final String username, final String password)
      throws Exception {
    String url = "https://api.github.com/repos/" + username + "/" + repository;
    requestFactory
        .fromUrl(url)
        .useDeleteMethod()
        .setAuthorizationHeader(createBasicAuthHeader(username, password))
        .request();
  }

  public List<String> getAllGrants(final String username, final String password) throws Exception {
    String url = "https://api.github.com/applications/grants";
    HttpJsonResponse response =
        requestFactory
            .fromUrl(url)
            .useGetMethod()
            .setAuthorizationHeader(createBasicAuthHeader(username, password))
            .request();
    @SuppressWarnings("unchecked")
    List<Map<String, String>> grants =
        response.as(List.class, new TypeToken<List<Map<String, String>>>() {}.getType());

    return grants.stream().map(g -> g.get("id")).collect(toList());
  }

  public void deleteAllGrants(final String username, final String password) throws Exception {
    List<String> grandsId = getAllGrants(username, password);
    for (String grandId : grandsId) {
      String url = "https://api.github.com/applications/grants/" + grandId;
      requestFactory
          .fromUrl(url)
          .useDeleteMethod()
          .setAuthorizationHeader(createBasicAuthHeader(username, password))
          .request();
    }
  }

  public String getName(final String username, final String password) throws Exception {
    String url = "https://api.github.com/users/" + username;
    HttpJsonResponse response =
        requestFactory
            .fromUrl(url)
            .useGetMethod()
            .setAuthorizationHeader(createBasicAuthHeader(username, password))
            .request();
    Map<String, String> properties = response.asProperties();
    return properties.getOrDefault("name", properties.get("login"));
  }

  private String createBasicAuthHeader(String username, String password)
      throws UnsupportedEncodingException {
    byte[] nameAndPass = (username + ":" + password).getBytes("UTF-8");
    String base64 = DatatypeConverter.printBase64Binary(nameAndPass);
    return "Basic " + base64;
  }
}

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
package org.eclipse.che.plugin.github.server;

import com.google.common.base.Strings;
import com.google.inject.Singleton;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.eclipse.che.api.core.UnauthorizedException;
import org.eclipse.che.api.git.GitUrlUtils;
import org.eclipse.che.commons.json.JsonHelper;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.github.shared.GitHubKey;
import org.eclipse.che.plugin.ssh.key.script.SshKeyUploader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** @author Vladyslav Zhukovskii */
@Singleton
public class GitHubKeyUploader implements SshKeyUploader {

  private static final Logger LOG = LoggerFactory.getLogger(GitHubKeyUploader.class);
  private static final Pattern GITHUB_URL_PATTERN = Pattern.compile(".*github\\.com.*");

  @Override
  public boolean match(String url) {
    return GitUrlUtils.isSSH(url) && GITHUB_URL_PATTERN.matcher(url).matches();
  }

  @Override
  public void uploadKey(String publicKey, String oauthToken)
      throws IOException, UnauthorizedException {

    if (Strings.isNullOrEmpty(oauthToken)) {
      LOG.debug("Token not found, user need to authorize to upload key.");
      throw new UnauthorizedException("To upload SSH key you need to authorize.");
    }

    StringBuilder answer = new StringBuilder();
    final String url =
        String.format("https://api.github.com/user/keys?access_token=%s", oauthToken);

    final List<GitHubKey> gitHubUserPublicKeys = getUserPublicKeys(url, answer);
    for (GitHubKey gitHubUserPublicKey : gitHubUserPublicKeys) {
      if (publicKey.startsWith(gitHubUserPublicKey.getKey())) {
        return;
      }
    }

    final Map<String, String> postParams = new HashMap<>(2);
    postParams.put("title", "IDE SSH Key (" + new SimpleDateFormat().format(new Date()) + ")");
    postParams.put("key", new String(publicKey.getBytes()));

    final String postBody = JsonHelper.toJson(postParams);

    LOG.debug("Upload public key: {}", postBody);

    int responseCode;
    HttpURLConnection conn = null;
    try {
      conn = (HttpURLConnection) new URL(url).openConnection();
      conn.setInstanceFollowRedirects(false);
      conn.setRequestMethod(HttpMethod.POST);
      conn.setRequestProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
      conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
      conn.setRequestProperty(HttpHeaders.CONTENT_LENGTH, String.valueOf(postBody.length()));
      conn.setDoOutput(true);
      try (OutputStream out = conn.getOutputStream()) {
        out.write(postBody.getBytes());
      }
      responseCode = conn.getResponseCode();
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }

    LOG.debug("Upload key response code: {}", responseCode);

    if (responseCode != HttpURLConnection.HTTP_CREATED) {
      throw new IOException(
          String.format("%d: Failed to upload public key to https://github.com/", responseCode));
    }
  }

  private List<GitHubKey> getUserPublicKeys(String url, StringBuilder answer) {
    HttpURLConnection conn = null;
    try {
      conn = (HttpURLConnection) new URL(url).openConnection();
      conn.setInstanceFollowRedirects(false);
      conn.setRequestMethod(HttpMethod.GET);
      conn.setRequestProperty(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON);
      if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
        try (BufferedReader reader =
            new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
          String line;
          while ((line = reader.readLine()) != null) {
            answer.append(line).append('\n');
          }
        }
        if (conn.getHeaderFields().containsKey("Link")) {
          String strForParsing = conn.getHeaderFields().get("Link").get(0);
          int indexNext = strForParsing.indexOf("rel=\"next\"");

          if (indexNext != -1) {
            String nextSubStr = strForParsing.substring(0, indexNext);
            String nextPageLink =
                nextSubStr.substring(nextSubStr.indexOf("<") + 1, nextSubStr.indexOf(">"));

            getUserPublicKeys(nextPageLink, answer);
          }
          int indexToReplace;
          while ((indexToReplace = answer.indexOf("]\n[")) != -1) {
            answer.replace(indexToReplace, indexToReplace + 3, ",");
          }
        }
        return DtoFactory.getInstance().createListDtoFromJson(answer.toString(), GitHubKey.class);
      }
      return Collections.emptyList();
    } catch (IOException e) {
      LOG.error(e.getMessage(), e);
      return Collections.emptyList();
    } finally {
      if (conn != null) {
        conn.disconnect();
      }
    }
  }
}

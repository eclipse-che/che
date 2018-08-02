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
package org.eclipse.che.ide.ext.java.client.formatter;

import static org.eclipse.che.ide.MimeType.TEXT_PLAIN;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENT_TYPE;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.ext.java.shared.dto.Change;
import org.eclipse.che.ide.rest.AsyncRequestFactory;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;

/**
 * Implementation of java formatter service. Have a functionality to format java code and update
 * formatter configuration for the project or for whole workspace.
 */
@Singleton
public class JavaFormatterServiceClient {
  private static final String FORMATTER_URL_PREFIX = "/java/formatter";

  private AppContext appContext;
  private AsyncRequestFactory asyncRequestFactory;
  private DtoUnmarshallerFactory unmarshallerFactory;

  @Inject
  public JavaFormatterServiceClient(
      AppContext appContext,
      AsyncRequestFactory asyncRequestFactory,
      DtoUnmarshallerFactory unmarshallerFactory) {
    this.appContext = appContext;
    this.asyncRequestFactory = asyncRequestFactory;
    this.unmarshallerFactory = unmarshallerFactory;
  }

  /**
   * Creates edits that describe how to format the given string. Returns the changes required to
   * format source. Note: Java code formatting is supported only.
   *
   * @param projectPath path to the parent project
   * @param offset The given offset to start recording the edits (inclusive).
   * @param length the given length to stop recording the edits (exclusive).
   * @param content the content to format
   */
  public Promise<List<Change>> format(
      String projectPath, final int offset, final int length, final String content) {
    return getFormatChanges(projectPath, offset, length, content)
        .then((Function<List<Change>, List<Change>>) ArrayList::new);
  }

  /**
   * Updates formatter for the whole workspace. This formatter will be applied if a project doesn't
   * have its own Note: Eclipse formatter is supported only.
   *
   * @param formatter configuration of the formatter
   */
  public Promise<Void> updateRootFormatter(String formatter) {
    final String baseUrl = appContext.getWsAgentServerApiEndpoint();
    final String url = baseUrl + FORMATTER_URL_PREFIX + "/update/workspace";
    return asyncRequestFactory
        .createPostRequest(url, null)
        .header(CONTENT_TYPE, TEXT_PLAIN)
        .data(formatter)
        .send();
  }

  /**
   * Updates formatter for the project. Note: Eclipse formatter is supported only.
   *
   * @param formatter configuration of the formatter
   * @param projectPath path to the project
   */
  public Promise<Void> updateProjectFormatter(String formatter, String projectPath) {
    final String baseUrl = appContext.getWsAgentServerApiEndpoint();
    final String url =
        baseUrl + FORMATTER_URL_PREFIX + "/update/project?projectpath=" + projectPath;
    return asyncRequestFactory
        .createPostRequest(url, null)
        .header(CONTENT_TYPE, TEXT_PLAIN)
        .data(formatter)
        .send();
  }

  private Promise<List<Change>> getFormatChanges(
      String projectPath, final int offset, final int length, final String content) {
    final String baseUrl = appContext.getWsAgentServerApiEndpoint();
    final String url =
        baseUrl
            + FORMATTER_URL_PREFIX
            + "/format?projectpath="
            + projectPath
            + "&offset="
            + offset
            + "&length="
            + length;
    return asyncRequestFactory
        .createPostRequest(url, null)
        .header(CONTENT_TYPE, TEXT_PLAIN)
        .data(content)
        .send(unmarshallerFactory.newListUnmarshaller(Change.class));
  }
}

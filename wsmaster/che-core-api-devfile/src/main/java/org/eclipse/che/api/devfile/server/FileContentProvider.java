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
package org.eclipse.che.api.devfile.server;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.devfile.server.exception.DevfileException;

/**
 * Some types of {@link org.eclipse.che.api.devfile.model.Component} may have configuration located
 * in a separate file. It fetches content of file by its name.
 *
 * @author Max Shaposhnyk
 * @author Sergii Leshchenko
 */
public interface FileContentProvider {

  /**
   * Short for {@code new CachingProvider(contentProvider);}. If the {@code contentProvider} is
   * itself an instance of the {@link CachingProvider}, no new instance is produced.
   *
   * @param contentProvider the content provider to cache
   * @return a file content provider that caches the responses
   */
  static FileContentProvider cached(FileContentProvider contentProvider) {
    if (contentProvider instanceof CachingProvider) {
      return contentProvider;
    } else {
      return new CachingProvider(contentProvider);
    }
  }

  /**
   * Fetches content of the specified file.
   *
   * @param fileName file name to fetch content. Only devfile-relative files are currently
   *     supported, so it means file should be localed at the same directory level as devfile (no
   *     matter in repository or PR or branch etc )
   * @return content of the specified file
   * @throws IOException when there is an error during content retrieval
   * @throws DevfileException when implementation does not support fetching of additional files
   *     content
   */
  String fetchContent(String fileName) throws IOException, DevfileException;

  /** Default implementation of {@link FileContentProvider} that does not support fetching. */
  class FetchNotSupportedProvider implements FileContentProvider {

    private String message;

    public FetchNotSupportedProvider() {
      this.message = "File content fetching is not supported";
    }

    public FetchNotSupportedProvider(String message) {
      this.message = message;
    }

    @Override
    public String fetchContent(String fileName) throws DevfileException {
      throw new DevfileException(message);
    }
  }

  /**
   * A file content provider that caches responses from the content provider it is wrapping. Useful
   * in situations where repeated calls to the {@link #fetchContent(String)} are necessary.
   */
  class CachingProvider implements FileContentProvider {

    private final FileContentProvider provider;

    // we don't want to be holding on to large strings with content
    private final Map<String, SoftReference<String>> cache = new HashMap<>();

    public CachingProvider(FileContentProvider provider) {
      this.provider = provider;
    }

    @Override
    public String fetchContent(String fileName) throws IOException, DevfileException {
      SoftReference<String> ref = cache.get(fileName);
      String ret = ref == null ? null : ref.get();

      if (ret == null) {
        ret = provider.fetchContent(fileName);
        cache.put(fileName, new SoftReference<>(ret));
      }

      return ret;
    }
  }
}

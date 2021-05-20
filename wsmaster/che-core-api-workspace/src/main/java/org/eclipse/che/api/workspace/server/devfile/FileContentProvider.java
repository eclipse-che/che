/*
 * Copyright (c) 2012-2021 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server.devfile;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;

/**
 * Interface for fetching devfile-related content from repositories or raw URL-s. Used for
 * retrieving devfile content as well as referenced files.
 *
 * @author Max Shaposhnyk
 * @author Sergii Leshchenko
 */
public interface FileContentProvider {

  /**
   * Fetches content of the specified file.
   *
   * @param fileURL absolute or devfile-relative file URL to fetch content
   * @return content of the specified file
   * @throws IOException when there is an error during content retrieval
   * @throws DevfileException when implementation does not support fetching of additional files
   *     content
   */
  String fetchContent(String fileURL) throws IOException, DevfileException;

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
    public String fetchContent(String fileURL) throws IOException, DevfileException {
      SoftReference<String> ref = cache.get(fileURL);
      String ret = ref == null ? null : ref.get();

      if (ret == null) {
        ret = provider.fetchContent(fileURL);
        cache.put(fileURL, new SoftReference<>(ret));
      }

      return ret;
    }
  }
}

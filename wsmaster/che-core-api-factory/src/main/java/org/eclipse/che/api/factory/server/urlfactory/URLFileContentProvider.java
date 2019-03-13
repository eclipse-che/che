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
package org.eclipse.che.api.factory.server.urlfactory;

import static java.lang.String.format;

import java.io.IOException;
import java.net.URI;
import org.eclipse.che.api.devfile.server.FileContentProvider;

/**
 * A simple implementation of the FileContentProvider that merely uses the function resolve relative
 * paths and {@link URLFetcher} for retrieving the content, handling common error cases.
 */
public class URLFileContentProvider implements FileContentProvider {

  private final URI devfileLocation;
  private final URLFetcher urlFetcher;

  public URLFileContentProvider(URI devfileLocation, URLFetcher urlFetcher) {
    this.devfileLocation = devfileLocation;
    this.urlFetcher = urlFetcher;
  }

  @Override
  public String fetchContent(String fileName) throws IOException {
    URI fileUrl = devfileLocation.resolve(fileName);
    try {
      return urlFetcher.fetch(fileUrl.toString());
    } catch (IOException e) {
      throw new IOException(
          format(
              "Failed to fetch a file %s as relative to devfile %s from URL %s. Make sure the URL"
                  + " of the devfile points to the raw content of it (e.g. not to the webpage"
                  + " showing it but really just its contents). Additionally, make sure the"
                  + " referenced files are actually stored relative to the devfile on the same"
                  + " host. If none of that is possible, try to host your devfile on some other"
                  + " location together with the referenced files in such a way that resolving the"
                  + " \"local\" name of a tool as a relative path against the devfile location"
                  + " gives a true downloadable URL for that file. The current attempt to download"
                  + " the file failed with the following error message: %s",
              fileName, devfileLocation, fileUrl, e.getMessage()),
          e);
    }
  }
}

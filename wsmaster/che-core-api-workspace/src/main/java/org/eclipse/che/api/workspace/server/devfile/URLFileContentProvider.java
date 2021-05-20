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

import static java.lang.String.format;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import org.eclipse.che.api.workspace.server.devfile.exception.DevfileException;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * A simple implementation of the FileContentProvider that merely uses the function resolve relative
 * paths and {@link URLFetcher} for retrieving the content, handling common error cases.
 */
public class URLFileContentProvider implements FileContentProvider {

  private final URI devfileLocation;
  private final URLFetcher urlFetcher;

  public URLFileContentProvider(@Nullable URI devfileLocation, URLFetcher urlFetcher) {
    this.devfileLocation = devfileLocation;
    this.urlFetcher = urlFetcher;
  }

  @Override
  public String fetchContent(String fileURL) throws IOException, DevfileException {
    URI fileURI;
    String requestURL;
    try {
      fileURI = new URI(fileURL);
    } catch (URISyntaxException e) {
      throw new DevfileException(e.getMessage(), e);
    }

    if (fileURI.isAbsolute()) {
      requestURL = fileURL;
    } else {
      if (devfileLocation == null) {
        throw new DevfileException(
            format(
                "It is unable to fetch a file %s as relative to devfile, since devfile location"
                    + " is unknown. Try specifying absolute URL.",
                fileURL));
      }
      requestURL = devfileLocation.resolve(fileURI).toString();
    }
    try {
      return urlFetcher.fetch(requestURL);
    } catch (IOException e) {
      throw new IOException(
          format(
              "Failed to fetch a file from URL %s. Make sure the URL"
                  + " of the devfile points to the raw content of it (e.g. not to the webpage"
                  + " showing it but really just its contents). Additionally, if you're using "
                  + " relative form, make sure the referenced files are actually stored"
                  + " relative to the devfile on the same host,"
                  + " or try to specify URL in absolute form. The current attempt to download"
                  + " the file failed with the following error message: %s",
              fileURL, e.getMessage()),
          e);
    }
  }
}

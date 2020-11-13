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
package org.eclipse.che.workspace.infrastructure.kubernetes;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.core.Response;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;

public class DirectKubernetesAPIAccessHelper {
  private DirectKubernetesAPIAccessHelper() {}

  public static Response call(
      String masterUrl,
      OkHttpClient httpClient,
      String httpMethod,
      URI relativeUri,
      @Nullable JsonNode body)
      throws InfrastructureException {
    if (relativeUri.isAbsolute() || relativeUri.isOpaque()) {
      throw new InfrastructureException(
          "The direct infrastructure URL must be relative and not opaque.");
    }

    try {
      URI fullUrl = new URI(masterUrl).resolve(relativeUri);
      RequestBody requestBody =
          body == null
              ? null
              : RequestBody.create(MediaType.parse("application/json"), body.toString());
      Call httpCall =
          httpClient.newCall(
              new Request.Builder().url(fullUrl.toURL()).method(httpMethod, requestBody).build());

      okhttp3.Response response = httpCall.execute();
      Response.ResponseBuilder bld = Response.status(response.code());
      for (int i = 0; i < response.headers().size(); ++i) {
        String name = response.headers().name(i);
        String value = response.headers().value(i);
        bld.header(name, value);
      }

      ResponseBody responseBody = response.body();
      if (responseBody != null) {
        bld.entity(responseBody.byteStream());
        MediaType contentType = responseBody.contentType();
        if (contentType != null) {
          bld.type(contentType.toString());
        }
      }

      return bld.build();
    } catch (URISyntaxException | MalformedURLException e) {
      throw new InfrastructureException("Could not compose the direct URI.", e);
    } catch (IOException e) {
      throw new InfrastructureException("Error sending the direct infrastructure request.", e);
    }
  }
}

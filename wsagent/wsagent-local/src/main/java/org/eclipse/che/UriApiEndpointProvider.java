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
package org.eclipse.che;

import java.net.URI;
import javax.inject.Provider;

/**
 * Provides URI of Che API endpoint for usage inside machine to be able to connect to host machine
 * using docker host IP.
 *
 * @author Alexander Garagatyi
 */
public class UriApiEndpointProvider implements Provider<URI> {

  public static final String API_ENDPOINT_URL_VARIABLE = "CHE_API";

  @Override
  public URI get() {
    try {
      return new URI(System.getenv(API_ENDPOINT_URL_VARIABLE));
    } catch (Exception e) {
      throw new RuntimeException(
          "System variable CHE_API contain invalid value of Che api endpoint:"
              + System.getenv(API_ENDPOINT_URL_VARIABLE));
    }
  }
}

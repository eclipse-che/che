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
package org.eclipse.che.api.core.rest;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

/**
 * Endpoint for the liveness checks.
 *
 * @author Max Shaposhnik (mshaposh@redhat.com)
 */
@Singleton
@Path("/liveness")
public class LivenessProbeService {
  @GET
  public Response checkAlive() {
    return Response.ok().build();
  }
}

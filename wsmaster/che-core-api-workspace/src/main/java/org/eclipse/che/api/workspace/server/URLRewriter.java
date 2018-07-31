/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.workspace.server;

import org.eclipse.che.api.core.model.workspace.runtime.RuntimeIdentity;
import org.eclipse.che.api.workspace.server.spi.InfrastructureException;
import org.eclipse.che.commons.annotation.Nullable;

/**
 * System specific strategy for rewriting URLs to use in rewriting Servers, Hyperlinks, etc For
 * example in a case when machines supposed to be accessible via reverse Proxy.
 *
 * @author gazarenkov
 */
public interface URLRewriter {

  /**
   * Rewrites URL according to URL rewriting strategy rules. May depend on
   * RuntimeIdentityImpl(workspace, owner,..) and name (some id) of this particular URL
   *
   * @param identity RuntimeIdentityImpl
   * @param machineName symbolic name of the machine
   * @param serverName symbolic name of the server
   * @param url URL to rewrite
   * @return rewritten URL (may be unchanged)
   * @throws InfrastructureException if URL rewriting failed
   */
  String rewriteURL(
      @Nullable RuntimeIdentity identity,
      @Nullable String machineName,
      @Nullable String serverName,
      String url)
      throws InfrastructureException;

  /** No rewriting, just pass URL back */
  class NoOpURLRewriter implements URLRewriter {

    @Override
    public String rewriteURL(
        @Nullable RuntimeIdentity identity,
        @Nullable String machineName,
        @Nullable String serverName,
        String url)
        throws InfrastructureException {

      return url;
    }
  }
}

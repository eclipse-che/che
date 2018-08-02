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
package org.eclipse.che.workspace.activity;

import java.io.IOException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Counts every HTTP request to the agent as a workspace activity
 *
 * @author Mihail Kuznyetsov
 */
@Singleton
public class LastAccessTimeFilter implements Filter {
  private static final Logger LOG = LoggerFactory.getLogger(LastAccessTimeFilter.class);

  private final WorkspaceActivityNotifier wsActivityEventSender;

  @Inject
  public LastAccessTimeFilter(WorkspaceActivityNotifier wsActivityEventSender) {
    this.wsActivityEventSender = wsActivityEventSender;
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {}

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    try {
      wsActivityEventSender.onActivity();
    } catch (Exception e) {
      LOG.error("Failed to notify about the workspace activity", e);
    } finally {
      chain.doFilter(request, response);
    }
  }

  @Override
  public void destroy() {}
}

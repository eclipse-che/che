/*
 *  [2012] - [2017] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package org.eclipse.che.api.agent.server.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;

/**
 * Counts every request to the agent as a workspace activity
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
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        try {
            wsActivityEventSender.onActivity();
        } catch (Exception e) {
            LOG.error("Failed to notify about the workspace activity", e);
        } finally {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {
    }
}

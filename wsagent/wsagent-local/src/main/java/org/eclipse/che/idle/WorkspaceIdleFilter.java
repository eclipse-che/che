/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.idle;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.eclipse.che.WorkspaceIdProvider;
import org.eclipse.che.api.core.rest.HttpJsonRequestFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class WorkspaceIdleFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(WorkspaceIdleFilter.class);

    private final long timeout;
    private HttpJsonRequestFactory httpJsonRequestFactory;
    private String apiEndpoint;
    private ScheduledFuture<?> future;
    private ScheduledExecutorService executor;
    private AtomicBoolean stopped;

    @Inject
    public WorkspaceIdleFilter(HttpJsonRequestFactory httpJsonRequestFactory, @Named("che.api") String apiEndpoint,
            @Named("che.machine.ws.agent.inactive.stop.timeout.ms") long timeout) {
        this.timeout = timeout;
        this.stopped = new AtomicBoolean();
        if (timeout > 0) {
            this.apiEndpoint = apiEndpoint;
            this.httpJsonRequestFactory = httpJsonRequestFactory;
            this.executor = Executors.newSingleThreadScheduledExecutor();
            this.future = executor.schedule(this::sendEvent, timeout, TimeUnit.MILLISECONDS);
            this.stopped.set(false);
        } else {
            this.stopped.set(true);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!stopped.get()) {
            future.cancel(true);
            future = executor.schedule(this::sendEvent, timeout, TimeUnit.MILLISECONDS);
        }
        chain.doFilter(request, response);
    }

    private void sendEvent() {
        try {
            httpJsonRequestFactory.fromUrl(apiEndpoint + "/workspace/" + WorkspaceIdProvider.getWorkspaceId() + "/runtime")
                                  .useDeleteMethod()
                                  .request();
            LOG.info("Stopping workspace " + WorkspaceIdProvider.getWorkspaceId() + " after "
                                  + timeout / 1000 + " seconds of inactivity.");
        } catch (Exception e) {
            LOG.error("Cannot stop workspace " + WorkspaceIdProvider.getWorkspaceId(), e);
        } finally {
            stopped.set(true);
        }
    }

    @Override
    public void destroy() {
        if (future != null && !future.isCancelled()) {
            future.cancel(true);
        }
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }

}

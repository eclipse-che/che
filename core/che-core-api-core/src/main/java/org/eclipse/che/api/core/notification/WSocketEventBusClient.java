/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.core.notification;

import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.commons.lang.Pair;
import org.eclipse.che.commons.lang.concurrent.LoggingUncaughtExceptionHandler;
import org.everrest.websockets.client.BaseClientMessageListener;
import org.everrest.websockets.client.WSClient;
import org.everrest.websockets.message.JsonMessageConverter;
import org.everrest.websockets.message.RestOutputMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.websocket.DeploymentException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Receives event over websocket and publish them to the local EventsService.
 *
 * @author andrew00x
 */
@Singleton
public final class WSocketEventBusClient {
    private static final Logger LOG = LoggerFactory.getLogger(WSocketEventBusClient.class);

    private static final long WS_CONNECTION_TIMEOUT = 2;
    private static final long MAX_ATTEMPTS          = 10;

    public static final String WORKSPACE_AGENT_STOPPED_MESSAGE = "The workspace agent has been forcefully stopped. " +
                                                                 "This error happens when the agent cannot resolve the location of the Che server. " +
                                                                 "This error can usually be fixed with additional configuration settings in /conf/che.properties. " +
                                                                 "The Che server will stop this workspace after a short timeout. " +
                                                                 "You can get help by posting your config, stacktrace and workspace /etc/hosts below as a GitHub issue.";

    private final EventService                         eventService;
    private final Pair<String, String>[]               eventSubscriptions;
    private final ClientEventPropagationPolicy         policy;
    private final JsonMessageConverter                 messageConverter;
    private final ConcurrentMap<URI, Future<WSClient>> connections;
    private final AtomicBoolean                        start;

    private ExecutorService executor;

    @Inject
    public WSocketEventBusClient(EventService eventService,
                                 @Nullable @Named("notification.client.event_subscriptions") Pair<String, String>[] eventSubscriptions,
                                 @Nullable ClientEventPropagationPolicy policy) {
        this.eventService = eventService;
        this.eventSubscriptions = eventSubscriptions;
        this.policy = policy;

        messageConverter = new JsonMessageConverter();
        connections = new ConcurrentHashMap<>();
        start = new AtomicBoolean(false);
    }

    @PostConstruct
    void start() {
        if (start.compareAndSet(false, true)) {
            if (policy != null) {
                eventService.subscribe(new EventSubscriber<Object>() {
                    @Override
                    public void onEvent(Object event) {
                        propagate(event);
                    }
                });
            }
            if (eventSubscriptions != null) {
                final Map<URI, Set<String>> cfg = new HashMap<>();
                for (Pair<String, String> service : eventSubscriptions) {
                    try {
                        final URI key = new URI(service.first);
                        Set<String> values = cfg.get(key);
                        if (values == null) {
                            cfg.put(key, values = new LinkedHashSet<>());
                        }
                        if (service.second != null) {
                            values.add(service.second);
                        }
                    } catch (URISyntaxException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
                if (!cfg.isEmpty()) {
                    executor = Executors.newCachedThreadPool(new ThreadFactoryBuilder().setNameFormat("WSocketEventBusClient-%d")
                                                                                       .setUncaughtExceptionHandler(
                                                                                               LoggingUncaughtExceptionHandler
                                                                                                       .getInstance())
                                                                                       .setDaemon(true).build());
                    for (Map.Entry<URI, Set<String>> entry : cfg.entrySet()) {
                        executor.execute(new ConnectTask(entry.getKey(), entry.getValue()));
                    }
                }
            }
        }
    }

    protected void propagate(Object event) {
        connections.values().stream().filter(future -> future.isDone()).forEach(future -> {
            try {
                final WSClient client = future.get();
                if (policy != null && policy.shouldPropagated(client.getServerUri(), event)) {
                    client.send(messageConverter.toString(Messages.clientMessage(event)));
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        });
    }

    @PreDestroy
    void stop() {
        if (start.compareAndSet(true, false) && executor != null) {
            executor.shutdownNow();
        }
    }

    private void connect(final URI wsUri, final Collection<String> channels) throws IOException, DeploymentException {
        Future<WSClient> clientFuture = connections.get(wsUri);
        if (clientFuture == null) {
            FutureTask<WSClient> newFuture = new FutureTask<>(() -> {
                WSClient wsClient = new WSClient(wsUri, new WSocketListener(wsUri, channels));
                wsClient.connect((int)WS_CONNECTION_TIMEOUT);
                return wsClient;
            });
            clientFuture = connections.putIfAbsent(wsUri, newFuture);
            if (clientFuture == null) {
                clientFuture = newFuture;
                newFuture.run();
            }
        }
        boolean connected = false;
        try {
            clientFuture.get(); // wait for connection
            connected = true;
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof Error) {
                throw (Error)cause;
            } else if (cause instanceof RuntimeException) {
                throw (RuntimeException)cause;
            } else if (cause instanceof IOException) {
                throw (IOException)cause;
            } else if (cause instanceof DeploymentException)
                throw (DeploymentException)cause;
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            LOG.info("Client interrupted " + e.getLocalizedMessage());
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            if (!connected) {
                connections.remove(wsUri);
            }
        }
    }

    private class WSocketListener extends BaseClientMessageListener {
        final URI         wsUri;
        final Set<String> channels;

        WSocketListener(URI wsUri, Collection<String> channels) {
            this.wsUri = wsUri;
            this.channels = new HashSet<>(channels);
        }

        @Override
        public void onClose(int status, String message) {
            connections.remove(wsUri);
            LOG.info("Close connection to {} with status {} message {}. ", wsUri, status, message);
            LOG.info("Init connection task {}", wsUri);
            if (start.get()) {
                executor.execute(new ConnectTask(wsUri, channels));
            }
        }

        @Override
        public void onMessage(String data) {
            try {
                final RestOutputMessage message = messageConverter.fromString(data, RestOutputMessage.class);
                if (message != null && message.getHeaders() != null) {
                    for (org.everrest.websockets.message.Pair header : message.getHeaders()) {
                        if ("x-everrest-websocket-channel".equals(header.getName())) {
                            final String channel = header.getValue();
                            if (channel != null && channels.contains(channel)) {
                                final Object event = Messages.restoreEventFromBroadcastMessage(message);
                                if (event != null) {
                                    eventService.publish(event);
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }

        @Override
        public void onOpen(WSClient client) {
            LOG.info("Open connection to {}. ", wsUri);
            for (String channel : channels) {
                try {
                    client.send(messageConverter.toString(Messages.subscribeChannelMessage(channel)));
                } catch (Exception e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    private class ConnectTask implements Runnable {
        final URI                wsUri;
        final Collection<String> channels;

        ConnectTask(URI wsUri, Collection<String> channels) {
            this.wsUri = wsUri;
            this.channels = channels;
        }

        @Override
        public void run() {
            int reconnectAttempts = 0;
            for (; ; ) {

                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                LOG.debug("Start connection loop {} channels {} ", wsUri, channels);
                try {
                    connect(wsUri, channels);
                    reconnectAttempts = 0;
                    LOG.debug("Connection complete");
                    return;
                } catch (IOException | DeploymentException e) {
                    LOG.warn("Not able to connect to {} because {}. Retrying ", wsUri, e.getLocalizedMessage());
                    LOG.debug(e.getLocalizedMessage(), e);

                    if (reconnectAttempts++ > MAX_ATTEMPTS) {
                        LOG.error(WORKSPACE_AGENT_STOPPED_MESSAGE);

                        try {
                            // content of /etc/hosts file may provide clues on why the connection failed, e.g. how che-host is resolved
                            LOG.info("Workspace /etc/hosts: " + IoUtil.readAndCloseQuietly(new FileInputStream(new File("/etc/hosts"))));
                        } catch (Exception ex) {
                            LOG.info(e.getLocalizedMessage(), ex);
                        }

                        System.exit(0);
                    }

                    synchronized (this) {
                        try {
                            wait(WS_CONNECTION_TIMEOUT * 2 * 1000);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            return;
                        }
                    }
                } catch (Throwable e) {
                    LOG.error("Unexpected here");
                    LOG.error(e.getLocalizedMessage(), e);
                }
                LOG.debug("Iteration complete");
            }
        }
    }
}

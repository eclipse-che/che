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
package org.eclipse.che.api.analytics.logger;

import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author Anatoliy Bazko
 */
@Singleton
public class EventLogger {
    private static final Logger LOG = LoggerFactory.getLogger(EventLogger.class);

    public static final String DASHBOARD_SOURCE = "com.codenvy.dashboard";

    public static final String EVENT_PARAM        = "EVENT";
    public static final String WS_PARAM           = "WS";
    public static final String USER_PARAM         = "USER";
    public static final String SOURCE_PARAM       = "SOURCE";
    public static final String ACTION_PARAM       = "ACTION";
    public static final String PROJECT_NAME_PARAM = "PROJECT";
    public static final String PROJECT_TYPE_PARAM = "TYPE";
    public static final String PARAMETERS_PARAM   = "PARAMETERS";

    public static final String IDE_USAGE             = "ide-usage";
    public static final String PROJECT_OPENED        = "project-opened";
    public static final String DASHBOARD_USAGE       = "dashboard-usage";
    public static final String USER_INVITE           = "user-invite";
    public static final String SESSION_USAGE         = "session-usage";
    public static final String SESSION_FACTORY_USAGE = "session-factory-usage";

    private static final int MAX_EXTENDED_PARAMS_NUMBER = 3;
    private static final int RESERVED_PARAMS_NUMBER     = 6;
    private static final int MAX_PARAM_NAME_LENGTH      = 20;
    private static final int MAX_PARAM_VALUE_LENGTH     = 100;
    private static final int QUEUE_MAX_CAPACITY         = 10000;

    private static final Set<String> ALLOWED_EVENTS = new HashSet<String>() {{
        add(IDE_USAGE);
        add(DASHBOARD_USAGE);
        add(USER_INVITE);
        add(SESSION_USAGE);
        add(SESSION_FACTORY_USAGE);
        add(PROJECT_OPENED);
    }};

    private final Thread        logThread;
    private final Queue<String> queue;

    /**
     * Stores the number of ignored events due to maximum queue capacity
     */
    private long ignoredEvents;

    public EventLogger() {
        this.queue = new LinkedBlockingQueue<>(QUEUE_MAX_CAPACITY);
        this.ignoredEvents = 0;

        logThread = new LogThread();
        logThread.setDaemon(true);
    }

    @PostConstruct
    public void init() {
        logThread.start();
    }

    @PreDestroy
    public void destroy() {
        logThread.interrupt();
    }

    public void log(String event, Map<String, String> parameters) throws UnsupportedEncodingException {
        if (event != null && ALLOWED_EVENTS.contains(event)) {
            if (event.equals(DASHBOARD_USAGE)) {
                parameters.put(EventLogger.SOURCE_PARAM, EventLogger.DASHBOARD_SOURCE);
            }

            validate(parameters);

            String message = createMessage(event, parameters);
            if (!offerEvent(message)) {
                if (ignoredEvents++ % 1000 == 0) {
                    LOG.warn("Ignored " + ignoredEvents + " events due to maximum queue capacity");
                }
            }
        }
    }

    protected boolean offerEvent(String message) {
        return queue.offer(message);
    }

    private String createMessage(String event, Map<String, String> parameters) throws UnsupportedEncodingException {
        StringBuilder message = new StringBuilder();

        addParam(message, EVENT_PARAM, event);

        addParam(message, WS_PARAM, parameters);
        addParam(message, USER_PARAM, parameters);
        addParam(message, PROJECT_NAME_PARAM, parameters);
        addParam(message, PROJECT_TYPE_PARAM, parameters);
        addParam(message, SOURCE_PARAM, parameters);
        addParam(message, ACTION_PARAM, parameters);

        addParam(message, PARAMETERS_PARAM, getParametersAsString(parameters));

        return message.toString();
    }

    private String getParametersAsString(Map<String, String> parameters) throws UnsupportedEncodingException {
        StringBuilder builder = new StringBuilder();

        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            if (builder.length() > 0) {
                builder.append(',');
            }

            builder.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            builder.append('=');
            builder.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return builder.toString();
    }

    private void addParam(StringBuilder message, String param, Map<String, String> parameters) {
        if (parameters.containsKey(param)) {
            addParam(message, param, parameters.remove(param));
        }
    }

    private void addParam(StringBuilder message, String param, String value) {
        if (message.length() > 0) {
            message.append(' ');
        }

        message.append(param);
        message.append('#');
        message.append(value);
        message.append('#');
    }

    private void validate(Map<String, String> additionalParams) throws IllegalArgumentException {
        if (additionalParams.size() > MAX_EXTENDED_PARAMS_NUMBER + RESERVED_PARAMS_NUMBER) {
            throw new IllegalArgumentException("The number of parameters exceeded the limit in " +
                                               MAX_EXTENDED_PARAMS_NUMBER);
        }

        for (Map.Entry<String, String> entry : additionalParams.entrySet()) {
            String param = entry.getKey();
            String value = entry.getValue();

            if (param.length() > MAX_PARAM_NAME_LENGTH) {
                throw new IllegalArgumentException(
                        "The length of parameter name " + param + " exceeded the length in " + MAX_PARAM_NAME_LENGTH +
                        " characters");

            } else if (value.length() > MAX_PARAM_VALUE_LENGTH) {
                throw new IllegalArgumentException(
                        "The length of parameter value " + value + " exceeded the length in " + MAX_PARAM_VALUE_LENGTH +
                        " characters");
            }
        }
    }

    /**
     * Is responsible for logging events.
     * Rate-limit is 50 messages per second.
     */
    private class LogThread extends Thread {
        private LogThread() {
            super("Analytics Event Logger");
        }

        @Override
        public void run() {
            LOG.info(getName() + " thread is started, queue is initialized for " + QUEUE_MAX_CAPACITY + " messages");
            while (!isInterrupted()) {
                String message = queue.poll();

                try {
                    if (message != null) {
                        LOG.info(message);
                        sleep(20);
                    } else {
                        sleep(1000);
                    }
                } catch (InterruptedException e) {
                    break;
                }
            }

            LOG.info(getName() + " thread is stopped");
        }
    }
}

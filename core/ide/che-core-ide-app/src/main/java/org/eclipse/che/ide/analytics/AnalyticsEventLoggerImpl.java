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
package org.eclipse.che.ide.analytics;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.analytics.shared.dto.EventParameters;
import org.eclipse.che.api.user.gwt.client.UserServiceClient;
import org.eclipse.che.api.user.shared.dto.UserDescriptor;
import org.eclipse.che.api.workspace.shared.dto.UsersWorkspaceDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.app.CurrentProject;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.ide.websocket.Message;
import org.eclipse.che.ide.websocket.MessageBuilder;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.events.ConnectionOpenedHandler;
import org.eclipse.che.ide.websocket.rest.RequestCallback;
import org.eclipse.che.api.workspace.gwt.client.event.WorkspaceStartedEvent;
import org.eclipse.che.api.workspace.gwt.client.event.WorkspaceStartedHandler;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import static com.google.gwt.http.client.RequestBuilder.POST;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.CONTENTTYPE;

/**
 * API to track Analytics events.
 *
 * @author Anatoliy Bazko
 */
@Singleton
public class AnalyticsEventLoggerImpl implements AnalyticsEventLoggerExt {
    private static final int    MAX_PENDING_MESSAGES = 1000;
    private static final String IDE_EVENT            = "ide-usage";
    private static final String API_ANALYTICS_PATH   = "/analytics/log/";

    protected static final String WS_PARAM           = "WS";
    protected static final String USER_PARAM         = "USER";
    protected static final String SOURCE_PARAM       = "SOURCE";
    protected static final String ACTION_PARAM       = "ACTION";
    protected static final String PROJECT_NAME_PARAM = "PROJECT";
    protected static final String PROJECT_TYPE_PARAM = "TYPE";

    private static final String EMPTY_PARAM_VALUE = "";

    private final DtoFactory             dtoFactory;
    private final UserServiceClient      user;
    private final AppContext             appContext;
    private final DtoUnmarshallerFactory dtoUnmarshallerFactory;
    private final Queue<Message>         pendingMessages;

    private volatile boolean isOpenedMessageBus;

    private MessageBus messageBus;
    private String     currentUser;

    @Inject
    public AnalyticsEventLoggerImpl(DtoFactory dtoFactory,
                                    UserServiceClient user,
                                    AppContext appContext,
                                    EventBus eventBus,
                                    final MessageBusProvider messageBusProvider,
                                    DtoUnmarshallerFactory dtoUnmarshallerFactory) {
        this.dtoFactory = dtoFactory;
        this.user = user;
        this.appContext = appContext;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;

        saveCurrentUser();

        this.pendingMessages = new LinkedList<Message>() {
            @Override
            public boolean add(Message message) {
                return size() < MAX_PENDING_MESSAGES && super.add(message);
            }
        };

        eventBus.addHandler(WorkspaceStartedEvent.TYPE, new WorkspaceStartedHandler() {
            @Override
            public void onWorkspaceStarted(UsersWorkspaceDto workspace) {
                messageBus = messageBusProvider.getMessageBus();

                messageBus.addOnOpenHandler(new ConnectionOpenedHandler() {
                    @Override
                    public void onOpen() {
                        isOpenedMessageBus = true;

                        Message message;
                        while ((message = pendingMessages.poll()) != null) {
                            doSend(message);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void log(Object action) {
        doLog(IDE_EVENT, action, null, null);
    }

    @Override
    public void log(Object action, String actionName) {
        doLog(IDE_EVENT, action, actionName, null);
    }

    @Override
    public void log(Object action, String actionName, Map<String, String> additionalParams) {
        doLog(IDE_EVENT, action, actionName, additionalParams);
    }

    @Override
    public void logEvent(String event, Map<String, String> additionalParams) {
        doLog(event, null, null, additionalParams);
    }

    private void doLog(@Nullable String event,
                       @Nullable Object action,
                       @Nullable String actionName,
                       @Nullable Map<String, String> additionalParams) {
        // we can put here additional params depending on action class
        doLog(event, action == null ? null : action.getClass(), actionName, additionalParams);
    }

    private void doLog(@Nullable String event,
                       @Nullable Class<?> actionClass,
                       @Nullable String actionName,
                       @Nullable Map<String, String> additionalParams) {
        if (event == null) {
            return;
        }

        additionalParams = additionalParams == null ? new HashMap<String, String>() : new HashMap<>(additionalParams);
        validate(additionalParams);

        if (actionName != null) {
            validate(actionName, MAX_PARAM_VALUE_LENGTH);
            additionalParams.put(ACTION_PARAM, actionName);
        }

        if (actionClass != null) {
            additionalParams.put(SOURCE_PARAM, actionClass.getName());
        }

        putReservedParameters(additionalParams);
        send(event, additionalParams);
    }

    private void putReservedParameters(Map<String, String> additionalParams) {
        CurrentProject project = appContext.getCurrentProject();
        if (project != null) {
            putIfNotNull(PROJECT_NAME_PARAM, project.getRootProject().getName(), additionalParams);
            putIfNotNull(PROJECT_TYPE_PARAM, project.getRootProject().getType(), additionalParams);
        }

        putIfNotNull(USER_PARAM, currentUser, additionalParams);
        putIfNotNull(WS_PARAM, getWorkspace(), additionalParams);
    }

    protected String getWorkspace() {
        return appContext.getWorkspace().getName();
    }

    private void putIfNotNull(String key,
                              @Nullable String value,
                              Map<String, String> additionalParams) {
        if (value != null) {
            additionalParams.put(key, value);
        }
    }

    private void validate(Map<String, String> additionalParams) throws IllegalArgumentException {
        if (additionalParams.size() > MAX_PARAMS_NUMBER) {
            throw new IllegalArgumentException("The number of parameters exceeded the limit in " + MAX_PARAMS_NUMBER);
        }

        for (Map.Entry<String, String> entry : additionalParams.entrySet()) {
            String param = entry.getKey();
            String value = entry.getValue();

            validate(param, MAX_PARAM_NAME_LENGTH);
            validate(value, MAX_PARAM_VALUE_LENGTH);
        }
    }

    private void validate(String s, int maxLength) {
        if (s.length() > maxLength) {
            throw new IllegalArgumentException(
                    "The length of '" + s.substring(0, maxLength) + "...' exceeded the maximum in " + maxLength + " characters");
        }
    }

    private void saveCurrentUser() {
        user.getCurrentUser(new AsyncRequestCallback<UserDescriptor>(dtoUnmarshallerFactory.newUnmarshaller(UserDescriptor.class)) {
            @Override
            protected void onSuccess(UserDescriptor result) {
                if (result != null) {
                    currentUser = result.getEmail();
                } else {
                    currentUser = EMPTY_PARAM_VALUE;
                }
            }

            @Override
            protected void onFailure(Throwable exception) {
                currentUser = EMPTY_PARAM_VALUE;
            }
        });
    }

    protected void send(String event, Map<String, String> parameters) {
        EventParameters additionalParams = dtoFactory.createDto(EventParameters.class).withParams(parameters);
        final String json = dtoFactory.toJson(additionalParams);

        MessageBuilder builder = new MessageBuilder(POST, API_ANALYTICS_PATH + event);
        builder.data(json);
        builder.header(CONTENTTYPE, APPLICATION_JSON);

        Message message = builder.build();
        if (isOpenedMessageBus) {
            doSend(message);
        } else {
            pendingMessages.offer(message);
        }
    }

    private void doSend(final Message message) {
        try {
            messageBus.send(message, new RequestCallback() {
                @Override
                protected void onSuccess(Object result) {
                }

                @Override
                protected void onFailure(Throwable exception) {
                    Log.error(getClass(), exception.getMessage());
                }
            });
        } catch (Exception e) {
            Log.error(getClass(), e.getMessage());
        }
    }
}

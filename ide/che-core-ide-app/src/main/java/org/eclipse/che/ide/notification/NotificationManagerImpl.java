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
package org.eclipse.che.ide.notification;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.event.ng.EditorFileStatusNotificationHandler;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationListener;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.notification.NotificationObserver;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.notification.StatusNotification.Status;
import org.eclipse.che.ide.api.notification.StatusNotificationListener;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.providers.DynaObject;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

import static org.eclipse.che.ide.api.notification.ReadState.READ;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.EMERGE_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.NOT_EMERGE_MODE;

/**
 * The implementation of {@link NotificationManager}.
 * Base notification part. Perform showing notification in the "Events panel". Also checking is performed on each created notification.
 * <p/>
 * Each notification may be simple notification {@link Notification} or status notification {@link StatusNotification}.
 * <p/>
 * If notification instance of {@link StatusNotification} then notification may be showed as balloon widget by need. By default all
 * notifications are showed only in events panel.
 *
 * @author Andrey Plotnikov
 * @author Dmitry Shnurenko
 */
@Singleton
@DynaObject
public class NotificationManagerImpl extends BasePresenter implements NotificationManager,
                                                                      NotificationObserver,
                                                                      StatusNotificationListener,
                                                                      NotificationManagerView.ActionDelegate {
    public static final String TITLE   = "Events";
    public static final String TOOLTIP = "Event Log";

    private final NotificationManagerView view;
    private final NotificationContainer   nContainer;
    private final NotificationPopupStack  nPopupStack;
    private final List<Notification> notifications = new ArrayList<>();

    private Resources resources;

    /** Count of unread notifications */
    private int unread = 0;

    /**
     * Create instance of notification panel.
     *
     * @param view
     *         the view
     * @param nContainer
     *         notification container. It holds showed notifications
     * @param nPopupStack
     *         popup notification stack. It showed each notification as balloon
     * @param resources
     *         core resources
     */
    @Inject
    public NotificationManagerImpl(NotificationManagerView view,
                                   NotificationContainer nContainer,
                                   NotificationPopupStack nPopupStack,
                                   Resources resources) {
        this.view = view;
        this.nContainer = nContainer;
        this.nContainer.setDelegate(this);
        this.nPopupStack = nPopupStack;
        this.nPopupStack.setDelegate(this);
        this.view.setDelegate(this);
        this.view.setContainer(nContainer);
        this.view.setTitle(TITLE);
        this.resources = resources;
    }

    @Inject
    @PostConstruct
    public void inject(EditorFileStatusNotificationHandler editorFileStatusNotificationHandler) {
        editorFileStatusNotificationHandler.inject(this);
    }

    /** {@inheritDoc} */
    @Override
    public View getView() {
        return view;
    }

    /** {@inheritDoc} */
    @Override
    public void onValueChanged() {
        unread = 0;

        for (Notification notification : notifications) {
            if (!notification.isRead()) {
                unread++;
            }
        }

        firePropertyChange(TITLE_PROPERTY);
    }

    /** {@inheritDoc} */
    @Override
    public int getUnreadNotificationsCount() {
        return unread;
    }

    /** {@inheritDoc} */
    @Override
    public <T extends Notification> T notify(T notification) {
        notification.addObserver(this);
        notifications.add(notification);

        if (notification instanceof StatusNotification) {
            handleStatusNotification((StatusNotification)notification);
        }

        nContainer.addNotification(notification);
        onValueChanged();

        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                view.scrollBottom();
            }
        });

        return notification;
    }

    /** Performs appropriate actions for correct displaying of {@link StatusNotification} */
    private void handleStatusNotification(StatusNotification notification) {
        DisplayMode displayMode = notification.getDisplayMode();
        if (!displayMode.equals(NOT_EMERGE_MODE)) {
            nPopupStack.push(notification);
        }

        if (displayMode.equals(EMERGE_MODE)) {
            notification.setStatusListener(this);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Notification notify(String title) {
        return notify(new Notification(title));
    }

    /** {@inheritDoc} */
    @Override
    public Notification notify(String title,
                               String content) {
        return notify(title, content, (NotificationListener)null);
    }

    /** {@inheritDoc} */
    @Override
    public Notification notify(String title,
                               String content,
                               NotificationListener listener) {
        return notify(title, content, listener, null);
    }

    /** {@inheritDoc} */
    @Override
    public Notification notify(String title,
                               NotificationListener listener) {
        return notify(title, listener, null);
    }

    /** {@inheritDoc} */
    @Override
    public StatusNotification notify(String title,
                                     Status status,
                                     DisplayMode displayMode) {
        return notify(new StatusNotification(title, status, displayMode));
    }

    /** {@inheritDoc} */
    @Override
    public StatusNotification notify(String title,
                                     Status status,
                                     DisplayMode displayMode,
                                     NotificationListener listener) {
        return notify(new StatusNotification(title, status, displayMode, listener));
    }

    /** {@inheritDoc} */
    @Override
    public StatusNotification notify(String title,
                                     String content,
                                     Status status,
                                     DisplayMode displayMode) {
        return notify(title, content, status, displayMode, (NotificationListener)null);
    }

    /** {@inheritDoc} */
    @Override
    public StatusNotification notify(String title,
                                     String content,
                                     Status status,
                                     DisplayMode displayMode,
                                     NotificationListener listener) {
        return notify(title, content, status, displayMode, listener, null);
    }

    /** {@inheritDoc} */
    @Override
    public Notification notify(String title,
                               String content,
                               ProjectConfigDto project) {
        return notify(title, content, null, project);
    }

    /** {@inheritDoc} */
    @Override
    public Notification notify(String title,
                               ProjectConfigDto project) {
        return notify(title, (NotificationListener)null, project);
    }

    /** {@inheritDoc} */
    @Override
    public Notification notify(String title,
                               String content,
                               NotificationListener listener,
                               ProjectConfigDto project) {
        return notify(new Notification(title, content, project, listener));
    }

    /** {@inheritDoc} */
    @Override
    public Notification notify(String title,
                               NotificationListener listener,
                               ProjectConfigDto project) {
        return notify(title, null, listener, project);
    }

    /** {@inheritDoc} */
    @Override
    public StatusNotification notify(String title,
                                     String content,
                                     Status status,
                                     DisplayMode displayMode,
                                     ProjectConfigDto project) {
        return notify(title, content, status, displayMode, null, project);
    }

    /** {@inheritDoc} */
    @Override
    public StatusNotification notify(String title,
                                     Status status,
                                     DisplayMode displayMode,
                                     ProjectConfigDto project) {
        return notify(title, null, status, displayMode, null, project);
    }

    /** {@inheritDoc} */
    @Override
    public StatusNotification notify(String title,
                                     String content,
                                     Status status,
                                     DisplayMode displayMode,
                                     NotificationListener listener,
                                     ProjectConfigDto project) {
        return notify(new StatusNotification(title, content, status, displayMode, project, listener));
    }

    /** {@inheritDoc} */
    @Override
    public StatusNotification notify(String title,
                                     Status status,
                                     DisplayMode displayMode,
                                     NotificationListener listener,
                                     ProjectConfigDto project) {
        return notify(title, null, status, displayMode, listener, project);
    }

    /**
     * Remove notification.
     *
     * @param notification
     *         notification that need to remove
     */
    public void removeNotification(Notification notification) {
        notification.removeObserver(this);
        notifications.remove(notification);
        nContainer.removeNotification(notification);
        onValueChanged();
    }

    /** {@inheritDoc} */
    @Override
    public void onClick(Notification notification) {
        NotificationListener listener = notification.getListener();
        if (listener != null) {
            listener.onClick(notification);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onDoubleClick(Notification notification) {
        NotificationListener listener = notification.getListener();
        if (listener != null) {
            listener.onDoubleClick(notification);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onClose(Notification notification) {
        removeNotification(notification);

        NotificationListener listener = notification.getListener();
        if (listener != null) {
            listener.onClose(notification);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String getTitle() {
        return TITLE;
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    /** {@inheritDoc} */
    @Override
    public SVGResource getTitleImage() {
        return resources.eventsPartIcon();
    }

    /** {@inheritDoc} */
    @Override
    public String getTitleToolTip() {
        return TOOLTIP;
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    /** {@inheritDoc} */
    @Override
    public void onOpen() {
        super.onOpen();

        for (Notification notification : notifications) {
            notification.setState(READ);
        }
    }

    @Override
    public void onNotificationStatusChanged(StatusNotification notification) {
        if (notification.getDisplayMode().equals(EMERGE_MODE)) {
            nPopupStack.remove(notification);//to avoid displaying of notification a few times
            nPopupStack.push(notification);
        }
    }
}

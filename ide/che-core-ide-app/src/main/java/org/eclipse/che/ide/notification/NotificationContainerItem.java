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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationObserver;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.notification.NotificationManagerView.NotificationActionDelegate;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;

import java.util.Date;

/**
 * Widget for showing notification in the notification panel.
 * <p/>
 * {@link Notification} may be instance of {@link StatusNotification}. Each rendered notification consists of:
 * <ul>
 * <li>Icon</li>
 * <li>Title</li>
 * <li>Message content (optional)</li>
 * <li>Close control</li>
 * </ul>
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 */
public class NotificationContainerItem extends Composite implements NotificationObserver {

    public static final String MESSAGE_WRAPPER_DBG_ID      = "message-wrapper";
    public static final String NOTIFICATION_WRAPPER_DBG_ID = "notification-wrapper";
    public static final String TITLE_DBG_ID                = "title";
    public static final String CONTENT_DBG_ID              = "content";
    public static final String ICON_DBG_ID                 = "icon";
    public static final String CLOSE_ICON_DBG_ID           = "close-icon";

    private static final DateTimeFormat DATA_FORMAT = DateTimeFormat.getFormat("h:mm a");

    private SimplePanel                titlePanel;
    private SimplePanel                messagePanel;
    private SimplePanel                iconPanel;
    private Resources                  resources;
    private Notification               notification;
    private NotificationActionDelegate delegate;

    private int clickCount = 0;

    /**
     * Create notification item.
     *
     * @param resources
     *         core resources
     * @param notification
     *         notification which should be wrapped into widget
     */
    public NotificationContainerItem(final Notification notification, @NotNull Resources resources) {
        notification.addObserver(this);

        this.resources = resources;
        this.notification = notification;

        iconPanel = new SimplePanel();

        FlowPanel contentWrapper = new FlowPanel();
        contentWrapper.add(titlePanel = createTitleWidget());
        contentWrapper.add(messagePanel = createContentWidget());
        contentWrapper.ensureDebugId(CONTENT_DBG_ID + notification.getId());
        contentWrapper.setStyleName(resources.notificationCss().notificationContentWrapper());


        FlowPanel notificationWrapper = new FlowPanel();
        notificationWrapper.add(iconPanel = createIconWidget());
        notificationWrapper.add(contentWrapper);
        notificationWrapper.add(createCloseWidget());
        notificationWrapper.ensureDebugId(NOTIFICATION_WRAPPER_DBG_ID + notification.getId());
        notificationWrapper.setStyleName(resources.notificationCss().notification());

        update();

        sinkEvents(Event.ONCLICK | Event.ONDBLCLICK);

        initWidget(notificationWrapper);
    }

    /** {@inheritDoc} */
    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);

        switch (DOM.eventGetType(event)) {
            case Event.ONCLICK:
                clickCount++;
                if (clickCount == 1) {
                    Timer timer = new Timer() {
                        @Override
                        public void run() {
                            if (clickCount == 1) {
                                clickCount = 0;
                                delegate.onClick(notification);
                            }
                        }
                    };
                    timer.schedule(200);
                }
                break;
            case Event.ONDBLCLICK:
                clickCount = 0;
                delegate.onDoubleClick(notification);
                break;
        }
    }

    /**
     * Create icon wrapper that contains an icon.
     *
     * @return {@link SimplePanel} as icon wrapper
     */
    private SimplePanel createIconWidget() {
        SimplePanel iconWrapper = new SimplePanel();
        iconWrapper.setStyleName(resources.notificationCss().notificationIconWrapper());
        iconWrapper.ensureDebugId(ICON_DBG_ID + notification.getId());
        return iconWrapper;
    }

    /**
     * Create close icon widget that contains an close notification icon.
     *
     * @return {@link SimplePanel} as close icon wrapper
     */
    private SimplePanel createCloseWidget() {
        SimplePanel closeWrapper = new SimplePanel();
        SVGImage closeImage = new SVGImage(resources.closeIcon());
        closeImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                delegate.onClose(notification);
            }
        });
        closeWrapper.add(closeImage);
        closeWrapper.setStyleName(resources.notificationCss().notificationCloseButtonWrapper());
        closeImage.ensureDebugId(CLOSE_ICON_DBG_ID + notification.getId());
        return closeWrapper;
    }

    /**
     * Create title widget that contains notification title.
     *
     * @return {@link SimplePanel} as title wrapper
     */
    private SimplePanel createTitleWidget() {
        SimplePanel titleWrapper = new SimplePanel();
        Label titleLabel = new Label();
        titleWrapper.add(titleLabel);
        titleWrapper.setStyleName(resources.notificationCss().notificationTitleWrapper());
        titleWrapper.ensureDebugId(TITLE_DBG_ID + notification.getId());
        return titleWrapper;
    }

    /**
     * Create message widget that contains notification message.
     *
     * @return {@link SimplePanel} as message wrapper
     */
    private SimplePanel createContentWidget() {
        SimplePanel messageWrapper = new SimplePanel();
        Label messageLabel = new Label();
        messageWrapper.add(messageLabel);
        messageWrapper.setStyleName(resources.notificationCss().notificationMessageWrapper());
        messageWrapper.ensureDebugId(MESSAGE_WRAPPER_DBG_ID + notification.getId());
        return messageWrapper;
    }

    /** {@inheritDoc} */
    @Override
    public Widget asWidget() {
        return super.asWidget();
    }

    /**
     * Sets the delegate for receiving events from this view.
     */
    public void setDelegate(NotificationActionDelegate delegate) {
        this.delegate = delegate;
    }

    /**
     * Return an icon based on {@link org.eclipse.che.ide.api.notification.StatusNotification.Status}.
     *
     * @return SVG image that represents icon status
     */
    private SVGImage getIconBaseOnStatus() {
        final SVGResource icon;
        final String status;

        switch (((StatusNotification)notification).getStatus()) {
            case PROGRESS:
                icon = resources.progress();
                status = "progress";
                break;
            case SUCCESS:
                icon = resources.success();
                status = "success";
                break;
            case FAIL:
                icon = resources.fail();
                status = "fail";
                break;
            case WARNING:
            	icon = resources.warning();
            	status = "warning";
            	break;
            default:
                throw new IllegalArgumentException("Can't determine notification icon");
        }

        SVGImage image = new SVGImage(icon);
        image.getElement().setAttribute("name", status);
        return image;
    }

    /** {@inheritDoc} */
    @Override
    public void onValueChanged() {
        update();
    }

    /**
     * Update notification's widget values.
     * <p/>
     * Widget consumes:
     * <ul>
     * <li>{@link Notification#title}</li>
     * <li>{@link Notification#content}</li>
     * <li>Icon and background color based on {@link StatusNotification.Status}</li>
     * </ul>
     */
    private void update() {
        Widget titleWidget = titlePanel.getWidget();
        if (titleWidget != null && titleWidget instanceof Label) {
            ((Label)titleWidget).setText(notification.getTitle());

            StringBuilder infoBuilder = new StringBuilder();
            if (notification.getProject() != null) {
                infoBuilder.append("Project: ").append(notification.getProject().getName()).append(". ");
            }

            infoBuilder.append(DATA_FORMAT.format(new Date(notification.getTime())));

            titlePanel.getElement().setAttribute("info", infoBuilder.toString());
        }

        Widget messageWidget = messagePanel.getWidget();
        if (messageWidget != null && messageWidget instanceof Label) {
            ((Label)messageWidget).setText(notification.getContent());
        }

        if (notification instanceof StatusNotification) {
            iconPanel.setWidget(getIconBaseOnStatus());
        }
    }

}
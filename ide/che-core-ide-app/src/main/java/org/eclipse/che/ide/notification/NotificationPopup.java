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
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import org.eclipse.che.ide.DelayedTask;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationObserver;
import org.eclipse.che.ide.api.notification.StatusNotification;
import org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode;
import org.eclipse.che.ide.api.notification.StatusNotification.Status;
import org.eclipse.che.ide.notification.NotificationManagerView.NotificationActionDelegate;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;

import static com.google.gwt.user.client.Event.ONCLICK;
import static com.google.gwt.user.client.Event.ONDBLCLICK;
import static com.google.gwt.user.client.Event.ONMOUSEOUT;
import static com.google.gwt.user.client.Event.ONMOUSEOVER;
import static org.eclipse.che.ide.api.notification.StatusNotification.DisplayMode.FLOAT_MODE;
import static org.eclipse.che.ide.api.notification.StatusNotification.Status.PROGRESS;

/**
 * Widget for showing notification popup.
 * <p/>
 * {@link Notification} in this case must extends {@link StatusNotification}. Status may have one of three values {@link
 * Status}.
 * By default, if the {@link Notification} is set to <code>Status.PROGRESS</code>, then hide timer won't be handle popup closing, but if
 * the popup has status <code>Status.SUCCESS</code> or <code>Status.FAIL</code> then hide timer will perform automatically hide popup
 * widget in 5 seconds.
 * <p/>
 * Popup's showing and hiding is controlled by two animations.
 * Showing animation simply sets opacity value from 0 to 1 in 1 second.
 * Hiding animation simply sets opacity value from 1 to 0 and margin-left from element offset width to negative value.
 * <p/>
 * Possible improvements:
 * <ul>
 * <li>Widget should handle {@link com.google.gwt.user.client.Window} onBlur and onFocus events. This need to stop hide timer if window
 * becomes blur.</li>
 * </ul>
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 * @see {@link Notification}.
 * @see {@link StatusNotification}.
 * @see {@link Status}.
 */
public class NotificationPopup extends SimplePanel implements NotificationObserver {

    public static final String MESSAGE_WRAPPER_DBG_ID      = "popup-message-wrapper";
    public static final String NOTIFICATION_WRAPPER_DBG_ID = "popup-notification-wrapper";
    public static final String TITLE_DBG_ID                = "popup-title";
    public static final String CONTENT_DBG_ID              = "popup-content";
    public static final String ICON_DBG_ID                 = "popup-icon";
    public static final String CLOSE_ICON_DBG_ID           = "popup-close-icon";

    /**
     * Value for the automatically hiding. By default it 5 seconds.
     */
    public static final int DEFAULT_TIME = 5000;

    private StatusNotification         notification;
    private NotificationActionDelegate delegate;
    private Resources                  resources;
    private SimplePanel                titlePanel;
    private SimplePanel                messagePanel;
    private SimplePanel                iconPanel;

    private int clickCount = 0;

    private final Timer hideTimer = new Timer() {

        /** {@inheritDoc} */
        @Override
        public void run() {
            delegate.onClose(notification);
        }

    };

    /**
     * Create notification message.
     *
     * @param notification
     *         the notification
     * @param resources
     *         the resources
     * @param delegate
     *         the delegate
     */
    public NotificationPopup(@NotNull StatusNotification notification,
                             @NotNull Resources resources,
                             @NotNull NotificationActionDelegate delegate) {

        this.notification = notification;
        this.resources = resources;
        this.delegate = delegate;

        setStyleName(resources.notificationCss().notificationPopupPanel());

        notification.addObserver(this);

        FlowPanel contentWrapper = new FlowPanel();
        contentWrapper.add(titlePanel = createTitleWidget());
        contentWrapper.add(messagePanel = createContentWidget());
        contentWrapper.setStyleName(resources.notificationCss().notificationPopupContentWrapper());
        contentWrapper.ensureDebugId(CONTENT_DBG_ID + notification.getId());

        FlowPanel notificationWrapper = new FlowPanel();
        notificationWrapper.add(iconPanel = createIconWidget());
        notificationWrapper.add(contentWrapper);
        notificationWrapper.add(createCloseWidget());
        notificationWrapper.setStyleName(resources.notificationCss().notificationPopup());
        notificationWrapper.ensureDebugId(NOTIFICATION_WRAPPER_DBG_ID + notification.getId());

        setWidget(notificationWrapper);
    }

    /** {@inheritDoc} */
    @Override
    public void onBrowserEvent(Event event) {
        super.onBrowserEvent(event);

        switch (DOM.eventGetType(event)) {
            case ONCLICK:
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

            case ONDBLCLICK:
                clickCount = 0;
                delegate.onDoubleClick(notification);
                break;

            case ONMOUSEOVER:
                hideTimer.cancel();
                break;

            case ONMOUSEOUT:
                if (notification.getStatus() == PROGRESS) {
                    hideTimer.cancel();
                } else {
                    hideTimer.schedule(DEFAULT_TIME);
                }
                break;
        }
    }

    /**
     * Return an icon based on {@link Status}.
     *
     * @return SVG image that represents icon status
     */
    private SVGImage getIconBaseOnStatus() {
        final SVGResource icon;
        final String status;

        switch (notification.getStatus()) {
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

    /**
     * Create icon wrapper that contains an icon.
     *
     * @return {@link SimplePanel} as icon wrapper
     */
    private SimplePanel createIconWidget() {
        SimplePanel iconWrapper = new SimplePanel();
        iconWrapper.setStyleName(resources.notificationCss().notificationPopupIconWrapper());
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
        closeWrapper.setStyleName(resources.notificationCss().notificationPopupCloseButtonWrapper());
        closeWrapper.ensureDebugId(CLOSE_ICON_DBG_ID + notification.getId());
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
        titleWrapper.setStyleName(resources.notificationCss().notificationPopupTitleWrapper());
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
        messageWrapper.setStyleName(resources.notificationCss().notificationPopupMessageWrapper());
        messageWrapper.ensureDebugId(MESSAGE_WRAPPER_DBG_ID + notification.getId());
        return messageWrapper;
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
     * <li>Icon and background color based on {@link Status}</li>
     * </ul>
     */
    private void update() {
        Widget titleWidget = titlePanel.getWidget();
        if (titleWidget != null && titleWidget instanceof Label) {
            ((Label)titleWidget).setText(notification.getTitle());
            titleWidget.setTitle(notification.getTitle());
        }

        Widget messageWidget = messagePanel.getWidget();
        if (messageWidget != null && messageWidget instanceof Label) {
            ((Label)messageWidget).setText(notification.getContent());
        }

        iconPanel.setWidget(getIconBaseOnStatus());

        removeStyleName(resources.notificationCss().notificationStatusProgress());
        removeStyleName(resources.notificationCss().notificationStatusSuccess());
        removeStyleName(resources.notificationCss().notificationStatusFail());
        removeStyleName(resources.notificationCss().notificationStatusWarning());

        DisplayMode displayMode = notification.getDisplayMode();
        Status status = notification.getStatus();
        switch (status) {
            case PROGRESS:
                setStyleName(resources.notificationCss().notificationStatusProgress(), true);
                break;
            case SUCCESS:
                setStyleName(resources.notificationCss().notificationStatusSuccess(), true);
                break;
            case FAIL:
            	setStyleName(resources.notificationCss().notificationStatusFail(), true);
                break;
            case WARNING:
            	setStyleName(resources.notificationCss().notificationStatusWarning(), true);
            	break;
        }

        if (FLOAT_MODE == displayMode && PROGRESS == status) {
            hideTimer.cancel();
            return;
        }
        hideTimer.schedule(DEFAULT_TIME);
    }

    /** {@inheritDoc} */
    @Override
    protected void onAttach() {
        boolean isOrWasAttached = isOrWasAttached();
        super.onAttach();

        if (!isOrWasAttached) {
            sinkEvents(ONCLICK | ONDBLCLICK | ONMOUSEOUT | ONMOUSEOVER);
            update();

            addStyleName(resources.notificationCss().notificationShowingAnimation());
        }
    }

    /**
     * Return notification related to this widget.
     *
     * @return the {@link StatusNotification}
     */
    public StatusNotification getNotification() {
        return notification;
    }

    /** {@inheritDoc} */
    @Override
    public void removeFromParent() {
        if (isOrWasAttached()) {
            addStyleName(resources.notificationCss().notificationHidingAnimation());

            new DelayedTask() {
                @Override
                public void onExecute() {
                    NotificationPopup.super.removeFromParent();
                }
            }.delay(500);
        } else {
            super.removeFromParent();
        }
    }
}

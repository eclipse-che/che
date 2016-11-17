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
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.parts.PartStackUIResources;
import org.eclipse.che.ide.api.parts.base.BaseView;

/**
 * The implementation of {@link NotificationManagerView}.
 *
 * @author Andrey Plotnikov
 */
@Singleton
public class NotificationManagerViewImpl extends BaseView<NotificationManagerView.ActionDelegate> implements NotificationManagerView {

    interface NotificationManagerViewImplUiBinder extends UiBinder<Widget, NotificationManagerViewImpl> {
    }

    @UiField
    FlowPanel mainPanel;

    @UiField
    ScrollPanel scrollPanel;

    /** scroll events to the bottom if view is visible */
    private boolean scrollBottomRequired = false;

    /**
     * Create view.
     *
     * @param resources
     */
    @Inject
    public NotificationManagerViewImpl(PartStackUIResources partStackUIResources,
                                       Resources resources,
                                       NotificationManagerViewImplUiBinder uiBinder) {
        super(partStackUIResources);
        setContentWidget(uiBinder.createAndBindUi(this));

        minimizeButton.ensureDebugId("notification-minimizeBut");

        scrollPanel.getElement().setTabIndex(0);
    }

    @Override
    public void setContainer(NotificationContainer container) {
        mainPanel.add(container);
    }

    /** {@inheritDoc} */
    @Override
    public void scrollBottom() {
        /** scroll bottom immediately if view is visible */
        if (scrollPanel.getElement().getOffsetParent() != null) {
            scrollPanel.getElement().setScrollTop(scrollPanel.getElement().getScrollHeight());
            return;
        }

        /** otherwise, check the visibility periodically and scroll the view when it's visible */
        if (!scrollBottomRequired) {
            scrollBottomRequired = true;

            Scheduler.get().scheduleFixedPeriod(new Scheduler.RepeatingCommand() {
                @Override
                public boolean execute() {
                    if (scrollPanel.getElement().getOffsetParent() != null) {
                        scrollPanel.getElement().setScrollTop(scrollPanel.getElement().getScrollHeight());
                        scrollBottomRequired = false;
                        return false;
                    }
                    return true;
                }
            }, 1000);
        }
    }

    @Override
    protected void focusView() {
        scrollPanel.getElement().focus();
    }

}

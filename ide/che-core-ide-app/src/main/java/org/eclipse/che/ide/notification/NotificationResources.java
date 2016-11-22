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

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Notifications resources. Contains definition of styles and icons.
 *
 * @author Andrey Plotnikov
 * @author Vlad Zhukovskyi
 **/
public interface NotificationResources extends ClientBundle {
    interface NotificationCss extends CssResource {

        String notificationPanel();

        String notificationPanelContainer();

        String notification();

        String notificationIconWrapper();

        String notificationContentWrapper();

        String notificationTitleWrapper();

        String notificationMessageWrapper();

        String notificationCloseButtonWrapper();

        String notificationStatusProgress();

        String notificationStatusSuccess();

        String notificationStatusFail();

        String notificationStatusWarning();
        
        String notificationPopup();

        String notificationPopupContentWrapper();

        String notificationPopupIconWrapper();

        String notificationPopupCloseButtonWrapper();

        String notificationPopupTitleWrapper();

        String notificationPopupMessageWrapper();

        String notificationPopupPanel();

        String notificationPopupPlaceholder();

        String notificationShowingAnimation();

        String notificationHidingAnimation();
    }

    @Source({"notification.css", "org/eclipse/che/ide/api/ui/style.css"})
    NotificationCss notificationCss();

    @Source("success.svg")
    SVGResource success();

    @Source("fail.svg")
    SVGResource fail();

    @Source("progress.svg")
    SVGResource progress();

    @Source("warning.svg")
    SVGResource warning();
}
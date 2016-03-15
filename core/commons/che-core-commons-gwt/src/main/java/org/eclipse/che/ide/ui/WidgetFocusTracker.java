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
package org.eclipse.che.ide.ui;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.user.client.ui.FocusWidget;
import com.google.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

import static com.google.common.base.MoreObjects.firstNonNull;

/**
 * The class contains business logic which allows to track the focus for widgets.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class WidgetFocusTracker {

    private Map<FocusWidget, Boolean> focusStates;

    /**
     * Add widget to track the focus
     *
     * @param widget
     *         the widget to track
     */
    public void subscribe(final FocusWidget widget) {
        if (focusStates == null) {
            focusStates = new HashMap<>();
        }

        focusStates.put(widget, false);
        widget.addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                focusStates.put(widget, true);
            }
        });

        widget.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                focusStates.put(widget, false);
            }
        });
    }

    /**
     * Unsubscribe widget from tracking the focus
     *
     * @param widget
     *         the widget to unsubscribe from tracking the focus
     */
    public void unSubscribe(FocusWidget widget) {
        if (widget == null) {
            return;
        }
        focusStates.remove(widget);
    }

    /**
     * Returns {@code true} if widget is in the focus and {@code false} - otherwise.
     * Note: this method returns {@code false} if widget hasn't subscribed to track the focus.
     */
    public boolean isWidgetFocused(FocusWidget widget) {
        return widget != null && firstNonNull(focusStates.get(widget), false);
    }
}

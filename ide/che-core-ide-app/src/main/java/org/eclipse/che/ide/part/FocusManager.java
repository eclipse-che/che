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
package org.eclipse.che.ide.part;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.parts.Focusable;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.part.PartStackPresenter.PartStackEventHandler;


/**
 * FocusManager is responsible for granting a focus for a stack when requested.
 *
 * @author Nikolay Zamosenchuk
 * @author Dmitry Shnurenko
 * @author Vlad Zhukovskyi
 */
@Singleton
public class FocusManager {

    private final PartStackEventHandler partStackHandler;

    private PartStack     activePartStack;
    private PartPresenter activePart;

    /**
     * Provides a handler, that is injected into PartStack, for the FocusManager to be able to track
     * PartStack focus requests and changes of the active Part.
     *
     * @return instance of PartStackEventHandler
     */
    public PartStackEventHandler getPartStackHandler() {
        return partStackHandler;
    }

    @Inject
    public FocusManager(final EventBus eventBus) {

        this.partStackHandler = new PartStackEventHandler() {
            @Override
            public void onRequestFocus(PartStack partStack) {
                if (partStack == null || partStack.getActivePart() == null) {
                    return;
                }

                if (partStack == activePartStack && partStack.getActivePart() == activePart) {
                    return;
                }

                /** unfocus active part stack */
                if (activePartStack != null) {
                    activePartStack.setFocus(false);
                }

                /** unfocus active part */
                if (activePart != null && activePart.getView() instanceof Focusable) {
                    ((Focusable)activePart.getView()).setFocus(false);
                }

                /** remember active part stack and part */
                activePartStack = partStack;
                activePart = partStack.getActivePart();

                /** focus part stack */
                activePartStack.setFocus(true);

                /** focus part if it has view and focusable */
                if (activePart != null) {
                    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                        @Override
                        public void execute() {
                            final IsWidget view = activePart.getView();
                            if (view instanceof Focusable) {
                                ((Focusable)view).setFocus(true);
                            }
                        }
                    });
                }

                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        eventBus.fireEvent(new ActivePartChangedEvent(activePart));
                    }
                });

            }
        };
    }

}

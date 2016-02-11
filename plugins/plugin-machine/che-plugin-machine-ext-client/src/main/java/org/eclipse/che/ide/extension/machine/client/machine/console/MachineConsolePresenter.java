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
package org.eclipse.che.ide.extension.machine.client.machine.console;

import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.event.ActivePartChangedHandler;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.HasView;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.ui.toolbar.ToolbarPresenter;

import javax.validation.constraints.NotNull;

/**
 * Machine console.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class MachineConsolePresenter extends BasePresenter implements MachineConsoleView.ActionDelegate, HasView {

    private final MachineLocalizationConstant machineLocalizationConstant;
    private final MachineConsoleView          view;
    private final ToolbarPresenter            consoleToolbar;
    private boolean hasUnreadMessages = false;

    @Inject
    public MachineConsolePresenter(MachineConsoleView view,
                                   @MachineConsoleToolbar ToolbarPresenter consoleToolbar,
                                   EventBus eventBus,
                                   MachineLocalizationConstant machineLocalizationConstant) {
        this.view = view;
        this.consoleToolbar = consoleToolbar;
        this.machineLocalizationConstant = machineLocalizationConstant;
        this.view.setTitle(machineLocalizationConstant.machineConsoleViewTitle());
        this.view.setDelegate(this);

        eventBus.addHandler(ActivePartChangedEvent.TYPE, new ActivePartChangedHandler() {
            @Override
            public void onActivePartChanged(ActivePartChangedEvent event) {
                onPartActivated(event.getActivePart());
            }
        });
    }

    private void onPartActivated(PartPresenter part) {
        if (part != null && part.equals(this) && hasUnreadMessages) {
            hasUnreadMessages = false;
        }

        firePropertyChange(TITLE_PROPERTY);
    }

    /** {@inheritDoc} */
    @Override
    public View getView() {
        return view;
    }

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String getTitle() {
        return machineLocalizationConstant.machineConsoleViewTitle() + (hasUnreadMessages ? " *" : "");
    }

    /** {@inheritDoc} */
    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    /** {@inheritDoc} */
    @Override
    public ImageResource getTitleImage() {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String getTitleToolTip() {
        return machineLocalizationConstant.machineConsoleViewTooltip();
    }

    /** {@inheritDoc} */
    @Override
    public void go(AcceptsOneWidget container) {
        consoleToolbar.go(view.getToolbarPanel());
        container.setWidget(view);
    }

    /** Print message to console. */
    public void print(String message) {
        setActive();

        view.print(message);
        view.scrollBottom();

        final PartPresenter activePart = partStack.getActivePart();
        if (activePart == null || !activePart.equals(this)) {
            hasUnreadMessages = true;
        }

        firePropertyChange(TITLE_PROPERTY);
    }

    /** Set the console active (selected) in the parts stack. */
    private void setActive() {
        final PartPresenter activePart = partStack.getActivePart();
        if (activePart == null || !activePart.equals(this)) {
            partStack.setActivePart(this);
        }
    }

    /** Clear console. */
    public void clear() {
        view.clear();
    }
}

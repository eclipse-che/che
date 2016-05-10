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
package org.eclipse.che.ide.extension.machine.client.outputspanel;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.event.project.CloseCurrentProjectEvent;
import org.eclipse.che.ide.api.event.project.CloseCurrentProjectHandler;
import org.eclipse.che.ide.api.event.project.ProjectReadyEvent;
import org.eclipse.che.ide.api.event.project.ProjectReadyHandler;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.HasView;
import org.eclipse.che.ide.api.outputconsole.OutputConsole;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.api.dialogs.ConfirmCallback;
import org.eclipse.che.ide.api.dialogs.DialogFactory;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * Container for the output consoles.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class OutputsContainerPresenter extends BasePresenter implements OutputsContainerView.ActionDelegate,
                                                                        HasView,
                                                                        ProjectReadyHandler,
                                                                        CloseCurrentProjectHandler {

    private final MachineLocalizationConstant localizationConstant;
    private final DialogFactory               dialogFactory;
    private final OutputsContainerView        view;
    private final Resources                   resources;

    private final List<OutputConsole> consoles;

    @Inject
    public OutputsContainerPresenter(OutputsContainerView view,
                                     MachineLocalizationConstant localizationConstant,
                                     EventBus eventBus,
                                     DialogFactory dialogFactory,
                                     Resources resources) {
        this.view = view;
        this.localizationConstant = localizationConstant;
        this.dialogFactory = dialogFactory;
        this.resources = resources;

        this.view.setTitle(localizationConstant.outputsConsoleViewTitle());
        this.view.setDelegate(this);

        consoles = new ArrayList<>();

        eventBus.addHandler(ProjectReadyEvent.TYPE, this);
    }

    /** Add {@code console} to the container. */
    public void addConsole(final OutputConsole console) {
        // check whether console for an appropriate command is already opened
        OutputConsole existingOutputConsole = null;
        for (final OutputConsole outputConsole : consoles) {
            if (outputConsole.isFinished() && console.getTitle().equals(outputConsole.getTitle())) {
                existingOutputConsole = outputConsole;
                break;
            }
        }

        if (existingOutputConsole == null) {
            console.go(new AcceptsOneWidget() {
                @Override
                public void setWidget(IsWidget widget) {
                    consoles.add(console);
                    view.addConsole(console.getTitle(), widget);
                    view.showConsole(consoles.size() - 1);
                }
            });
        } else {
            // replace existing console with new one
            final int existingConsoleIndex = consoles.indexOf(existingOutputConsole);
            console.go(new AcceptsOneWidget() {
                @Override
                public void setWidget(IsWidget widget) {
                    // add new console in place of existing one
                    consoles.add(existingConsoleIndex, console);
                    view.insertConsole(console.getTitle(), widget, existingConsoleIndex);

                    // remove existing console
                    consoles.remove(existingConsoleIndex + 1);
                    view.removeConsole(existingConsoleIndex + 1);

                    view.showConsole(existingConsoleIndex);
                }
            });
        }

        firePropertyChange(TITLE_PROPERTY);
    }

    @Override
    public View getView() {
        return view;
    }

    @NotNull
    @Override
    public String getTitle() {
        return localizationConstant.outputsConsoleViewTitle();
    }

    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    @Nullable
    @Override
    public SVGResource getTitleImage() {
        return resources.outputPartIcon();
    }

    @Override
    public int getUnreadNotificationsCount() {
        return consoles.size();
    }

    @Override
    public String getTitleToolTip() {
        return localizationConstant.outputsConsoleViewTooltip();
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(view);
    }

    @Override
    public void onConsoleSelected(int index) {
        view.showConsole(index);
    }

    @Override
    public void onConsoleClose(final int index) {
        final OutputConsole console = consoles.get(index);
        if (console.isFinished()) {
            closeConsole(console);
        } else {
            final ConfirmCallback confirmCallback = new ConfirmCallback() {
                @Override
                public void accepted() {
                    closeConsole(console);
                }
            };

            dialogFactory.createConfirmDialog("",
                                              localizationConstant.outputsConsoleViewStopProcessConfirmation(console.getTitle()),
                                              confirmCallback, null).show();
        }
    }

    private void closeConsole(OutputConsole console) {
        console.stop();
        console.close();

        final int index = consoles.indexOf(console);
        consoles.remove(index);
        view.removeConsole(index);

        // activate previous console
        if (index > 0) {
            view.showConsole(index - 1);
        }

        firePropertyChange(TITLE_PROPERTY);
    }

    @Override
    public void onProjectReady(ProjectReadyEvent event) {
        firePropertyChange(TITLE_PROPERTY);
    }

    @Override
    public void onCloseCurrentProject(CloseCurrentProjectEvent event) {
        consoles.clear();
        view.removeAllConsoles();
        firePropertyChange(TITLE_PROPERTY);
    }

}

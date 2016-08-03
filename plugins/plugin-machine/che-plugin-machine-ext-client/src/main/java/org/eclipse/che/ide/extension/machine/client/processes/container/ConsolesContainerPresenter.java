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
package org.eclipse.che.ide.extension.machine.client.processes.container;

import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.extension.machine.client.MachineLocalizationConstant;
import org.eclipse.che.ide.extension.machine.client.MachineResources;
import org.eclipse.che.ide.extension.machine.client.processes.ConsolesPanelPresenter;
import org.eclipse.che.ide.extension.machine.client.processes.TerminalsPanelPresenter;
import org.vectomatic.dom.svg.ui.SVGResource;

import javax.validation.constraints.NotNull;

/**
 * Presenter to control the displaying machines processes and terminals.
 *
 * @author Roman Nikitenko
 */
@Singleton
public class ConsolesContainerPresenter extends BasePresenter implements ConsolesContainerView.ActionDelegate {

    private final ConsolesPanelPresenter      consolesPanelPresenter;
    private final TerminalsPanelPresenter     terminalsPanelPresenter;
    private final MachineLocalizationConstant localizationConstant;
    private final ConsolesContainerView       view;
    private final MachineResources            resources;

    @Inject
    public ConsolesContainerPresenter(ConsolesContainerView view,
                                      ConsolesPanelPresenter consolesPanelPresenter,
                                      TerminalsPanelPresenter terminalsPanelPresenter,
                                      MachineLocalizationConstant localizationConstant,
                                      MachineResources resources) {
        this.view = view;
        this.consolesPanelPresenter = consolesPanelPresenter;
        this.terminalsPanelPresenter = terminalsPanelPresenter;
        this.localizationConstant = localizationConstant;
        this.resources = resources;

        this.view.setDelegate(this);
        this.view.setTitle(localizationConstant.viewConsolesTitle());
        consolesPanelPresenter.setParent(this);
        terminalsPanelPresenter.setParent(this);
    }

    @Override
    public View getView() {
        return view;
    }

    @NotNull
    @Override
    public String getTitle() {
        return localizationConstant.viewConsolesTitle();
    }

    @Override
    public void setVisible(boolean visible) {
        view.setVisible(visible);
    }

    @Nullable
    @Override
    public SVGResource getTitleImage() {
        return resources.terminal();
    }

    @Override
    public String getTitleToolTip() {
        return localizationConstant.viewProcessesTooltip();
    }

    @Override
    public void go(AcceptsOneWidget container) {
        consolesPanelPresenter.go(view.getProcessesContainer());
        container.setWidget(view);
    }

    @Override
    public void onDefaultModeClick() {
        view.applyDefaultMode();
        consolesPanelPresenter.go(view.getProcessesContainer());
    }

    @Override
    public void onSplitVerticallyClick() {
        view.splitVertically();
        consolesPanelPresenter.go(view.getProcessesContainer());
        terminalsPanelPresenter.go(view.getTerminalsContainer());
    }

    @Override
    public void onSplitHorizontallyClick() {
        view.splitHorizontally();
        consolesPanelPresenter.go(view.getProcessesContainer());
        terminalsPanelPresenter.go(view.getTerminalsContainer());
    }
}

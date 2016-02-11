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
package org.eclipse.che.ide.actions;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.analytics.client.logger.AnalyticsEventLogger;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.event.ActivePartChangedHandler;
import org.eclipse.che.ide.api.parts.PartPresenter;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.ResourceBasedNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Action for copying items.
 *
 * @author Vitaliy Guliy
 */
@Singleton
public class CopyAction extends Action {
    private final AnalyticsEventLogger     eventLogger;
    private       ProjectExplorerPresenter projectExplorer;
    private       AppContext               appContext;

    private PasteAction    pasteAction;
    private SelectionAgent agent;
    private PartPresenter  activePart;

    @Inject
    public CopyAction(Resources resources,
                      AnalyticsEventLogger eventLogger,
                      ProjectExplorerPresenter projectExplorer,
                      CoreLocalizationConstant localization,
                      AppContext appContext,
                      PasteAction pasteAction,
                      SelectionAgent agent,
                      EventBus eventBus) {
        super(localization.copyItemsActionText(), localization.copyItemsActionDescription(), null, resources.copy());
        this.projectExplorer = projectExplorer;
        this.eventLogger = eventLogger;
        this.appContext = appContext;
        this.pasteAction = pasteAction;
        this.agent = agent;

        eventBus.addHandler(ActivePartChangedEvent.TYPE, new ActivePartChangedHandler() {
            @Override
            public void onActivePartChanged(ActivePartChangedEvent event) {
                activePart = event.getActivePart();
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public void update(ActionEvent e) {
        if ((appContext.getCurrentProject() == null
             && !appContext.getCurrentUser().isUserPermanent()
             && !(activePart instanceof EditorPartPresenter))) {
            e.getPresentation().setVisible(true);
            e.getPresentation().setEnabled(false);
            return;
        }

        e.getPresentation().setEnabled(canCopySelection());
    }

    /**
     * Determines whether the selection can be copied.
     *
     * @return <b>true</b> if the selection can be copied, otherwise returns <b>false</b>
     */
    private boolean canCopySelection() {
        if (activePart instanceof EditorPartPresenter) {
            return false;
        }

        Selection<?> selection = agent.getSelection();
        if (selection == null || selection.isEmpty()) {
            return false;
        }

        if (appContext.getCurrentProject() == null || appContext.getCurrentProject().getRootProject() == null) {
            return false;
        }

        String projectPath = appContext.getCurrentProject().getRootProject().getPath();

        for (Object o : selection.getAllElements()) {
            if (!(o instanceof ResourceBasedNode<?> && o instanceof HasStorablePath)) {
                return false;
            }

            if (projectPath.equals(((HasStorablePath)o).getStorablePath())) {
                return false;
            }
        }

        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(ActionEvent e) {
        eventLogger.log(this);

        if (!canCopySelection()) {
            return;
        }

        List<ResourceBasedNode<?>> copyItems = new ArrayList<>();
        List<?> selection = projectExplorer.getSelection().getAllElements();
        for (Object aSelection : selection) {
            copyItems.add(((ResourceBasedNode<?>)aSelection));
        }
        pasteAction.copyItems(copyItems);
    }

}

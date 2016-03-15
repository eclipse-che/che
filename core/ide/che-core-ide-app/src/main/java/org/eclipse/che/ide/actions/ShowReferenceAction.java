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

import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.selection.Selection;
import org.eclipse.che.ide.api.selection.SelectionAgent;
import org.eclipse.che.ide.reference.ShowReferencePresenter;

/**
 * @author Dmitry Shnurenko
 */
@Singleton
public class ShowReferenceAction extends Action {

    private final ShowReferencePresenter showReferencePresenter;
    private final SelectionAgent         selectionAgent;

    private Object selectedElement;

    @Inject
    public ShowReferenceAction(CoreLocalizationConstant locale,
                               ShowReferencePresenter showReferencePresenter,
                               SelectionAgent selectionAgent) {
        super(locale.showReference());

        this.showReferencePresenter = showReferencePresenter;
        this.selectionAgent = selectionAgent;
    }

    @Override
    public void update(ActionEvent event) {
        Selection<?> selection = selectionAgent.getSelection();

        if (selection == null || selection.isEmpty()) {
            return;
        }

        selectedElement = selection.getHeadElement();


        event.getPresentation().setEnabledAndVisible(selectedElement != null && (selectedElement instanceof HasStorablePath));
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (selectedElement instanceof HasStorablePath) {
            showReferencePresenter.show((HasStorablePath)selectedElement);
        }
    }
}

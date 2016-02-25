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
package org.eclipse.che.ide.ext.java.client.refactoring.move;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.event.ActivePartChangedHandler;
import org.eclipse.che.ide.ext.java.client.JavaLocalizationConstant;

/**
 * The action is called Move presenter when the user has clicked Ctrl+X on some class or package into the 'Project Explorer'.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class CutJavaSourceAction extends Action implements ActivePartChangedHandler {
    private final MoveAction moveAction;

    private boolean isEditorPartActive;

    @Inject
    public CutJavaSourceAction(JavaLocalizationConstant locale, MoveAction moveAction, EventBus eventBus) {
        super(locale.moveActionName(), locale.moveActionDescription());

        this.moveAction = moveAction;

        eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
    }

    @Override
    public void update(ActionEvent actionEvent) {
        actionEvent.getPresentation().setEnabled(isActionEnable());
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        if (isActionEnable()) {
            moveAction.actionPerformed(actionEvent);
        }
    }

    @Override
    public void onActivePartChanged(ActivePartChangedEvent event) {
        isEditorPartActive = event.getActivePart() instanceof EditorPartPresenter;
    }

    private boolean isActionEnable() {
        return !isEditorPartActive && moveAction.isActionEnable();
    }
}

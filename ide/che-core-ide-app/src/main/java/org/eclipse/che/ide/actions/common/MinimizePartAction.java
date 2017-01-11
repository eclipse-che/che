/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.actions.common;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import org.eclipse.che.ide.CoreLocalizationConstant;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.event.ActivePartChangedEvent;
import org.eclipse.che.ide.api.event.ActivePartChangedHandler;
import org.eclipse.che.ide.api.parts.PartStack;

/**
 * Action to minimize active part and corresponding part stack.
 * 
 * @author Vitaliy Guliy
 */
public class MinimizePartAction extends Action implements ActivePartChangedHandler {

    private PartStack activePartStack;

    @Inject
    public MinimizePartAction(final EventBus eventBus,
                              final CoreLocalizationConstant coreLocalizationConstant) {
        super(coreLocalizationConstant.actionMinimizePartTitle(), coreLocalizationConstant.actionMinimizePartDescription());
        eventBus.addHandler(ActivePartChangedEvent.TYPE, this);
    }

    @Override
    public void update(ActionEvent e) {
        if (activePartStack == null) {
            e.getPresentation().setEnabledAndVisible(false);
            return;
        }

        e.getPresentation().setEnabledAndVisible(PartStack.State.NORMAL == activePartStack.getPartStackState());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        activePartStack.minimize();
    }

    @Override
    public void onActivePartChanged(ActivePartChangedEvent event) {
        activePartStack = event.getActivePart().getPartStack();
    }

}

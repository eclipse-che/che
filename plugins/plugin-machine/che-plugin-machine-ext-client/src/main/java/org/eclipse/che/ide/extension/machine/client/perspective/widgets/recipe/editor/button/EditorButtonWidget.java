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
package org.eclipse.che.ide.extension.machine.client.perspective.widgets.recipe.editor.button;

import com.google.inject.ImplementedBy;

import org.eclipse.che.ide.api.mvp.View;

/**
 * Provides methods which allow change visual representation of button on properties panel.
 *
 * @author Valeriy Svydenko
 */
@ImplementedBy(EditorButtonWidgetImpl.class)
public interface EditorButtonWidget extends View<EditorButtonWidget.ActionDelegate> {

    /**
     * Performs some actions when button is enable or disable.
     *
     * @param isEnable
     *         <code>true</code> button is enable, <code>false</code> button is disable
     */
    void setEnable(boolean isEnable);

    /**
     * Sets visibility of buttons.
     *
     * @param visible
     *         <code>true</code> button is visible, <code>false</code> button isn't visible
     */
    void setVisible(boolean visible);

    interface ActionDelegate {
        /** Performs some actions when user click on button. */
        void onButtonClicked();
    }
}
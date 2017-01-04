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
package org.eclipse.che.ide.extension.machine.client.targets;

import com.google.gwt.user.client.ui.IsWidget;

import org.eclipse.che.ide.api.mvp.View;

import java.util.List;

/**
 * View to manage targets.
 *
 * @author Vitaliy Guliy
 * @author Oleksii Orel
 */
public interface TargetsView extends View<TargetsView.ActionDelegate> {

    /**
     * Shows Targets dialog.
     */
    void show();

    /**
     * Hides Targets dialog.
     */
    void hide();

    /**
     * Resets the view to its default value.
     */
    void clear();

    /**
     * Shows a list of available targets.
     *
     * @param targets
     *           list of targets
     */
    void showTargets(List<Target> targets);

    /**
     * Selects a target in the list.
     *
     * @param target
     *         target to select
     */
    boolean selectTarget(Target target);

    /**
     * Shows hint panel.
     */
    void showHintPanel();

    /**
     * Sets a properties panel.
     *
     * @param widget
     */
    void setPropertiesPanel(IsWidget widget);

    interface ActionDelegate {
        // Perform actions when clicking Add target button
        void onAddTarget(String type);

        // Is called when target is deleted
        void onDeleteTarget(Target target);

        // Perform actions when clicking Close button
        void onCloseClicked();

        // Perform actions when selecting a target
        void onTargetSelected(Target target);
    }
}

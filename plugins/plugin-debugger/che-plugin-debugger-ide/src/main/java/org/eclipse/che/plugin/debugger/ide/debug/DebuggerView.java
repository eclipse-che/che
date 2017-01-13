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
package org.eclipse.che.plugin.debugger.ide.debug;

import com.google.gwt.user.client.ui.AcceptsOneWidget;

import org.eclipse.che.api.debug.shared.model.Location;
import org.eclipse.che.api.debug.shared.model.MutableVariable;
import org.eclipse.che.api.debug.shared.model.Variable;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;
import org.eclipse.che.ide.api.debug.Breakpoint;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * Provides methods which allow change view representation of debugger panel. Also the interface contains inner action delegate
 * interface which provides methods which allows react on user's actions.
 *
 * @author Andrey Plotnikov
 * @author Dmitry Shnurenko
 */
public interface DebuggerView extends View<DebuggerView.ActionDelegate> {
    /** Needs for delegate some function into Debugger view. */
    interface ActionDelegate extends BaseActionDelegate {
        /** Performs any actions appropriate in response to the user having pressed the expand button in variables tree. */
        void onExpandVariablesTree();

        /**
         * Performs any actions appropriate in response to the user having selected variable in variables tree.
         *
         * @param variable
         *         variable that is selected
         */
        void onSelectedVariableElement(@NotNull MutableVariable variable);
    }

    /**
     * Sets information about the execution point.
     *
     * @param location
     *         information about the execution point
     */
    void setExecutionPoint(@NotNull Location location);

    /**
     * Sets variables.
     *
     * @param variables
     *         available variables
     */
    void setVariables(@NotNull List<? extends Variable> variables);

    /**
     * Sets breakpoints.
     *
     * @param breakPoints
     *         available breakpoints
     */
    void setBreakpoints(@NotNull List<Breakpoint> breakPoints);

    /**
     * Sets java virtual machine name and version.
     *
     * @param name
     *         virtual machine name
     */
    void setVMName(@NotNull String name);

    /**
     * Sets title.
     *
     * @param title
     *         title of view
     */
    void setTitle(@NotNull String title);

    /** Update contents for selected variable. */
    void updateSelectedVariable();

    /**
     * Add elements into selected variable.
     *
     * @param variables
     *         variable what need to add into
     */
    void setVariablesIntoSelectedVariable(@NotNull List<? extends Variable> variables);

    /**
     * Sets whether this object is visible.
     *
     * @param visible
     *         <code>true</code> to show the tab, <code>false</code> to
     *         hide it
     */
    void setVisible(boolean visible);

    /**
     * Returns selected variable in the variables list on debugger panel or null if no selection.
     *
     * @return selected variable or null if no selection.
     */
    MutableVariable getSelectedDebuggerVariable();

    AcceptsOneWidget getDebuggerToolbarPanel();
}

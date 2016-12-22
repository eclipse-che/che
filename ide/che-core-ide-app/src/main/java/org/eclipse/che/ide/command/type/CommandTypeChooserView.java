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

package org.eclipse.che.ide.command.type;

import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.mvp.View;

import java.util.List;

/**
 * Contract for the view of the machine selector.
 *
 * @author Artem Zatsarynnyi
 */
public interface CommandTypeChooserView extends View<CommandTypeChooserView.ActionDelegate> {

    /** Show the view. */
    void show(int left, int top);

    /** Close the view. */
    void close();

    /** Sets the command types to display in the view. */
    void setTypes(List<CommandType> commandTypes);

    /** The action delegate for this view. */
    interface ActionDelegate {

        /** Called when command type is selected. */
        void onSelected(CommandType commandType);

        /** Called when command type selection has been canceled. Note that view will be already closed. */
        void onCanceled();
    }
}

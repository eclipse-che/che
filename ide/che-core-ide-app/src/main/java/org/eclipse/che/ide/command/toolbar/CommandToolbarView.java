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
package org.eclipse.che.ide.command.toolbar;

import com.google.inject.ImplementedBy;
import org.eclipse.che.api.core.model.machine.Machine;
import org.eclipse.che.ide.api.command.ContextualCommand;
import org.eclipse.che.ide.api.mvp.View;

import java.util.List;

/**
 *
 */
@ImplementedBy(CommandToolbarViewImpl.class)
public interface CommandToolbarView extends View<CommandToolbarView.ActionDelegate> {


    void setRunCommands(List<ContextualCommand> commands);

    interface ActionDelegate {

        void runCommand(ContextualCommand command, Machine machine);
    }
}

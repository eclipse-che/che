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
package org.eclipse.che.ide.command.node;

import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandImpl;

import java.util.List;

/** Factory for different command tree nodes. */
public interface NodeFactory {

    CommandGoalNode newCommandGoalNode(CommandGoal data, List<? extends AbstractCommandNode> commands);

    ExecutableCommandNode newExecutableCommandNode(CommandImpl command, ExecutableCommandNode.ActionDelegate actionDelegate);

    CommandFileNode newCommandFileNode(CommandImpl data);
}

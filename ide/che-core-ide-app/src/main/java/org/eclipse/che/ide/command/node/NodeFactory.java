/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.command.node;

import java.util.List;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.api.command.CommandImpl;

/** Factory for different command tree nodes. */
public interface NodeFactory {

  CommandGoalNode newCommandGoalNode(
      CommandGoal data, List<? extends AbstractCommandNode> commands);

  ExecutableCommandNode newExecutableCommandNode(
      CommandImpl command, ExecutableCommandNode.ActionDelegate actionDelegate);

  CommandFileNode newCommandFileNode(CommandImpl data);
}

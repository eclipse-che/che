/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.command.toolbar.commands.button;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.eclipse.che.ide.api.command.CommandGoal;
import org.eclipse.che.ide.command.toolbar.ToolbarMessages;
import org.eclipse.che.ide.ui.menubutton.MenuItem;

/**
 * A {@link MenuItem} represents a hint which guides the user into the flow of creating a command.
 */
public class GuideItem implements MenuItem {

  private final ToolbarMessages messages;
  private final CommandGoal goal;

  @Inject
  public GuideItem(ToolbarMessages messages, @Assisted CommandGoal goal) {
    this.messages = messages;
    this.goal = goal;
  }

  @Override
  public String getName() {
    return messages.guideItemLabel(goal.getId());
  }
}

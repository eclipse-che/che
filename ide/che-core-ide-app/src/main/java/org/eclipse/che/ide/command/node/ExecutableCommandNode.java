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
package org.eclipse.che.ide.command.node;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import org.eclipse.che.ide.api.command.CommandImpl;
import org.eclipse.che.ide.command.CommandUtils;
import org.eclipse.che.ide.ui.smartTree.data.HasAction;

/**
 * Extension of {@link AbstractCommandNode} that can execute a command when performing an action is
 * requested.
 */
public class ExecutableCommandNode extends AbstractCommandNode implements HasAction {

  private final ActionDelegate actionDelegate;

  @Inject
  public ExecutableCommandNode(
      @Assisted CommandImpl data,
      @Assisted ActionDelegate actionDelegate,
      CommandUtils commandUtils) {
    super(data, null, commandUtils);

    this.actionDelegate = actionDelegate;
  }

  @Override
  public void actionPerformed() {
    actionDelegate.actionPerformed();
  }

  /** Interface for delegating performing action on node. */
  public interface ActionDelegate {
    void actionPerformed();
  }
}

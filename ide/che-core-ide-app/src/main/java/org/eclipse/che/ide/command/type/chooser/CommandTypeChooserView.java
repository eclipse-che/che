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
package org.eclipse.che.ide.command.type.chooser;

import java.util.List;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.api.mvp.View;

/**
 * View for command type chooser.
 *
 * @author Artem Zatsarynnyi
 */
public interface CommandTypeChooserView extends View<CommandTypeChooserView.ActionDelegate> {

  /**
   * Show the view at the position relative to the browser's client area.
   *
   * @param left the left position, in pixels
   * @param top the top position, in pixels
   */
  void show(int left, int top);

  /** Close the view. */
  void close();

  /** Sets the command types to display in the view. */
  void setCommandTypes(List<CommandType> commandTypes);

  /** The action delegate for this view. */
  interface ActionDelegate {

    /** Called when command type is selected. */
    void onSelected(CommandType commandType);

    /**
     * Called when command type selection has been canceled. Note that view will be already closed.
     */
    void onCanceled();
  }
}

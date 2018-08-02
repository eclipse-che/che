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
package org.eclipse.che.plugin.languageserver.ide.editor.quickassist;

import java.util.List;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.Presentation;

/**
 * An extension of {@link ActionEvent} to pass command paramenters as a list of Objects to the
 * action.
 *
 * @author Thomas Mäder
 */
public class QuickassistActionEvent extends ActionEvent {

  private List<Object> arguments;

  public QuickassistActionEvent(
      Presentation presentation, ActionManager actionManager, List<Object> arguments) {
    super(presentation, actionManager);
    this.arguments = arguments;
  }

  public List<Object> getArguments() {
    return arguments;
  }
}

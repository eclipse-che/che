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
package org.eclipse.che.ide.actions.find;

import java.util.Map;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.mvp.View;

/** @author Evgen Vidolob */
public interface FindActionView extends View<FindActionView.ActionDelegate> {

  void focusOnInput();

  void show();

  void hide();

  String getName();

  void showActions(Map<Action, String> actions);

  void hideActions();

  boolean getCheckBoxState();

  interface ActionDelegate {

    void nameChanged(String name, boolean checkBoxState);

    void onClose();

    void onActionSelected(Action action);
  }
}

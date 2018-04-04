/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

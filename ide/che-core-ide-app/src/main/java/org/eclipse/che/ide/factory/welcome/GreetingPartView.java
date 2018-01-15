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
package org.eclipse.che.ide.factory.welcome;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;

/** @author Vitaliy Guliy */
public interface GreetingPartView extends View<GreetingPartView.ActionDelegate> {

  interface ActionDelegate extends BaseActionDelegate {}

  /**
   * Set title of greeting part.
   *
   * @param title title that need to be set
   */
  void setTitle(String title);

  /**
   * Sets new URL of greeting page.
   *
   * @param url
   */
  void showGreeting(String url);

  void setVisible(boolean visible);
}

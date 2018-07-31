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
package org.eclipse.che.selenium.core.action;

import com.google.inject.Singleton;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

/**
 * Mac OS based actions factory.
 *
 * @author Vlad Zhukovskyi
 * @see MacOSActions
 * @see ActionsFactory
 */
@Singleton
public class MacOSActionsFactory implements ActionsFactory {

  @Override
  public Actions createAction(WebDriver webDriver) {
    return new MacOSActions(webDriver);
  }
}

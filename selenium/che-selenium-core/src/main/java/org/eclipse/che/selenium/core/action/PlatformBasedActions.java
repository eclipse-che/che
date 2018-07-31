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

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.SendKeysAction;
import org.openqa.selenium.internal.Locatable;

/**
 * Abstract class for platform based actions. Generify the interface for using selenium action
 * independently from the OS on which tests are running.
 *
 * @author Vlad Zhukovskyi
 */
public abstract class PlatformBasedActions extends Actions {

  public PlatformBasedActions(WebDriver driver) {
    super(driver);
  }

  @Override
  public Actions sendKeys(WebElement element, CharSequence... keysToSend) {
    action.addAction(
        new SendKeysAction(keyboard, mouse, (Locatable) element, modifyCharSequence(keysToSend)));
    return this;
  }

  protected abstract CharSequence[] modifyCharSequence(CharSequence... keysToSend);
}

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
import org.openqa.selenium.interactions.Actions;

/**
 * Generic actions. Default extension of the {@link Actions} which actually doesn't modify a
 * behavior of internal {@link Actions}.
 *
 * @author Vlad Zhukovskyi
 * @see Actions
 */
public class GenericActions extends PlatformBasedActions {
  public GenericActions(WebDriver driver) {
    super(driver);
  }

  @Override
  protected CharSequence[] modifyCharSequence(CharSequence... keysToSend) {
    return keysToSend;
  }
}

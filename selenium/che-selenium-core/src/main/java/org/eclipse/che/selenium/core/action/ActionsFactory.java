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
 * Main interface for producing new instances of {@link Actions}.
 *
 * @author Vlad Zhukovskyi
 */
public interface ActionsFactory {

  /**
   * Creates a new instance of {@link Actions} based on input {@code webDriver}
   *
   * @param webDriver instance of {@link WebDriver}
   * @return a new instance of {@link Actions}
   */
  Actions createAction(WebDriver webDriver);
}

/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
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

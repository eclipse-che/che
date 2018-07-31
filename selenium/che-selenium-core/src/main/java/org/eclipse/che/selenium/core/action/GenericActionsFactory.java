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

import com.google.inject.Singleton;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Actions;

/**
 * Default actions factory for the generic operation system, linux, windows, etc.
 *
 * @author Vlad Zhukovskyi
 * @see GenericActions
 * @see ActionsFactory
 */
@Singleton
public class GenericActionsFactory implements ActionsFactory {
  @Override
  public Actions createAction(WebDriver webDriver) {
    return new GenericActions(webDriver);
  }
}

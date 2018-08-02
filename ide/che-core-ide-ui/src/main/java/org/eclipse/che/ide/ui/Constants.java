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
package org.eclipse.che.ide.ui;

import org.eclipse.che.ide.util.browser.UserAgent;

/** Constants that we can use in CssResource expressions. */
public final class Constants {
  public static final int SCROLLBAR_SIZE = UserAgent.isFirefox() ? 7 : 5;

  /** A timer delay for actions that happen after a "hover" period. */
  public static final int MOUSE_HOVER_DELAY = 600;

  private Constants() {} // COV_NF_LINE
}

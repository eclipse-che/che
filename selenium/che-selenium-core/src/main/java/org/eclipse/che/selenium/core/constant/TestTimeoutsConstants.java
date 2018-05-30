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
package org.eclipse.che.selenium.core.constant;

/** Represent of main constants for tests in seconds */
public final class TestTimeoutsConstants {
  public static final int MULTIPLE = 1;
  public static final int APPLICATION_START_TIMEOUT_SEC = 300 * MULTIPLE;
  public static final int PREPARING_WS_TIMEOUT_SEC = 240 * MULTIPLE;
  public static final int UPDATING_PROJECT_TIMEOUT_SEC = 180 * MULTIPLE;
  public static final int EXPECTED_MESS_IN_CONSOLE_SEC = 120 * MULTIPLE;
  public static final int LOADER_TIMEOUT_SEC = 60 * MULTIPLE;
  public static final int WIDGET_TIMEOUT_SEC = 40 * MULTIPLE;
  public static final int ELEMENT_TIMEOUT_SEC = 20 * MULTIPLE;
  public static final int LOAD_PAGE_TIMEOUT_SEC = 10 * MULTIPLE;
  public static final int REDRAW_UI_ELEMENTS_TIMEOUT_SEC = 5 * MULTIPLE;
  public static final int ATTACHING_ELEM_TO_DOM_SEC = 3 * MULTIPLE;
  public static final int MINIMUM_SEC = MULTIPLE;
  public static final int DEFAULT_TIMEOUT = LOAD_PAGE_TIMEOUT_SEC;
}

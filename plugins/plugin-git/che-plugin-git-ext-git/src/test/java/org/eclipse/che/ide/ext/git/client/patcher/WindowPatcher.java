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
package org.eclipse.che.ide.ext.git.client.patcher;

import com.google.gwt.user.client.Window;
import com.googlecode.gwt.test.patchers.PatchClass;
import com.googlecode.gwt.test.patchers.PatchMethod;

/**
 * Patcher for Window class. Replace native method into Window.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
@PatchClass(Window.class)
public class WindowPatcher {
  public static final String RETURNED_MESSAGE = "returned text";

  /** Patch prompt method. */
  @PatchMethod(override = true)
  public static String prompt(String msg, String initialValue) {
    return RETURNED_MESSAGE;
  }

  /** Patch confirm method. */
  @PatchMethod(override = true)
  public static boolean confirm(String msg) {
    return true;
  }

  /** Patch alert method. */
  @PatchMethod(override = true)
  public static void alert(String msg) {
    // do nothing
  }
}

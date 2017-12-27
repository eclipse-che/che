/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */

package org.eclipse.che.ide.js.api.dialog;

import jsinterop.annotations.JsFunction;

/**
 * Provides ability to perform some actions when the user clicks on confirmation button of input
 * dialog.
 *
 * @author Roman Nikitenko
 */
@JsFunction
@FunctionalInterface
public interface InputAcceptedHandler {

  /**
   * Used when the user clicks on confirmation button.
   *
   * @param value the string typed into input dialog
   */
  void onInputAccepted(String value);
}

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

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * Container for the information necessary to create Choice dialog
 *
 * @author Roman Nikitenko
 */
@JsType(isNative = true, name = "?", namespace = JsPackage.GLOBAL)
public interface ChoiceDialogData extends DialogData {

  /** Returns the first choice button text. */
  @JsProperty(name = "firstChoiceButtonText")
  String getFirstChoiceButtonText();

  /** Returns the second choice button text. */
  @JsProperty(name = "secondChoiceButtonText")
  String getSecondChoiceButtonText();

  /** Returns the third choice button text. */
  @JsProperty(name = "thirdChoiceButtonText")
  String getThirdChoiceButtonText();
}

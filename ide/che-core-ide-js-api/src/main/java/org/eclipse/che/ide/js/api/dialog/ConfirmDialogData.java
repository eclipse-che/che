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

import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

/**
 * Container for the information necessary to create Confirm dialog
 *
 * @author Roman Nikitenko
 */
@JsType(isNative = true)
public interface ConfirmDialogData extends MessageDialogData {

  /** Returns the Cancel button text. */
  @JsProperty
  String getCancelButtonText();
}

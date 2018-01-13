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
package org.eclipse.che.ide.terminal.helpers;

import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import org.eclipse.che.ide.terminal.Terminal;

/**
 * Helper element to simplify evaluation {@link Terminal} scrollbar size.
 *
 * @author Alexander Andrienko
 */
@JsType(isNative = true, namespace = JsPackage.GLOBAL)
public class ScrollBarMeasure {

  public native int getHorizontalWidth();

  public native int getVerticalWidth();
}

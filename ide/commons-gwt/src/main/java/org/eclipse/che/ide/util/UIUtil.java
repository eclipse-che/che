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
package org.eclipse.che.ide.util;

import com.google.gwt.user.client.ui.FocusWidget;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for GWT Elements.
 *
 * @author Evgen Vidolob
 * @author Vlad Zhukovskyi
 */
public class UIUtil {
  public static final char MNEMONIC = 0x1B;

  /**
   * Lookup all nested children in widget and tries to search children which implement Focusable
   * interface.
   *
   * @param widget widget to lookup
   * @return list of {@link com.google.gwt.user.client.ui.Focusable} widgets or empty list if none
   *     was found
   * @see com.google.gwt.user.client.ui.Focusable
   */
  public static List<FocusWidget> getFocusableChildren(Widget widget) {
    List<FocusWidget> focusable = new ArrayList<>();

    if (widget instanceof FocusWidget) {
      focusable.add((FocusWidget) widget);
    }

    if (widget instanceof HasWidgets) {
      for (Widget w : ((HasWidgets) widget)) {
        focusable.addAll(getFocusableChildren(w));
      }
    }

    return focusable;
  }
}

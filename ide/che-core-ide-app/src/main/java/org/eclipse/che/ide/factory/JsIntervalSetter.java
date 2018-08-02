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
package org.eclipse.che.ide.factory;

import com.google.inject.Singleton;

/**
 * When user changes browser tab and IDE executes into inactive tab, browser set code execution
 * interval to improve performance. For example Chrome and Firefox set 1000ms interval. The method
 * override global setInterval function and set custom value (100ms) of interval. This solution fix
 * issue when we need execute some code into inactive tab permanently, for example launch factory.
 */
@Singleton
class JsIntervalSetter {

  JsIntervalSetter() {
    setCustomInterval();
  }

  private native void setCustomInterval() /*-{
        var customInterval = 10;
        var setInterval = function () {
            clearInterval(interval);
            customInterval *= 10;
        };

        var interval = setInterval(setInterval, customInterval);
    }-*/;
}

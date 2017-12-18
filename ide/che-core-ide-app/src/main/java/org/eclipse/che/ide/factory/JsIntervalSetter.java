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

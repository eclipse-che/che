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
package org.eclipse.che.ide.ext.git.client.patcher;

import com.googlecode.gwt.test.patchers.PatchClass;
import com.googlecode.gwt.test.patchers.PatchMethod;
import org.eclipse.che.security.oauth.JsOAuthWindow;

/**
 * Patcher for JsOAuthWindow class. Replace native method into JsOAuthWindow.
 *
 * @author <a href="mailto:aplotnikov@codenvy.com">Andrey Plotnikov</a>
 */
@PatchClass(JsOAuthWindow.class)
public class JsOAuthWindowPatcher {

  /** Patch loginWithOAuth method. */
  @PatchMethod(override = true)
  public static void loginWithOAuth(
      JsOAuthWindow window,
      String authUrl,
      String errUrl,
      int popupHeight,
      int popupWidth,
      int clientHeight,
      int clientWidth) {
    // do nothing
  }
}

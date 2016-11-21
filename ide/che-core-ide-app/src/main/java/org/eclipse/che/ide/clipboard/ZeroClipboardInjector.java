/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.clipboard;

import org.eclipse.che.ide.api.component.Component;
import org.eclipse.che.ide.util.loging.Log;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.ScriptInjector;
import com.google.inject.Singleton;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class ZeroClipboardInjector implements Component {

    @Override
    public void start(Callback<Component, Exception> callback) {
        // Inject ZeroClipboard script
        ScriptInjector.fromUrl(GWT.getModuleBaseForStaticFiles() + "ZeroClipboard.min.js").setWindow(ScriptInjector.TOP_WINDOW)
                      .setCallback(new Callback<Void, Exception>() {
                          @Override
                          public void onSuccess(Void result) {
                          }

                          @Override
                          public void onFailure(Exception e) {
                              Log.error(getClass(), "Unable to inject ZeroClipboard.min.js", e);
                          }
                      }).inject();
        callback.onSuccess(this);
    }
}

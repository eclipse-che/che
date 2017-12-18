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
package org.eclipse.che.plugin.activity.ide;

import static com.google.gwt.core.client.ScriptInjector.TOP_WINDOW;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.ScriptInjector;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.extension.Extension;

/**
 * Adds activity tracking script to IDE.
 *
 * @author Max Shaposhnik (mshaposhnik@codenvy.com)
 */
@Singleton
@Extension(title = "Activity Tracking Extension", version = "1.0.0")
public class ActivityTrackingExtension {

  @Inject
  public ActivityTrackingExtension(AppContext appContext) {

    ScriptInjector.fromUrl("/_app/activity.js")
        .setWindow(TOP_WINDOW)
        .setCallback(
            new Callback<Void, Exception>() {
              @Override
              public void onSuccess(Void result) {
                init(appContext.getMasterApiEndpoint(), appContext.getWorkspaceId());
              }

              @Override
              public void onFailure(Exception reason) {}

              private native void init(String restContext, String wsId) /*-{
                              $wnd.ActivityTracker.init(restContext, wsId);
                          }-*/;
            })
        .inject();
  }
}

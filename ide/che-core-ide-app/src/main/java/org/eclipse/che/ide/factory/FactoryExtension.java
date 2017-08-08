/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.factory;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.ScriptInjector;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.DefaultActionGroup;
import org.eclipse.che.ide.api.extension.Extension;
import org.eclipse.che.ide.factory.accept.AcceptFactoryHandler;
import org.eclipse.che.ide.factory.action.CreateFactoryAction;
import org.eclipse.che.ide.factory.json.ImportFromConfigAction;
import org.eclipse.che.ide.factory.welcome.OpenWelcomePageAction;

import static com.google.gwt.core.client.ScriptInjector.TOP_WINDOW;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_PROJECT;
import static org.eclipse.che.ide.api.action.IdeActions.GROUP_WORKSPACE;

/**
 * @author Vladyslav Zhukovskii
 */
@Singleton
@Extension(title = "Factory", version = "3.0.0")
public class FactoryExtension {

    @Inject
    public FactoryExtension(AcceptFactoryHandler acceptFactoryHandler,
                            ActionManager actionManager,
                            FactoryResources resources,
                            CreateFactoryAction configureFactoryAction,
                            ImportFromConfigAction importFromConfigAction,
                            OpenWelcomePageAction openWelcomePageAction) {
        acceptFactoryHandler.process();

        /*
         * Inject resources and js
         */
        ScriptInjector.fromUrl("https://apis.google.com/js/client:plusone.js?parsetags=explicit")
                      .setWindow(TOP_WINDOW)
                      .inject();

        ScriptInjector.fromUrl("https://connect.facebook.net/en_US/sdk.js")
                      .setWindow(TOP_WINDOW)
                      .setCallback(new Callback<Void, Exception>() {
                          @Override
                          public void onSuccess(Void result) {
                              init();
                          }

                          @Override
                          public void onFailure(Exception reason) {
                          }

                          private native void init() /*-{
                              $wnd.FB.init({
                                  appId: "318167898391385",
                                  xfbml: true,
                                  version: "v2.1"
                              });
                          }-*/;
                      }).inject();

        resources.factoryCSS().ensureInjected();

        DefaultActionGroup projectGroup = (DefaultActionGroup)actionManager.getAction(GROUP_PROJECT);
        DefaultActionGroup workspaceGroup = (DefaultActionGroup)actionManager.getAction(GROUP_WORKSPACE);

        actionManager.registerAction("openWelcomePage", openWelcomePageAction);
        actionManager.registerAction("importProjectFromCodenvyConfigAction", importFromConfigAction);
        actionManager.registerAction("configureFactoryAction", configureFactoryAction);

        projectGroup.add(importFromConfigAction);
        workspaceGroup.add(configureFactoryAction);
    }
}

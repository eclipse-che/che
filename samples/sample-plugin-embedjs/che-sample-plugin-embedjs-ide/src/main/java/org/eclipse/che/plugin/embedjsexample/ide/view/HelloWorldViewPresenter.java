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
package org.eclipse.che.plugin.embedjsexample.ide.view;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BasePresenter;
import org.eclipse.che.ide.util.loging.Log;
import org.eclipse.che.plugin.embedjsexample.ide.HelloWorldResources;
import org.eclipse.che.plugin.embedjsexample.ide.common.Constants;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * @author Mathias Schaefer <mathias.schaefer@eclipsesource.com>
 */
@Singleton
public class HelloWorldViewPresenter extends BasePresenter implements HelloWorldView.ActionDelegate {

    private final HelloWorldView helloWorldView;

    @Inject
    public HelloWorldViewPresenter(final HelloWorldView helloWorldView) {
        this.helloWorldView = helloWorldView;

        ScriptInjector.fromUrl(GWT.getModuleBaseURL() + Constants.JAVASCRIPT_FILE_ID)
                .setWindow(ScriptInjector.TOP_WINDOW)
                .setCallback(new Callback<Void, Exception>() {
                    @Override
                    public void onSuccess(final Void result) {
                        Log.info(HelloWorldViewPresenter.class, Constants.JAVASCRIPT_FILE_ID + " loaded.");
                        sayHello("Hello from Java Script!");
                    }

                    @Override
                    public void onFailure(final Exception e) {
                        Log.error(HelloWorldViewPresenter.class, "Unable to load "+Constants.JAVASCRIPT_FILE_ID, e);
                    }
                }).inject();

    }

    private void sayHello(String content) {
        this.helloWorldView.sayHello(content);
    }

    @Override
    public String getTitle() {
        return "Hello world";
    }

    @Override
    public SVGResource getTitleImage() {
        return (HelloWorldResources.INSTANCE.icon());
    }

    @Override
    public View getView() {
        return helloWorldView;
    }

    @Override
    public String getTitleToolTip() {
        return getTitle();
    }

    @Override
    public void go(AcceptsOneWidget container) {
        container.setWidget(helloWorldView);
    }
}

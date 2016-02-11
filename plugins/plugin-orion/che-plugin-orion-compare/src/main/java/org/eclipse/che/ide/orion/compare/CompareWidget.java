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
package org.eclipse.che.ide.orion.compare;

import elemental.events.Event;
import elemental.events.EventListener;
import elemental.html.Window;
import elemental.js.html.JsIFrameElement;
import elemental.json.Json;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Frame;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.api.promises.client.callback.PromiseHelper;
import org.eclipse.che.ide.ui.loaders.request.LoaderFactory;
import org.eclipse.che.ide.ui.loaders.request.MessageLoader;

/**
 * Widget for compering (diff) for two files.
 * @author Evgen Vidolob
 */
public class CompareWidget extends Composite {

    private Frame           frame;
    private Promise<Window> framePromise;
    private CompareConfig   compareConfig;

    public CompareWidget(final CompareConfig compareConfig, final String themeId, LoaderFactory loaderFactory) {
        this.compareConfig = compareConfig;
        this.frame = new Frame(GWT.getModuleBaseURL() + "/Compare.html");
        initWidget(frame);
        setSize("100%", "100%");
        final MessageLoader loader = loaderFactory.newLoader();
        loader.show();
        frame.getElement().getStyle().setBorderStyle(Style.BorderStyle.NONE);
        AsyncPromiseHelper.RequestCall<Window> call = new AsyncPromiseHelper.RequestCall<Window>() {
            @Override
            public void makeCall(final AsyncCallback<Window> callback) {
                frame.addLoadHandler(new LoadHandler() {
                    @Override
                    public void onLoad(LoadEvent event) {
                        frame.getElement().cast();
                        final JsIFrameElement iFrame = frame.getElement().cast();
                        callback.onSuccess(iFrame.getContentWindow());
                        loader.hide();
                    }
                });
            }
        };
        framePromise = PromiseHelper.newPromise(call);
        framePromise.then(new Operation<Window>() {
            @Override
            public void apply(Window arg) throws OperationException {
                sendThemeId(arg, themeId);
            }
        });
        framePromise.then(new Operation<Window>() {
            @Override
            public void apply(final Window arg) throws OperationException {
                arg.getDocument().addEventListener("onThemeLoaded", new EventListener() {
                    @Override
                    public void handleEvent(Event evt) {
                        sendConfig(arg, compareConfig);
                        loader.hide();
                    }
                }, false);
            }
        });
    }

    private void sendThemeId(Window arg, String themeId) {
        JSONObject obj = new JSONObject();
        obj.put("type", new JSONString("theme"));
        obj.put("message", new JSONString(themeId));
        arg.postMessage(obj.toString(), "*");
    }

    private void sendConfig(Window arg, CompareConfig compareConfig) {
        String message = "{\"type\": \"config\", \"message\":" + Json.create(compareConfig.toJson()).toJson() + "}";
        arg.postMessage(message, "*");
    }

    /**
     * Refresh compare according to configuration
     */
    public void refresh() {
        framePromise.then(new Operation<Window>() {
            @Override
            public void apply(Window arg) throws OperationException {
                sendConfig(arg, compareConfig);
            }
        });
    }
}

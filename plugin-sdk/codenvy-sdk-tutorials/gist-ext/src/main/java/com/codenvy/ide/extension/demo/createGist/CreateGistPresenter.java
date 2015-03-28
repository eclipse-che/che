/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package com.codenvy.ide.extension.demo.createGist;

import org.eclipse.che.ide.api.notification.Notification;
import org.eclipse.che.ide.api.notification.NotificationManager;
import org.eclipse.che.ide.api.parts.ConsolePart;
import com.codenvy.ide.extension.demo.GistExtensionLocalizationConstant;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/** Presenter for creating Gist on GitHub from code snippet. */
@Singleton
public class CreateGistPresenter implements CreateGistView.ActionDelegate {
    private static final String GIT_HUB_GISTS_API            = "https://api.github.com/gists";
    private static final String GIT_HUB_ANONYMOUS_GISTS_HOST = "gist.github.com/anonymous";
    private CreateGistView                    view;
    private GistExtensionLocalizationConstant constant;
    private ConsolePart                       console;
    private NotificationManager               notificationManager
            ;

    /**
     * Create presenter.
     *  @param view
     * @param console
     * @param constant
     * @param notificationManager
     */
    @Inject
    public CreateGistPresenter(CreateGistView view, ConsolePart console,
                               GistExtensionLocalizationConstant constant, NotificationManager notificationManager) {
        this.view = view;
        this.notificationManager = notificationManager;
        this.view.setDelegate(this);
        this.console = console;
        this.constant = constant;
    }

    /** Show dialog. */
    public void showDialog() {
        view.setPublic(true);
        view.setSnippet("sample snippet");
        view.focusInSnippetField();
        view.setEnableCreateButton(true);
        view.showDialog();
    }

    /** {@inheritDoc} */
    @Override
    public void onCreateClicked() {
        RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, GIT_HUB_GISTS_API);
        try {
            builder.sendRequest(getRequestData(), new RequestCallback() {
                public void onResponseReceived(Request request, Response response) {
                    JSONObject json = JSONParser.parseStrict(response.getText()).isObject();
                    if (json == null) {
                        Window.alert(constant.detectGistIdError());
                        return;
                    }
                    afterGistCreated(json.get("id").isString().stringValue());
                }

                public void onError(Request request, Throwable exception) {
                    Window.alert(constant.createGistError());
                }
            });
        } catch (RequestException e) {
            Window.alert(constant.createGistError());
        }
        view.close();
    }

    /** {@inheritDoc} */
    @Override
    public void onCancelClicked() {
        view.close();
    }

    /** {@inheritDoc} */
    @Override
    public void onValueChanged() {
        String message = view.getSnippet();
        view.setEnableCreateButton(!message.isEmpty());
    }

    private String getRequestData() {
        final String title = "Sample snippet title";
        final String content = view.getSnippet();
        final boolean isPublic = view.isPublic();
        final String description = "This snippet created from Codenvy";

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("description", new JSONString(description));
        jsonObject.put("public", JSONBoolean.getInstance(isPublic));
        JSONObject filesJsonObject = new JSONObject();
        JSONObject contentJsonObject = new JSONObject();
        contentJsonObject.put("content", new JSONString(content));
        jsonObject.put("files", filesJsonObject);
        filesJsonObject.put(title, contentJsonObject);

        return jsonObject.toString();
    }

    private void afterGistCreated(String gistId) {
        UrlBuilder builder = new UrlBuilder();
        final String url = builder.setProtocol("https").setHost(GIT_HUB_ANONYMOUS_GISTS_HOST + "/" + gistId).buildString();

        Notification notification = new Notification("Your Gist available on " + "<a href=\"" + url + "\" target=\"_blank\">" + url + "</a>", Notification.Type.INFO);
        notificationManager.showNotification(notification);


    }
}
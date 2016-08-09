/*******************************************************************************
 * Copyright (c) 2016 Serli SAS.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Sun Seng David TAN <sunix@sunix.org> - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.flux.liveedit;

import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.events.DocumentChangeEvent;
import org.eclipse.che.ide.socketio.Message;

import com.google.gwt.core.client.JsonUtils;

public class FluxMessageBuilder {
    private String fullPath;
    private String addedCharacters;
    private int    offset;
    private int    removeCharCount;
    private String project;
    private String resource;
    private String username;
    private String channelName;

    public FluxMessageBuilder with(Document document) {
        fullPath = document.getFile().getLocation().toString().substring(1);
        project = fullPath.substring(0, fullPath.indexOf('/'));
        resource = fullPath.substring(fullPath.indexOf('/') + 1);
        return this;
    }


    public FluxMessageBuilder with(DocumentChangeEvent event) {
        return this.with(event.getDocument().getDocument()) //
                   .withAddedCharacters(event.getText()) //
                   .withOffset(event.getOffset()) //
                   .withRemovedCharCount(event.getRemoveCharCount());
    }

    public FluxMessageBuilder withRemovedCharCount(int removeCharCount) {
        this.removeCharCount = removeCharCount;
        return this;
    }

    public FluxMessageBuilder withOffset(int offset) {
        this.offset = offset;
        return this;
    }

    public FluxMessageBuilder withUserName(String userName){
        this.username = userName;
        return this;
    }

    public FluxMessageBuilder withChannelName(String channelName){
        this.channelName = channelName;
        return this;
    }

    public FluxMessageBuilder withAddedCharacters(String addedCharacters) {
        this.addedCharacters = JsonUtils.escapeValue(addedCharacters);
        return this;
    }

    public Message buildResourceRequestMessage() {
        String json = "{"//
                      + "\"username\":\""+username+"\","//
                      + "\"project\":\"" + project + "\","//
                      + "\"resource\":\"" + resource + "\","//
                      + "\"channelName\":\"" + channelName + "\"" //
                      + "}";

        return new Message().withType("getResourceRequest")//
                                .withJsonContent(JsonUtils.unsafeEval(json));
    }

    public Message buildLiveResourceChangeMessage() {
        String json = "{"//
                      + "\"username\":\""+username+"\","//
                      + "\"project\":\"" + project + "\","//
                      + "\"resource\":\"" + resource + "\"," //
                      + "\"channelName\":\"" + channelName + "\"," //
                      + "\"offset\":" + offset + "," //
                      + "\"removedCharCount\":" + removeCharCount + "," //
                      + "\"addedCharacters\": " + addedCharacters //
                      + "}";
        return new Message().withType("liveResourceChanged")//
                                .withJsonContent(JsonUtils.unsafeEval(json));
    }

    public Message buildLiveCursorOffsetChangeMessage() {
        String json = "{"//
                + "\"username\":\""+username+"\","//
                + "\"project\":\"" + project + "\","//
                + "\"resource\":\"" + resource + "\"," //
                + "\"channelName\":\"" + channelName + "\"," //
                + "\"offset\":" + offset + "," //
                + "}";
        return new Message().withType("liveCursorOffsetChanged")//
                .withJsonContent(JsonUtils.unsafeEval(json));
    }

}

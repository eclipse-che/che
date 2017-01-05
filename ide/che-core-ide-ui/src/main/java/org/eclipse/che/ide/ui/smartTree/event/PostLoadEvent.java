/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ui.smartTree.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;

import org.eclipse.che.ide.api.data.tree.Node;

import java.util.List;

/**
 * Event fires when children has been loaded and processed.
 *
 * @author Vlad Zhukovskiy
 */
public class PostLoadEvent extends GwtEvent<PostLoadEvent.PostLoadHandler> {

    public interface PostLoadHandler extends EventHandler {
        void onPostLoad(PostLoadEvent event);
    }

    public interface HasPostLoadHandlers {
        public HandlerRegistration addPostLoadHandler(PostLoadHandler handler);
    }

    private static Type<PostLoadHandler> TYPE;

    public static Type<PostLoadHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<>();
        }
        return TYPE;
    }

    private Node       requestedNode;
    private List<Node> receivedNodes;

    public PostLoadEvent(Node requestedNode, List<Node> receivedNodes) {
        this.requestedNode = requestedNode;
        this.receivedNodes = receivedNodes;
    }

    @Override
    public Type<PostLoadHandler> getAssociatedType() {
        return TYPE;
    }

    public Node getRequestedNode() {
        return requestedNode;
    }

    public List<Node> getReceivedNodes() {
        return receivedNodes;
    }

    @Override
    protected void dispatch(PostLoadHandler handler) {
        handler.onPostLoad(this);
    }
}

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
package org.eclipse.che.ide.navigation;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.rest.shared.dto.ServiceError;
import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateEvent;
import org.eclipse.che.api.machine.gwt.client.events.WsAgentStateHandler;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.project.node.Node;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.part.explorer.project.ProjectExplorerPresenter;
import org.eclipse.che.ide.project.node.FileReferenceNode;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.websocket.Message;
import org.eclipse.che.ide.websocket.MessageBuilder;
import org.eclipse.che.ide.websocket.MessageBus;
import org.eclipse.che.ide.websocket.MessageBusProvider;
import org.eclipse.che.ide.websocket.WebSocketException;
import org.eclipse.che.ide.websocket.rest.RequestCallback;
import org.eclipse.che.ide.websocket.rest.Unmarshallable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.gwt.http.client.RequestBuilder.GET;
import static org.eclipse.che.ide.MimeType.APPLICATION_JSON;
import static org.eclipse.che.ide.rest.HTTPHeader.ACCEPT;

/**
 * Presenter for file navigation (find file by name and open it).
 *
 * @author Ann Shumilova
 * @author Artem Zatsarynnyi
 */
@Singleton
public class NavigateToFilePresenter implements NavigateToFileView.ActionDelegate, WsAgentStateHandler {

    private final ProjectExplorerPresenter projectExplorer;
    private final MessageBusProvider       messageBusProvider;
    private final DtoUnmarshallerFactory   dtoUnmarshallerFactory;
    private final NavigateToFileView       view;
    private final DtoFactory               dtoFactory;
    private final AppContext               appContext;

    private Map<String, ItemReference> resultMap;
    private String                     SEARCH_URL;
    private MessageBus                 wsMessageBus;

    @Inject
    public NavigateToFilePresenter(NavigateToFileView view,
                                   AppContext appContext,
                                   EventBus eventBus,
                                   DtoUnmarshallerFactory dtoUnmarshallerFactory,
                                   ProjectExplorerPresenter projectExplorer,
                                   MessageBusProvider messageBusProvider,
                                   DtoFactory dtoFactory) {
        this.view = view;
        this.dtoFactory = dtoFactory;
        this.appContext = appContext;
        this.dtoUnmarshallerFactory = dtoUnmarshallerFactory;
        this.projectExplorer = projectExplorer;
        this.messageBusProvider = messageBusProvider;

        this.view.setDelegate(this);

        resultMap = new HashMap<>();
        eventBus.addHandler(WsAgentStateEvent.TYPE, this);
    }

    @Override
    public void onWsAgentStarted(WsAgentStateEvent event) {
        wsMessageBus = messageBusProvider.getMachineMessageBus();
        SEARCH_URL = "/project/" + appContext.getWorkspace().getId() + "/search";
    }

    @Override
    public void onWsAgentStopped(WsAgentStateEvent event) {
    }

    /** Show dialog with view for navigation. */
    public void showDialog() {
        view.showDialog();
        view.clearInput();
    }

    @Override
    public void onRequestSuggestions(String query, final AsyncCallback<List<ItemReference>> callback) {
        resultMap = new HashMap<>();

        // add '*' to allow search files by first letters
        search(query + "*", new AsyncCallback<List<ItemReference>>() {
            @Override
            public void onSuccess(List<ItemReference> result) {
                for (ItemReference item : result) {
                    final String path = item.getPath();
                    // skip hidden items
                    if (!isItemHidden(path)) {
                        resultMap.put(path, item);
                    }
                }
                List itemReference = new ArrayList<>(resultMap.values());
                callback.onSuccess(itemReference);
            }

            @Override
            public void onFailure(Throwable caught) {
                callback.onFailure(caught);
            }
        });
    }

    @Override
    public void onFileSelected() {
        view.close();
        final ItemReference selectedItem = resultMap.get(view.getItemPath());

        projectExplorer.getNodeByPath(new HasStorablePath.StorablePath(selectedItem.getPath())).then(new Operation<Node>() {
            @Override
            public void apply(Node node) throws OperationException {
                projectExplorer.select(node, false);
                projectExplorer.scrollToNode(node);

                if (node instanceof FileReferenceNode) {
                    ((FileReferenceNode)node).actionPerformed();
                }
            }
        });
    }

    private void search(String fileName, final AsyncCallback<List<ItemReference>> callback) {
        final String url = SEARCH_URL + "/?name=" + URL.encodePathSegment(fileName);
        final Message message = new MessageBuilder(GET, url).header(ACCEPT, APPLICATION_JSON).build();
        final Unmarshallable<List<ItemReference>> unmarshaller = dtoUnmarshallerFactory.newWSListUnmarshaller(ItemReference.class);
        try {
            wsMessageBus.send(message, new RequestCallback<List<ItemReference>>(unmarshaller) {
                @Override
                protected void onSuccess(List<ItemReference> result) {
                    callback.onSuccess(result);
                }

                @Override
                protected void onFailure(Throwable exception) {
                    final String message = dtoFactory.createDtoFromJson(exception.getMessage(), ServiceError.class).getMessage();
                    callback.onFailure(new Exception(message));
                }
            });
        } catch (WebSocketException e) {
            callback.onFailure(e);
        }
    }

    private boolean isItemHidden(String path) {
        return path.contains("/.");
    }
}

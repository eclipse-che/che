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
package org.eclipse.che.ide.project.node.resource;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.project.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper;
import org.eclipse.che.api.promises.client.callback.AsyncPromiseHelper.RequestCall;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.node.HasDataObject;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;

import javax.validation.constraints.NotNull;

import static org.eclipse.che.api.promises.client.callback.PromiseHelper.*;

/**
 * @author Vlad Zhukovskiy
 */
public class ItemReferenceProcessor extends AbstractResourceProcessor<ItemReference> {

    private final AppContext appContext;

    @Inject
    public ItemReferenceProcessor(EventBus eventBus,
                                  AppContext appContext,
                                  ProjectServiceClient projectServiceClient,
                                  DtoUnmarshallerFactory unmarshallerFactory) {
        super(eventBus, projectServiceClient, unmarshallerFactory);
        this.appContext = appContext;
    }

    @Override
    public Promise<ItemReference> delete(@NotNull final HasDataObject<ItemReference> node) {
        return AsyncPromiseHelper.createFromAsyncRequest(new RequestCall<ItemReference>() {
            @Override
            public void makeCall(final AsyncCallback<ItemReference> callback) {
                projectService.delete(appContext.getDevMachine(), node.getData().getPath(), new AsyncRequestCallback<Void>() {
                    @Override
                    protected void onSuccess(Void result) {
                        callback.onSuccess(node.getData());
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        callback.onFailure(exception);
                    }
                });
            }
        });
    }

    @Override
    public Promise<ItemReference> rename(@Nullable final HasStorablePath parent, final @NotNull HasDataObject<ItemReference> node, final @NotNull String newName) {

        return newPromise(new RequestCall<Void>() {
            @Override
            public void makeCall(AsyncCallback<Void> callback) {
                projectService.rename(appContext.getDevMachine(), parent.getStorablePath() + "/" + node.getData().getName(), newName, null, newCallback(callback));
            }
        }).thenPromise(new Function<Void, Promise<ItemReference>>() {
            @Override
            public Promise<ItemReference> apply(Void arg) throws FunctionException {
                return newPromise(new RequestCall<ItemReference>() {
                    @Override
                    public void makeCall(AsyncCallback<ItemReference> callback) {
                        projectService.getItem(appContext.getDevMachine(), parent.getStorablePath() + "/" + newName, newCallback(callback, unmarshallerFactory.newUnmarshaller(ItemReference.class)));
                    }
                });
            }
        });
    }
}

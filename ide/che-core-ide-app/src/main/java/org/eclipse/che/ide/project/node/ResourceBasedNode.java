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
package org.eclipse.che.ide.project.node;

import com.google.gwt.core.client.Scheduler;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.promises.client.Function;
import org.eclipse.che.api.promises.client.FunctionException;
import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.js.JsPromiseError;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.api.project.node.resource.DeleteProcessor;
import org.eclipse.che.ide.api.project.node.resource.RenameProcessor;
import org.eclipse.che.ide.api.project.node.resource.SupportDelete;
import org.eclipse.che.ide.api.project.node.resource.SupportRename;
import org.eclipse.che.ide.api.data.tree.settings.NodeSettings;
import org.eclipse.che.ide.project.event.ResourceNodeDeletedEvent;
import org.eclipse.che.ide.project.event.ResourceNodeRenamedEvent;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;

/**
 * Based node which allow perform operations with data object, e.g. renaming and deletion.
 *
 * @author Vlad Zhukovskiy
 */
public abstract class ResourceBasedNode<DataObject> extends AbstractProjectBasedNode<DataObject> implements SupportRename<DataObject>,
                                                                                                            SupportDelete<DataObject> {

    protected EventBus    eventBus;
    protected NodeManager nodeManager;

    public ResourceBasedNode(@NotNull DataObject dataObject,
                             @NotNull ProjectConfigDto projectConfig,
                             @NotNull NodeSettings nodeSettings,
                             @NotNull EventBus eventBus,
                             @NotNull NodeManager nodeManager) {
        super(dataObject, projectConfig, nodeSettings);
        this.eventBus = eventBus;
        this.nodeManager = nodeManager;
    }

    @Override
    public Promise<Void> delete() {
        DeleteProcessor<DataObject> deleteProcessor = getDeleteProcessor();
        if (deleteProcessor == null) {
            return Promises.reject(JsPromiseError.create("Delete processor wasn't provided for this type of node"));
        }

        return deleteProcessor.delete(this)
                              .thenPromise(onDelete())
                              .catchError(onFailed());
    }

    @NotNull
    private Function<DataObject, Promise<Void>> onDelete() {
        return new Function<DataObject, Promise<Void>>() {
            @Override
            public Promise<Void> apply(DataObject deletedObject) throws FunctionException {
                eventBus.fireEvent(new ResourceNodeDeletedEvent(ResourceBasedNode.this));

                return Promises.resolve(null);
            }
        };
    }

    @NotNull
    private Operation<PromiseError> onFailed() {
        return new Operation<PromiseError>() {
            @Override
            public void apply(PromiseError arg) throws OperationException {
                Log.error(ResourceBasedNode.class, arg.getMessage());
            }
        };
    }

    private Operation<DataObject> onRename() {
        return new Operation<DataObject>() {
            @Override
            public void apply(final DataObject arg) throws OperationException {
                Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        eventBus.fireEvent(new ResourceNodeRenamedEvent<>(ResourceBasedNode.this, arg));
                    }
                });
            }
        };
    }

    @Override
    public void rename(@NotNull String newName) {
        RenameProcessor<DataObject> renameProcessor = getRenameProcessor();
        if (renameProcessor == null) {
            return;
        }

        if (getParent() != null && getParent() instanceof HasStorablePath) {
            renameProcessor.rename((HasStorablePath)getParent(), this, newName)
                           .then(onRename())
                           .catchError(onFailed());
        } else {
            renameProcessor.rename(null, this, newName)
                           .then(onRename())
                           .catchError(onFailed());
        }


    }
}

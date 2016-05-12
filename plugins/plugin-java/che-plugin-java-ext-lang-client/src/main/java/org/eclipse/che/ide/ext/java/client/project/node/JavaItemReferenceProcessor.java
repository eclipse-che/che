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
package org.eclipse.che.ide.ext.java.client.project.node;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.ide.api.project.ProjectServiceClient;
import org.eclipse.che.api.project.shared.dto.ItemReference;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.ide.api.app.AppContext;
import org.eclipse.che.ide.api.project.node.HasDataObject;
import org.eclipse.che.ide.api.project.node.HasStorablePath;
import org.eclipse.che.ide.project.node.resource.ItemReferenceProcessor;
import org.eclipse.che.ide.rest.DtoUnmarshallerFactory;
import org.eclipse.che.ide.api.dialogs.DialogFactory;

import javax.validation.constraints.NotNull;

/**
 * @author Vlad Zhukovskiy
 */
public class JavaItemReferenceProcessor extends ItemReferenceProcessor {

    private final DialogFactory dialogFactory;

    @Inject
    public JavaItemReferenceProcessor(EventBus eventBus,
                                      ProjectServiceClient projectServiceClient,
                                      AppContext appContext,
                                      DtoUnmarshallerFactory unmarshallerFactory,
                                      DialogFactory dialogFactory) {
        super(eventBus,appContext, projectServiceClient, unmarshallerFactory);
        this.dialogFactory = dialogFactory;
    }

    @Override
    public Promise<ItemReference> delete(@NotNull HasDataObject<ItemReference> node) {
        return super.delete(node);
    }

    @Override
    public Promise<ItemReference> rename(@Nullable HasStorablePath parent, @NotNull HasDataObject<ItemReference> node,
                                         @NotNull String newName) {
        dialogFactory.createMessageDialog("Unsupported operation",
                                          "At this moment we don't support to rename java files",
                                          null).show();

        return Promises.resolve(node.getData());
    }
}

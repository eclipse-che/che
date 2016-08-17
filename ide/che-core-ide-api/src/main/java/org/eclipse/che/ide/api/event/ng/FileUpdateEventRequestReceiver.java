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
package org.eclipse.che.ide.api.event.ng;


import com.google.web.bindery.event.shared.EventBus;

import org.eclipse.che.api.core.jsonrpc.shared.JsonRpcRequest;
import org.eclipse.che.api.project.shared.dto.event.FileUpdatedDto;
import org.eclipse.che.ide.api.event.FileContentUpdateEvent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.jsonrpc.JsonRpcRequestReceiver;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class FileUpdateEventRequestReceiver implements JsonRpcRequestReceiver {
    private final DtoFactory dtoFactory;
    private final EventBus   eventBus;

    @Inject
    public FileUpdateEventRequestReceiver(DtoFactory dtoFactory, EventBus eventBus) {
        this.dtoFactory = dtoFactory;
        this.eventBus = eventBus;
    }

    @Override
    public void receive(JsonRpcRequest request) {
        final String params = request.getParams();
        final FileUpdatedDto dto = dtoFactory.createDtoFromJson(params, FileUpdatedDto.class);
        eventBus.fireEvent(new FileContentUpdateEvent(dto.getPath()));
    }
}

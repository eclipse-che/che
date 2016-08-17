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
import org.eclipse.che.api.project.shared.dto.event.FileClosedDto;
import org.eclipse.che.api.project.shared.dto.event.FileOpenedDto;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.jsonrpc.JsonRpcRequestTransmitter;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class ClientServerEventService {
    private final JsonRpcRequestTransmitter transmitter;
    private final DtoFactory                dtoFactory;

    @Inject
    public ClientServerEventService(final JsonRpcRequestTransmitter transmitter,
                                    final EventBus eventBus,
                                    final DtoFactory dtoFactory) {
        this.transmitter = transmitter;
        this.dtoFactory = dtoFactory;

        Log.info(getClass(), "Adding file event listener");
        eventBus.addHandler(FileEvent.TYPE, new FileEvent.FileEventHandler() {
            @Override
            public void onFileOperation(FileEvent event) {
                final String path = event.getFile().getLocation().toString();

                switch (event.getOperationType()) {
                    case OPEN: {
                        transmitOpenFileNotification(path);

                        break;
                    }
                    case CLOSE: {
                        transmitCloseFileNotification(path);

                        break;
                    }
                }
            }
        });


    }

    private void transmitCloseFileNotification(String path) {
        Log.info(ClientServerEventService.class, "Sending file closed event: " + path);

        final FileClosedDto dto = dtoFactory.createDto(FileClosedDto.class).withPath(path);

        final JsonRpcRequest request = dtoFactory.createDto(JsonRpcRequest.class)
                                                 .withJsonrpc("2.0")
                                                 .withMethod("event:file-closed")
                                                 .withParams(dto.toString());

        transmitter.transmit(request);
    }

    private void transmitOpenFileNotification(String path) {
        Log.info(ClientServerEventService.class, "Sending file opened event: " + path);

        final FileOpenedDto dto = dtoFactory.createDto(FileOpenedDto.class).withPath(path);

        final JsonRpcRequest notification = dtoFactory.createDto(JsonRpcRequest.class)
                                                      .withJsonrpc("2.0")
                                                      .withMethod("event:file-opened")
                                                      .withParams(dto.toString());

        transmitter.transmit(notification);
    }
}

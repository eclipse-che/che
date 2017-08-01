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
package org.eclipse.che.ide.api.event.ng;

import com.google.gwt.user.client.rpc.AsyncCallback;

import org.eclipse.che.api.core.jsonrpc.commons.JsonRpcPromise;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.dto.DtoFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto.Type.MOVE;
import static org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto.Type.RESUME;
import static org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto.Type.START;
import static org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto.Type.STOP;
import static org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto.Type.SUSPEND;
import static org.eclipse.che.ide.api.workspace.Constants.WS_AGENT_JSON_RPC_ENDPOINT_ID;

/**
 * @author Roman Nikitenko
 */
@Singleton
public class ClientServerEventServiceImpl implements ClientServerEventService {
    private static final String OUTCOMING_METHOD = "track:editor-file";

    private final DtoFactory         dtoFactory;
    private final RequestTransmitter requestTransmitter;
    private final PromiseProvider    promises;

    @Inject
    public ClientServerEventServiceImpl(DtoFactory dtoFactory,
                                        RequestTransmitter requestTransmitter,
                                        PromiseProvider promises) {
        this.dtoFactory = dtoFactory;
        this.requestTransmitter = requestTransmitter;
        this.promises = promises;
    }

    @Override
    public Promise<Void> sendFileTrackingStartEvent(String path) {
        return transmit(path, "", START);
    }

    @Override
    public Promise<Void> sendFileTrackingStopEvent(String path) {
        return transmit(path, "", STOP);
    }

    @Override
    public Promise<Void> sendFileTrackingSuspendEvent() {
        return transmit("", "", SUSPEND);
    }

    @Override
    public Promise<Void> sendFileTrackingResumeEvent() {
        return transmit("", "", RESUME);
    }

    @Override
    public Promise<Void> sendFileTrackingMoveEvent(String oldPath, String newPath) {
        return transmit(oldPath, newPath, MOVE);
    }

    private Promise<Void> transmit(String path, String oldPath, FileTrackingOperationDto.Type type) {
        final FileTrackingOperationDto dto = dtoFactory.createDto(FileTrackingOperationDto.class)
                                                       .withPath(path)
                                                       .withType(type)
                                                       .withOldPath(oldPath);
        return promises.create((AsyncCallback<Void> callback) -> {
            JsonRpcPromise<Void> jsonRpcPromise = requestTransmitter.newRequest()
                                                                    .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                                                                    .methodName(OUTCOMING_METHOD)
                                                                    .paramsAsDto(dto)
                                                                    .sendAndReceiveResultAsEmpty();
            jsonRpcPromise.onSuccess(aVoid -> {
                callback.onSuccess(null);
            });

            jsonRpcPromise.onFailure(jsonRpcError -> {
                callback.onFailure(new Throwable(jsonRpcError.getMessage()));
            });
        });
    }
}

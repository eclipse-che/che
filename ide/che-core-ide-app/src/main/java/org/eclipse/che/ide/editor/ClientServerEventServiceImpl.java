/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.editor;

import static org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto.Type.MOVE;
import static org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto.Type.RESUME;
import static org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto.Type.START;
import static org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto.Type.STOP;
import static org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto.Type.SUSPEND;
import static org.eclipse.che.ide.api.jsonrpc.Constants.WS_AGENT_JSON_RPC_ENDPOINT_ID;

import com.google.gwt.user.client.rpc.AsyncCallback;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.jsonrpc.commons.RequestTransmitter;
import org.eclipse.che.api.project.shared.dto.event.FileTrackingOperationDto;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseProvider;
import org.eclipse.che.ide.api.filewatcher.ClientServerEventService;
import org.eclipse.che.ide.dto.DtoFactory;

/** @author Roman Nikitenko */
@Singleton
public class ClientServerEventServiceImpl implements ClientServerEventService {
  private static final String OUTCOMING_METHOD = "track:editor-file";

  private final DtoFactory dtoFactory;
  private final RequestTransmitter requestTransmitter;
  private final PromiseProvider promises;

  @Inject
  public ClientServerEventServiceImpl(
      DtoFactory dtoFactory, RequestTransmitter requestTransmitter, PromiseProvider promises) {
    this.dtoFactory = dtoFactory;
    this.requestTransmitter = requestTransmitter;
    this.promises = promises;
  }

  @Override
  public Promise<Boolean> sendFileTrackingStartEvent(String path) {
    return transmit(path, "", START);
  }

  @Override
  public Promise<Boolean> sendFileTrackingStopEvent(String path) {
    return transmit(path, "", STOP);
  }

  @Override
  public Promise<Boolean> sendFileTrackingSuspendEvent() {
    return transmit("", "", SUSPEND);
  }

  @Override
  public Promise<Boolean> sendFileTrackingResumeEvent() {
    return transmit("", "", RESUME);
  }

  @Override
  public Promise<Boolean> sendFileTrackingMoveEvent(String oldPath, String newPath) {
    return transmit(oldPath, newPath, MOVE);
  }

  private Promise<Boolean> transmit(
      String path, String oldPath, FileTrackingOperationDto.Type type) {
    final FileTrackingOperationDto dto =
        dtoFactory
            .createDto(FileTrackingOperationDto.class)
            .withPath(path)
            .withType(type)
            .withOldPath(oldPath);

    return promises.create(
        (AsyncCallback<Boolean> callback) ->
            requestTransmitter
                .newRequest()
                .endpointId(WS_AGENT_JSON_RPC_ENDPOINT_ID)
                .methodName(OUTCOMING_METHOD)
                .paramsAsDto(dto)
                .sendAndReceiveResultAsBoolean()
                .onSuccess(callback::onSuccess)
                .onFailure(error -> callback.onFailure(new Throwable(error.getMessage()))));
  }
}

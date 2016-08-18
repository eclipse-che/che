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
import org.eclipse.che.api.project.shared.dto.event.FileInEditorStatusDto;
import org.eclipse.che.api.project.shared.dto.event.FileInEditorStatusDto.Status;
import org.eclipse.che.ide.api.editor.EditorAgent;
import org.eclipse.che.ide.api.editor.EditorPartPresenter;
import org.eclipse.che.ide.api.event.FileEvent;
import org.eclipse.che.ide.dto.DtoFactory;
import org.eclipse.che.ide.jsonrpc.JsonRpcRequestTransmitter;
import org.eclipse.che.ide.resource.Path;
import org.eclipse.che.ide.util.loging.Log;

import javax.inject.Inject;
import javax.inject.Singleton;

import static org.eclipse.che.api.project.shared.dto.event.FileInEditorStatusDto.Status.CLOSED;
import static org.eclipse.che.api.project.shared.dto.event.FileInEditorStatusDto.Status.OPENED;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class ClientServerEventService {
    private final JsonRpcRequestTransmitter transmitter;
    private final DtoFactory                dtoFactory;

    @Inject
    public ClientServerEventService(final JsonRpcRequestTransmitter transmitter,
                                    final EditorAgent editorAgent,
                                    final EventBus eventBus,
                                    final DtoFactory dtoFactory) {
        this.transmitter = transmitter;
        this.dtoFactory = dtoFactory;

        Log.info(getClass(), "Adding file event listener");
        eventBus.addHandler(FileEvent.TYPE, new FileEvent.FileEventHandler() {
            @Override
            public void onFileOperation(FileEvent event) {
                final Path path = event.getFile().getLocation();

                switch (event.getOperationType()) {
                    case OPEN: {
                        transmit(path.toString(), OPENED);

                        break;
                    }
                    case CLOSE: {
                        EditorPartPresenter editorToClose = event.getEditorTab().getRelativeEditorPart();
                        boolean isNeedToTransmit = true;
                        for (EditorPartPresenter editor : editorAgent.getOpenedEditors()) {
                            Path currentPath = editor.getEditorInput().getFile().getLocation();
                            if (path.equals(currentPath) && editorToClose != editor) {
                                isNeedToTransmit = false;
                            }
                        }

                        if (isNeedToTransmit) {
                            transmit(path.toString(), CLOSED);
                        }

                        break;
                    }
                }
            }
        });
    }

    private void transmit(String path, Status status) {
        Log.info(getClass(), "Sending file status changed event: " + path);

        final FileInEditorStatusDto fileInEditorStatusDto = dtoFactory.createDto(FileInEditorStatusDto.class)
                                                                      .withPath(path)
                                                                      .withStatus(status);

        final JsonRpcRequest request = dtoFactory.createDto(JsonRpcRequest.class)
                                                 .withJsonrpc("2.0")
                                                 .withMethod("event:file-in-editor-status-changed")
                                                 .withParams(fileInEditorStatusDto.toString());

        transmitter.transmit(request);
    }
}

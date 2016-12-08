/*
 * Copyright (c) 2016-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
import {org} from "../../../api/dto/che-dto"
import {MessageBusSubscriber} from "../../../spi/websocket/messagebus-subscriber";
import {MessageBus} from "../../../spi/websocket/messagebus";
import {Log} from "../../../spi/log/log";
/**
 * Handle a promise that will be resolved when workspace is stopped.
 * If workspace has error, promise will be rejected
 * @author Florent Benoit
 */
export class WorkspaceStopEventPromiseMessageBusSubscriber implements MessageBusSubscriber {

    messageBus : MessageBus;
    workspaceDto : org.eclipse.che.api.workspace.shared.dto.WorkspaceDto;

    resolve : any;
    reject : any;
    promise: Promise<org.eclipse.che.api.workspace.shared.dto.WorkspaceDto>;

    constructor(messageBus : MessageBus, workspaceDto : org.eclipse.che.api.workspace.shared.dto.WorkspaceDto) {
        this.messageBus = messageBus;
        this.workspaceDto = workspaceDto;
        this.promise = new Promise<org.eclipse.che.api.workspace.shared.dto.WorkspaceDto>((resolve, reject) => {
            this.resolve = resolve;
            this.reject = reject;
        });
    }

    handleMessage(message: any) {
        if ('STOPPED' === message.eventType) {
            this.resolve(this.workspaceDto);
            this.messageBus.close();
        } else if ('ERROR' === message.eventType) {
            try {
                let stringify: any = JSON.stringify(message);
                this.reject('Error when stopping the workspace' + stringify);
            } catch (error) {
                this.reject('Error when stopping the workspace' + message.toString());
            }
            this.messageBus.close();

        } else {
            Log.getLogger().debug('Event on workspace : ', message.eventType);
        }

    }

}

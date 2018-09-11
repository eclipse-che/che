/*
 * Copyright (c) 2016-2017 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc.- initial API and implementation
 */


import {MessageBusSubscriber} from "../../../spi/websocket/messagebus-subscriber";
import {Log} from "../../../spi/log/log";
/**
 * Class that will display to console all workspace output messages.
 * @author Florent Benoit
 */
export class WorkspaceDisplayOutputMessageBusSubscriber implements MessageBusSubscriber {

    handleMessage(message: any) {
        try {
            Log.getLogger().info(message.params.text);
        } catch (error) {
            // maybe parse data to add colors
            Log.getLogger().info(message);
        }
    }

}

/*
 * Copyright (c) 2016-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

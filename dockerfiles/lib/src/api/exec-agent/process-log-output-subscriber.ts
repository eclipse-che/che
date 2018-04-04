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

import {MessageBusSubscriber} from "../../spi/websocket/messagebus-subscriber";
import {Log} from "../../spi/log/log";
/**
 * Class that will display to console all process output messages.
 * @author Florent Benoit
 */
export class ProcesLogOutputMessageBusSubscriber implements MessageBusSubscriber {

    private id : string;


    constructor(id : string) {
        this.id = id;
    }

    handleMessage(event: any) {
        if (event.params && event.params.pid === this.id) {
            if (event.method === "process_stdout") {
                console.log(Log.GREEN + event.params.text + Log.NC);
            } else if (event.method === "process_stderr") {
                console.log(Log.RED + event.params.text + Log.NC);
            }
        }
    }
}

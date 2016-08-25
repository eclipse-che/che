/*
 * Copyright (c) 2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */


import {MessageBusSubscriber} from "../../../spi/websocket/messagebus-subscriber";
import {StringUtils} from "../../../utils/string-utils";
import {Log} from "../../../spi/log/log";
/**
 * Class that will display to console all process output messages.
 * @author Florent Benoit
 */
export class ProcesLogOutputMessageBusSubscriber implements MessageBusSubscriber {

    handleMessage(message: string) {
        if (StringUtils.startsWith(message, '[STDOUT] ')) {
            console.log(Log.GREEN + message.substr('[STDOUT] '.length) + Log.NC);
        } else if (StringUtils.startsWith(message, '[STDERR] ')) {
            console.log(Log.RED + message.substr('[STDERR] '.length) + Log.NC);
        } else {
            console.log(message);
        }
    }
}

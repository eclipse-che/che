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
/**
 * Interface required to be implemented to subscribe to message bus messages.
 * @author Florent Benoit
 */
export interface MessageBusSubscriber {

    handleMessage(message: string);


}

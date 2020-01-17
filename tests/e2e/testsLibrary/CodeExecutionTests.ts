/*********************************************************************
 * Copyright (c) 2019 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { CLASSES, Terminal, TopMenu, Ide } from '..';
import { e2eContainer } from '../inversify.config';

const terminal: Terminal = e2eContainer.get(CLASSES.Terminal);
const topMenu: TopMenu = e2eContainer.get(CLASSES.TopMenu);
const ide: Ide = e2eContainer.get(CLASSES.Ide);

export function runTask(taskName: string, timeout: number) {
    test( `Run command '${taskName}'`, async () => {
        await topMenu.runTask(taskName);
        await ide.waitNotification('has exited with code 0.', timeout);
    });
}

export function closeTerminal(taskName: string) {
    test('Close the terminal tasks', async () => {
        await terminal.closeTerminalTab(taskName);
    });
}

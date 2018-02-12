/*
 * Copyright (c) 2018-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
import { sendGet, sendPost } from './rest';

export function getWorkspace(wsId: string): Promise<che.IWorkspace> {
    return sendGet('/api/workspace/' + wsId);
}
export function startWorkspace(ws: che.IWorkspace): Promise<che.IWorkspace> {
    return sendPost(`/api/workspace/${ws.id}/runtime`);
}

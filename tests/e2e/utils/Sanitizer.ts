/*********************************************************************
 * Copyright (c) 2021 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

import { injectable } from "inversify";

@injectable()
export class Sanitizer {

    public sanitize(arg: string): string {
        return arg.replace(/[\/]/g,'+').replace(/[\,]/g,'.').replace(/[\:]/g, '-').replace(/[\'\"]/g, '').replace(/[^a-z0-9\+\-\.\(\)\[\]\_]/gi, '_');
    }

}
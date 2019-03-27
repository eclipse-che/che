/*********************************************************************
 * Copyright (c) 2018 Red Hat, Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

export class ElementStateChecker {

    public isVisible(element: JQuery<HTMLElement>): boolean {
        if (element === null || element === undefined) {
            return false;
        }

        return element[0].offsetWidth > 0 &&
            element[0].offsetHeight > 0 &&
            element[0].getClientRects().length > 0
    }


}

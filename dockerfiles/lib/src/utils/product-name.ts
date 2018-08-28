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
 * Creates an helper class for getting product name
 * @author Florent Benoit
 */
export class ProductName {
    static getDisplayName() : string {
        let productName: string = process.env.CHE_PRODUCT_NAME;
        if (!productName) {
            return 'Eclipse Che';
        }
        return productName;
    }

    static getMiniDisplayName() : string {
        let miniProductName: string = process.env.CHE_MINI_PRODUCT_NAME;
        if (!miniProductName) {
            return 'Eclipse Che';
        }
        return miniProductName;
    }

}

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

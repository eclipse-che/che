/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package renameVirtualMethods.testEnum2;
class Generic<E> {
    enum A {
        ONE {
            A getSquare() {
                return ONE;
            }
        },
        TWO {
            A getSquare() {
                return MANY;
            }
        },
        MANY {
            A getSquare() {
                return MANY;
            }
        };
        abstract A getSquare();
    }
}

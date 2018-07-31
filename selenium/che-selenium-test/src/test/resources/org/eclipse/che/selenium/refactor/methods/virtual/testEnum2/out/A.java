/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
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
            A get2ndPower() {
                return ONE;
            }
        },
        TWO {
            A get2ndPower() {
                return MANY;
            }
        },
        MANY {
            A get2ndPower() {
                return MANY;
            }
        };
        abstract A get2ndPower();
    }
}

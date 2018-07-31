/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

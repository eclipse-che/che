/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
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

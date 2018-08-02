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
package renametype.testEnum1.p2;

import static renametype.testEnum1.B.*;

class A {
    renametype.testEnum1.B a= ONE;
    renametype.testEnum1.B b= renametype.testEnum1.B.ONE;
    A a2= new A();
}

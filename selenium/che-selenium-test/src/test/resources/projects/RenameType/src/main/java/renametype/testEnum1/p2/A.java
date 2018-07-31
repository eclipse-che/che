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
package renametype.testEnum1.p2;

import static renametype.testEnum1.A.*;

class A {
    renametype.testEnum1.A a= ONE;
    renametype.testEnum1.A b= renametype.testEnum1.A.ONE;
    A a2= new A();
}

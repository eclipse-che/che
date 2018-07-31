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
package renametype.testEnum1.p2;

import static renametype.testEnum1.A.*;

class A {
    renametype.testEnum1.A a= ONE;
    renametype.testEnum1.A b= renametype.testEnum1.A.ONE;
    A a2= new A();
}

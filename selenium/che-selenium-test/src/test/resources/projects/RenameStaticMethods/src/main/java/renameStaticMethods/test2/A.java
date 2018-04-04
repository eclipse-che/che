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
package renameStaticMethods.test2;
class A{
    static void m(){
    }
    void f(){
	m();
    }
    static int fred(){
	m();
	return 1;
    }
    {
	A.m();
	m();
	new A().m();
    }
    static {
	A.m();
	m();
	new A().m();
    }
}
class D{
    static void m(){
	A.m();
	new A().m();
	m();
    }
    static {
	A.m();
	new A().m();
	m();
    }
    {
	A.m();
	new A().m();
	m();
    }
}

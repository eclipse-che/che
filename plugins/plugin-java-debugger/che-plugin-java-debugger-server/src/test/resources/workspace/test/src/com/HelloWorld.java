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
package com;

public class HelloWorld {

    public static void main(String[] args) {

        String test = "hello";
        System.out.println(test);

        String msg = say("world");
        System.out.println(msg);

        msg = say("debugger");
        System.out.println(msg);
    }

    private static String say(String message) {
        return "Hello, " + message + "!";
    }
}

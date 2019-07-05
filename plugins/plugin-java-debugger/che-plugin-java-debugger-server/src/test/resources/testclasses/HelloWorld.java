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
package org.eclipse;

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

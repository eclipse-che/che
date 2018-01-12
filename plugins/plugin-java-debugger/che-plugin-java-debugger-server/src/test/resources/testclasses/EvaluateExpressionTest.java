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
package org.eclipse;

public class EvaluateExpressionTest {
  public static void main(String[] args) throws Exception {
    int i = 1;
    do1(i);
  }

  private static void do1(int i) {
    i = 2;
    System.out.println("Set breakpoint here.");
  }
}

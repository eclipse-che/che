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
package org.eclipse.qa.examples;

public class HintTestClass {

    HintTestClass() {

    }

    HintTestClass(int arg) {

    }

    HintTestClass(int arg, String arg2) {

    }

    HintTestClass(int arg, String arg2, boolean arg3) {

    }

    public void runCommand() {
        // action for run command
    }

    public void runCommand(int arg) {
        // action for run command
    }

    public String runCommand(boolean arg) {
        return "test hint";
    }

    public void runCommand(String arg) {
        // action for run command
    }

    public void runCommand(int arg, String arg2) {
        // action for run command
    }

    public void runCommand(int arg, String arg2, boolean arg3) {
        // action for run command
    }
}

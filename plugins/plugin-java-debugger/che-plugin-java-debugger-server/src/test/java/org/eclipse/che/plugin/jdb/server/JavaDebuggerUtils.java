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
package org.eclipse.che.plugin.jdb.server;

import com.sun.jdi.VirtualMachine;

import java.lang.reflect.Field;

/**
 * @author Anatolii Bazko
 */
public class JavaDebuggerUtils {

    /**
     * Terminates Virtual Machine.
     *
     * @see VirtualMachine#exit(int)
     */
    public static void terminateVirtualMachineQuietly(JavaDebugger javaDebugger) throws Exception {
        Field vmField = JavaDebugger.class.getDeclaredField("vm");
        vmField.setAccessible(true);
        VirtualMachine vm = (VirtualMachine)vmField.get(javaDebugger);

        try {
            vm.exit(0);
        } catch (Exception ignored) {
        }
    }
}

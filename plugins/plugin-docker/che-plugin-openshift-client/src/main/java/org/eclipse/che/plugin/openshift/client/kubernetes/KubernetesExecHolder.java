/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.che.plugin.openshift.client.kubernetes;

import java.util.Arrays;

public class KubernetesExecHolder {

    private String[] command;
    private String podName;

    public KubernetesExecHolder withCommand(String[] command) {
        this.command = command;
        return this;
    }

    public KubernetesExecHolder withPod(String podName) {
        this.podName = podName;
        return this;
    }

    public String[] getCommand() {
        return command;
    }

    public String getPod() {
        return podName;
    }

    public String toString() {
        return String.format("KubernetesExecHolder {command=%s, podName=%s}",
                             Arrays.asList(command).toString(),
                             podName);
    }
}

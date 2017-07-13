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

package org.eclipse.che.plugin.openshift.client;

/**
 * Display the path to workspace log.
 * @author Florent Benoit
 */
public class OpenShiftWorkspaceLogCommandAppender implements OpenShiftCommandAppender {

    /**
     * Provides the command that will be executed on the container in addition to other commands.
     *
     * @return the command to execute
     */
    @Override
    public String getCommand() {
        return "echo \'\nWorkspaces does not log to stdout. Agents log files can be found inside the container:\n   - ws-agent logs are in folder /home/user/che/ws-agent/logs\'";
    }
}

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
package org.eclipse.che.plugin.docker.client;

import org.eclipse.che.plugin.docker.client.json.ProgressStatus;

/**
 * Receives updated progress statuses to be able to show user beatified progress info.
 *
 * @author andrew00x
 * @author Alexander Garagatyi
 */
public interface ProgressMonitor {
    void updateProgress(ProgressStatus currentProgressStatus);

    ProgressMonitor DEV_NULL = new ProgressMonitor() {
        @Override
        public void updateProgress(ProgressStatus currentProgressStatus) {
        }
    };

}

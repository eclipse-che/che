/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.internal.api;

import com.google.common.util.concurrent.ListenableFuture;

/**
 * Defines install status returned after executing install script of plugins
 * @author Florent Benoit
 */
public interface IPluginInstall {

    /**
     * @return id of this current result
     */
    long getId();

    /**
     * @return {@link IPluginStatus} the status of this current install
     */
    IPluginInstallStatus getStatus();

    /**
     * @return available log content of this install
     */
    String getLog();


    ListenableFuture<Integer> getFuture();


    Throwable getError();
}

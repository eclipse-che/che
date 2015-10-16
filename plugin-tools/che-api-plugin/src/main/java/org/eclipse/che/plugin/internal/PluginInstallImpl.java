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
package org.eclipse.che.plugin.internal;

import com.google.common.util.concurrent.ListenableFuture;

import org.eclipse.che.plugin.internal.api.IPluginInstall;
import org.eclipse.che.plugin.internal.api.IPluginInstallStatus;

/**
 * @author Florent Benoit
 */
public class PluginInstallImpl implements IPluginInstall {

    private long id;

    private IPluginInstallStatus status = IPluginInstallStatus.WAIT;

    private String log;

    private ListenableFuture<Integer> future;

    private Throwable error;

    public PluginInstallImpl(long id) {
        this.id = id;
    }


    public PluginInstallImpl setStatus(IPluginInstallStatus status) {
        this.status = status;
        return this;
    }

    public PluginInstallImpl setLog(String log) {
        this.log = log;
        return this;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public IPluginInstallStatus getStatus() {
        return status;
    }

    @Override
    public String getLog() {
        return log;
    }

    public ListenableFuture<Integer> getFuture() {
        return future;
    }

    public void setFuture(ListenableFuture<Integer> future) {
        this.future = future;
    }

    public Throwable getError() {
        return error;
    }

    public void setError(Throwable error) {
        this.error = error;
    }
}

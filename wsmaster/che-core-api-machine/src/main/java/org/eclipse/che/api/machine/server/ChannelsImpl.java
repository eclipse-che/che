/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/

package org.eclipse.che.api.machine.server;

/**
 * @author Alexander Garagatyi
 */
public class ChannelsImpl {
    private final String outputChannel;
    private final String statusChannel;

    public ChannelsImpl(String outputChannel, String statusChannel) {
        this.outputChannel = outputChannel;
        this.statusChannel = statusChannel;
    }

    public String getOutput() {
        return outputChannel;
    }

    public String getStatus() {
        return statusChannel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChannelsImpl)) return false;

        ChannelsImpl channels = (ChannelsImpl)o;

        if (outputChannel != null ? !outputChannel.equals(channels.outputChannel) : channels.outputChannel != null) return false;
        return !(statusChannel != null ? !statusChannel.equals(channels.statusChannel) : channels.statusChannel != null);

    }

    @Override
    public int hashCode() {
        int result = outputChannel != null ? outputChannel.hashCode() : 0;
        result = 31 * result + (statusChannel != null ? statusChannel.hashCode() : 0);
        return result;
    }
}

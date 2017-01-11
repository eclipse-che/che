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
package org.eclipse.che.plugin.docker.client.json;

/** @author andrew00x */
public class ContainerState {
    private boolean running;
    private int     pid;
    private int     exitCode;
    // Date format: yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX
    private String  startedAt;
    private String  finishedAt;
    private boolean paused;
    private boolean restarting;
    private boolean dead;
    private boolean oOMKilled;
    private String  error;

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getExitCode() {
        return exitCode;
    }

    public void setExitCode(int exitCode) {
        this.exitCode = exitCode;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    public String getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(String finishedAt) {
        this.finishedAt = finishedAt;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    public boolean isRestarting() {
        return restarting;
    }

    public void setRestarting(boolean restarting) {
        this.restarting = restarting;
    }

    public boolean isDead() {
        return dead;
    }

    public void setDead(boolean dead) {
        this.dead = dead;
    }

    public boolean isOOMKilled() {
        return oOMKilled;
    }

    public void setOOMKilled(boolean OOMKilled) {
        this.oOMKilled = OOMKilled;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "ContainerState{" +
               "running=" + running +
               ", pid=" + pid +
               ", exitCode=" + exitCode +
               ", startedAt='" + startedAt + '\'' +
               ", finishedAt='" + finishedAt + '\'' +
               ", paused=" + paused +
               ", restarting=" + restarting +
               ", dead=" + dead +
               ", oOMKilled=" + oOMKilled +
               ", error='" + error + '\'' +
               '}';
    }
}

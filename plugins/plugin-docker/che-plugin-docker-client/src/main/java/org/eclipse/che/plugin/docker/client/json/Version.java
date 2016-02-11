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
package org.eclipse.che.plugin.docker.client.json;

/**
 * @author Anton Korneta
 */
public class Version {
    private String version;
    private String aPIVersion;
    private String goVersion;
    private String gitCommit;
    private String os;
    private String arch;

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getaPIVersion() {
        return aPIVersion;
    }

    public void setaPIVersion(String aPIVersion) {
        this.aPIVersion = aPIVersion;
    }

    public String getGoVersion() {
        return goVersion;
    }

    public void setGoVersion(String goVersion) {
        this.goVersion = goVersion;
    }

    public String getGitCommit() {
        return gitCommit;
    }

    public void setGitCommit(String gitCommit) {
        this.gitCommit = gitCommit;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getArch() {
        return arch;
    }

    public void setArch(String arch) {
        this.arch = arch;
    }

    @Override
    public String toString() {
        return "Version{" +
               "Version='" + version + '\'' +
               ", APIVersion='" + aPIVersion + '\'' +
               ", GoVersion='" + goVersion + '\'' +
               ", GitCommit='" + gitCommit + '\'' +
               ", Os='" + os + '\'' +
               ", Arch='" + arch + '\'' +
               '}';
    }
}

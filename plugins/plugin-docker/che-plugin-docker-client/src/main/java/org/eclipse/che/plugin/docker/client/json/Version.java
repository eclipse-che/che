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

import java.util.Objects;

/**
 * @author Anton Korneta
 */
public class Version {
    private String version;
    private String apiVersion;
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

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
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
               ", APIVersion='" + apiVersion + '\'' +
               ", GoVersion='" + goVersion + '\'' +
               ", GitCommit='" + gitCommit + '\'' +
               ", Os='" + os + '\'' +
               ", Arch='" + arch + '\'' +
               '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Version version1 = (Version)o;
        return Objects.equals(version, version1.version) &&
               Objects.equals(apiVersion, version1.apiVersion) &&
               Objects.equals(goVersion, version1.goVersion) &&
               Objects.equals(gitCommit, version1.gitCommit) &&
               Objects.equals(os, version1.os) &&
               Objects.equals(arch, version1.arch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(version, apiVersion, goVersion, gitCommit, os, arch);
    }

}

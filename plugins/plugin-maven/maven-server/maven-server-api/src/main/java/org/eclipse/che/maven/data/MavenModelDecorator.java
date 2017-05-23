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
package org.eclipse.che.maven.data;

import java.util.List;
import java.util.Properties;

/**
 * Object decorator for {@link MavenModel}.
 *
 * @author Vlad Zhukovskyi
 * @since 5.11.0
 */
public class MavenModelDecorator extends MavenModel {
    private final MavenModel modelDelegate;

    public MavenModelDecorator(MavenModel modelDelegate) {
        this.modelDelegate = modelDelegate;
    }

    @Override
    public MavenKey getMavenKey() {
        return modelDelegate.getMavenKey();
    }

    @Override
    public void setMavenKey(MavenKey mavenKey) {
        modelDelegate.setMavenKey(mavenKey);
    }

    @Override
    public MavenParent getParent() {
        return modelDelegate.getParent();
    }

    @Override
    public void setParent(MavenParent parent) {
        modelDelegate.setParent(parent);
    }

    @Override
    public Properties getProperties() {
        return modelDelegate.getProperties();
    }

    @Override
    public void setProperties(Properties properties) {
        modelDelegate.setProperties(properties);
    }

    @Override
    public String getPackaging() {
        return modelDelegate.getPackaging();
    }

    @Override
    public void setPackaging(String packaging) {
        modelDelegate.setPackaging(packaging);
    }

    @Override
    public String getName() {
        return modelDelegate.getName();
    }

    @Override
    public void setName(String name) {
        modelDelegate.setName(name);
    }

    @Override
    public List<MavenPlugin> getPlugins() {
        return modelDelegate.getPlugins();
    }

    @Override
    public void setPlugins(List<MavenPlugin> plugins) {
        modelDelegate.setPlugins(plugins);
    }

    @Override
    public List<MavenProfile> getProfiles() {
        return modelDelegate.getProfiles();
    }

    @Override
    public void setProfiles(List<MavenProfile> profiles) {
        modelDelegate.setProfiles(profiles);
    }

    @Override
    public List<MavenArtifact> getExtensions() {
        return modelDelegate.getExtensions();
    }

    @Override
    public void setExtensions(List<MavenArtifact> extensions) {
        modelDelegate.setExtensions(extensions);
    }

    @Override
    public MavenBuild getBuild() {
        return modelDelegate.getBuild();
    }

    @Override
    public String toString() {
        return modelDelegate.toString();
    }

    @Override
    public List<MavenArtifact> getDependencies() {
        return modelDelegate.getDependencies();
    }

    @Override
    public void setDependencies(List<MavenArtifact> dependencies) {
        modelDelegate.setDependencies(dependencies);
    }

    @Override
    public List<MavenRemoteRepository> getRemoteRepositories() {
        return modelDelegate.getRemoteRepositories();
    }

    @Override
    public void setRemoteRepositories(List<MavenRemoteRepository> remoteRepositories) {
        modelDelegate.setRemoteRepositories(remoteRepositories);
    }

    @Override
    public List<String> getModules() {
        return modelDelegate.getModules();
    }

    @Override
    public void setModules(List<String> modules) {
        modelDelegate.setModules(modules);
    }
}

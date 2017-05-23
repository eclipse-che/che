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

/**
 * Object decorator for {@link MavenBuild}.
 *
 * @author Vlad Zhukovskyi
 * @since 5.11.0
 */
public class MavenBuildDecorator extends MavenBuild {
    private final MavenBuild buildDelegate;

    public MavenBuildDecorator(MavenBuild buildDelegate) {
        this.buildDelegate = buildDelegate;
    }

    @Override
    public String getOutputDirectory() {
        return buildDelegate.getOutputDirectory();
    }

    @Override
    public void setOutputDirectory(String outputDirectory) {
        buildDelegate.setOutputDirectory(outputDirectory);
    }

    @Override
    public String getTestOutputDirectory() {
        return buildDelegate.getTestOutputDirectory();
    }

    @Override
    public String getFinalName() {
        return buildDelegate.getFinalName();
    }

    @Override
    public void setTestOutputDirectory(String testOutputDirectory) {
        buildDelegate.setTestOutputDirectory(testOutputDirectory);
    }

    @Override
    public void setFinalName(String finalName) {
        buildDelegate.setFinalName(finalName);
    }

    @Override
    public String getDefaultGoal() {
        return buildDelegate.getDefaultGoal();
    }

    @Override
    public List<String> getSources() {
        return buildDelegate.getSources();
    }

    @Override
    public void setDefaultGoal(String defaultGoal) {
        buildDelegate.setDefaultGoal(defaultGoal);
    }

    @Override
    public void setSources(List<String> sources) {
        buildDelegate.setSources(sources);
    }

    @Override
    public String getDirectory() {
        return buildDelegate.getDirectory();
    }

    @Override
    public List<String> getTestSources() {
        return buildDelegate.getTestSources();
    }

    @Override
    public void setDirectory(String directory) {
        buildDelegate.setDirectory(directory);
    }

    @Override
    public void setTestSources(List<String> testSources) {
        buildDelegate.setTestSources(testSources);
    }

    @Override
    public List<MavenResource> getResources() {
        return buildDelegate.getResources();
    }

    @Override
    public void setResources(List<MavenResource> resources) {
        buildDelegate.setResources(resources);
    }

    @Override
    public List<String> getFilters() {
        return buildDelegate.getFilters();
    }

    @Override
    public void setFilters(List<String> filters) {
        buildDelegate.setFilters(filters);
    }

    @Override
    public List<MavenResource> getTestResources() {
        return buildDelegate.getTestResources();
    }

    @Override
    public void setTestResources(List<MavenResource> testResources) {
        buildDelegate.setTestResources(testResources);
    }
}

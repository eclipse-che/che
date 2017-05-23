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

import java.io.File;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * Wrapper for {@link MavenBuild} class that translates absolute paths into relative based on the given project directory.
 *
 * @author Vlad Zhukovskyi
 * @see MavenBuild
 * @since 5.11.0
 */
public class MavenBuildRelPathTransformer extends MavenBuildDecorator {

    private final File projectDir;

    public MavenBuildRelPathTransformer(File projectDir, MavenBuild build) {
        super(build);

        this.projectDir = projectDir;
    }

    private static String relativize(File basePath, String rawPath) {
        return basePath.toURI().relativize(new File(rawPath).toURI()).getPath();
    }

    @Override
    public String getOutputDirectory() {
        return relativize(projectDir, super.getOutputDirectory());
    }

    @Override
    public String getTestOutputDirectory() {
        return relativize(projectDir, super.getTestOutputDirectory());
    }

    @Override
    public List<String> getSources() {
        return super.getSources()
                    .stream()
                    .map(path -> relativize(projectDir, path))
                    .collect(toList());
    }

    @Override
    public List<String> getTestSources() {
        return super.getTestSources()
                    .stream()
                    .map(path -> relativize(projectDir, path))
                    .collect(toList());
    }

    @Override
    public String getDirectory() {
        return relativize(projectDir, super.getDirectory());
    }

    @Override
    public List<MavenResource> getResources() {
        return super.getResources()
                    .stream()
                    .map(mavenResource -> new MavenResource(relativize(projectDir, mavenResource.getDirectory()),
                                                            mavenResource.isFiltered(),
                                                            mavenResource.getTargetPath(),
                                                            mavenResource.getIncludes(),
                                                            mavenResource.getExcludes()))
                    .collect(toList());
    }

    @Override
    public List<MavenResource> getTestResources() {
        return super.getTestResources()
                    .stream()
                    .map(mavenTestResource -> new MavenResource(relativize(projectDir, mavenTestResource.getDirectory()),
                                                                mavenTestResource.isFiltered(),
                                                                mavenTestResource.getTargetPath(),
                                                                mavenTestResource.getIncludes(),
                                                                mavenTestResource.getExcludes()))
                    .collect(toList());
    }
}

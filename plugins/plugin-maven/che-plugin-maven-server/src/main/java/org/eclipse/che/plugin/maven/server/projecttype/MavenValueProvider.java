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
package org.eclipse.che.plugin.maven.server.projecttype;

import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FileEntry;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.type.ReadonlyValueProvider;
import org.eclipse.che.api.project.server.type.ValueStorageException;
import org.eclipse.che.commons.xml.XMLTreeException;
import org.eclipse.che.ide.maven.tools.Build;
import org.eclipse.che.ide.maven.tools.Model;
import org.eclipse.che.ide.maven.tools.Resource;
import org.eclipse.che.maven.data.MavenResource;
import org.eclipse.che.plugin.maven.server.core.MavenProjectManager;
import org.eclipse.che.plugin.maven.server.core.project.MavenProject;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.ARTIFACT_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.DEFAULT_PACKAGING;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.DEFAULT_RESOURCES_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.DEFAULT_SOURCE_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.DEFAULT_TEST_RESOURCES_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.DEFAULT_TEST_SOURCE_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.GROUP_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.PACKAGING;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.PARENT_ARTIFACT_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.PARENT_GROUP_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.PARENT_VERSION;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.RESOURCE_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.TEST_SOURCE_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.VERSION;

/**
 * @author Vitalii Parfonov
 */
public class MavenValueProvider extends ReadonlyValueProvider {


    private MavenProjectManager mavenProjectManager;
    private FolderEntry projectFolder;

    protected MavenValueProvider(MavenProjectManager mavenProjectManager,
                                 FolderEntry projectFolder) {
        this.mavenProjectManager = mavenProjectManager;
        this.projectFolder = projectFolder;
    }

    @Override
    public List<String> getValues(String attributeName) throws ValueStorageException {
        try {
            final MavenProject mavenProject = mavenProjectManager.getMavenProject(projectFolder.getPath().toString());
            if (mavenProject != null) {
                return getFromMavenProject(mavenProject, attributeName);
            } else {
                return readFromPom(attributeName);
            }
        } catch (ServerException | ForbiddenException | IOException e) {
            throwReadException(e);
        } catch (XMLTreeException e) {
            throw new ValueStorageException("Error parsing pom.xml : " + e.getMessage());
        }
        return null;
    }

    private List<String> getFromMavenProject(MavenProject mavenProject, String attributeName) {
        String value = "";
        if (attributeName.equals(ARTIFACT_ID)) {
            value = mavenProject.getMavenKey().getArtifactId();
        } else if (attributeName.equals(GROUP_ID)) {
            value = mavenProject.getMavenKey().getGroupId();
        } else if (attributeName.equals(PACKAGING)) {
            final String packaging = mavenProject.getPackaging();
            value = packaging == null ? DEFAULT_PACKAGING : packaging;
        } else if (attributeName.equals(VERSION)) {
            value = mavenProject.getMavenKey().getVersion();
        } else if (attributeName.equals(PARENT_ARTIFACT_ID) && mavenProject.getParentKey() != null) {
            value = mavenProject.getParentKey().getArtifactId();
        } else if (attributeName.equals(PARENT_GROUP_ID) && mavenProject.getParentKey() != null) {
            value = mavenProject.getParentKey().getGroupId();
        } else if (attributeName.equals(PARENT_VERSION) && mavenProject.getParentKey() != null) {
            value = mavenProject.getParentKey().getVersion();
        } else if (attributeName.equals(SOURCE_FOLDER)) {
            if (mavenProject.getSources() != null && !mavenProject.getSources().isEmpty()) {
                return mavenProject.getSources();
            } else {
                value = DEFAULT_SOURCE_FOLDER;
            }
        } else if (attributeName.equals(TEST_SOURCE_FOLDER)) {
            if (mavenProject.getTestSources() != null && !mavenProject.getTestSources().isEmpty()) {
                return mavenProject.getTestSources();
            } else {
                value = DEFAULT_TEST_SOURCE_FOLDER;
            }
        } else if (attributeName.equals(RESOURCE_FOLDER)) {
            if (mavenProject.getResources() != null && !mavenProject.getResources().isEmpty()) {
                return mavenProject.getResources().stream().map(MavenResource::getDirectory).collect(Collectors.toList());
            } else {
                return Arrays.asList(DEFAULT_RESOURCES_FOLDER, DEFAULT_TEST_RESOURCES_FOLDER);
            }
        }
        return Collections.singletonList(value);
    }


    private List<String> readFromPom(String attributeName)
            throws ServerException, ForbiddenException, IOException, XMLTreeException, ValueStorageException {
        String value = "";
        final Model model = readModel(projectFolder);
        if (attributeName.equals(ARTIFACT_ID)) {
            value = model.getArtifactId();
        } else if (attributeName.equals(GROUP_ID)) {
            value = model.getGroupId();
        } else if (attributeName.equals(PACKAGING)) {
            final String packaging = model.getPackaging();
            value = packaging == null ? DEFAULT_PACKAGING : packaging;
        } else if (attributeName.equals(VERSION)) {
            value = model.getVersion();
        } else if (attributeName.equals(PARENT_ARTIFACT_ID) && model.getParent() != null) {
            value = model.getParent().getArtifactId();
        } else if (attributeName.equals(PARENT_GROUP_ID) && model.getParent() != null) {
            value = model.getParent().getGroupId();
        } else if (attributeName.equals(PARENT_VERSION) && model.getParent() != null) {
            value = model.getParent().getVersion();
        } else if (attributeName.equals(SOURCE_FOLDER)) {
            Build build = model.getBuild();
            if (build != null && build.getSourceDirectory() != null) {
                value = build.getSourceDirectory();
            } else {
                value = DEFAULT_SOURCE_FOLDER;
            }
        } else if (attributeName.equals(TEST_SOURCE_FOLDER)) {
            Build build = model.getBuild();
            if (build != null && build.getTestSourceDirectory() != null) {
                value = build.getTestSourceDirectory();
            } else {
                value = DEFAULT_TEST_SOURCE_FOLDER;
            }
        } else if (attributeName.equals(RESOURCE_FOLDER)) {
            Build build = model.getBuild();
            if (build != null && build.getResources() != null) {
                return build.getResources().stream().map(Resource::getDirectory).collect(Collectors.toList());
            } else {
                return Arrays.asList(DEFAULT_RESOURCES_FOLDER, DEFAULT_TEST_RESOURCES_FOLDER);
            }
        }

        return Collections.singletonList(value);
    }

    protected Model readModel(FolderEntry projectFolder) throws ValueStorageException, ServerException, ForbiddenException, IOException {
        FileEntry pomFile = (FileEntry)projectFolder.getChild("pom.xml");
        if (pomFile == null) {
            throw new ValueStorageException("pom.xml does not exist.");
        }
        return Model.readFrom(pomFile.getInputStream());
    }

    protected void throwReadException(Exception e) throws ValueStorageException {
        throw new ValueStorageException("Can't read pom.xml : " + e.getMessage());
    }

}

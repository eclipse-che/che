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
package org.eclipse.che.plugin.maven.server.projecttype.handler;

import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.project.server.FolderEntry;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.api.vfs.VirtualFileSystem;
import org.eclipse.che.api.vfs.VirtualFileSystemProvider;
import org.eclipse.che.ide.maven.tools.Build;
import org.eclipse.che.ide.maven.tools.Model;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;

import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.ARTIFACT_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.DEFAULT_SOURCE_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.DEFAULT_TEST_SOURCE_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.GROUP_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.PACKAGING;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.PARENT_ARTIFACT_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.PARENT_GROUP_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.PARENT_VERSION;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.SIMPLE_GENERATION_STRATEGY;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.TEST_SOURCE_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.VERSION;

/**
 * Generates simple Maven project.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class SimpleGeneratorStrategy implements GeneratorStrategy {


    private final VirtualFileSystem vfs;

    @Inject
    public SimpleGeneratorStrategy(VirtualFileSystemProvider vfsProvider) throws ServerException {
        vfs = vfsProvider.getVirtualFileSystem();
    }

    @Override
    public String getId() {
        return SIMPLE_GENERATION_STRATEGY;
    }

    @Override
    public void generateProject(Path projectPath, Map<String, AttributeValue> attributes, Map<String, String> options)
            throws ForbiddenException, ConflictException, ServerException {
        AttributeValue artifactId = attributes.get(ARTIFACT_ID);
        AttributeValue groupId = attributes.get(GROUP_ID);
        AttributeValue version = attributes.get(VERSION);
        if (artifactId == null) {
            throw new ConflictException("Missed required attribute artifactId");
        }

        if (groupId == null) {
            throw new ConflictException("Missed required attribute groupId");
        }

        if (version == null) {
            throw new ConflictException("Missed required attribute version");
        }

        Model model = Model.createModel();
        model.setModelVersion("4.0.0");

        final FolderEntry baseFolder = new FolderEntry(vfs.getRoot().createFolder(projectPath.toString()));


        if (baseFolder.getChild("pom.xml") == null) {
            baseFolder.createFile("pom.xml", new byte[0]);
        }

        AttributeValue parentArtifactId = attributes.get(PARENT_ARTIFACT_ID);
        if (parentArtifactId != null) {
            model.setArtifactId(parentArtifactId.getString());
        }
        AttributeValue parentGroupId = attributes.get(PARENT_GROUP_ID);
        if (parentGroupId != null) {
            model.setGroupId(parentGroupId.getString());
        }
        AttributeValue parentVersion = attributes.get(PARENT_VERSION);
        if (parentVersion != null) {
            model.setVersion(parentVersion.getString());
        }
        model.setArtifactId(artifactId.getString());
        model.setGroupId(groupId.getString());
        model.setVersion(version.getString());
        AttributeValue packaging = attributes.get(PACKAGING);
        if (packaging != null) {
            model.setPackaging(packaging.getString());
        }
        AttributeValue sourceFolders = attributes.get(SOURCE_FOLDER);
        if (sourceFolders != null) {
            final String sourceFolder = sourceFolders.getString();
            baseFolder.createFolder(sourceFolder);
            if (!DEFAULT_SOURCE_FOLDER.equals(sourceFolder)) {
                model.setBuild(new Build().setSourceDirectory(sourceFolder));
            }
        }
        AttributeValue testSourceFolders = attributes.get(TEST_SOURCE_FOLDER);
        if (testSourceFolders != null) {
            final String testSourceFolder = testSourceFolders.getString();
            baseFolder.createFolder(testSourceFolder);
            if (!DEFAULT_TEST_SOURCE_FOLDER.equals(testSourceFolder)) {
                Build build = model.getBuild();
                if (build != null) {
                    build.setTestSourceDirectory(testSourceFolder);
                } else {
                    model.setBuild(new Build().setTestSourceDirectory(testSourceFolder));
                }
            }
        }
        model.writeTo(baseFolder.getChild("pom.xml").getVirtualFile());
    }
}

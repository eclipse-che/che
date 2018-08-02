/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.server.projecttype.handler;

import static java.io.File.separator;
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.api.core.ConflictException;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.NotFoundException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.server.type.AttributeValue;
import org.eclipse.che.ide.maven.tools.Build;
import org.eclipse.che.ide.maven.tools.Model;

/**
 * Generates simple Maven project.
 *
 * @author Artem Zatsarynnyi
 */
@Singleton
public class SimpleGeneratorStrategy implements GeneratorStrategy {

  private final FsManager fsManager;

  @Inject
  public SimpleGeneratorStrategy(FsManager fsManager) throws ServerException {
    this.fsManager = fsManager;
  }

  @Override
  public String getId() {
    return SIMPLE_GENERATION_STRATEGY;
  }

  @Override
  public void generateProject(
      String projectPath, Map<String, AttributeValue> attributes, Map<String, String> options)
      throws ForbiddenException, ConflictException, ServerException, NotFoundException {
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

    String pomXml;
    fsManager.createDir(projectPath);

    pomXml = projectPath + separator + "pom.xml";

    if (!fsManager.existsAsFile(pomXml)) {
      fsManager.createFile(pomXml, new ByteArrayInputStream(new byte[0]));
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
      String sourceFolder = sourceFolders.getString();
      String sourceFolderPath = projectPath + separator + sourceFolder;
      fsManager.createDir(sourceFolderPath);
      if (!DEFAULT_SOURCE_FOLDER.equals(sourceFolder)) {
        model.setBuild(new Build().setSourceDirectory(sourceFolder));
      }
    }
    AttributeValue testSourceFolders = attributes.get(TEST_SOURCE_FOLDER);
    if (testSourceFolders != null) {
      String testSourceFolder = testSourceFolders.getString();
      String testSourceFolderPath = projectPath + separator + testSourceFolder;
      fsManager.createDir(testSourceFolderPath);

      if (!DEFAULT_TEST_SOURCE_FOLDER.equals(testSourceFolder)) {
        Build build = model.getBuild();
        if (build != null) {
          build.setTestSourceDirectory(testSourceFolder);
        } else {
          model.setBuild(new Build().setTestSourceDirectory(testSourceFolder));
        }
      }
    }
    File file = fsManager.toIoFile(pomXml);
    try {
      model.writeTo(file);
    } catch (IOException e) {
      throw new ServerException(e);
    }
  }
}

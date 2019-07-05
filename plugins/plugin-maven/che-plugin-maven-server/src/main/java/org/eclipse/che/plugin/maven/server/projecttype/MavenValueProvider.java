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
package org.eclipse.che.plugin.maven.server.projecttype;

import static java.util.Collections.singletonList;
import static org.eclipse.che.api.fs.server.WsPathUtils.absolutize;
import static org.eclipse.che.api.fs.server.WsPathUtils.resolve;
import static org.eclipse.che.ide.ext.java.shared.Constants.OUTPUT_FOLDER;
import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.ARTIFACT_ID;
import static org.eclipse.che.plugin.maven.shared.MavenAttributes.DEFAULT_OUTPUT_FOLDER;
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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.eclipse.che.api.core.ForbiddenException;
import org.eclipse.che.api.core.ServerException;
import org.eclipse.che.api.fs.server.FsManager;
import org.eclipse.che.api.project.server.type.ReadonlyValueProvider;
import org.eclipse.che.api.project.server.type.ValueStorageException;
import org.eclipse.che.commons.xml.XMLTreeException;
import org.eclipse.che.ide.maven.tools.Model;
import org.eclipse.che.ide.maven.tools.Resource;

/** @author Vitalii Parfonov */
public class MavenValueProvider extends ReadonlyValueProvider {

  private final String projectWsPath;
  private final FsManager fsManager;

  protected MavenValueProvider(String projectWsPath, FsManager fsManager) {
    this.projectWsPath = absolutize(projectWsPath);
    this.fsManager = fsManager;
  }

  @Override
  public List<String> getValues(String attributeName) throws ValueStorageException {
    try {
      return readFromPom(attributeName);
    } catch (ServerException | ForbiddenException | IOException e) {
      throwReadException(e);
    } catch (XMLTreeException e) {
      throw new ValueStorageException("Error parsing pom.xml : " + e.getMessage());
    }
    return null;
  }

  private List<String> readFromPom(String attributeName)
      throws ServerException, ForbiddenException, IOException, XMLTreeException,
          ValueStorageException {
    final Model model = readModel(projectWsPath);
    switch (attributeName) {
      case ARTIFACT_ID:
        return singletonList(model.getArtifactId());
      case GROUP_ID:
        return singletonList(model.getGroupId());
      case PACKAGING:
        String packaging = model.getPackaging();
        return singletonList(packaging != null ? packaging : DEFAULT_PACKAGING);
      case VERSION:
        return singletonList(model.getVersion());
      case PARENT_ARTIFACT_ID:
        return singletonList(model.getParent() == null ? "" : model.getParent().getArtifactId());
      case PARENT_GROUP_ID:
        return singletonList(model.getParent() == null ? "" : model.getParent().getGroupId());
      case PARENT_VERSION:
        return singletonList(model.getParent() == null ? "" : model.getParent().getVersion());
      case SOURCE_FOLDER:
        if (model.getBuild() != null && model.getBuild().getSourceDirectory() != null) {
          return singletonList(model.getBuild().getSourceDirectory());
        } else {
          return singletonList(DEFAULT_SOURCE_FOLDER);
        }
      case TEST_SOURCE_FOLDER:
        if (model.getBuild() != null && model.getBuild().getTestSourceDirectory() != null) {
          return singletonList(model.getBuild().getTestSourceDirectory());
        } else {
          return singletonList(DEFAULT_TEST_SOURCE_FOLDER);
        }
      case RESOURCE_FOLDER:
        if (model.getBuild() != null && model.getBuild().getResources() != null) {
          return model
              .getBuild()
              .getResources()
              .stream()
              .map(Resource::getDirectory)
              .collect(Collectors.toList());
        } else {
          return Arrays.asList(DEFAULT_RESOURCES_FOLDER, DEFAULT_TEST_RESOURCES_FOLDER);
        }
      case OUTPUT_FOLDER:
        if (model.getBuild() != null && model.getBuild().getOutputDirectory() != null) {
          return singletonList(model.getBuild().getOutputDirectory());
        } else {
          return singletonList(DEFAULT_OUTPUT_FOLDER);
        }
      default:
        throw new ValueStorageException(String.format("Unknown attribute %s", attributeName));
    }
  }

  protected Model readModel(String wsPath)
      throws ValueStorageException, ServerException, ForbiddenException, IOException {
    String pomXmlWsPath = resolve(wsPath, "pom.xml");

    if (!fsManager.exists(pomXmlWsPath)) {
      throw new ValueStorageException("pom.xml does not exist.");
    }

    return Model.readFrom(fsManager.toIoFile(pomXmlWsPath));
  }

  protected void throwReadException(Exception e) throws ValueStorageException {
    throw new ValueStorageException("Can't read pom.xml : " + e.getMessage());
  }
}

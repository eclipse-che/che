/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.maven.server.core.classpath;

import java.io.File;
import org.eclipse.che.maven.data.MavenArtifactKey;

/**
 * Util class. Helps to navigate in local maven repository.
 *
 * @author Evgen Vidolob
 */
public class MavenLocalRepositoryUtil {

  public static File getFileForArtifact(File localRepo, MavenArtifactKey artifactKey) {
    return getFileForArtifact(
        localRepo,
        artifactKey.getGroupId(),
        artifactKey.getArtifactId(),
        artifactKey.getVersion(),
        artifactKey.getClassifier(),
        artifactKey.getPackaging());
  }

  public static File getFileForArtifact(
      File localRepo,
      String groupId,
      String artifactId,
      String version,
      String classifier,
      String packaging) {
    String artifactPath = groupId.replace('.', '/') + "/" + artifactId + "/" + version;

    File artifactDir = new File(localRepo, artifactPath);
    String artifactName = artifactId + "-" + version;

    if (classifier != null) {
      artifactName += "-" + classifier;
    }

    if (packaging == null) {
      packaging = "jar";
    }
    return new File(artifactDir, artifactName + "." + packaging);
  }
}

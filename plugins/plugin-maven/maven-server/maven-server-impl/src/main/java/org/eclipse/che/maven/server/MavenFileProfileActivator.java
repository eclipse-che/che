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
package org.eclipse.che.maven.server;

import java.io.File;
import java.io.IOException;
import org.apache.maven.model.Activation;
import org.apache.maven.model.ActivationFile;
import org.apache.maven.model.Profile;
import org.apache.maven.profiles.activation.DetectedProfileActivator;
import org.codehaus.plexus.interpolation.EnvarBasedValueSource;
import org.codehaus.plexus.interpolation.InterpolationException;
import org.codehaus.plexus.interpolation.MapBasedValueSource;
import org.codehaus.plexus.interpolation.RegexBasedInterpolator;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;

/**
 * Copied from org.apache.maven.profiles.activation.FileProfileActivator, added parameter baseDit to
 * test file existing.
 */
public class MavenFileProfileActivator extends DetectedProfileActivator implements LogEnabled {
  private Logger logger;

  private final File baseDir;

  public MavenFileProfileActivator(File baseDir) {
    this.baseDir = baseDir;
  }

  protected boolean canDetectActivation(Profile profile) {
    return profile.getActivation() != null && profile.getActivation().getFile() != null;
  }

  /**
   * Returns {@code true} if the file exists, if it does then the profile will be active, otherwise
   * returns {@code false}.
   */
  public boolean isActive(Profile profile) {
    Activation activation = profile.getActivation();
    ActivationFile actFile = activation.getFile();

    if (actFile != null) {
      // check if the file exists, if it does then the profile will be active
      String fileString = actFile.getExists();

      RegexBasedInterpolator interpolator = new RegexBasedInterpolator();
      try {
        interpolator.addValueSource(new EnvarBasedValueSource());
      } catch (IOException e) {
        // ignored
      }
      interpolator.addValueSource(new MapBasedValueSource(System.getProperties()));

      try {
        if (StringUtils.isNotEmpty(fileString)) {
          fileString = StringUtils.replace(interpolator.interpolate(fileString, ""), "\\", "/");
          return fileExists(fileString);
        }

        // check if the file is missing, if it is then the profile will be active
        fileString = actFile.getMissing();

        if (StringUtils.isNotEmpty(fileString)) {
          fileString = StringUtils.replace(interpolator.interpolate(fileString, ""), "\\", "/");
          return !fileExists(fileString);
        }
      } catch (InterpolationException e) {
        if (logger.isDebugEnabled()) {
          logger.debug(
              "Failed to interpolate missing file location for profile activator: " + fileString,
              e);
        } else {
          logger.warn(
              "Failed to interpolate missing file location for profile activator: "
                  + fileString
                  + ". Run in debug mode (-X) for more information.");
        }
      }
    }

    return false;
  }

  private boolean fileExists(String path) {
    return new File(path).exists() || new File(baseDir, path).exists();
  }

  public void enableLogging(Logger logger) {
    this.logger = logger;
  }
}

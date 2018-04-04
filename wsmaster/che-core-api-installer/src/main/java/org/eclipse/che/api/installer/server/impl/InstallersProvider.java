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
package org.eclipse.che.api.installer.server.impl;

import static java.nio.file.Files.isDirectory;

import com.google.inject.Provider;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.che.api.installer.shared.dto.InstallerDto;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.dto.server.DtoFactory;

/**
 * Scans resources to create {@link Installer} based upon them. To be able to find appropriate
 * resources the structure has to be the follow:
 *
 * <p>installers | -- version_1 | |------- installer_1_version_1.script.sh | |-------
 * installer_1_version_1.json -- version_2 |------- installer_1_version_2.script.sh |-------
 * installer_1_version_2.json
 *
 * @author Anatolii Bazko
 */
public class InstallersProvider implements Provider<Set<Installer>> {

  @Override
  public Set<Installer> get() {
    Set<Installer> installers = new HashSet<>();

    try {
      Enumeration<URL> installerResources =
          Thread.currentThread().getContextClassLoader().getResources("/installers");
      while (installerResources.hasMoreElements()) {
        URL installerResource = installerResources.nextElement();

        IoUtil.listResources(
            installerResource.toURI(),
            versionDir -> {
              if (!isDirectory(versionDir)) {
                return;
              }

              List<Path> descriptors = findInstallersDescriptors(versionDir);
              for (Path descriptor : descriptors) {
                Optional<Path> script = findInstallerScript(descriptor);
                script.ifPresent(
                    path -> {
                      Installer installer = init(descriptor, script.get());
                      installers.add(installer);
                    });
              }
            });
      }
    } catch (IOException | URISyntaxException e) {
      throw new IllegalStateException(e);
    }

    return installers;
  }

  private List<Path> findInstallersDescriptors(Path dir) {
    try {
      return Files.find(dir, 1, (path, basicFileAttributes) -> path.toString().endsWith(".json"))
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private Optional<Path> findInstallerScript(Path descriptor) {
    String scriptFileName =
        descriptor.getFileName().toString().replaceAll("[.]json$", ".script.sh");
    try {
      return Files.find(
              descriptor.getParent(),
              1,
              (path, basicFileAttributes) -> path.getFileName().toString().equals(scriptFileName))
          .findFirst();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
  }

  private Installer init(Path descriptorPath, Path scriptPath) {
    try {
      String descriptor = IoUtil.readAndCloseQuietly(Files.newInputStream(descriptorPath));
      String script = IoUtil.readAndCloseQuietly(Files.newInputStream(scriptPath));

      InstallerDto installer =
          DtoFactory.getInstance().createDtoFromJson(descriptor, InstallerDto.class);
      return installer.withScript(script);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }
}

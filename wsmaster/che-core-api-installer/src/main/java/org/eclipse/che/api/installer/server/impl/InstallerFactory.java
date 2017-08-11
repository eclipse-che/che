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
package org.eclipse.che.api.installer.server.impl;

import org.eclipse.che.api.installer.server.model.impl.BasicInstaller;
import org.eclipse.che.api.installer.shared.model.Installer;
import org.eclipse.che.commons.lang.IoUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.nio.file.Files.isDirectory;

/**
 * Scans resources to create {@link Installer} about them.
 * To be able to find appropriate resources the structure has to be the follow:
 *
 *     installers
 *      |
 *      -- dir_1
 *      |   |------- installer_1.script.sh
 *      |   |------- installer_1.json
 *      -- dir_2
 *          |------- installer_2.script.sh
 *          |------- installer_2.json
 *
 * @author Anatolii Bazko
 */
public class InstallerFactory {

    private static final Logger LOG = LoggerFactory.getLogger(InstallerFactory.class);

    /**
     * Finds resources and initializes installers based upon them.
     *
     * @see BasicInstaller#BasicInstaller(Path, Path)
     *
     * @return initialized installers
     *
     * @throws IOException
     *      if any i/o error occurred
     * @throws URISyntaxException
     */
    public static List<Installer> find() throws IOException, URISyntaxException {
        List<Installer> installers = new LinkedList<>();

        Enumeration<URL> installerResources = Thread.currentThread().getContextClassLoader().getResources("/installers");
        while (installerResources.hasMoreElements()) {
            URL installerResource = installerResources.nextElement();

            IoUtil.listResources(installerResource.toURI(), versionDir -> {
                if (!isDirectory(versionDir)) {
                    return;
                }

                List<Path> descriptors = findInstallersDescriptors(versionDir);
                for (Path descriptor : descriptors) {
                    Optional<Path> script = findInstallerScript(descriptor);
                    script.ifPresent(path -> {
                        Installer installer = init(descriptor, script.get());
                        installers.add(installer);
                    });
                }
            });
        }

        return installers;
    }

    private static List<Path> findInstallersDescriptors(Path dir) {
        try {
            return Files.find(dir,
                              1,
                              (path, basicFileAttributes) -> path.toString().endsWith(".json"))
                        .collect(Collectors.toList());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Optional<Path> findInstallerScript(Path descriptor) {
        String scriptFileName = descriptor.getFileName().toString().replace("json", "script.sh");
        try {
            return Files.find(descriptor.getParent(),
                              1,
                              (path, basicFileAttributes) -> path.getFileName().toString().equals(scriptFileName))
                        .findFirst();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    private static Installer init(Path descriptor, Path script) {
        try {
            BasicInstaller installer = new BasicInstaller(descriptor, script);
            LOG.info(String.format("Installer '%s' found", InstallerFqn.of(installer).toKey()));
            return installer;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}

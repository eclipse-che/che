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
package org.eclipse.che.api.project.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.io.File.separator;
import static java.nio.file.Files.readAllBytes;

/**
 * @author Dmitry Kuleshov
 */
@Singleton
public class ReadmeContentProvider {
    public static final String DEFAULT_README_NAME     = "README.md";
    public static final String DEFAULT_README_LOCATION = "/mnt/che/readme";

    @Inject(optional = true)
    @Named("project.default.readme.filename")
    private final String filename = DEFAULT_README_NAME;
    @Inject(optional = true)
    @Named("project.default.readme.location")
    private final String location = DEFAULT_README_LOCATION;

    public byte[] get() throws IOException {
        final String absoluteName = location + separator + filename;
        final Path path = Paths.get(absoluteName);
        return readAllBytes(path);
    }

    public String getFilename() {
        return this.filename;
    }

    public String getLocation() {
        return location;
    }
}

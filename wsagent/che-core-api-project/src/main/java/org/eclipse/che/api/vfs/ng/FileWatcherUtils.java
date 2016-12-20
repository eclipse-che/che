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
package org.eclipse.che.api.vfs.ng;

import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.Set;

public class FileWatcherUtils {

    public static Path toNormalPath(Path root, String name) {
        return root.resolve(name.startsWith("/") ? name.substring(1) : name).toAbsolutePath();
    }

    public static String toInternalPath(Path root, Path path) {
        return "/" + root.toAbsolutePath().relativize(path);
    }

    public static boolean isExcluded(Set<PathMatcher> excludes, Path path) {
        for (PathMatcher matcher : excludes) {
            if (matcher.matches(path)) {
                return true;
            }
        }
        return false;
    }
}

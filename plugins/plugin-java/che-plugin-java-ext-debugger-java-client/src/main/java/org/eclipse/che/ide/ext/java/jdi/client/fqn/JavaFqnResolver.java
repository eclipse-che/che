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
package org.eclipse.che.ide.ext.java.jdi.client.fqn;

import com.google.inject.Singleton;

import org.eclipse.che.ide.api.project.node.HasProjectConfig;
import org.eclipse.che.ide.api.project.tree.VirtualFile;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.eclipse.che.ide.ext.java.client.projecttree.JavaSourceFolderUtil.getProjectBuilder;

/**
 * @author Evgen Vidolob
 * @author Anatoliy Bazko
 */
@Singleton
public class JavaFqnResolver implements FqnResolver {

    /** {@inheritDoc} */
    @NotNull
    @Override
    public String resolveFqn(@NotNull final VirtualFile file) {
        final HasProjectConfig projectNode = file.getProject();
        final List<String> sourceFolders = detectSourceFolders(projectNode);

        final String projectPath = projectNode.getProjectConfig().getPath();
        String filePath = file.getPath();
        int i = 1;
        int j = filePath.lastIndexOf('.');
        if (j < 0) {
            j = filePath.length();
        }
        for (String sourceFolder : sourceFolders) {
            boolean projectPathEndsWithSeparator = projectPath.charAt(projectPath.length() - 1) == '/';
            boolean sourcePathStartsWithSeparator = sourceFolder.charAt(0) == '/';
            boolean sourcePathEndsWithSeparator = sourceFolder.charAt(sourceFolder.length() - 1) == '/';
            String base;
            if (projectPathEndsWithSeparator && sourcePathStartsWithSeparator) {
                base = projectNode + sourceFolder.substring(1);
            } else if (!(projectPathEndsWithSeparator || sourcePathStartsWithSeparator)) {
                base = projectPath + '/' + sourceFolder;
            } else {
                base = projectNode + sourceFolder;
            }
            if (!sourcePathEndsWithSeparator) {
                base = base + '/';
            }

            if (filePath.startsWith(base)) {
                i = base.length();
                return filePath.substring(i, j).replaceAll("/", ".");
            }
        }
        return filePath.substring(i, j).replaceAll("/", ".");
    }

    private List<String> detectSourceFolders(HasProjectConfig projectNode) {
        List<String> sourceFolders = new ArrayList<>();

        String projectBuilder = getProjectBuilder(projectNode);
        Map<String, List<String>> attributes = projectNode.getProjectConfig().getAttributes();

        sourceFolders.addAll(attributes.containsKey(projectBuilder + ".source.folder") ? attributes.get(projectBuilder + ".source.folder")
                                                                                       : Collections.<String>emptyList());

        sourceFolders.addAll(attributes.containsKey(projectBuilder + ".test.source.folder") ? attributes.get(projectBuilder + ".test.source.folder")
                                                                                            : Collections.<String>emptyList());

        return sourceFolders;
    }
}
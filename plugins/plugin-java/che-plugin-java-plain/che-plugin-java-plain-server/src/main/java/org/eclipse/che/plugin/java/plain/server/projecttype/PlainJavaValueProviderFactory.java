/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.plain.server.projecttype;

import static java.util.Collections.singletonList;
import static java.util.stream.Stream.concat;
import static org.eclipse.che.ide.ext.java.shared.Constants.OUTPUT_FOLDER;
import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;
import static org.eclipse.che.plugin.java.plain.shared.PlainJavaProjectConstants.DEFAULT_SOURCE_FOLDER_VALUE;
import static org.eclipse.jdt.core.IClasspathEntry.CPE_SOURCE;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.type.SettableValueProvider;
import org.eclipse.che.api.project.server.type.ValueProvider;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.api.project.server.type.ValueStorageException;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.JavaModel;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * {@link ValueProviderFactory} for Plain Java project type. Factory crates a class which provides
 * values of Plain Java project's attributes.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class PlainJavaValueProviderFactory implements ValueProviderFactory {

  private final ProjectManager projectManager;

  @Inject
  public PlainJavaValueProviderFactory(ProjectManager projectManager) {
    this.projectManager = projectManager;
  }

  @Override
  public ValueProvider newInstance(String wsPath) {
    return new PlainJavaValueProvider(wsPath);
  }

  private class PlainJavaValueProvider extends SettableValueProvider {

    private String wsPath;

    PlainJavaValueProvider(String wsPath) {
      this.wsPath = wsPath;
    }

    @Override
    public List<String> getValues(String attributeName) throws ValueStorageException {
      if (SOURCE_FOLDER.equals(attributeName)) {
        return getSourceFolders();
      } else if (OUTPUT_FOLDER.equals(attributeName)) {
        return getOutputFolder();
      }
      return null;
    }

    @Override
    public void setValues(String attributeName, List<String> values) throws ValueStorageException {
      Map<String, List<String>> attributes =
          projectManager
              .get(wsPath)
              .orElseThrow(() -> new ValueStorageException("Can't get project"))
              .getAttributes();

      if (attributes.containsKey(attributeName)) {
        attributes.put(
            attributeName,
            concat(values.stream(), attributes.get(attributeName).stream())
                .collect(Collectors.toList()));
      } else {
        attributes.put(attributeName, values);
      }
    }

    private List<String> getOutputFolder() throws ValueStorageException {
      JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
      IJavaProject project = model.getJavaProject(wsPath);

      try {
        String outputDirPath = project.getOutputLocation().toOSString();
        return outputDirPath.startsWith(wsPath)
            ? singletonList(outputDirPath.substring(wsPath.length() + 1))
            : singletonList(outputDirPath);
      } catch (JavaModelException e) {
        throw new ValueStorageException("Can't get output location: " + e.getMessage());
      }
    }

    private List<String> getSourceFolders() throws ValueStorageException {
      List<String> sourceFolders = new ArrayList<>();

      JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
      IJavaProject project = model.getJavaProject(wsPath);

      try {
        IClasspathEntry[] classpath = project.getRawClasspath();

        for (IClasspathEntry entry : classpath) {
          String entryPath = entry.getPath().toOSString();
          if (CPE_SOURCE == entry.getEntryKind() && !entryPath.equals(wsPath)) {
            if (entryPath.startsWith(wsPath)) {
              sourceFolders.add(entryPath.substring(wsPath.length() + 1));
            } else {
              sourceFolders.add(entryPath);
            }
          }
        }
      } catch (JavaModelException e) {
        throw new ValueStorageException(
            "Classpath does not exist or an exception occurs while accessing its corresponding resource : "
                + e.getMessage());
      }

      return sourceFolders.isEmpty() ? singletonList(DEFAULT_SOURCE_FOLDER_VALUE) : sourceFolders;
    }
  }
}

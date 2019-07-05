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
package org.eclipse.che.plugin.java.plain.server.projecttype;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.eclipse.che.ide.ext.java.shared.Constants.OUTPUT_FOLDER;
import static org.eclipse.che.ide.ext.java.shared.Constants.SOURCE_FOLDER;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.List;
import org.eclipse.che.api.core.notification.EventService;
import org.eclipse.che.api.fs.server.PathTransformer;
import org.eclipse.che.api.project.server.ProjectManager;
import org.eclipse.che.api.project.server.type.ReadonlyValueProvider;
import org.eclipse.che.api.project.server.type.ValueProvider;
import org.eclipse.che.api.project.server.type.ValueProviderFactory;
import org.eclipse.che.api.project.server.type.ValueStorageException;
import org.eclipse.che.ide.ext.java.shared.Constants;
import org.eclipse.che.plugin.java.languageserver.JavaLanguageServerExtensionService;
import org.eclipse.che.plugin.java.languageserver.NotifyJsonRpcTransmitter;

/**
 * {@link ValueProviderFactory} for Plain Java project type. Factory crates a class which provides
 * values of Plain Java project's attributes.
 *
 * @author Valeriy Svydenko
 */
@Singleton
public class PlainJavaValueProviderFactory implements ValueProviderFactory {

  private final PathTransformer transformer;
  private final JavaLanguageServerExtensionService extensionService;

  @Inject
  public PlainJavaValueProviderFactory(
      PathTransformer transformer,
      JavaLanguageServerExtensionService extensionService,
      NotifyJsonRpcTransmitter notifyTransmitter,
      ProjectManager projectManager,
      EventService eventService) {
    this.transformer = transformer;
    this.extensionService = extensionService;
  }

  @Override
  public ValueProvider newInstance(String wsPath) {
    return new PlainJavaValueProvider(wsPath);
  }

  private class PlainJavaValueProvider extends ReadonlyValueProvider {

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

    private List<String> getOutputFolder() throws ValueStorageException {
      String outputDir;
      try {
        outputDir = extensionService.getOutputDir(wsPath);
        if (isNullOrEmpty(outputDir)) {
          outputDir = Constants.DEFAULT_OUTPUT_FOLDER_VALUE;
        }
      } catch (Exception e) {
        // can't read output dir
        outputDir = Constants.DEFAULT_OUTPUT_FOLDER_VALUE;
      }

      String fsPath = transformer.transform(wsPath).toString();
      return outputDir.startsWith(fsPath)
          ? singletonList(outputDir.substring(fsPath.length() + 1))
          : singletonList(outputDir);
    }

    private List<String> getSourceFolders() throws ValueStorageException {
      List<String> sourceFolders;
      try {
        sourceFolders = extensionService.getSourceFolders(wsPath);
      } catch (Exception e) {
        throw new ValueStorageException(
            format("Failed to get '%s'. ", SOURCE_FOLDER), e.getCause());
      }

      return sourceFolders
          .stream()
          .map(it -> it.startsWith(wsPath) ? it.substring(wsPath.length() + 1) : it)
          .collect(toList());
    }
  }
}

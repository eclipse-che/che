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
package org.eclipse.che.ide.editor;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.editor.EditorProvider;
import org.eclipse.che.ide.api.editor.EditorRegistry;
import org.eclipse.che.ide.api.extension.SDK;
import org.eclipse.che.ide.api.filetypes.FileType;

/**
 * Registry for holding {@link EditorProvider} for specific {@link FileType}.
 *
 * @author Evgen Vidolob
 */
@SDK(title = "ide.api.editorRegistry")
public class EditorRegistryImpl implements EditorRegistry {

  private Map<FileType, List<EditorProvider>> registry;
  private EditorProvider defaultProvider;

  @Inject
  public EditorRegistryImpl(
      @Named("defaultEditor") EditorProvider defaultProvider,
      @Named("defaultFileType") FileType defaultFile) {
    super();
    this.defaultProvider = defaultProvider;
    registry = new HashMap<>();
    register(defaultFile, defaultProvider);
  }

  /** {@inheritDoc} */
  @Override
  public void register(@NotNull FileType fileType, @NotNull EditorProvider provider) {
    if (!registry.containsKey(fileType)) {
      registry.put(fileType, new ArrayList<EditorProvider>());
    }
    registry.get(fileType).add(provider);
  }

  @Override
  public void registerDefaultEditor(@NotNull FileType fileType, @NotNull EditorProvider provider) {
    // todo store default editor, add checks to ensure that default editor sets only one time
    register(fileType, provider);
  }

  @Override
  public void unRegister(FileType fileType, EditorProvider provider) {
    if (fileType != null && registry.containsKey(fileType)) {
      registry.get(fileType).remove(provider);
    }
  }

  /** {@inheritDoc} */
  @Override
  public EditorProvider getEditor(@NotNull FileType fileType) {
    if (registry.containsKey(fileType) && !registry.get(fileType).isEmpty()) {
      return registry.get(fileType).get(0);
    }
    return defaultProvider;
  }

  @Override
  public List<EditorProvider> getAllEditorsForFileType(@NotNull FileType fileType) {
    List<EditorProvider> result = new ArrayList<>();
    if (registry.containsKey(fileType)) {
      result.addAll(registry.get(fileType));
    }
    return result;
  }

  @Override
  public EditorProvider findEditorProviderById(String id) {
    for (List<EditorProvider> providers : registry.values()) {
      for (EditorProvider provider : providers) {
        if (provider.getId().equals(id)) {
          return provider;
        }
      }
    }
    return defaultProvider;
  }
}

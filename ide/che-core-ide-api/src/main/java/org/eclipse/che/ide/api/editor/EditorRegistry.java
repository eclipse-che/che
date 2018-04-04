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
package org.eclipse.che.ide.api.editor;

import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.api.filetypes.FileType;

/**
 * Editor Registry allows to register new Editor for given FileType. This editor will be used as
 * default to open such kind of Files.
 *
 * @author Nikolay Zamosenchuk
 * @author Evgen Vidolob
 */
public interface EditorRegistry {
  /**
   * Register editor provider for file type.
   *
   * @param fileType
   * @param provider
   */
  void register(@NotNull FileType fileType, @NotNull EditorProvider provider);

  /**
   * Register default editor.
   *
   * @param fileType the file type
   * @param provider the provider
   */
  void registerDefaultEditor(@NotNull FileType fileType, @NotNull EditorProvider provider);

  /**
   * Get editor provide assigned for file type;
   *
   * @param fileType resource file type
   * @return editor provider
   */
  @NotNull
  EditorProvider getEditor(@NotNull FileType fileType);

  /**
   * Gets all editors for file type.
   *
   * @param fileType the file type
   * @return the all editors for file type
   */
  @NotNull
  List<EditorProvider> getAllEditorsForFileType(@NotNull FileType fileType);

  /**
   * Search editor provider by his id. Should return default editor provider if provider with that
   * id cannot be found.
   *
   * @param id the provider id
   * @return the editor provider
   */
  @NotNull
  EditorProvider findEditorProviderById(@NotNull String id);
}

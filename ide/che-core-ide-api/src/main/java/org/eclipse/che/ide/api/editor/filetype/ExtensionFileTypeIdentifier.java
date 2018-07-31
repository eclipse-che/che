/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.api.editor.filetype;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.util.loging.Log;

@Singleton
public class ExtensionFileTypeIdentifier implements FileTypeIdentifier {

  /** The known extensions registry. */
  Map<String, List<String>> mappings = new HashMap<>();

  @Inject
  public ExtensionFileTypeIdentifier(
      DefaultExtensionToMimeTypeMappings defaultExtensionToMimeTypeMappings) {
    defaultExtensionToMimeTypeMappings
        .getMappings()
        .forEach(
            (extension, mimeTypes) -> mappings.put(extension, ImmutableList.copyOf(mimeTypes)));
  }

  @Override
  public List<String> identifyType(final VirtualFile file) {
    final String filename = file.getName();
    if (filename != null) {
      final int dotPos = filename.lastIndexOf('.');
      if (dotPos
          < 1) { // either -1 (not found) or 0 (first position, for example .project or .che etc.
        Log.debug(ExtensionFileTypeIdentifier.class, "File name has no suffix ");
        return null;
      }
      final String suffix = filename.substring(dotPos + 1);
      Log.debug(ExtensionFileTypeIdentifier.class, "Looking for a type for suffix " + suffix);
      List<String> result = mappings.get(suffix);
      Log.debug(ExtensionFileTypeIdentifier.class, "Found mime-types: " + printList(result));
      return result;
    }
    return null;
  }

  public void registerNewExtension(String extension, List<String> contentTypes) {
    mappings.put(extension, contentTypes);
  }

  /**
   * Builds a list from the parameters.
   *
   * @param strings the elements of the list
   * @return the list
   */
  private List<String> makeList(final String... strings) {
    final List<String> result = new ArrayList<>();
    for (String string : strings) {
      result.add(string);
    }
    return result;
  }

  /**
   * Format a list of String for logging.
   *
   * @param strings le list to display
   * @return a representation of the list
   */
  private String printList(final List<String> strings) {
    final StringBuilder sb = new StringBuilder("[");
    if (strings != null && !strings.isEmpty()) {
      sb.append(strings.get(0));

      for (String string : strings.subList(1, strings.size())) {
        sb.append(", ");
        sb.append(string);
      }
    }
    sb.append("]");
    return sb.toString();
  }
}

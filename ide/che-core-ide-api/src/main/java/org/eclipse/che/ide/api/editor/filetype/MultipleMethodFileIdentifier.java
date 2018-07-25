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

import com.google.inject.Singleton;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.che.ide.api.resources.VirtualFile;
import org.eclipse.che.ide.util.loging.Log;

/**
 * {@link FileTypeIdentifier} that chains multiples ways to try to recognize file types.
 *
 * @author "Mickaël Leduque"
 */
@Singleton
public class MultipleMethodFileIdentifier implements FileTypeIdentifier {

  private final FileNameFileTypeIdentifier fileNameFileTypeIdentifier;
  private final ExtensionFileTypeIdentifier extensionFileTypeIdentifier;
  private final FirstLineFileTypeIdentifier firstLineFileTypeIdentifier;

  @Inject
  public MultipleMethodFileIdentifier(
      FileNameFileTypeIdentifier fileNameFileTypeIdentifier,
      ExtensionFileTypeIdentifier extensionFileTypeIdentifier,
      FirstLineFileTypeIdentifier firstLineFileTypeIdentifier) {
    this.fileNameFileTypeIdentifier = fileNameFileTypeIdentifier;
    this.extensionFileTypeIdentifier = extensionFileTypeIdentifier;
    this.firstLineFileTypeIdentifier = firstLineFileTypeIdentifier;
  }

  public void registerNewExtension(String extension, List<String> contentTypes) {
    extensionFileTypeIdentifier.registerNewExtension(extension, contentTypes);
  }

  @Override
  public List<String> identifyType(final VirtualFile file) {
    Log.debug(MultipleMethodFileIdentifier.class, "Try identification by file name.");
    final List<String> firstTry = this.fileNameFileTypeIdentifier.identifyType(file);
    if (firstTry != null && !firstTry.isEmpty()) {
      return firstTry;
    }
    Log.debug(MultipleMethodFileIdentifier.class, "Try identification by file name suffix.");
    final List<String> secondTry = this.extensionFileTypeIdentifier.identifyType(file);
    if (secondTry != null && !secondTry.isEmpty()) {
      return secondTry;
    }
    // try harder
    Log.debug(MultipleMethodFileIdentifier.class, "Try identification by looking at the content.");
    final List<String> thirdTry = this.firstLineFileTypeIdentifier.identifyType(file);
    if (thirdTry != null && !thirdTry.isEmpty()) {
      return thirdTry;
    }
    // other means may be added later
    Log.debug(MultipleMethodFileIdentifier.class, "No identification method gave an answer.");
    return null;
  }
}

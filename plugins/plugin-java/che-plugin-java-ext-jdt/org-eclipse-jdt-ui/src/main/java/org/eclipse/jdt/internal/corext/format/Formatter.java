/**
 * ***************************************************************************** Copyright (c)
 * 2012-2015 Red Hat, Inc. All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Red Hat, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.jdt.internal.corext.format;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.eclipse.che.ide.ext.java.shared.dto.Change;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

/** @author Roman Nikitenko */
public class Formatter {

  /**
   * Creates edits that describe how to format the given string. Returns the changes required to
   * format source.
   *
   * @param formatter The file with custom formatter settings
   * @param content The content to format
   * @param offset The given offset to start recording the edits (inclusive).
   * @param length the given length to stop recording the edits (exclusive).
   * @return <code>List<Change></code> describing the changes required to format source
   * @throws IllegalArgumentException If the offset and length are not inside the string, a
   *     IllegalArgumentException is thrown.
   */
  public List<Change> getFormatChanges(File formatter, String content, int offset, int length)
      throws BadLocationException, IllegalArgumentException {
    IDocument document = new Document(content);
    DocumentChangeListener documentChangeListener = new DocumentChangeListener(document);
    Map<String, String> options = null;
    if (formatter != null && formatter.exists()) {
      options = CheCodeFormatterOptions.getFormatSettingsFromFile(formatter);
    }
    TextEdit textEdit =
        CodeFormatterUtil.format2(
            CodeFormatter.K_COMPILATION_UNIT, content, offset, length, 0, null, options);
    textEdit.apply(document);
    return documentChangeListener.getChanges();
  }
}

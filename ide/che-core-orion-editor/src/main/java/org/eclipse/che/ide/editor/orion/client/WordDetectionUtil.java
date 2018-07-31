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
package org.eclipse.che.ide.editor.orion.client;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import org.eclipse.che.ide.api.editor.document.Document;
import org.eclipse.che.ide.api.editor.text.Position;

/** */
public class WordDetectionUtil {

  private static final String COMMON_WORD_REGEXP =
      "(-?\\d*\\.\\d\\w*)|([^\\`\\~\\!\\@\\#\\$\\%\\^\\&\\*\\(\\)\\-\\=\\+\\[\\{\\]\\}\\\\\\|\\;\\:\\'\\\"\\,\\.\\<\\>\\/\\?\\s]+)";

  public Position getWordAtOffset(Document document, int offset) {
    if (document == null) {
      return null;
    }

    RegExp regExp = RegExp.compile(COMMON_WORD_REGEXP, "g");
    int line = document.getLineAtOffset(offset);
    String lineContent = document.getLineContent(line);
    int lineStart = document.getLineStart(line);

    int pos = offset - lineStart;
    int start = lineContent.lastIndexOf(' ', pos - 1) + 1;

    regExp.setLastIndex(start);
    MatchResult matchResult;
    while ((matchResult = regExp.exec(lineContent)) != null) {
      if (matchResult.getIndex() <= pos && regExp.getLastIndex() >= pos) {
        return new Position(matchResult.getIndex() + lineStart, matchResult.getGroup(0).length());
      }
    }
    return null;
  }
}

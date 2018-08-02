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
package org.eclipse.che.ide.api.editor.changeintercept;

import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import org.eclipse.che.ide.api.editor.document.ReadOnlyDocument;

/** Automatic insertion of c-style /* and /** comment end. */
public final class CloseCStyleCommentChangeInterceptor implements TextChangeInterceptor {

  @Override
  public TextChange processChange(final TextChange change, ReadOnlyDocument document) {
    final RegExp regex = RegExp.compile("^\n(\\s*)\\*\\s*$");
    final MatchResult matchResult = regex.exec(change.getNewText());
    // either must be on the first line or be just after a line break (regexp)
    if (matchResult != null) {
      final String line = document.getLineContent(change.getFrom().getLine());
      // matches a line containing only whitespaces followed by either /** or /* and then optionally
      // whitespaces again
      if (!line.matches("^\\s*\\/\\*\\*?\\s*$")) {
        return null;
      }

      final String whitespaces = matchResult.getGroup(1);

      final String modifiedInsert = "\n" + whitespaces + "* \n" + whitespaces + "*/";

      return new TextChange.Builder()
          .from(change.getFrom())
          .to(change.getFrom())
          .insert(modifiedInsert)
          .build();
    } else {
      return null;
    }
  }
}

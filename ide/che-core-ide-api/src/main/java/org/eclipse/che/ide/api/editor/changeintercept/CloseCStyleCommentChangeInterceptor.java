/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.api.editor.changeintercept;

import org.eclipse.che.ide.api.editor.document.ReadOnlyDocument;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;

/**
 * Automatic insertion of c-style /* and /** comment end.
 */
public final class CloseCStyleCommentChangeInterceptor implements TextChangeInterceptor {

    @Override
    public TextChange processChange(final TextChange change, ReadOnlyDocument document) {
        final RegExp regex = RegExp.compile("^\n(\\s*)\\*\\s*$");
        final MatchResult matchResult = regex.exec(change.getNewText());
        // either must be on the first line or be just after a line break (regexp)
        if (matchResult != null) {
            final String line = document.getLineContent(change.getFrom().getLine());
            // matches a line containing only whitespaces followed by either /** or /* and then optionally whitespaces again
            if (!line.matches("^\\s*\\/\\*\\*?\\s*$")) {
                return null;
            }

            final String whitespaces = matchResult.getGroup(1);

            final String modifiedInsert = "\n" + whitespaces + "* \n" + whitespaces + "*/";

            return new TextChange.Builder().from(change.getFrom()).to(change.getFrom()).insert(modifiedInsert).build();
        } else {
            return null;
        }
    }
}

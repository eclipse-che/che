/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.corext.format;

import org.eclipse.che.ide.ext.java.shared.dto.Change;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

import java.util.List;

/**
 * @author Roman Nikitenko
 */
public class Formatter {

    /**
     * Creates edits that describe how to format the given string.
     * Returns the changes required to format source.
     *
     * @param content
     *         The content to format
     * @param offset
     *         The given offset to start recording the edits (inclusive).
     * @param length
     *         the given length to stop recording the edits (exclusive).
     * @return <code>List<Change></code> describing the changes required to format source
     * @throws IllegalArgumentException
     *         If the offset and length are not inside the string, a IllegalArgumentException is thrown.
     */
    public List<Change> getFormatChanges(String content, int offset, int length) throws BadLocationException, IllegalArgumentException {
        IDocument document = new Document(content);
        DocumentChangeListener documentChangeListener = new DocumentChangeListener(document);
        TextEdit textEdit = CodeFormatterUtil.format2(CodeFormatter.K_COMPILATION_UNIT, content, offset, length, 0, null, null);
        textEdit.apply(document);
        return documentChangeListener.getChanges();
    }
}

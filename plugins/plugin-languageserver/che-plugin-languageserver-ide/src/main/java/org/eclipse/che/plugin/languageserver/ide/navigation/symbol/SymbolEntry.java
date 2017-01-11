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
package org.eclipse.che.plugin.languageserver.ide.navigation.symbol;

import org.eclipse.che.ide.api.editor.text.TextPosition;
import org.eclipse.che.ide.api.editor.text.TextRange;
import org.eclipse.che.ide.api.editor.texteditor.TextEditor;
import org.eclipse.che.plugin.languageserver.ide.filters.Match;
import org.eclipse.che.plugin.languageserver.ide.quickopen.QuickOpenEntryGroup;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.List;

/**
 * @author Evgen Vidolob
 */
class SymbolEntry extends QuickOpenEntryGroup {

    private String    name;
    private String    type;
    private String    description;
    private TextRange range;
    private TextEditor editor;
    private SVGResource icon;

    public SymbolEntry(String name,
                       String type,
                       String description,
                       TextRange range,
                       TextEditor editor,
                       List<Match> highlights,
                       SVGResource icon) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.range = range;
        this.editor = editor;
        this.icon = icon;
        setHighlights(highlights);
    }

    @Override
    public String getLabel() {
        return name;
    }

    @Override
    public SVGResource getIcon() {
        return icon;
    }

    @Override
    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public TextRange getRange() {
        return range;
    }

    @Override
    public boolean run(Mode mode) {
        if (mode == Mode.OPEN) {
            return runOpen();
        }
        return runPreview();
    }

    private boolean runPreview() {
        editor.getDocument().setSelectedRange(range, true);
        return false;
    }

    private boolean runOpen() {
        editor.getDocument().setCursorPosition(new TextPosition(range.getFrom().getLine(), range.getFrom().getCharacter()));
        editor.setFocus();
        return true;
    }
}

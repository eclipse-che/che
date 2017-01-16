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
package org.eclipse.che.plugin.languageserver.ide.navigation.workspace;

import org.eclipse.che.ide.api.editor.text.TextRange;
import org.eclipse.che.plugin.languageserver.ide.filters.Match;
import org.eclipse.che.plugin.languageserver.ide.util.OpenFileInEditorHelper;
import org.eclipse.che.plugin.languageserver.ide.quickopen.EditorQuickOpenEntry;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.List;

/**
 * @author Evgen Vidolob
 */
class SymbolEntry extends EditorQuickOpenEntry {

    private String    name;
    private String    parameters;
    private String    description;
    private String    filePath;
    private String    type;
    private TextRange range;
    private SVGResource icon;

    public SymbolEntry(String name,
                       String parameters,
                       String description,
                       String filePath,
                       String type,
                       TextRange range,
                       SVGResource icon,
                       OpenFileInEditorHelper editorHelper, List<Match> matches) {
        super(editorHelper);
        this.name = name;
        this.parameters = parameters;
        this.description = description;
        this.filePath = filePath;
        this.type = type;
        this.range = range;
        this.icon = icon;
        setHighlights(matches);
    }

    @Override
    protected String getFilePath() {
        return filePath;
    }

    @Override
    public String getLabel() {
        return name + parameters;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public SVGResource getIcon() {
        return icon;
    }

    public String getType(){
        return type;
    }

    @Override
    protected TextRange getTextRange() {
        return range;
    }
}

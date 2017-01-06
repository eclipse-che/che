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
package org.eclipse.che.ide.editor.preferences.editorproperties;

import javax.validation.constraints.NotNull;

/**
 * The interface contains editor's constants.
 *
 * @author Roman Nikitenko
 */
public enum EditorProperties {

    //Tabs section
    TAB_SIZE("tabSize"),
    EXPAND_TAB("expandTab"),

    //Typing section
    AUTO_PAIR_PARENTHESES("autoPairParentheses"),
    AUTO_PAIR_BRACES("autoPairBraces"),
    AUTO_PAIR_SQUARE_BRACKETS("autoPairSquareBrackets"),
    AUTO_PAIR_ANGLE_BRACKETS("autoPairAngleBrackets"),
    AUTO_PAIR_QUOTATIONS("autoPairQuotations"),
    AUTO_COMPLETE_COMMENTS("autoCompleteComments"),
    SMART_INDENTATION("smartIndentation"),

    //White spaces section
    SHOW_WHITESPACES("showWhitespaces"),

    //Rulers section
    SHOW_ANNOTATION_RULER("annotationRuler"),
    SHOW_LINE_NUMBER_RULER("lineNumberRuler"),
    SHOW_FOLDING_RULER("foldingRuler"),
    SHOW_OVERVIEW_RULER("overviewRuler"),
    SHOW_ZOOM_RULER("zoomRuler"),

    //Language tools section
    SHOW_OCCURRENCES("showOccurrences"),
    SHOW_CONTENT_ASSIST_AUTOMATICALLY("contentAssistAutoTrigger");

    private final String value;

    EditorProperties(@NotNull String value) {
        this.value = value;
    }

    /** Returns value which associated with enum */
    @NotNull
    @Override
    public String toString() {
        return value;
    }
}

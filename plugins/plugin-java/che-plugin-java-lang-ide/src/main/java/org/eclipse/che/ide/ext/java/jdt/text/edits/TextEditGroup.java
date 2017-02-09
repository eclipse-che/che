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
package org.eclipse.che.ide.ext.java.jdt.text.edits;

import org.eclipse.che.ide.api.editor.text.Region;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A text edit group combines a list of {@link TextEdit}s and a name into a single object. The name must be a human readable
 * string use to present the text edit group in the user interface.
 * <p>
 * Clients may extend this class to add extra information to a text edit group.
 * </p>
 */
public class TextEditGroup {

    private String fDescription;

    private List fEdits;

    /**
     * Creates a new text edit group with the given name.
     *
     * @param name
     *         the name of the text edit group. Must be a human readable string
     */
    public TextEditGroup(String name) {
        super();
        // Assert.isNotNull(name);
        fDescription = name;
        fEdits = new ArrayList(3);
    }

    /**
     * Creates a new text edit group with a name and a single {@link TextEdit}.
     *
     * @param name
     *         the name of the text edit group. Must be a human readable string
     * @param edit
     *         the edit to manage
     */
    public TextEditGroup(String name, TextEdit edit) {
        // Assert.isNotNull(name);
        // Assert.isNotNull(edit);
        fDescription = name;
        fEdits = new ArrayList(1);
        fEdits.add(edit);
    }

    /**
     * Creates a new text edit group with the given name and array of edits.
     *
     * @param name
     *         the name of the text edit group. Must be a human readable string
     * @param edits
     *         the array of edits
     */
    public TextEditGroup(String name, TextEdit[] edits) {
        super();
        // Assert.isNotNull(name);
        // Assert.isNotNull(edits);
        fDescription = name;
        fEdits = new ArrayList(Arrays.asList(edits));
    }

    /**
     * Returns the edit group's name.
     *
     * @return the edit group's name
     */
    public String getName() {
        return fDescription;
    }

    /**
     * Adds the given {@link TextEdit} to this group.
     *
     * @param edit
     *         the edit to add
     */
    public void addTextEdit(TextEdit edit) {
        fEdits.add(edit);
    }

    /**
     * Removes the given {@link TextEdit} from this group.
     *
     * @param edit
     *         the edit to remove
     * @return <code>true</code> if this group contained the specified edit.
     * @since 3.3
     */
    public boolean removeTextEdit(TextEdit edit) {
        return fEdits.remove(edit);
    }

    /**
     * Removes all text edits from this group.
     *
     * @since 3.3
     */
    public void clearTextEdits() {
        fEdits.clear();
    }

    /**
     * Returns <code>true</code> if the list of managed {@link TextEdit}s is empty; otherwise <code>false
     * </code> is returned.
     *
     * @return whether the list of managed text edits is empty or not
     */
    public boolean isEmpty() {
        return fEdits.isEmpty();
    }

    /**
     * Returns an array of {@link TextEdit}s containing the edits managed by this group.
     *
     * @return the managed text edits
     */
    public TextEdit[] getTextEdits() {
        return (TextEdit[])fEdits.toArray(new TextEdit[fEdits.size()]);
    }

    /**
     * Returns the text region covered by the edits managed via this edit group. If the group doesn't manage any edits <code>null
     * </code> is returned.
     *
     * @return the text region covered by this edit group or <code>
     * null</code> if no edits are managed
     */
    public Region getRegion() {
        int size = fEdits.size();
        if (size == 0) {
            return null;
        } else if (size == 1) {
            return ((TextEdit)fEdits.get(0)).getRegion();
        } else {
            return TextEdit.getCoverage((TextEdit[])fEdits.toArray(new TextEdit[fEdits.size()]));
        }
    }
}

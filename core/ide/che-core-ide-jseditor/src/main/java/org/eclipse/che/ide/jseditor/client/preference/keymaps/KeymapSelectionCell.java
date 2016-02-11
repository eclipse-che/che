/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.jseditor.client.preference.keymaps;

import java.util.List;

import org.eclipse.che.ide.jseditor.client.editortype.EditorType;
import org.eclipse.che.ide.jseditor.client.keymap.Keymap;
import com.google.gwt.cell.client.AbstractInputCell;
import com.google.gwt.cell.client.SelectionCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.SelectElement;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class KeymapSelectionCell extends AbstractInputCell<Keymap, String> {

    interface Template extends SafeHtmlTemplates {
        @Template("<option value=\"{0}\">{1}</option>")
        SafeHtml deselected(String key, String display);

        @Template("<option value=\"{0}\" selected=\"selected\">{1}</option>")
        SafeHtml selected(String key, String display);

        @Template("<select class=\"{0} {1}\">")
        SafeHtml select(String classname, String selectWidthStyle);
    }

    private static Template template;

    private final String stylename;
    private final String widthStylename;

    /**
     * Construct a new {@link SelectionCell} with the specified options.
     * 
     * @param options the options in the cell
     */
    public KeymapSelectionCell(final String stylename, final String widthStylename) {
        super(BrowserEvents.CHANGE);

        initTemplate();

        this.stylename = stylename;
        this.widthStylename = widthStylename;
    }

    @Override
    public void onBrowserEvent(final Context context, final Element parent, final Keymap value,
                               final NativeEvent event, final ValueUpdater<Keymap> valueUpdater) {
        super.onBrowserEvent(context, parent, value, event, valueUpdater);
        String type = event.getType();
        if (BrowserEvents.CHANGE.equals(type)) {
            final EditorType key = (EditorType)context.getKey();
            final SelectElement select = parent.getFirstChild().cast();

            final List<Keymap> keymapsForRow = Keymap.getInstances(key);
            final Keymap newValue = keymapsForRow.get(select.getSelectedIndex());
            setViewData(key, newValue.getKey());
            finishEditing(parent, newValue, key, valueUpdater);
            if (valueUpdater != null) {
                valueUpdater.update(newValue);
            }
        }
    }

    @Override
    public void render(final Context context, final Keymap keymap, final SafeHtmlBuilder sb) {
        // Get the view data.
        EditorType editorType = (EditorType)context.getKey();
        String viewData = getViewData(editorType);
        if (viewData != null && viewData.equals(keymap.getKey())) {
            clearViewData(editorType);
            viewData = null;
        }

        final List<Keymap> keymapsForRow = Keymap.getInstances(editorType);
        final int selectedIndex = getIndex(keymapsForRow, viewData, keymap);

        if (keymapsForRow == null || keymapsForRow.isEmpty()) {
            return;
        }
        sb.append(template.select(this.stylename, this.widthStylename));
        int index = 0;
        for (final Keymap option : keymapsForRow) {
            if (index++ == selectedIndex) {
                sb.append(template.selected(option.getKey(), option.getDisplay()));
            } else {
                sb.append(template.deselected(option.getKey(), option.getDisplay()));
            }
        }
        sb.appendHtmlConstant("</select>");
    }

    private int getIndex(final List<Keymap> keymapsForRow, final String viewData, final Keymap keymap) {
        String value = viewData;
        if (value == null) {
            if (keymap != null) {
                value = keymap.getKey();
            } else {
                return -1;
            }
        }
        for (int i = 0; i < keymapsForRow.size(); i++) {
            final Keymap item = keymapsForRow.get(i);
            if (item != null && item.getKey().equals(value)) {
                return i;
            }
        }
        return -1;
    }

    private static void initTemplate() {
        if (template == null) {
            template = GWT.create(Template.class);
        }
    }
}

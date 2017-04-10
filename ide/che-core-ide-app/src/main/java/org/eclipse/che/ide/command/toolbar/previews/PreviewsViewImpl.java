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
package org.eclipse.che.ide.command.toolbar.previews;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.ui.dropdown.BaseListItem;
import org.eclipse.che.ide.ui.dropdown.DropdownList;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.eclipse.che.ide.command.toolbar.previews.PreviewUrlItemRenderer.HEADER_WIDGET;

/** Implementation of {@link PreviewsView} that displays preview URLs in a dropdown list. */
@Singleton
public class PreviewsViewImpl implements PreviewsView {

    /** Mapping of URL to list item. */
    private final Map<PreviewUrlItem, BaseListItem<PreviewUrlItem>> listItems;

    private final DropdownList dropdownList;

    private ActionDelegate delegate;

    @Inject
    public PreviewsViewImpl() {
        listItems = new HashMap<>();

        dropdownList = new DropdownList(HEADER_WIDGET);
        dropdownList.setWidth("43px");
        dropdownList.ensureDebugId("dropdown-preview_url");
        dropdownList.setSelectionHandler(item -> {
            for (Entry<PreviewUrlItem, BaseListItem<PreviewUrlItem>> entry : listItems.entrySet()) {
                if (item.equals(entry.getValue())) {
                    delegate.onUrlChosen(entry.getKey());
                    return;
                }
            }
        });
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Widget asWidget() {
        return dropdownList;
    }

    @Override
    public void addUrl(PreviewUrlItem previewUrlItem) {
        if (listItems.containsKey(previewUrlItem)) {
            return; // no sense to add the equals URLs even if they belong to different commands
        }

        BaseListItem<PreviewUrlItem> listItem = new BaseListItem<>(previewUrlItem);
        PreviewUrlItemRenderer renderer = new PreviewUrlItemRenderer(listItem);

        listItems.put(previewUrlItem, listItem);
        dropdownList.addItem(listItem, renderer);
    }

    @Override
    public void removeUrl(PreviewUrlItem previewUrlItem) {
        final BaseListItem<PreviewUrlItem> listItem = listItems.remove(previewUrlItem);

        if (listItem != null) {
            dropdownList.removeItem(listItem);
        }
    }

    @Override
    public void removeAllURLs() {
        listItems.clear();
        dropdownList.clear();
    }
}

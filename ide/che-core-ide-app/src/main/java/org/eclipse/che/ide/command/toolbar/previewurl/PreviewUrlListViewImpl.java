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
package org.eclipse.che.ide.command.toolbar.previewurl;

import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.ui.dropdown.BaseListItem;
import org.eclipse.che.ide.ui.dropdown.DropDownList;

import java.util.HashMap;
import java.util.Map;

/** Implementation of {@link PreviewUrlListView} that displays preview URLs in a drop down list. */
@Singleton
public class PreviewUrlListViewImpl implements PreviewUrlListView {

    private final Map<String, BaseListItem<String>> listItems;
    private final DropDownList                      dropDownList;
    private final SimplePanel                       rootPanel;

    private ActionDelegate delegate;

    @Inject
    public PreviewUrlListViewImpl() {
        listItems = new HashMap<>();

        dropDownList = new DropDownList("Preview");
        dropDownList.setWidth("100px");
        dropDownList.setSelectionHandler(item -> {
            for (Map.Entry<String, BaseListItem<String>> entry : listItems.entrySet()) {
                if (item.equals(entry.getValue())) {
                    delegate.onUrlChosen(entry.getKey());
                    return;
                }
            }
        });

        rootPanel = new SimplePanel(dropDownList);
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public Widget asWidget() {
        return rootPanel;
    }

    @Override
    public void addUrl(String url) {
        listItems.put(url, dropDownList.addItem(url));
    }

    @Override
    public void removeUrl(String url) {
        final BaseListItem<String> listItem = listItems.get(url);

        if (listItem != null) {
            dropDownList.removeItem(listItem);
        }
    }

    @Override
    public void clearList() {
        listItems.clear();
        dropDownList.clear();
    }
}

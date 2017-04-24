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

import elemental.dom.Element;

import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.command.toolbar.ToolbarMessages;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.dropdown.BaseListItem;
import org.eclipse.che.ide.ui.dropdown.DropdownList;
import org.eclipse.che.ide.ui.dropdown.StringItemRenderer;

import java.util.HashMap;
import java.util.Map;

import static org.eclipse.che.ide.command.toolbar.previews.PreviewUrlItemRenderer.HEADER_WIDGET;
import static org.eclipse.che.ide.ui.menu.PositionController.HorizontalAlign.MIDDLE;
import static org.eclipse.che.ide.ui.menu.PositionController.VerticalAlign.BOTTOM;

/** Implementation of {@link PreviewsView} that displays preview URLs in a dropdown list. */
@Singleton
public class PreviewsViewImpl implements PreviewsView {

    /** Mapping of URL to list item. */
    private final Map<PreviewUrlItem, BaseListItem<PreviewUrlItem>> listItems;

    private final DropdownList           dropdownList;
    private final NoPreviewsItem         noPreviewsItem;
    private final NoPreviewsItemRenderer noPreviewsItemRenderer;
    private final ToolbarMessages        messages;

    private ActionDelegate delegate;

    @Inject
    public PreviewsViewImpl(ToolbarMessages messages) {
        this.messages = messages;
        listItems = new HashMap<>();

        dropdownList = new DropdownList(HEADER_WIDGET, false);
        dropdownList.setWidth("43px");
        dropdownList.ensureDebugId("dropdown-preview_url");

        dropdownList.setSelectionHandler(item -> listItems.entrySet()
                                                          .stream()
                                                          .filter(entry -> item.equals(entry.getValue()))
                                                          .findAny()
                                                          .ifPresent(entry -> delegate.onUrlChosen(entry.getKey())));

        noPreviewsItem = new NoPreviewsItem();
        noPreviewsItemRenderer = new NoPreviewsItemRenderer();
        checkNoPreviewsItem();

        Tooltip.create((Element)dropdownList.getElement(), BOTTOM, MIDDLE, messages.previewsTooltip());
    }

    private void checkNoPreviewsItem() {
        if (listItems.isEmpty()) {
            dropdownList.addItem(noPreviewsItem, noPreviewsItemRenderer);
        } else {
            dropdownList.removeItem(noPreviewsItem);
        }
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

        checkNoPreviewsItem();
    }

    @Override
    public void removeUrl(PreviewUrlItem previewUrlItem) {
        final BaseListItem<PreviewUrlItem> listItem = listItems.remove(previewUrlItem);

        if (listItem != null) {
            dropdownList.removeItem(listItem);

            checkNoPreviewsItem();
        }
    }

    @Override
    public void removeAllURLs() {
        listItems.clear();
        dropdownList.clear();

        checkNoPreviewsItem();
    }

    private class NoPreviewsItem extends BaseListItem<String> {
        NoPreviewsItem() {
            super(messages.previewsNoPreviews());
        }
    }

    private class NoPreviewsItemRenderer extends StringItemRenderer {
        NoPreviewsItemRenderer() {
            super(noPreviewsItem);
        }

        @Override
        public Widget renderHeaderWidget() {
            return HEADER_WIDGET;
        }
    }
}

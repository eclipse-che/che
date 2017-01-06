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
package org.eclipse.che.ide.command.type;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.command.CommandType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of {@link CommandTypeChooserView} which which pops up list of the command types.
 * User can select command type with Enter key or cancel selection with Esc key.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandTypeChooserViewImpl extends PopupPanel implements CommandTypeChooserView {

    private static final CommandTypeChooserViewImplUiBinder UI_BINDER = GWT.create(CommandTypeChooserViewImplUiBinder.class);

    /** Map that contains all shown command types. */
    private final Map<String, CommandType> commandTypesById;

    @UiField
    DockLayoutPanel layoutPanel;

    @UiField
    ListBox typesList;

    private ActionDelegate delegate;

    @Inject
    public CommandTypeChooserViewImpl() {
        commandTypesById = new HashMap<>();

        setWidget(UI_BINDER.createAndBindUi(this));

        initView();
        addHandlers();
    }

    private void initView() {
        setAutoHideEnabled(true);
        setAnimationEnabled(true);
        setAnimationType(AnimationType.ROLL_DOWN);
    }

    private void addHandlers() {
        addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                if (event.isAutoClosed()) {
                    delegate.onCanceled();
                }
            }
        });

        typesList.addDoubleClickHandler(new DoubleClickHandler() {
            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                final String selectedTypeId = typesList.getSelectedValue();

                if (selectedTypeId != null) {
                    final CommandType selectedCommandType = commandTypesById.get(selectedTypeId);

                    if (selectedCommandType != null) {
                        delegate.onSelected(selectedCommandType);
                    }
                }
            }
        });

        typesList.addKeyPressHandler(new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                final int keyCode = event.getNativeEvent().getKeyCode();

                if (KeyCodes.KEY_ENTER == keyCode || KeyCodes.KEY_MAC_ENTER == keyCode) {
                    final String selectedTypeId = typesList.getSelectedValue();

                    if (selectedTypeId != null) {
                        final CommandType selectedCommandType = commandTypesById.get(selectedTypeId);

                        if (selectedCommandType != null) {
                            delegate.onSelected(selectedCommandType);
                        }
                    }
                }
            }
        });

        typesList.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (KeyCodes.KEY_ESCAPE == event.getNativeKeyCode()) {
                    hide(true);
                }
            }
        });
    }

    @Override
    public void setDelegate(ActionDelegate delegate) {
        this.delegate = delegate;
    }

    @Override
    public void show(int left, int top) {
        setPopupPosition(left, top);

        super.show();

        typesList.setFocus(true);
    }

    @Override
    public void close() {
        hide();
    }

    @Override
    public void setTypes(List<CommandType> commandTypes) {
        typesList.clear();
        commandTypesById.clear();

        for (CommandType commandType : commandTypes) {
            commandTypesById.put(commandType.getId(), commandType);

            typesList.addItem(commandType.getDisplayName(), commandType.getId());
        }

        typesList.setVisibleItemCount(commandTypes.size());
        typesList.setSelectedIndex(0);

        // set height of the each row in the list to 15 px
        final int listHeight = 15 * commandTypes.size();
        typesList.setHeight(listHeight + "px");

        // set height of the entire panel
        layoutPanel.setHeight(listHeight + "px");
    }

    interface CommandTypeChooserViewImplUiBinder extends UiBinder<DockLayoutPanel, CommandTypeChooserViewImpl> {
    }
}

/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.command.type.chooser;

import static com.google.gwt.user.client.ui.PopupPanel.AnimationType.ROLL_DOWN;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.che.ide.api.command.CommandType;
import org.eclipse.che.ide.command.CommandResources;

/**
 * Implementation of {@link CommandTypeChooserView} which which pops up list of the command types.
 * User can select command type with Enter key or cancel selection with Esc key.
 *
 * @author Artem Zatsarynnyi
 */
public class CommandTypeChooserViewImpl extends PopupPanel implements CommandTypeChooserView {

  private static final CommandTypeChooserViewImplUiBinder UI_BINDER =
      GWT.create(CommandTypeChooserViewImplUiBinder.class);

  /** Map that contains all shown command types. */
  private final Map<String, CommandType> commandTypesById;

  @UiField ListBox typesList;

  private ActionDelegate delegate;

  @Inject
  public CommandTypeChooserViewImpl(CommandResources resources) {
    commandTypesById = new HashMap<>();

    addStyleName(resources.commandTypeChooserCss().chooserPopup());

    setWidget(UI_BINDER.createAndBindUi(this));

    initView();
    addHandlers();
  }

  private void initView() {
    setAutoHideEnabled(true);
    setAnimationEnabled(true);
    setAnimationType(ROLL_DOWN);
  }

  private void addHandlers() {
    addCloseHandler(
        event -> {
          if (event.isAutoClosed()) {
            delegate.onCanceled();
          }
        });

    typesList.addDoubleClickHandler(
        event -> {
          final String selectedTypeId = typesList.getSelectedValue();

          if (selectedTypeId != null) {
            final CommandType selectedCommandType = commandTypesById.get(selectedTypeId);

            if (selectedCommandType != null) {
              delegate.onSelected(selectedCommandType);
            }
          }
        });

    typesList.addKeyPressHandler(
        event -> {
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
        });

    typesList.addKeyDownHandler(
        event -> {
          if (KeyCodes.KEY_ESCAPE == event.getNativeKeyCode()) {
            hide(true);
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
  public void setCommandTypes(List<CommandType> commandTypes) {
    typesList.clear();
    commandTypesById.clear();

    commandTypes.forEach(
        commandType -> {
          commandTypesById.put(commandType.getId(), commandType);
          typesList.addItem(commandType.getDisplayName(), commandType.getId());
        });

    typesList.setVisibleItemCount(commandTypes.size());
    typesList.setSelectedIndex(0);
  }

  interface CommandTypeChooserViewImplUiBinder
      extends UiBinder<ListBox, CommandTypeChooserViewImpl> {}
}

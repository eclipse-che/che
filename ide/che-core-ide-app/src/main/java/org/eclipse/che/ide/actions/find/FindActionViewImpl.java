/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.actions.find;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;
import elemental.dom.Element;
import elemental.dom.Node;
import elemental.html.TableCellElement;
import elemental.html.TableElement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.action.Action;
import org.eclipse.che.ide.api.action.ActionEvent;
import org.eclipse.che.ide.api.action.ActionManager;
import org.eclipse.che.ide.api.action.Presentation;
import org.eclipse.che.ide.api.editor.codeassist.AutoCompleteResources;
import org.eclipse.che.ide.api.keybinding.KeyBindingAgent;
import org.eclipse.che.ide.ui.ElementWidget;
import org.eclipse.che.ide.ui.list.SimpleList;
import org.eclipse.che.ide.ui.toolbar.PresentationFactory;
import org.eclipse.che.ide.ui.toolbar.ToolbarResources;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.ide.util.input.KeyMapUtil;

/**
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 */
public class FindActionViewImpl extends PopupPanel implements FindActionView {

  interface FindActionViewImplUiBinder extends UiBinder<DockLayoutPanel, FindActionViewImpl> {}

  private final AutoCompleteResources.Css css;

  private final PresentationFactory presentationFactory;

  private final SimpleList.ListEventDelegate<Action> eventDelegate =
      new SimpleList.ListEventDelegate<Action>() {
        @Override
        public void onListItemClicked(Element listItemBase, Action itemData) {
          list.getSelectionModel().setSelectedItem(itemData);
        }

        @Override
        public void onListItemDoubleClicked(Element listItemBase, Action itemData) {
          delegate.onActionSelected(itemData);
        }
      };

  private final SimpleList.ListItemRenderer<Action> listItemRenderer =
      new SimpleList.ListItemRenderer<Action>() {
        @Override
        public void render(Element itemElement, Action itemData) {
          TableCellElement icon = Elements.createTDElement(css.proposalIcon());
          TableCellElement label = Elements.createTDElement(css.proposalLabel());
          TableCellElement group = Elements.createTDElement(css.proposalGroup());

          Presentation presentation = presentationFactory.getPresentation(itemData);
          itemData.update(new ActionEvent(presentation, actionManager));

          if (presentation.getImageElement() != null) {
            ElementWidget image = new ElementWidget(presentation.getImageElement());
            image.getElement().setAttribute("class", toolbarResources.toolbar().iconButtonIcon());
            image.getElement().getStyle().setMargin(0, Style.Unit.PX);
            icon.appendChild((Node) image.getElement());

          } else if (presentation.getHTMLResource() != null) {
            icon.setInnerHTML(presentation.getHTMLResource());
          }

          String hotKey =
              KeyMapUtil.getShortcutText(
                  keyBindingAgent.getKeyBinding(actionManager.getId(itemData)));
          if (hotKey == null) {
            hotKey = "&nbsp;";
          } else {
            hotKey = "<nobr>&nbsp;[" + hotKey + "]&nbsp;</nobr>";
          }
          label.setInnerHTML(presentation.getText() + hotKey);
          if (!presentation.isEnabled() || !presentation.isVisible()) {
            itemElement.getStyle().setProperty("opacity", "0.6");
          }
          String groupName = actions.get(itemData);
          if (groupName != null) {
            group.setInnerHTML(groupName);
          }
          itemElement.appendChild(icon);
          itemElement.appendChild(label);
          itemElement.appendChild(group);
        }

        @Override
        public Element createElement() {
          return Elements.createTRElement();
        }
      };

  @UiField TextBox nameField;

  @UiField CheckBox includeNonMenu;

  private ActionDelegate delegate;
  private Resources resources;
  private KeyBindingAgent keyBindingAgent;
  private ActionManager actionManager;

  @UiField DockLayoutPanel layoutPanel;

  @UiField FlowPanel actionsPanel;

  @UiField HTML actionsContainer;

  private SimpleList<Action> list;
  private Map<Action, String> actions;
  private ToolbarResources toolbarResources;

  @Inject
  public FindActionViewImpl(
      Resources resources,
      KeyBindingAgent keyBindingAgent,
      ActionManager actionManager,
      AutoCompleteResources autoCompleteResources,
      ToolbarResources toolbarResources,
      FindActionViewImplUiBinder uiBinder) {
    this.resources = resources;
    this.keyBindingAgent = keyBindingAgent;
    this.actionManager = actionManager;
    this.toolbarResources = toolbarResources;
    this.presentationFactory = new PresentationFactory();

    css = autoCompleteResources.autocompleteComponentCss();
    css.ensureInjected();

    DockLayoutPanel rootElement = uiBinder.createAndBindUi(this);
    setWidget(rootElement);
    setAutoHideEnabled(true);
    setAnimationEnabled(true);

    layoutPanel.setWidgetHidden(actionsPanel, true);
    layoutPanel.setHeight("60px");

    addCloseHandler(event -> delegate.onClose());

    includeNonMenu.addValueChangeHandler(
        event -> {
          includeNonMenu.getElement().setAttribute("checked", Boolean.toString(event.getValue()));
          delegate.nameChanged(nameField.getText(), event.getValue());
        });
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void focusOnInput() {
    nameField.setFocus(true);
  }

  @Override
  public void show() {
    super.show();

    if (nameField.getValue() != null && nameField.getValue().trim().isEmpty()) {
      hideActions();
    }

    Scheduler.get().scheduleDeferred(() -> center());
  }

  @Override
  public String getName() {
    return nameField.getText();
  }

  @Override
  public void showActions(Map<Action, String> actions) {
    this.actions = actions;

    actionsContainer.getElement().setInnerHTML("");

    TableElement itemHolder = Elements.createTableElement();
    itemHolder.setClassName(css.items());
    actionsContainer.getElement().appendChild(((com.google.gwt.dom.client.Element) itemHolder));

    list =
        SimpleList.create(
            (SimpleList.View) actionsContainer.getElement().cast(),
            (Element) actionsContainer.getElement(),
            itemHolder,
            resources.defaultSimpleListCss(),
            listItemRenderer,
            eventDelegate);

    list.render(new ArrayList<>(actions.keySet()));

    if (!actions.isEmpty()) {
      list.getSelectionModel().setSelectedItem(0);
    }

    layoutPanel.setWidgetHidden(actionsPanel, false);
    layoutPanel.setHeight("250px");

    if (isVisible()) {
      Scheduler.get().scheduleDeferred(() -> center());
    }
  }

  @Override
  public void hideActions() {
    actions = new HashMap<>();
    actionsContainer.getElement().setInnerHTML("");

    layoutPanel.setWidgetHidden(actionsPanel, true);
    layoutPanel.setHeight("60px");

    if (isVisible()) {
      Scheduler.get().scheduleDeferred(() -> center());
    }
  }

  @Override
  public boolean getCheckBoxState() {
    return includeNonMenu.getValue();
  }

  @UiHandler("nameField")
  void handleKeyDown(KeyDownEvent event) {
    switch (event.getNativeKeyCode()) {
      case KeyCodes.KEY_UP:
        event.stopPropagation();
        event.preventDefault();
        list.getSelectionModel().selectPrevious();
        break;

      case KeyCodes.KEY_DOWN:
        event.stopPropagation();
        event.preventDefault();
        list.getSelectionModel().selectNext();
        break;

      case KeyCodes.KEY_PAGEUP:
        event.stopPropagation();
        event.preventDefault();
        list.getSelectionModel().selectPreviousPage();
        break;

      case KeyCodes.KEY_PAGEDOWN:
        event.stopPropagation();
        event.preventDefault();
        list.getSelectionModel().selectNextPage();
        break;

      case KeyCodes.KEY_ENTER:
        event.stopPropagation();
        event.preventDefault();
        delegate.onActionSelected(list.getSelectionModel().getSelectedItem());
        break;

      case KeyCodes.KEY_ESCAPE:
        event.stopPropagation();
        event.preventDefault();
        hide();
        break;
      default:
        // here we need some delay to be sure that input box initiated with given value
        // in manually testing hard to reproduce this problem but it reproduced with selenium tests
        new Timer() {
          @Override
          public void run() {
            delegate.nameChanged(nameField.getText(), includeNonMenu.getValue());
          }
        }.schedule(300);
        break;
    }
  }
}

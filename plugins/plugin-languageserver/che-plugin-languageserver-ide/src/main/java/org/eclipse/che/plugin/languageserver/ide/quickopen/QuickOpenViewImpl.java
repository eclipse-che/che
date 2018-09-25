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
package org.eclipse.che.plugin.languageserver.ide.quickopen;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.inject.Inject;
import elemental.dom.Element;
import elemental.html.SpanElement;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.ide.Resources;
import org.eclipse.che.ide.api.editor.codeassist.AutoCompleteResources;
import org.eclipse.che.ide.filters.Match;
import org.eclipse.che.ide.ui.list.SimpleList;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.plugin.languageserver.ide.LanguageServerResources;
import org.eclipse.che.plugin.languageserver.ide.quickopen.QuickOpenEntry.Mode;
import org.vectomatic.dom.svg.ui.SVGImage;

/**
 * @author Evgen Vidolob
 * @author Dmitry Shnurenko
 */
public class QuickOpenViewImpl extends PopupPanel implements QuickOpenView {

  private final AutoCompleteResources.Css css;
  private final LanguageServerResources languageServerResources;
  private final SimpleList.ListItemRenderer<QuickOpenEntry> listItemRenderer =
      new SimpleList.ListItemRenderer<QuickOpenEntry>() {
        @Override
        public void render(Element itemElement, QuickOpenEntry itemData) {
          Element label = Elements.createSpanElement(css.proposalLabel());
          Element icon = Elements.createSpanElement(css.proposalIcon());
          Element group = Elements.createSpanElement(css.proposalGroup());

          SafeHtmlBuilder builder = new SafeHtmlBuilder();
          List<Match> highlights = itemData.getHighlights();
          String text = itemData.getLabel();
          int pos = 0;
          SpanElement spanElement = Elements.createSpanElement();
          for (Match highlight : highlights) {
            if (highlight.getStart() == highlight.getEnd()) {
              continue;
            }

            if (pos < highlight.getStart()) {
              builder.appendHtmlConstant("<span>");
              builder.appendEscaped(text.substring(pos, highlight.getStart()));
              builder.appendHtmlConstant("</span>");
            }

            builder.appendHtmlConstant(
                "<span class=\""
                    + languageServerResources.quickOpenListCss().searchMatch()
                    + "\">");
            builder.appendEscaped(text.substring(highlight.getStart(), highlight.getEnd()));
            builder.appendHtmlConstant("</span>");
            pos = highlight.getEnd();
          }

          if (pos < text.length()) {
            builder.appendHtmlConstant("<span>");
            builder.appendEscaped(text.substring(pos));
            builder.appendHtmlConstant("</span>");
          }
          spanElement.setInnerHTML(builder.toSafeHtml().asString());
          label.getStyle().setPaddingLeft("5px");
          label.getStyle().setPaddingRight("5px");
          if (itemData.getIcon() != null) {
            SVGImage svgImage = new SVGImage(itemData.getIcon());
            icon.appendChild((elemental.dom.Node) svgImage.getElement());
            itemElement.appendChild(icon);
          }
          if (itemData instanceof QuickOpenEntryGroup) {
            QuickOpenEntryGroup entryGroup = (QuickOpenEntryGroup) itemData;
            if (entryGroup.isWithBorder()) {
              Elements.addClassName(
                  languageServerResources.quickOpenListCss().groupSeparator(), itemElement);
            }
            if (entryGroup.getGroupLabel() != null) {
              group.setInnerText(entryGroup.getGroupLabel());
            }
          } else {
            if (itemData.getDescription() != null) {
              group.setInnerText(itemData.getDescription());
            }
          }

          label.appendChild(spanElement);
          itemElement.appendChild(label);
          itemElement.appendChild(group);
        }

        @Override
        public Element createElement() {
          return Elements.createDivElement();
        }
      };
  @UiField TextBox nameField;
  @UiField DockLayoutPanel layoutPanel;
  @UiField FlowPanel actionsPanel;
  @UiField HTML actionsContainer;
  private ActionDelegate delegate;
  private Resources resources;
  private SimpleList<QuickOpenEntry> list;
  private QuickOpenModel model;

  private final SimpleList.ListEventDelegate<QuickOpenEntry> eventDelegate =
      new SimpleList.ListEventDelegate<QuickOpenEntry>() {
        @Override
        public void onListItemClicked(Element listItemBase, QuickOpenEntry itemData) {
          run(itemData, true);
        }

        @Override
        public void onListItemDoubleClicked(Element listItemBase, QuickOpenEntry itemData) {}
      };

  @Inject
  public QuickOpenViewImpl(
      Resources resources,
      AutoCompleteResources autoCompleteResources,
      QuickOpenViewImplUiBinder uiBinder,
      LanguageServerResources languageServerResources) {
    this.resources = resources;
    this.languageServerResources = languageServerResources;

    css = autoCompleteResources.autocompleteComponentCss();
    css.ensureInjected();

    DockLayoutPanel rootElement = uiBinder.createAndBindUi(this);
    setWidget(rootElement);
    setAutoHideEnabled(true);
    setAnimationEnabled(true);

    layoutPanel.setWidgetHidden(actionsPanel, true);

    addCloseHandler(
        new CloseHandler<PopupPanel>() {
          @Override
          public void onClose(CloseEvent<PopupPanel> event) {
            delegate.onClose(event.isAutoClosed());
          }
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
  public void show(final String value) {
    super.show();

    nameField.setValue(value);

    setPopupPositionAndShow(
        new PositionCallback() {
          @Override
          public void setPosition(int offsetWidth, int offsetHeight) {
            setPopupPosition((Window.getClientWidth() / 2) - (offsetWidth / 2), 60);
          }
        });

    Scheduler.get()
        .scheduleDeferred(
            new Scheduler.ScheduledCommand() {
              @Override
              public void execute() {
                delegate.valueChanged(value);
                nameField.setFocus(true);
              }
            });
  }

  @Override
  public void setModel(QuickOpenModel model) {
    this.model = model;
    actionsContainer.getElement().setInnerHTML("");
    Element itemHolder = Elements.createDivElement();
    itemHolder.setClassName(css.items());
    actionsContainer.getElement().appendChild(((com.google.gwt.dom.client.Element) itemHolder));

    list =
        SimpleList.create(
            (SimpleList.View) actionsContainer.getElement().cast(),
            (Element) actionsContainer.getElement(),
            itemHolder,
            languageServerResources.quickOpenListCss(),
            listItemRenderer,
            eventDelegate);

    list.render(new ArrayList<>(model.getEntries()));

    for (int i = 0; i < itemHolder.getChildElementCount(); i++) {
      Element element = (Element) itemHolder.getChildren().item(i);
      element.setId("quick-open-form-action-node-" + i);
    }

    layoutPanel.setWidgetHidden(actionsPanel, false);
    layoutPanel.setHeight("200px");

    if (!nameField.getValue().isEmpty()) {
      list.getSelectionModel().setSelectedItem(0);
      run(list.getSelectionModel().getSelectedItem(), false);
    }
  }

  protected void run(QuickOpenEntry entry, boolean isOpen) {
    if (entry == null) {
      return;
    }
    if (model.run(entry, isOpen ? Mode.OPEN : Mode.PREVIEW)) {
      hide(false);
    }
  }

  @UiHandler("nameField")
  void handleKeyDown(KeyDownEvent event) {
    switch (event.getNativeKeyCode()) {
      case KeyCodes.KEY_UP:
        event.stopPropagation();
        event.preventDefault();
        list.getSelectionModel().selectPrevious();
        run(list.getSelectionModel().getSelectedItem(), false);
        return;

      case KeyCodes.KEY_DOWN:
        event.stopPropagation();
        event.preventDefault();
        list.getSelectionModel().selectNext();
        run(list.getSelectionModel().getSelectedItem(), false);
        return;

      case KeyCodes.KEY_PAGEUP:
        event.stopPropagation();
        event.preventDefault();
        list.getSelectionModel().selectPreviousPage();
        run(list.getSelectionModel().getSelectedItem(), false);
        return;

      case KeyCodes.KEY_PAGEDOWN:
        event.stopPropagation();
        event.preventDefault();
        list.getSelectionModel().selectNextPage();
        run(list.getSelectionModel().getSelectedItem(), false);
        return;

      case KeyCodes.KEY_ENTER:
        event.stopPropagation();
        event.preventDefault();
        run(list.getSelectionModel().getSelectedItem(), true);
        return;

      case KeyCodes.KEY_ESCAPE:
        event.stopPropagation();
        event.preventDefault();
        hide(true);
        return;
    }

    Scheduler.get()
        .scheduleDeferred(
            new Command() {
              @Override
              public void execute() {
                delegate.valueChanged(nameField.getText());
              }
            });
  }

  interface QuickOpenViewImplUiBinder extends UiBinder<DockLayoutPanel, QuickOpenViewImpl> {}
}

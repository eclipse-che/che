/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.git.client.branch;

import static com.google.gwt.event.dom.client.KeyCodes.KEY_BACKSPACE;
import static org.eclipse.che.ide.util.dom.DomUtils.isWidgetOrChildFocused;
import static org.eclipse.che.ide.util.input.SignalEventImpl.getKeyIdentifier;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import elemental.dom.Element;
import elemental.html.TableCellElement;
import elemental.html.TableElement;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.git.shared.Branch;
import org.eclipse.che.ide.FontAwesome;
import org.eclipse.che.ide.ext.git.client.GitLocalizationConstant;
import org.eclipse.che.ide.ext.git.client.GitResources;
import org.eclipse.che.ide.ui.list.FilterableSimpleList;
import org.eclipse.che.ide.ui.list.SimpleList;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.ide.util.dom.Elements;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * The implementation of {@link BranchView}.
 *
 * @author Andrey Plotnikov
 * @author Igor Vinokur
 */
@Singleton
public class BranchViewImpl extends Window implements BranchView {
  interface BranchViewImplUiBinder extends UiBinder<Widget, BranchViewImpl> {}

  private static BranchViewImplUiBinder ourUiBinder = GWT.create(BranchViewImplUiBinder.class);

  Button btnClose;
  Button btnRename;
  Button btnDelete;
  Button btnCreate;
  Button btnCheckout;
  @UiField ScrollPanel branchesPanel;
  @UiField ListBox localRemoteFilter;
  @UiField Label searchFilterLabel;
  @UiField Label searchFilterIcon;

  @UiField(provided = true)
  final GitResources res;

  @UiField(provided = true)
  final GitLocalizationConstant locale;

  private FilterableSimpleList<Branch> branchesList;
  private ActionDelegate delegate;

  @Inject
  protected BranchViewImpl(
      GitResources resources,
      GitLocalizationConstant locale,
      org.eclipse.che.ide.Resources coreRes) {
    this.res = resources;
    this.locale = locale;
    this.ensureDebugId("git-branches-window");

    setTitle(locale.branchTitle());
    setWidget(ourUiBinder.createAndBindUi(this));
    searchFilterIcon.getElement().setInnerHTML(FontAwesome.SEARCH);

    TableElement branchElement = Elements.createTableElement();
    branchElement.setAttribute("style", "width: 100%");
    SimpleList.ListEventDelegate<Branch> listBranchesDelegate =
        new SimpleList.ListEventDelegate<Branch>() {
          public void onListItemClicked(Element itemElement, Branch itemData) {
            branchesList.getSelectionModel().setSelectedItem(itemData);
            delegate.onBranchSelected(itemData);
          }

          public void onListItemDoubleClicked(Element listItemBase, Branch itemData) {}
        };
    SimpleList.ListItemRenderer<Branch> listBranchesRenderer =
        new SimpleList.ListItemRenderer<Branch>() {
          @Override
          public void render(Element itemElement, Branch itemData) {
            TableCellElement label = Elements.createTDElement();

            SafeHtmlBuilder sb = new SafeHtmlBuilder();

            sb.appendHtmlConstant("<table><tr><td>");
            sb.appendHtmlConstant(
                "<div id=\""
                    + UIObject.DEBUG_ID_PREFIX
                    + "git-branches-"
                    + itemData.getDisplayName()
                    + "\">");
            sb.appendEscaped(itemData.getDisplayName());
            sb.appendHtmlConstant("</td>");

            if (itemData.isActive()) {
              SVGResource icon = res.currentBranch();
              sb.appendHtmlConstant("<td><img src=\"" + icon.getSafeUri().asString() + "\"></td>");
            }

            sb.appendHtmlConstant("</tr></table>");

            label.setInnerHTML(sb.toSafeHtml().asString());

            itemElement.appendChild(label);
          }

          @Override
          public Element createElement() {
            return Elements.createTRElement();
          }
        };
    branchesList =
        FilterableSimpleList.create(
            (SimpleList.View) branchElement,
            coreRes.defaultSimpleListCss(),
            listBranchesRenderer,
            listBranchesDelegate,
            this::onFilterChanged);
    this.branchesPanel.add(branchesList);

    this.localRemoteFilter.addItem("All", "all");
    this.localRemoteFilter.addItem("Local", "local");
    this.localRemoteFilter.addItem("Remote", "remote");

    setCloseOnEscape(false);

    createButtons();
  }

  private void onFilterChanged(String filter) {
    if (branchesList.getSelectionModel().getSelectedItem() == null) {
      delegate.onBranchUnselected();
    }
    delegate.onSearchFilterChanged(filter);
  }

  @UiHandler("localRemoteFilter")
  public void onLocalRemoteFilterChanged(ChangeEvent event) {
    delegate.onLocalRemoteFilterChanged();
  }

  private void createButtons() {
    btnClose =
        addFooterButton(locale.buttonClose(), "git-branches-close", event -> delegate.onClose());
    btnRename =
        addFooterButton(
            locale.buttonRename(), "git-branches-rename", event -> delegate.onRenameClicked());
    btnDelete =
        addFooterButton(
            locale.buttonDelete(), "git-branches-delete", event -> delegate.onDeleteClicked());
    btnCreate =
        addFooterButton(
            locale.buttonCreate(), "git-branches-create", event -> delegate.onCreateClicked());
    btnCheckout =
        addFooterButton(
            locale.buttonCheckout(),
            "git-branches-checkout",
            event -> delegate.onCheckoutClicked());
  }

  @Override
  public void onKeyPress(NativeEvent evt) {
    if (evt.getKeyCode() == KEY_BACKSPACE) {
      branchesList.removeLastCharacter();
      return;
    }

    String keyIdentifier = getKeyIdentifier((Event) evt);

    if (keyIdentifier.length() == 1) {
      branchesList.addCharacterToFilter(keyIdentifier);
    }
  }

  @Override
  public void onEscPress(NativeEvent evt) {
    if (branchesList.getFilter().isEmpty()) {
      hide();
    } else {
      branchesList.resetFilter();
    }
  }

  @Override
  public void onEnterPress(NativeEvent evt) {
    if (isWidgetOrChildFocused(btnClose)) {
      delegate.onClose();
    } else if (isWidgetOrChildFocused(btnRename)) {
      delegate.onRenameClicked();
    } else if (isWidgetOrChildFocused(btnDelete)) {
      delegate.onDeleteClicked();
    } else if (isWidgetOrChildFocused(btnCreate)) {
      delegate.onCreateClicked();
    } else if (isWidgetOrChildFocused(btnCheckout)) {
      delegate.onCheckoutClicked();
    }
  }

  @Override
  public void setDelegate(ActionDelegate delegate) {
    this.delegate = delegate;
  }

  @Override
  public void setBranches(@NotNull List<Branch> branches) {
    branchesList.render(
        branches.stream().collect(Collectors.toMap(Branch::getDisplayName, branch -> branch)));
    if (branchesList.getSelectionModel().getSelectedItem() == null) {
      delegate.onBranchUnselected();
    }
  }

  @Override
  public void setEnableDeleteButton(boolean enabled) {
    btnDelete.setEnabled(enabled);
  }

  @Override
  public void setEnableCheckoutButton(boolean enabled) {
    btnCheckout.setEnabled(enabled);
  }

  @Override
  public void setEnableRenameButton(boolean enabled) {
    btnRename.setEnabled(enabled);
  }

  @Override
  public String getFilterValue() {
    return localRemoteFilter.getSelectedValue();
  }

  @Override
  public void closeDialogIfShowing() {
    hide();
    delegate.onClose();
  }

  @Override
  public void showDialogIfClosed() {
    show(btnCreate);
  }

  @Override
  public void setTextToSearchFilterLabel(String filter) {
    searchFilterLabel.setText(filter.isEmpty() ? locale.branchSearchFilterLabel() : filter);
  }

  @Override
  public void clearSearchFilter() {
    branchesList.clearFilter();
  }

  @Override
  public void setFocus() {
    super.focus();
  }
}

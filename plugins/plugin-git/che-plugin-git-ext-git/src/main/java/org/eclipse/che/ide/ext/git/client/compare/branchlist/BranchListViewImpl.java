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
package org.eclipse.che.ide.ext.git.client.compare.branchlist;

import static com.google.gwt.event.dom.client.KeyCodes.KEY_BACKSPACE;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Label;
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
 * The implementation of {@link BranchListView}.
 *
 * @author Igor Vinokur
 */
@Singleton
public class BranchListViewImpl extends Window implements BranchListView {
  interface BranchViewImplUiBinder extends UiBinder<Widget, BranchListViewImpl> {}

  private static BranchViewImplUiBinder uiBinder = GWT.create(BranchViewImplUiBinder.class);

  Button btnClose;
  Button btnCompare;
  @UiField ScrollPanel branchesPanel;
  @UiField Label searchFilterLabel;
  @UiField Label searchFilterIcon;

  @UiField(provided = true)
  final GitResources res;

  @UiField(provided = true)
  GitLocalizationConstant locale;

  private ActionDelegate delegate;
  private final FilterableSimpleList<Branch> branchesList;

  @Inject
  protected BranchListViewImpl(
      GitResources resources,
      GitLocalizationConstant locale,
      org.eclipse.che.ide.Resources coreRes) {
    this.res = resources;
    this.locale = locale;
    this.ensureDebugId("git-compare-branch-window");

    setTitle(locale.compareWithBranchTitle());
    setWidget(uiBinder.createAndBindUi(this));
    searchFilterIcon.getElement().setInnerHTML(FontAwesome.SEARCH);

    TableElement branchElement = Elements.createTableElement();
    branchElement.setAttribute("style", "width: 100%");
    SimpleList.ListEventDelegate<Branch> listBranchesDelegate =
        new SimpleList.ListEventDelegate<Branch>() {
          public void onListItemClicked(Element itemElement, Branch itemData) {
            branchesList.getSelectionModel().setSelectedItem(itemData);
            delegate.onBranchSelected(itemData);
          }

          public void onListItemDoubleClicked(Element listItemBase, Branch itemData) {
            delegate.onCompareClicked();
          }
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
                    + "git-compare-branch-"
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
    branchesPanel.add(branchesList);

    setCloseOnEscape(false);

    createButtons();
  }

  private void onFilterChanged(String filter) {
    if (branchesList.getSelectionModel().getSelectedItem() == null) {
      delegate.onBranchUnselected();
    }
    delegate.onSearchFilterChanged(filter);
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
  public void setEnableCompareButton(boolean enabled) {
    btnCompare.setEnabled(enabled);
  }

  @Override
  public void closeDialogIfShowing() {
    hide();
  }

  @Override
  public void showDialog() {
    show();
  }

  @Override
  public void onKeyPress(NativeEvent evt) {
    if (evt.getKeyCode() == KEY_BACKSPACE) {
      branchesList.removeLastCharacter();
    } else {
      branchesList.addCharacterToFilter(String.valueOf(evt.getCharCode()));
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
  public void updateSearchFilterLabel(String filter) {
    searchFilterLabel.setText(filter.isEmpty() ? locale.branchSearchFilterLabel() : filter);
  }

  @Override
  public void clearSearchFilter() {
    branchesList.clearFilter();
    searchFilterLabel.setText(locale.branchSearchFilterLabel());
  }

  private void createButtons() {
    btnClose =
        addFooterButton(
            locale.buttonClose(), "git-compare-branch-close", event -> delegate.onClose());
    btnCompare =
        addFooterButton(
            locale.buttonCompare(),
            "git-compare-branch-compare",
            event -> delegate.onCompareClicked());
  }
}

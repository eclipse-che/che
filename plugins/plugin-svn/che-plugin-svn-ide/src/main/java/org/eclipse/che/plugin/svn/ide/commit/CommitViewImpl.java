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
package org.eclipse.che.plugin.svn.ide.commit;

import elemental.dom.Element;
import elemental.dom.Node;
import elemental.events.MouseEvent;
import elemental.html.TableCellElement;
import elemental.html.TableElement;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Singleton;

import org.eclipse.che.plugin.svn.ide.SubversionExtensionLocalizationConstants;
import org.eclipse.che.plugin.svn.ide.SubversionExtensionResources;
import org.eclipse.che.plugin.svn.shared.StatusItem;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.list.SimpleList;
import org.eclipse.che.ide.ui.menu.PositionController;
import org.eclipse.che.ide.ui.window.Window;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.ide.util.dom.MouseGestureListener;
import org.vectomatic.dom.svg.OMSVGSVGElement;

import javax.inject.Inject;
import java.util.List;

/**
 * Implementation of {@link CommitView}.
 *
 * @author Vladyslav Zhukovskyi
 */
@Singleton
public class CommitViewImpl extends Window implements CommitView {

    interface CommitViewImplUiBinder extends UiBinder<Widget, CommitViewImpl> {
    }

    private static CommitViewImplUiBinder uiBinder = GWT.create(CommitViewImplUiBinder.class);

    private ActionDelegate delegate;

    @UiField(provided = true)
    SubversionExtensionLocalizationConstants locale;

    @UiField(provided = true)
    SubversionExtensionResources resources;

    @UiField
    RadioButton commitAll;

    @UiField
    RadioButton commitSelection;

    @UiField
    CheckBox keepLocks;

    @UiField
    TextArea message;

    @UiField
    ScrollPanel changesWrapper;

    @UiField
    Label changedFilesCount;

    private SimpleList<StatusItem> changesList;

    private Button          btnCommit;
    private OMSVGSVGElement alertMarker;

    private static final String PLACEHOLDER = "placeholder";

    private static final String ADDED      = org.eclipse.che.ide.api.theme.Style.getVcsConsoleStagedFilesColor();
    private static final String CONFLICTED = org.eclipse.che.ide.api.theme.Style.getVcsConsoleErrorColor();
    private static final String DELETED    = org.eclipse.che.ide.api.theme.Style.getVcsConsoleErrorColor();
    private static final String MODIFIED   = org.eclipse.che.ide.api.theme.Style.getVcsConsoleModifiedFilesColor();
    private static final String REPLACED   = org.eclipse.che.ide.api.theme.Style.getVcsConsoleModifiedFilesColor();
    private static final String DEFAULT    = org.eclipse.che.ide.api.theme.Style.getMainFontColor();

    @Inject
    public CommitViewImpl(final SubversionExtensionLocalizationConstants constants,
                          final SubversionExtensionResources resources,
                          final Window.Resources windowResources,
                          org.eclipse.che.ide.Resources coreRes) {
        this.locale = constants;
        this.resources = resources;

        this.setTitle(locale.commitTitle());
        this.setWidget(uiBinder.createAndBindUi(this));

        Button btnCancel = createButton(locale.buttonCancel(), "svn-commit-cancel", new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                delegate.onCancelClicked();
            }
        });

        btnCommit = createButton(locale.buttonCommit(), "svn-commit-commit", new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                delegate.onCommitClicked();
            }
        });

        addButtonToFooter(btnCancel);
        addButtonToFooter(btnCommit);

        btnCommit.setEnabled(false);

        SimpleList.ListEventDelegate<StatusItem> listChangesDelegate = new SimpleList.ListEventDelegate<StatusItem>() {
            /** {@inheritDoc} */
            @Override
            public void onListItemClicked(Element listItemBase, StatusItem itemData) {
                //stub
            }

            /** {@inheritDoc} */
            @Override
            public void onListItemDoubleClicked(Element listItemBase, StatusItem itemData) {
                //stub
            }
        };

        TableElement changesElement = Elements.createTableElement();
        changesElement.setAttribute("style", "width: 100%; background: none;");
        changesElement.setCellSpacing("1px");
        changesList = SimpleList.create((SimpleList.View)changesElement, coreRes.defaultSimpleListCss(), new ChangesListRenderer(),
                                        listChangesDelegate);

        changesWrapper.add(changesList);

        message.getElement().setAttribute(PLACEHOLDER, locale.commitPlaceholder());

        alertMarker = resources.alert().getSvg();
        alertMarker.getStyle().setWidth(22, Style.Unit.PX);
        alertMarker.getStyle().setHeight(22, Style.Unit.PX);
        alertMarker.getStyle().setMarginTop(5, Style.Unit.PX);
        getFooter().getElement().appendChild(alertMarker.getElement());

        Tooltip.create((elemental.dom.Element)alertMarker.getElement(),
                       PositionController.VerticalAlign.TOP,
                       PositionController.HorizontalAlign.MIDDLE,
                       locale.commitMessageEmpty());

        alertMarker.getStyle().setVisibility(Style.Visibility.VISIBLE);
    }

    /** {@inheritDoc} */
    @Override
    public void onClose() {
        hide();
    }

    /** {@inheritDoc} */
    @Override
    public void setDelegate(final ActionDelegate delegate) {
        this.delegate = delegate;
    }

    /** {@inheritDoc} */
    @Override
    public void onShow() {
        commitAll.setValue(true);
        commitSelection.setValue(false);
        show();
    }

    /** {@inheritDoc} */
    @Override
    public String getMessage() {
        return this.message.getText();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isKeepLocksStateSelected() {
        return keepLocks.getValue();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCommitAllSelected() {
        return commitAll.getValue();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCommitSelectionSelected() {
        return commitSelection.getValue();
    }

    /** {@inheritDoc} */
    @Override
    public void setChangesList(List<StatusItem> changes) {
        changesList.render(changes);
        changedFilesCount.setText(String.valueOf(changes.size()));
    }

    @UiHandler("message")
    @SuppressWarnings("unused")
    public void onMessageChanged(KeyUpEvent event) {
        btnCommit.setEnabled(!message.getText().isEmpty());

        alertMarker.getStyle().setVisibility(!message.getText().isEmpty() ? Style.Visibility.HIDDEN : Style.Visibility.VISIBLE);
    }

    @UiHandler({"commitAll", "commitSelection"})
    @SuppressWarnings("unused")
    public void onCommitModeChanged(ClickEvent event) {
        delegate.onCommitModeChanged();
    }

    private class ChangesListRenderer extends SimpleList.ListItemRenderer<StatusItem> {

        /** {@inheritDoc} */
        @Override
        public void render(Element listItemBase, StatusItem itemData) {
            Node changedItemStatusNote = getChangedItemStatus(itemData);
            Node changedItemPath = getChangedItemPath(itemData);
            Node changedItemDiffLink = getChangedItemDiffLink(itemData);

            listItemBase.appendChild(changedItemStatusNote);
            listItemBase.appendChild(changedItemPath);
            listItemBase.appendChild(changedItemDiffLink);
        }

        /** {@inheritDoc} */
        @Override
        public Element createElement() {
            return Elements.createTRElement();
        }

        private Node getChangedItemStatus(StatusItem item) {
            TableCellElement htmlNode = Elements.createTDElement();
            htmlNode.setInnerText(item.getFileState().getValue());
            htmlNode.setWidth("16px");
            htmlNode.setAlign("center");
            htmlNode.getStyle().setPaddingTop("3px");
            htmlNode.getStyle().setFontSize("11px");
            return htmlNode;
        }

        private Node getChangedItemPath(StatusItem item) {
            final String[] pathElements = item.getPath().split("/");

            final StringBuilder sb = new StringBuilder();
            //append file name
            sb.append("<span style=\"color:")
              .append(getChangedItemColor(item))
              .append("\">")
              .append(new SafeHtmlBuilder().appendEscaped(pathElements[pathElements.length - 1]).toSafeHtml().asString())
              .append("</span>");

            //append path
            if (pathElements.length > 1) {
                String rawPath = item.getPath().substring(0, item.getPath().length() - pathElements[pathElements.length - 1].length() - 1);
                String escapedPath = new SafeHtmlBuilder().appendEscaped(rawPath).toSafeHtml().asString();

                sb.append(" <span>(");
                sb.append(escapedPath);
                sb.append(")</span>");
            }

            final String html = sb.toString();
            TableCellElement htmlNode = Elements.createTDElement();
            htmlNode.setInnerHTML(html);
            htmlNode.getStyle().setProperty("max-width", "200px");
            htmlNode.getStyle().setProperty("text-overflow", "ellipsis");

            return htmlNode;
        }

        private Node getChangedItemDiffLink(final StatusItem item) {

            Button showDiff = new Button();
            showDiff.setText("Diff");

            TableCellElement htmlNode = Elements.createTDElement();
            htmlNode.appendChild((Element)showDiff.getElement());
            htmlNode.setWidth("1px");

            if (!(item.getFileState() == StatusItem.FileState.MODIFIED
                  || item.getFileState() == StatusItem.FileState.DELETED
                  || item.getFileState() == StatusItem.FileState.CONFLICTED)) {
                showDiff.setEnabled(false);

                Tooltip.create((Element)showDiff.getElement(),
                               PositionController.VerticalAlign.MIDDLE,
                               PositionController.HorizontalAlign.LEFT,
                               locale.commitDiffUnavailable());
            }

            MouseGestureListener.createAndAttach((Element)showDiff.getElement(), new MouseGestureListener.Callback() {
                /** {@inheritDoc} */
                @Override
                public boolean onClick(int clickCount, MouseEvent event) {
                    delegate.showDiff(item.getPath());
                    return false;
                }

                /** {@inheritDoc} */
                @Override
                public void onDrag(MouseEvent event) {
                    //stub
                }

                /** {@inheritDoc} */
                @Override
                public void onDragRelease(MouseEvent event) {
                    //stub
                }
            });

            return htmlNode;
        }

        private String getChangedItemColor(StatusItem item) {
            switch (item.getFileState()) {
                case ADDED:
                    return ADDED;
                case CONFLICTED:
                    return CONFLICTED;
                case DELETED:
                    return DELETED;
                case MODIFIED:
                    return MODIFIED;
                case REPLACED:
                    return REPLACED;
                default:
                    return DEFAULT;
            }
        }
    }
}

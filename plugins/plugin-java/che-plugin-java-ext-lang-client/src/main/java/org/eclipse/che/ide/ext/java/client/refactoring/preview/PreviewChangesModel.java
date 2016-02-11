/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.client.refactoring.preview;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.CompositeCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.HasCell;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.TreeViewModel;

import org.eclipse.che.ide.ext.java.shared.dto.refactoring.RefactoringPreview;

import java.util.ArrayList;
import java.util.List;

/**
 * A model of a tree which contains all possible refactoring changes.
 *
 * @author Valeriy Svydenko
 */
public class PreviewChangesModel implements TreeViewModel {
    private final RefactoringPreview                 changes;
    private final SelectionModel<RefactoringPreview> selectionModel;
    private final Cell<RefactoringPreview>           refactoringPreviewCell;

    public PreviewChangesModel(RefactoringPreview changes,
                               final SelectionModel<RefactoringPreview> selectionModel,
                               final PreviewView.ActionDelegate delegate) {
        this.changes = changes;
        this.selectionModel = selectionModel;

        List<HasCell<RefactoringPreview, ?>> hasCells = new ArrayList<>();

        hasCells.add(new HasCell<RefactoringPreview, Boolean>() {

            private CheckboxCell cell = new CheckboxCell(false, false);

            @Override
            public Cell<Boolean> getCell() {
                return cell;
            }

            @Override
            public FieldUpdater<RefactoringPreview, Boolean> getFieldUpdater() {
                return new FieldUpdater<RefactoringPreview, Boolean>() {
                    @Override
                    public void update(int index, RefactoringPreview object, Boolean value) {
                        object.setEnabled(value);
                        delegate.onEnabledStateChanged(object);
                    }
                };
            }

            @Override
            public Boolean getValue(RefactoringPreview object) {
                return object.isEnabled();
            }

        });

        hasCells.add(new HasCell<RefactoringPreview, RefactoringPreview>() {

            private RefactoringPreviewCell cell = new RefactoringPreviewCell();

            @Override
            public Cell<RefactoringPreview> getCell() {
                return cell;
            }

            @Override
            public FieldUpdater<RefactoringPreview, RefactoringPreview> getFieldUpdater() {
                return null;
            }

            @Override
            public RefactoringPreview getValue(RefactoringPreview object) {
                return object;
            }
        });

        refactoringPreviewCell = new CompositeCell<RefactoringPreview>(hasCells) {
            @Override
            public void render(Context context, RefactoringPreview value, SafeHtmlBuilder sb) {
                sb.appendHtmlConstant("<div style=\"display: inline-flex\">");
                super.render(context, value, sb);
                sb.appendHtmlConstant("</div>");
            }

            @Override
            protected Element getContainerElement(Element parent) {
                return parent.getFirstChildElement();
            }

            @Override
            protected <X> void render(Context context,
                                      RefactoringPreview value,
                                      SafeHtmlBuilder sb,
                                      HasCell<RefactoringPreview, X> hasCell) {
                Cell<X> cell = hasCell.getCell();
                sb.appendHtmlConstant("<div style=\"display: flex; align-items: center;\">");
                cell.render(context, hasCell.getValue(value), sb);
                sb.appendHtmlConstant("</div>");
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public <T> NodeInfo<?> getNodeInfo(T value) {
        if (value == null) {
            return new DefaultNodeInfo<>(new ListDataProvider<>(changes.getChildrens()),
                                         refactoringPreviewCell,
                                         selectionModel,
                                         null);
        }

        return new DefaultNodeInfo<>(new ListDataProvider<>(((RefactoringPreview)value).getChildrens()),
                                     refactoringPreviewCell,
                                     selectionModel,
                                     null);
    }

    /** {@inheritDoc} */
    @Override
    public boolean isLeaf(Object value) {
        return value instanceof RefactoringPreview && ((RefactoringPreview)value).getChildrens().isEmpty();
    }

    /**Class for rendering the information about {@link RefactoringPreview}*/
    private class RefactoringPreviewCell extends AbstractCell<RefactoringPreview> {
        @Override
        public void render(Context context, RefactoringPreview value, SafeHtmlBuilder sb) {
            //TODO add an respective image for current change by value.getImage()
            sb.appendEscaped(" ").appendEscaped(value.getText());
        }
    }
}
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
package org.eclipse.che.ide.part.editor.multipart;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.parts.PartStack;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;
import java.util.HashMap;
import java.util.Map;

import static com.google.gwt.dom.client.Style.Unit.PCT;

/**
 * @author Roman Nikitenko
 */
public class EditorMultiPartStackViewImpl extends ResizeComposite implements EditorMultiPartStackView {
    private static final PartStackUiBinder UI_BINDER = GWT.create(PartStackUiBinder.class);

    @UiField
    DockLayoutPanel  root;
    @UiField
    SplitLayoutPanel contentPanel;

    private final Map<PartStack, SplitEditorPartView> splitEditorParts;
    private final SplitEditorPartFactory              splitEditorPartFactory;

    @Inject
    public EditorMultiPartStackViewImpl(SplitEditorPartFactory splitEditorPartFactory) {
        this.splitEditorPartFactory = splitEditorPartFactory;
        this.splitEditorParts = new HashMap<>();

        initWidget(UI_BINDER.createAndBindUi(this));
    }

    @Override
    public void addPartStack(@NotNull final PartStack partStack, final PartStack relativePartStack, final Constraints constraints) {
        AcceptsOneWidget partViewContainer = new AcceptsOneWidget() {
            @Override
            public void setWidget(IsWidget widget) {
                if (relativePartStack == null) {
                    SplitEditorPartView splitEditorPartView = splitEditorPartFactory.create(widget);
                    splitEditorParts.put(partStack, splitEditorPartView);
                    contentPanel.add(splitEditorPartView);
                    return;
                }

                SplitEditorPartView relativePartStackView = splitEditorParts.get(relativePartStack);
                if (relativePartStackView == null) {
                    Log.error(getClass(), "Can not find container for specified editor");
                    return;
                }


                relativePartStackView.split(widget, constraints.direction);
                splitEditorParts.put(partStack, relativePartStackView.getReplica());
                splitEditorParts.put(relativePartStack, relativePartStackView.getSpecimen());
            }
        };
        partStack.go(partViewContainer);
    }

    @Override
    public void removePartStack(@NotNull PartStack partStack) {
        SplitEditorPartView splitEditorPartView = splitEditorParts.remove(partStack);
        if (splitEditorPartView != null) {
            splitEditorPartView.removeFromParent();
        }
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        Style style = getElement().getParentElement().getStyle();
        style.setHeight(100, PCT);
        style.setWidth(100, PCT);
    }

    interface PartStackUiBinder extends UiBinder<Widget, EditorMultiPartStackViewImpl> {
    }
}

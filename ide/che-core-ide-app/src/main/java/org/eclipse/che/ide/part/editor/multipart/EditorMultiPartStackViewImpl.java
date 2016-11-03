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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.LayoutPanel;
import com.google.gwt.user.client.ui.ResizeComposite;
import com.google.inject.Inject;

import org.eclipse.che.ide.api.constraints.Constraints;
import org.eclipse.che.ide.api.parts.EditorMultiPartStackState;
import org.eclipse.che.ide.api.parts.EditorPartStack;
import org.eclipse.che.ide.util.loging.Log;

import javax.validation.constraints.NotNull;

import static com.google.gwt.dom.client.Style.Unit.PCT;

/**
 * @author Roman Nikitenko
 */
public class EditorMultiPartStackViewImpl extends ResizeComposite implements EditorMultiPartStackView{

    private LayoutPanel contentPanel;

    private final BiMap<EditorPartStack, SplitEditorPartView> splitEditorParts;
    private final SplitEditorPartViewFactory                  splitEditorPartViewFactory;

    private SplitEditorPartView rootView;

    @Inject
    public EditorMultiPartStackViewImpl(SplitEditorPartViewFactory splitEditorPartViewFactory) {
        this.splitEditorPartViewFactory = splitEditorPartViewFactory;
        this.splitEditorParts = HashBiMap.create();

        contentPanel = new LayoutPanel();
        contentPanel.setSize("100%", "100%");
        contentPanel.ensureDebugId("editorMultiPartStack-contentPanel");
        initWidget(contentPanel);
    }

    @Override
    public void addPartStack(@NotNull final EditorPartStack partStack, final EditorPartStack relativePartStack,
                             final Constraints constraints, final double size) {
        AcceptsOneWidget partViewContainer = new AcceptsOneWidget() {
            @Override
            public void setWidget(IsWidget widget) {
                if (relativePartStack == null) {
                    rootView = splitEditorPartViewFactory.create(widget);
                    splitEditorParts.put(partStack, rootView);
                    contentPanel.add(rootView);
                    return;
                }

                SplitEditorPartView relativePartStackView = splitEditorParts.get(relativePartStack);
                if (relativePartStackView == null) {
                    Log.error(getClass(), "Can not find container for specified editor");
                    return;
                }


                relativePartStackView.split(widget, constraints.direction, size);
                splitEditorParts.put(partStack, relativePartStackView.getReplica());
                splitEditorParts.put(relativePartStack, relativePartStackView.getSpecimen());
            }
        };
        partStack.go(partViewContainer);
    }

    @Override
    public void removePartStack(@NotNull EditorPartStack partStack) {
        SplitEditorPartView splitEditorPartView = splitEditorParts.remove(partStack);
        if (splitEditorPartView != null) {
            splitEditorPartView.removeFromParent();
        }
    }

    @Override
    public EditorMultiPartStackState getState() {
        if (rootView == null) {
            return null;
        }
        return rootView.getState(splitEditorParts.inverse());
    }

    @Override
    protected void onAttach() {
        super.onAttach();

        Style style = getElement().getParentElement().getStyle();
        style.setHeight(100, PCT);
        style.setWidth(100, PCT);
    }

}

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
package org.eclipse.che.plugin.svn.ide.property;

import java.util.List;

import org.eclipse.che.ide.api.mvp.View;
import org.eclipse.che.ide.api.parts.base.BaseActionDelegate;
import org.eclipse.che.plugin.svn.shared.Depth;

/**
 * View for {@link PropertyEditorPresenter}.
 *
 * @author Vladyslav Zhukovskyi
 * @author Stephane Tournie
 */
public interface PropertyEditorView extends View<PropertyEditorView.ActionDelegate> {

    interface ActionDelegate {

        void onCancelClicked();

        void onOkClicked();

        void onPropertyNameChanged(String propertyName);

        void obtainExistingPropertiesForPath();

    }

    /** Perform actions when close window performed. */
    void onClose();

    /** Perform actions when open window performed. */
    void onShow();

    /** Return selected user's property. */
    String getSelectedProperty();

    /** Get property depth. */
    Depth getDepth();

    /** Get property value. */
    String getPropertyValue();

    /** Return true if user selected property edit. */
    boolean isEditPropertySelected();

    /** Return true if user selected property delete. */
    boolean isDeletePropertySelected();

    /** Return true if user selected forcing. */
    boolean isForceSelected();

    void setPropertyCurrentValue(List<String> values);

    void setExistingPropertiesForPath(List<String> properties);
}

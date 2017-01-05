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
package org.eclipse.che.ide.ui.toolbar;

import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;

/**
 * @author Evgen Vidolob
 */
public interface ToolbarResources extends ClientBundle {

    interface Css extends CssResource {

        String iconButtonPanel();

        String iconButtonIcon();

        String iconButtonIconInner();

        String popupButtonIconInner();

        String toolbarPanel();

        String iconButtonPanelDown();

        String iconButtonPanelSelectedDown();

        String toolbarDelimiter();

        String toolbarActionGroupPanel();

        String iconButtonPanelSelected();

        String disabled();

        String popupButtonPanel();

        String popupButtonPanelDown();

        String popupButtonIcon();

        String tooltip();

        String tooltipBody();

        String tooltipArrow();

        String caret();

        String leftToolbarPart();

        String centerToolbarPart();

        String rightToolbarPart();
    }

    @Source({"toolbar.css", "org/eclipse/che/ide/api/ui/style.css"})
    Css toolbar();
}

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
package org.eclipse.che.ide.editor.codeassist;

import elemental.dom.Element;

import org.eclipse.che.ide.ui.popup.PopupResources;
import org.eclipse.che.ide.ui.popup.PopupWidget;

public class AdditionalInformationWidget extends PopupWidget<Element> {

    public AdditionalInformationWidget(PopupResources popupResources) {
        super(popupResources, "Proposals:");
    }

    @Override
    public String getEmptyMessage() {
        return "No information available";
    }

    @Override
    public Element createItem(final Element itemModel) {
        return itemModel;
    }
}

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
package org.eclipse.che.ide.ui.smartTree.presentation;

import com.google.gwt.dom.client.Element;

import org.eclipse.che.ide.util.Pair;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * Node presentation.
 *
 * @author Vlad Zhukovskiy
 */
public class NodePresentation {
    private String               presentableText;
    private String               presentableTextCss;
    private String               infoText;
    private String               infoTextCss;
    private Pair<String, String> infoTextWrapper;
    private SVGResource          presentableIcon;
    private Element              userElement;

    public NodePresentation(String presentableText, String infoText,
                            Pair<String, String> infoTextWrapper, SVGResource presentableIcon) {
        this.presentableText = presentableText;
        this.infoText = infoText;
        this.infoTextWrapper = infoTextWrapper;
        this.presentableIcon = presentableIcon;
    }

    public NodePresentation() {
    }

    public String getPresentableText() {
        return presentableText;
    }

    public void setPresentableText(String presentableText) {
        this.presentableText = presentableText;
    }

    public String getInfoText() {
        return infoText;
    }

    public void setInfoText(String infoText) {
        this.infoText = infoText;
    }

    public Pair<String, String> getInfoTextWrapper() {
        return infoTextWrapper;
    }

    public void setInfoTextWrapper(Pair<String, String> infoTextWrapper) {
        this.infoTextWrapper = infoTextWrapper;
    }

    public SVGResource getPresentableIcon() {
        return presentableIcon;
    }

    public void setPresentableIcon(SVGResource presentableIcon) {
        this.presentableIcon = presentableIcon;
    }

    public Element getUserElement() {
        return userElement;
    }

    public void setUserElement(Element userElement) {
        this.userElement = userElement;
    }

    public String getPresentableTextCss() {
        return presentableTextCss;
    }

    public void setPresentableTextCss(String presentableTextCss) {
        this.presentableTextCss = presentableTextCss;
    }

    public String getInfoTextCss() {
        return infoTextCss;
    }

    public void setInfoTextCss(String infoTextCss) {
        this.infoTextCss = infoTextCss;
    }
}

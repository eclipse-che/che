// Copyright 2012 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.eclipse.che.ide.util.dom;

import elemental.css.CSSStyleDeclaration;
import elemental.dom.Element;


/**
 * A class that computes and caches the width of a vertical scrollbar and height
 * of a horizontal scrollbar.
 */
public class ScrollbarSizeCalculator {

    public static final ScrollbarSizeCalculator INSTANCE = new ScrollbarSizeCalculator();

    private int heightOfHorizontalScrollbar = -1;

    private int widthOfVerticalScrollbar = -1;

    /** Calculates (or recalculates) the sizes of the scrollbars. */
    public void calculateSize() {
        Element container = createContainer();

        // No scrollbars
        container.getStyle().setOverflow(CSSStyleDeclaration.Overflow.HIDDEN);
        int noScrollbarClientHeight = container.getClientHeight();
        int noScrollbarClientWidth = container.getClientWidth();

        // Force scrollbars
        container.getStyle().setOverflow(CSSStyleDeclaration.Overflow.SCROLL);
        heightOfHorizontalScrollbar = noScrollbarClientHeight - container.getClientHeight();
        widthOfVerticalScrollbar = noScrollbarClientWidth - container.getClientWidth();

        DomUtils.removeFromParent(container);
    }

    private Element createContainer() {
        Element container = Elements.createDivElement();

        final int containerSize = 500;
        CSSStyleDeclaration containerStyle = container.getStyle();
        containerStyle.setWidth(containerSize, CSSStyleDeclaration.Unit.PX);
        containerStyle.setHeight(containerSize, CSSStyleDeclaration.Unit.PX);
        containerStyle.setPosition(CSSStyleDeclaration.Position.ABSOLUTE);
        containerStyle.setLeft(-containerSize, CSSStyleDeclaration.Unit.PX);
        containerStyle.setTop(-containerSize, CSSStyleDeclaration.Unit.PX);

        Elements.getBody().appendChild(container);

        return container;
    }

    /**
     * Gets the height of a horizontal scrollbar. This will calculate the size if
     * it has not already been calculated.
     */
    public int getHeightOfHorizontalScrollbar() {
        ensureSizeCalculated();
        return heightOfHorizontalScrollbar;
    }

    /**
     * Gets the width of a vertical scrollbar. This will calculate the size if it
     * has not already been calculated.
     */
    public int getWidthOfVerticalScrollbar() {
        ensureSizeCalculated();
        return widthOfVerticalScrollbar;
    }

    private void ensureSizeCalculated() {
        if (heightOfHorizontalScrollbar < 0 || widthOfVerticalScrollbar < 0) {
            calculateSize();
        }
    }
}

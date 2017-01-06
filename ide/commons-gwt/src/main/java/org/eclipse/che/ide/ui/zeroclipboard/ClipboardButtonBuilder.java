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
package org.eclipse.che.ide.ui.zeroclipboard;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;

import org.vectomatic.dom.svg.ui.SVGImage;

import javax.validation.constraints.NotNull;

/**
 * Simple clipboard button builder which adds to the widget "copy to clipboard" button.
 *
 * @author Oleksii Orel
 */
public interface ClipboardButtonBuilder {

    /**
     * Return builder.
     *
     * @param resourceWidget
     *         data resource widget
     *
     */
    ClipboardButtonBuilder withResourceWidget(@NotNull Widget resourceWidget);

    /**
     * Return builder.
     *
     * @param parentWidget
     *         parent widget for clipboard button
     *
     */
    ClipboardButtonBuilder withParentWidget(@NotNull Widget parentWidget);

    /**
     * Return builder.
     *
     * @param svgImage
     *         image for copy to clipboard button
     */
    ClipboardButtonBuilder withSvgImage(@NotNull SVGImage svgImage);


    /**
     * Return builder.
     *
     * @param mimeType
     *         mime type for copy to clipboard content
     */
    ClipboardButtonBuilder withMimeType(@NotNull String mimeType);


    /**
     * Return builder.
     *
     * @param promptReadyToCopy
     *         ready to copy prompt
     */
    ClipboardButtonBuilder withPromptReadyToCopy(@NotNull String promptReadyToCopy);


    /**
     * Return builder.
     *
     * @param promptAfterCopy
     *         after copy prompt
     */
    ClipboardButtonBuilder withPromptAfterCopy(@NotNull String promptAfterCopy);


    /**
     * Return builder.
     *
     * @param promptCopyError
     *         copy to clipboard error prompt
     */
    ClipboardButtonBuilder withPromptCopyError(@NotNull String promptCopyError);


    /**
     * Return builder.
     *
     * @param promptReadyToSelect
     *          ready to select prompt
     */
    ClipboardButtonBuilder  withPromptReadyToSelect(@NotNull String promptReadyToSelect);


    /**
     * Return Element of the built clipboard button. Adds the Element to the parent Widget.
     * If the parent is not defined - adds Element to the parent of resource Widget
     */
    Element build();

}

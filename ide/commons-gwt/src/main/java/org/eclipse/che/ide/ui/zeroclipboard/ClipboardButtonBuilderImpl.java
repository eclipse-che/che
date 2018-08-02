/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ui.zeroclipboard;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import javax.validation.constraints.NotNull;
import org.eclipse.che.ide.MimeType;
import org.vectomatic.dom.svg.ui.SVGImage;

/**
 * Implementation of ClipboardButtonBuilder is able to create "copy to clipboard" button.
 *
 * @author Oleksii Orel
 * @author Kevin Pollet
 */
public class ClipboardButtonBuilderImpl implements ClipboardButtonBuilder {

  private final ClipboardResources res;
  private Widget resourceWidget;
  private Widget parentWidget;
  private SVGImage svgImage;
  private String mimeType;
  private String promptReadyToCopy;
  private String promptAfterCopy;
  private String promptCopyError;
  private String promptReadyToSelect;

  @Inject
  public ClipboardButtonBuilderImpl(ClipboardResources res, ClipboardConstant locale) {
    this.res = res;
    promptReadyToCopy = locale.promptReadyToCopy();
    promptAfterCopy = locale.promptAfterCopy();
    promptCopyError = locale.promptCopyError();
    promptReadyToSelect = locale.promptReadyToSelect();
    mimeType = MimeType.TEXT_PLAIN;
  }

  @Override
  public ClipboardButtonBuilder withResourceWidget(Widget resourceWidget) {
    this.resourceWidget = resourceWidget;
    return this;
  }

  @Override
  public ClipboardButtonBuilder withParentWidget(Widget parentWidget) {
    this.parentWidget = parentWidget;
    return this;
  }

  @Override
  public ClipboardButtonBuilder withSvgImage(@NotNull SVGImage svgImage) {
    this.svgImage = svgImage;
    return this;
  }

  @Override
  public ClipboardButtonBuilder withMimeType(@NotNull String mimeType) {
    this.mimeType = mimeType;
    return this;
  }

  @Override
  public ClipboardButtonBuilder withPromptReadyToCopy(@NotNull String promptReadyToCopy) {
    this.promptReadyToCopy = promptReadyToCopy;
    return this;
  }

  @Override
  public ClipboardButtonBuilder withPromptAfterCopy(@NotNull String promptAfterCopy) {
    this.promptAfterCopy = promptAfterCopy;
    return this;
  }

  @Override
  public ClipboardButtonBuilder withPromptCopyError(@NotNull String promptCopyError) {
    this.promptCopyError = promptCopyError;
    return this;
  }

  @Override
  public ClipboardButtonBuilder withPromptReadyToSelect(@NotNull String promptReadyToSelect) {
    this.promptReadyToSelect = promptReadyToSelect;
    return this;
  }

  @Override
  public Element build() {
    Element button = null;
    if (resourceWidget != null) {
      Element buttonImage =
          svgImage != null ? svgImage.getElement() : new SVGImage(res.clipboard()).getElement();
      button =
          buildCopyToClipboardButton(
              resourceWidget.getElement(),
              buttonImage,
              res.clipboardCss().clipboardButton(),
              mimeType,
              promptReadyToCopy,
              promptAfterCopy,
              promptCopyError,
              promptReadyToSelect);
      append(button);
    }
    return button;
  }

  /**
   * Append to parentWidget as a child element.
   *
   * @param element
   */
  private void append(Element element) {
    if (parentWidget == null && (resourceWidget == null || resourceWidget.getParent() == null)) {
      return;
    }
    Widget parent = parentWidget != null ? parentWidget : resourceWidget.getParent();
    parent.getElement().appendChild(element);
  }

  /**
   * Build ZeroClipboard button.
   *
   * @param textBox
   * @param image
   * @param className
   * @param readyCopyPrompt
   * @param afterCopyPrompt
   * @param copyErrorPrompt
   * @param readySelectPrompt
   */
  private native Element buildCopyToClipboardButton(
      Element textBox,
      Element image,
      String className,
      String mimeType,
      String readyCopyPrompt,
      String afterCopyPrompt,
      String copyErrorPrompt,
      String readySelectPrompt) /*-{
        var button = document.createElement('div');
        var tooltip = document.createElement('span');
        var isCopySupported = document.queryCommandSupported('copy');
        button.appendChild(image);
        button.appendChild(tooltip);
        button.setAttribute('class', className);
        tooltip.innerHTML = isCopySupported ? readyCopyPrompt : readySelectPrompt;
        button.onclick = function () {
        if (typeof textBox.select !== 'undefined') {
          textBox.select();
        } else {
          var range = document.createRange();
          range.selectNodeContents(textBox);
          $wnd.getSelection().removeAllRanges();
          $wnd.getSelection().addRange(range);
        }
        if (!isCopySupported) {
          return;
        }
        try {
          if ($wnd.document.execCommand('copy')) {
            $wnd.getSelection().removeAllRanges();
            tooltip.innerHTML = afterCopyPrompt;
          }
        } catch (error) {
          console.log('Error. ' + error);
          tooltip.innerHTML = copyErrorPrompt;
        }
        setTimeout(function () {
          tooltip.innerHTML = readyCopyPrompt;
        }, 2000);
      };
      return button;
    }-*/;
}

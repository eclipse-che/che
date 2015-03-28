/*
        * Copyright 2008 Google Inc.
        *
        * Licensed under the Apache License, Version 2.0 (the "License"); you may not
        * use this file except in compliance with the License. You may obtain a copy of
        * the License at
        *
        * http://www.apache.org/licenses/LICENSE-2.0
        *
        * Unless required by applicable law or agreed to in writing, software
        * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
        * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
        * License for the specific language governing permissions and limitations under
        * the License.
        */
package com.codenvy.ide.tutorial.wysiwyg.editor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.ToggleButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * A sample toolbar for use with {@link RichTextArea}. It provides a simple UI
 * for all rich text formatting, dynamically displayed only for the available
 * functionality.
 */
@SuppressWarnings("deprecation")
public class RichTextToolbar extends Composite {

    private static final RichTextArea.FontSize[] fontSizesConstants = new RichTextArea.FontSize[]{
            RichTextArea.FontSize.XX_SMALL, RichTextArea.FontSize.X_SMALL,
            RichTextArea.FontSize.SMALL, RichTextArea.FontSize.MEDIUM,
            RichTextArea.FontSize.LARGE, RichTextArea.FontSize.X_LARGE,
            RichTextArea.FontSize.XX_LARGE};
    private              Images                  images             = (Images)GWT.create(Images.class);
    private              Strings                 strings            = (Strings)GWT.create(Strings.class);
    private              EventHandler            handler            = new EventHandler();
    private RichTextArea                   richText;
    private RichTextArea.BasicFormatter    basic;
    private RichTextArea.ExtendedFormatter extended;
    private VerticalPanel   outer       = new VerticalPanel();
    private HorizontalPanel topPanel    = new HorizontalPanel();
    private HorizontalPanel bottomPanel = new HorizontalPanel();
    private ToggleButton bold;
    private ToggleButton italic;
    private ToggleButton underline;
    private ToggleButton subscript;
    private ToggleButton superscript;
    private ToggleButton strikethrough;
    private PushButton   indent;
    private PushButton   outdent;
    private PushButton   justifyLeft;
    private PushButton   justifyCenter;
    private PushButton   justifyRight;
    private PushButton   hr;
    private PushButton   ol;
    private PushButton   ul;
    private PushButton   insertImage;
    private PushButton   createLink;
    private PushButton   removeLink;
    private PushButton   removeFormat;
    private ListBox      backColors;
    private ListBox      foreColors;
    private ListBox      fonts;
    private ListBox      fontSizes;

    /**
     * Creates a new toolbar that drives the given rich text area.
     *
     * @param richText
     *         the rich text area to be controlled
     */
    public RichTextToolbar(RichTextArea richText) {
        this.richText = richText;
        this.basic = richText.getBasicFormatter();
        this.extended = richText.getExtendedFormatter();

        outer.add(topPanel);
        outer.add(bottomPanel);
        topPanel.setWidth("100%");
        bottomPanel.setWidth("100%");

        initWidget(outer);
        setStyleName("gwt-RichTextToolbar");
        richText.addStyleName("hasRichTextToolbar");

        if (basic != null) {
            topPanel.add(bold = createToggleButton(images.bold(), strings.bold()));
            topPanel.add(italic = createToggleButton(images.italic(),
                                                     strings.italic()));
            topPanel.add(underline = createToggleButton(images.underline(),
                                                        strings.underline()));
            topPanel.add(subscript = createToggleButton(images.subscript(),
                                                        strings.subscript()));
            topPanel.add(superscript = createToggleButton(images.superscript(),
                                                          strings.superscript()));
            topPanel.add(justifyLeft = createPushButton(images.justifyLeft(),
                                                        strings.justifyLeft()));
            topPanel.add(justifyCenter = createPushButton(images.justifyCenter(),
                                                          strings.justifyCenter()));
            topPanel.add(justifyRight = createPushButton(images.justifyRight(),
                                                         strings.justifyRight()));
        }

        if (extended != null) {
            topPanel.add(strikethrough = createToggleButton(images.strikeThrough(),
                                                            strings.strikeThrough()));
            topPanel.add(indent = createPushButton(images.indent(), strings.indent()));
            topPanel.add(outdent = createPushButton(images.outdent(),
                                                    strings.outdent()));
            topPanel.add(hr = createPushButton(images.hr(), strings.hr()));
            topPanel.add(ol = createPushButton(images.ol(), strings.ol()));
            topPanel.add(ul = createPushButton(images.ul(), strings.ul()));
            topPanel.add(insertImage = createPushButton(images.insertImage(),
                                                        strings.insertImage()));
            topPanel.add(createLink = createPushButton(images.createLink(),
                                                       strings.createLink()));
            topPanel.add(removeLink = createPushButton(images.removeLink(),
                                                       strings.removeLink()));
            topPanel.add(removeFormat = createPushButton(images.removeFormat(),
                                                         strings.removeFormat()));
        }

        if (basic != null) {
            bottomPanel.add(backColors = createColorList("Background"));
            bottomPanel.add(foreColors = createColorList("Foreground"));
            bottomPanel.add(fonts = createFontList());
            bottomPanel.add(fontSizes = createFontSizes());

            // We only use these handlers for updating status, so don't hook them up
            // unless at least basic editing is supported.
            richText.addKeyUpHandler(handler);
            richText.addClickHandler(handler);
        }
    }

    private ListBox createColorList(String caption) {
        ListBox lb = new ListBox();
        lb.addChangeHandler(handler);
        lb.setVisibleItemCount(1);

        lb.addItem(caption);
        lb.addItem(strings.white(), "white");
        lb.addItem(strings.black(), "black");
        lb.addItem(strings.red(), "red");
        lb.addItem(strings.green(), "green");
        lb.addItem(strings.yellow(), "yellow");
        lb.addItem(strings.blue(), "blue");
        return lb;
    }

    private ListBox createFontList() {
        ListBox lb = new ListBox();
        lb.addChangeHandler(handler);
        lb.setVisibleItemCount(1);

        lb.addItem(strings.font(), "");
        lb.addItem(strings.normal(), "");
        lb.addItem("Times New Roman", "Times New Roman");
        lb.addItem("Arial", "Arial");
        lb.addItem("Courier New", "Courier New");
        lb.addItem("Georgia", "Georgia");
        lb.addItem("Trebuchet", "Trebuchet");
        lb.addItem("Verdana", "Verdana");
        return lb;
    }

    private ListBox createFontSizes() {
        ListBox lb = new ListBox();
        lb.addChangeHandler(handler);
        lb.setVisibleItemCount(1);

        lb.addItem(strings.size());
        lb.addItem(strings.xxsmall());
        lb.addItem(strings.xsmall());
        lb.addItem(strings.small());
        lb.addItem(strings.medium());
        lb.addItem(strings.large());
        lb.addItem(strings.xlarge());
        lb.addItem(strings.xxlarge());
        return lb;
    }

    private PushButton createPushButton(ImageResource img, String tip) {
        PushButton pb = new PushButton(new Image(img));
        pb.addClickHandler(handler);
        pb.setTitle(tip);
        return pb;
    }

    private ToggleButton createToggleButton(ImageResource img, String tip) {
        ToggleButton tb = new ToggleButton(new Image(img));
        tb.addClickHandler(handler);
        tb.setTitle(tip);
        return tb;
    }

    /** Updates the status of all the stateful buttons. */
    private void updateStatus() {
        if (basic != null) {
            bold.setDown(basic.isBold());
            italic.setDown(basic.isItalic());
            underline.setDown(basic.isUnderlined());
            subscript.setDown(basic.isSubscript());
            superscript.setDown(basic.isSuperscript());
        }

        if (extended != null) {
            strikethrough.setDown(extended.isStrikethrough());
        }
    }

    /**
     * This {@link ClientBundle} is used for all the button icons. Using a bundle
     * allows all of these images to be packed into a single image, which saves a
     * lot of HTTP requests, drastically improving startup time.
     */
    public interface Images extends ClientBundle {

        ImageResource bold();

        ImageResource createLink();

        ImageResource hr();

        ImageResource indent();

        ImageResource insertImage();

        ImageResource italic();

        ImageResource justifyCenter();

        ImageResource justifyLeft();

        ImageResource justifyRight();

        ImageResource ol();

        ImageResource outdent();

        ImageResource removeFormat();

        ImageResource removeLink();

        ImageResource strikeThrough();

        ImageResource subscript();

        ImageResource superscript();

        ImageResource ul();

        ImageResource underline();
    }

    /**
     * We use an inner EventHandler class to avoid exposing event methods on the
     * RichTextToolbar itself.
     */
    private class EventHandler implements ClickHandler, ChangeHandler,
                                          KeyUpHandler {

        public void onChange(ChangeEvent event) {
            Widget sender = (Widget)event.getSource();

            if (sender == backColors) {
                basic.setBackColor(backColors.getValue(backColors.getSelectedIndex()));
                backColors.setSelectedIndex(0);
            } else if (sender == foreColors) {
                basic.setForeColor(foreColors.getValue(foreColors.getSelectedIndex()));
                foreColors.setSelectedIndex(0);
            } else if (sender == fonts) {
                basic.setFontName(fonts.getValue(fonts.getSelectedIndex()));
                fonts.setSelectedIndex(0);
            } else if (sender == fontSizes) {
                basic.setFontSize(fontSizesConstants[fontSizes.getSelectedIndex() - 1]);
                fontSizes.setSelectedIndex(0);
            }
        }

        public void onClick(ClickEvent event) {
            Widget sender = (Widget)event.getSource();

            if (sender == bold) {
                basic.toggleBold();
            } else if (sender == italic) {
                basic.toggleItalic();
            } else if (sender == underline) {
                basic.toggleUnderline();
            } else if (sender == subscript) {
                basic.toggleSubscript();
            } else if (sender == superscript) {
                basic.toggleSuperscript();
            } else if (sender == strikethrough) {
                extended.toggleStrikethrough();
            } else if (sender == indent) {
                extended.rightIndent();
            } else if (sender == outdent) {
                extended.leftIndent();
            } else if (sender == justifyLeft) {
                basic.setJustification(RichTextArea.Justification.LEFT);
            } else if (sender == justifyCenter) {
                basic.setJustification(RichTextArea.Justification.CENTER);
            } else if (sender == justifyRight) {
                basic.setJustification(RichTextArea.Justification.RIGHT);
            } else if (sender == insertImage) {
                String url = Window.prompt("Enter an image URL:", "http://");
                if (url != null) {
                    extended.insertImage(url);
                }
            } else if (sender == createLink) {
                String url = Window.prompt("Enter a link URL:", "http://");
                if (url != null) {
                    extended.createLink(url);
                }
            } else if (sender == removeLink) {
                extended.removeLink();
            } else if (sender == hr) {
                extended.insertHorizontalRule();
            } else if (sender == ol) {
                extended.insertOrderedList();
            } else if (sender == ul) {
                extended.insertUnorderedList();
            } else if (sender == removeFormat) {
                extended.removeFormat();
            } else if (sender == richText) {
                // We use the RichTextArea's onKeyUp event to update the toolbar status.
                // This will catch any cases where the user moves the cursur using the
                // keyboard, or uses one of the browser's built-in keyboard shortcuts.
                updateStatus();
            }
        }

        public void onKeyUp(KeyUpEvent event) {
            Widget sender = (Widget)event.getSource();
            if (sender == richText) {
                // We use the RichTextArea's onKeyUp event to update the toolbar status.
                // This will catch any cases where the user moves the cursur using the
                // keyboard, or uses one of the browser's built-in keyboard shortcuts.
                updateStatus();
            }
        }
    }
}
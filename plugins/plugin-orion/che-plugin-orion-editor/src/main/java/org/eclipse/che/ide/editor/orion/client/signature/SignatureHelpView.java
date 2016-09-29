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
package org.eclipse.che.ide.editor.orion.client.signature;

import elemental.dom.Element;
import elemental.dom.Node;
import elemental.events.EventListener;
import elemental.html.SpanElement;

import com.google.common.base.Optional;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.ide.api.editor.signature.ParameterInfo;
import org.eclipse.che.ide.api.editor.signature.SignatureHelp;
import org.eclipse.che.ide.api.editor.signature.SignatureInfo;
import org.eclipse.che.ide.util.Pair;
import org.eclipse.che.ide.util.dom.Elements;

import java.util.ArrayList;
import java.util.List;

/**
 * Widget for showing signature information
 *
 * @author Evgen Vidolob
 */
public class SignatureHelpView extends PopupPanel {
    private final SignatureHelpResources       resources;
    private final Element                      rootElement;
    private final Element                      signatures;
    private final Element                      overloads;
    private       SignatureHelp                signatureHelp;
    private       int                          activeSignature;
    private       List<Pair<Integer, Integer>> signatureViews;
    private       int                          x;
    private       int                          y;

    @Inject
    public SignatureHelpView(SignatureHelpResources resources) {
        super(true);
        this.resources = resources;
        rootElement = Elements.createDivElement(resources.css().main(), resources.css().parameterHintsWidget());
        Element wrapper = Elements.createDivElement(resources.css().wrapper());
        rootElement.appendChild(wrapper);
        Element buttons = Elements.createDivElement(resources.css().buttons());
        wrapper.appendChild(buttons);
        Element previous = Elements.createDivElement(resources.css().button(), resources.css().previous());
        previous.appendChild((Node)resources.arrow().getSvg().getElement());
        buttons.appendChild(previous);
        previous.addEventListener(elemental.events.Event.CLICK, new EventListener() {
            @Override
            public void handleEvent(elemental.events.Event evt) {
                previous();
            }
        }, true);

        Element next = Elements.createDivElement(resources.css().button(), resources.css().next());
        next.appendChild((Node)resources.arrow().getSvg().getElement());
        next.addEventListener(elemental.events.Event.CLICK, new EventListener() {
            @Override
            public void handleEvent(elemental.events.Event evt) {
                next();
            }
        }, true);

        buttons.appendChild(next);

        overloads = Elements.createDivElement(resources.css().overloads());
        wrapper.appendChild(overloads);

        signatures = Elements.createDivElement(resources.css().signatures());
        wrapper.appendChild(signatures);

        Widget widget = new ElementWidget((com.google.gwt.dom.client.Element)rootElement);


        setWidget(widget);
    }

    private void previous() {
        if (signatureViews.size() < 2) {
            return;
        }

        activeSignature--;
        if (activeSignature < 0) {
            activeSignature = signatureViews.size() - 1;
        }

        this.select(activeSignature);
    }

    private void next() {
        if (signatureViews.size() < 2) {
            return;
        }
        activeSignature = (activeSignature + 1) % signatureViews.size();
        select(activeSignature);
    }

    public void showSignature(Promise<Optional<SignatureHelp>> promise, final int x, final int y) {
        this.x = x;
        this.y = y;
        promise.then(new Operation<Optional<SignatureHelp>>() {
            @Override
            public void apply(Optional<SignatureHelp> arg) throws OperationException {
                if (arg.isPresent() && !arg.get().getSignatures().isEmpty()) {
                    activeSignature = 0;
                    signatureHelp = arg.get();
                    if (signatureHelp.getActiveSignature().isPresent()) {
                        activeSignature = signatureHelp.getActiveSignature().get();
                    }

                    show();
                    render();
                    select(activeSignature);
                }
            }
        });

    }

    private void select(int position) {
        Pair<Integer, Integer> signaturePosition = signatureViews.get(position);
        if (signaturePosition == null) {
            return;
        }

        signatures.getStyle().setHeight(signaturePosition.second + "px");
        signatures.setScrollTop(signaturePosition.first);
        String overloads = "" + (position + 1);
        if (signatureViews.size() < 10) {
            overloads += ("/" + signatureViews.size());
        }

        this.overloads.setInnerText(overloads);
        setPopupPosition(x, y - getElement().getOffsetHeight());
    }

    private void render() {
        if (signatureHelp.getSignatures().size() > 1) {
            Elements.addClassName(resources.css().multiple(), rootElement);
            overloads.getStyle().setDisplay("block");
        } else {
            Elements.removeClassName(resources.css().multiple(), rootElement);
            overloads.getStyle().setDisplay("none");
        }

        signatures.setInnerHTML("");
        signatureViews = new ArrayList<>();
        int height = 0;
        for (SignatureInfo signatureInfo : signatureHelp.getSignatures()) {
            Element signatureElement = renderSignature(signatures, signatureInfo, signatureHelp.getActiveParameter());
            renderDocumentation(signatureElement, signatureInfo, signatureHelp.getActiveParameter());

            int signatureHeight = signatureElement.getOffsetHeight();
            signatureViews.add(Pair.of(height, signatureHeight));
            height += signatureHeight;
        }

    }

    private void renderDocumentation(Element element, SignatureInfo signatureInfo, Optional<Integer> activeParameter) {
        if (signatureInfo.getDocumentation().isPresent()) {
            elemental.html.DivElement documentation = Elements.createDivElement(resources.css().documentation());
            documentation.setTextContent(signatureInfo.getDocumentation().get());
            element.appendChild(documentation);
        }

        if (signatureInfo.getParameters().isPresent() && activeParameter.isPresent() &&
            signatureInfo.getParameters().get().size() > activeParameter.get()) {
            ParameterInfo parameterInfo = signatureInfo.getParameters().get().get(activeParameter.get());
            if (parameterInfo.getDocumentation().isPresent()) {
                elemental.html.DivElement parameter = Elements.createDivElement(resources.css().documentationParameter());
                SpanElement label = Elements.createSpanElement(resources.css().documentationParameter());
                label.setTextContent(parameterInfo.getLabel());

                SpanElement documentation = Elements.createSpanElement(resources.css().documentation());
                documentation.setTextContent(parameterInfo.getDocumentation().get());

                parameter.appendChild(label);
                parameter.appendChild(documentation);
                element.appendChild(parameter);
            }
        }
    }

    private Element renderSignature(Element signatures, SignatureInfo signatureInfo, Optional<Integer> activeParameter) {
        Element signatureElement = Elements.createDivElement();
        signatures.appendChild(signatureElement);

        Element code = Elements.createDivElement();
        signatureElement.appendChild(code);
        boolean hasParameters = signatureInfo.getParameters().isPresent() && !signatureInfo.getParameters().get().isEmpty();

        if (hasParameters) {
            renderParameters(code, signatureInfo, activeParameter);
        } else {
            Node label = code.appendChild(Elements.createSpanElement());
            label.setTextContent(signatureInfo.getLabel());
        }

        return signatureElement;
    }

    private void renderParameters(Element parent, SignatureInfo signatureInfo, Optional<Integer> activeParameter) {
        int end = signatureInfo.getLabel().length();
        int idx;
        Element element;
        for (int i = signatureInfo.getParameters().get().size() - 1; i >= 0; i--) {
            ParameterInfo parameterInfo = signatureInfo.getParameters().get().get(i);
            idx = signatureInfo.getLabel().lastIndexOf(parameterInfo.getLabel(), end);
            int signatureLabelOffset = 0;
            int signatureLabelEnd = 0;
            if (idx >= 0) {
                signatureLabelOffset = idx;
                signatureLabelEnd = idx + parameterInfo.getLabel().length();
            }

            element = Elements.createSpanElement();
            element.setTextContent(signatureInfo.getLabel().substring(signatureLabelEnd, end));
            parent.insertBefore(element, parent.getFirstElementChild());

            element = Elements.createSpanElement(resources.css().parameter());
            if (activeParameter.isPresent() && i == activeParameter.get()) {
                Elements.addClassName(resources.css().active(), element);
            }
            element.setTextContent(signatureInfo.getLabel().substring(signatureLabelOffset, signatureLabelEnd));
            parent.insertBefore(element, parent.getFirstElementChild());
            end = signatureLabelOffset;
        }
        element = Elements.createSpanElement();
        element.setTextContent(signatureInfo.getLabel().substring(0, end));
        parent.insertBefore(element, parent.getFirstElementChild());

    }

    /** The method used to hide popup with parameters when user press 'Escape' button. */
    @Override
    protected void onPreviewNativeEvent(Event.NativePreviewEvent event) {
        super.onPreviewNativeEvent(event);
        switch (event.getTypeInt()) {
            case Event.ONKEYDOWN:
                if (event.getNativeEvent().getKeyCode() == KeyCodes.KEY_ESCAPE) {
                    hide();
                }
                break;
        }
    }

}
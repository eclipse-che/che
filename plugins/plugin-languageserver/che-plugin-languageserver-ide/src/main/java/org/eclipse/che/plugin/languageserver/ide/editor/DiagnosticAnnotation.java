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
package org.eclipse.che.plugin.languageserver.ide.editor;

import elemental.dom.Element;
import elemental.js.dom.JsElement;
import io.typefox.lsapi.DiagnosticSeverity;

import org.eclipse.che.api.languageserver.shared.lsapi.DiagnosticDTO;
import org.eclipse.che.ide.api.editor.text.annotation.Annotation;
import org.eclipse.che.ide.util.dom.Elements;
import org.eclipse.che.plugin.languageserver.ide.LanguageServerResources;
import org.vectomatic.dom.svg.ui.SVGImage;

/**
 * @author Evgen Vidolob
 */
public class DiagnosticAnnotation extends Annotation {

    public static final String ERROR_ANNOTATION_TYPE   = "org.eclipse.che.ls.error";
    public static final String WARNING_ANNOTATION_TYPE = "org.eclipse.che.ls.warning";
    public static final String INFO_ANNOTATION_TYPE    = "org.eclipse.che.ls.info";
    public static final String HINT_ANNOTATION_TYPE    = "org.eclipse.che.ls.hint";

    private static final int HINT_LAYER    = 0;
    private static final int INFO_LAYER    = 1;
    private static final int WARNING_LAYER = 2;
    private static final int ERROR_LAYER   = 3;

    private static final LanguageServerResources RESOURCES = LanguageServerResources.INSTANCE;

    private static JsElement fgTaskElement    = new SVGImage(RESOURCES.taskMark()).getElement().cast();
    private static JsElement fgInfoElement    = new SVGImage(RESOURCES.importItem()).getElement().cast();
    private static JsElement fgWarningElement = new SVGImage(RESOURCES.markWarning()).getElement().cast();
    private static JsElement fgErrorElement   = new SVGImage(RESOURCES.markError()).getElement().cast();

    private DiagnosticDTO diagnostic;
    private Element imageElement = null;

    public DiagnosticAnnotation(DiagnosticDTO diagnostic) {

        this.diagnostic = diagnostic;

        DiagnosticSeverity severity = diagnostic.getSeverity();
        if (severity == null) {
            layer = ERROR_LAYER;
            setType(ERROR_ANNOTATION_TYPE);
        } else {
            switch (severity) {
                case Error:
                    layer = ERROR_LAYER;
                    setType(ERROR_ANNOTATION_TYPE);
                    break;
                case Warning:
                    layer = WARNING_LAYER;
                    setType(WARNING_ANNOTATION_TYPE);
                    break;
                case Information:
                    layer = INFO_LAYER;
                    setType(INFO_ANNOTATION_TYPE);
                    break;
                case Hint:
                    layer = HINT_LAYER;
                    setType(HINT_ANNOTATION_TYPE);
                    break;
                default:
                    layer = ERROR_LAYER;
                    setType(ERROR_ANNOTATION_TYPE);
                    break;
            }
        }
    }

    @Override
    public String getText() {
        return diagnostic.getMessage();
    }


    private void initializeImage() {

        imageElement = Elements.createDivElement();
        imageElement.setClassName(RESOURCES.css().markElement());

        final Element selectedImageElement = getSelectedImageElement();
        if (selectedImageElement != null) {
            imageElement.appendChild(selectedImageElement.cloneNode(true));
        }
    }

    private Element getSelectedImageElement() {
        final String type = getType();
        switch (type) {
            case HINT_ANNOTATION_TYPE:
                return fgTaskElement;
            case INFO_ANNOTATION_TYPE:
                return fgInfoElement;
            case WARNING_ANNOTATION_TYPE:
                return fgWarningElement;
            case ERROR_ANNOTATION_TYPE:
                return fgErrorElement;
            default:
                return null;
        }
    }

    @Override
    public Element getImageElement() {
        if (imageElement == null) {
            initializeImage();
        }
        return imageElement;
    }

}

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
package org.eclipse.che.plugin.languageserver.ide.navigation.symbol;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.plugin.languageserver.ide.LanguageServerResources;
import org.eclipse.che.plugin.languageserver.shared.lsapi.SymbolInformationDTO;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class SymbolKind {


    private final LanguageServerResources resources;

    private final Map<Integer, SVGResource> iconMap = new HashMap<>();

    @Inject

    public SymbolKind(LanguageServerResources resources) {
        this.resources = resources;
        //TODO Replace markWarning() image with proper images
        iconMap.put(SymbolInformationDTO.KIND_METHOD, resources.methodItem());
        iconMap.put(SymbolInformationDTO.KIND_FUNCTION, resources.textItem());
        iconMap.put(SymbolInformationDTO.KIND_CONSTRUCTOR, resources.textItem());
        iconMap.put(SymbolInformationDTO.KIND_VARIABLE, resources.variableItem());
        iconMap.put(SymbolInformationDTO.KIND_CLASS, resources.classItem());
        iconMap.put(SymbolInformationDTO.KIND_INTERFACE, resources.interfaceItem());
        iconMap.put(SymbolInformationDTO.KIND_NAMESPACE, resources.textItem());
        iconMap.put(SymbolInformationDTO.KIND_PACKAGE, resources.textItem());
        iconMap.put(SymbolInformationDTO.KIND_MODULE, resources.moduleItem());
        iconMap.put(SymbolInformationDTO.KIND_PROPERTY, resources.propertyItem());
        iconMap.put(SymbolInformationDTO.KIND_ENUM, resources.enumItem());
        iconMap.put(SymbolInformationDTO.KIND_STRING, resources.textItem());
        iconMap.put(SymbolInformationDTO.KIND_FILE, resources.textItem());
        iconMap.put(SymbolInformationDTO.KIND_ARRAY, resources.textItem());
        iconMap.put(SymbolInformationDTO.KIND_NUMBER, resources.textItem());
        iconMap.put(SymbolInformationDTO.KIND_BOOLEAN, resources.textItem());
        iconMap.put(SymbolInformationDTO.KIND_FIELD, resources.fieldItem());
        iconMap.put(SymbolInformationDTO.KIND_CONSTANT, resources.textItem());
    }

    public SVGResource getIcon(int kind) {
        if (iconMap.containsKey(kind)) {
            return iconMap.get(kind);
        }
        return null;
    }

    public String from(int kind) {
        switch (kind) {
            case SymbolInformationDTO.KIND_METHOD:
                return "method";
            case SymbolInformationDTO.KIND_FUNCTION:
                return "function";
            case SymbolInformationDTO.KIND_CONSTRUCTOR:
                return "constructor";
            case SymbolInformationDTO.KIND_VARIABLE:
                return "variable";
            case SymbolInformationDTO.KIND_CLASS:
                return "class";
            case SymbolInformationDTO.KIND_INTERFACE:
                return "interface";
            case SymbolInformationDTO.KIND_NAMESPACE:
                return "namespace";
            case SymbolInformationDTO.KIND_PACKAGE:
                return "package";
            case SymbolInformationDTO.KIND_MODULE:
                return "module";
            case SymbolInformationDTO.KIND_PROPERTY:
                return "property";
            case SymbolInformationDTO.KIND_ENUM:
                return "enum";
            case SymbolInformationDTO.KIND_STRING:
                return "string";
            case SymbolInformationDTO.KIND_FILE:
                return "file";
            case SymbolInformationDTO.KIND_ARRAY:
                return "array";
            case SymbolInformationDTO.KIND_NUMBER:
                return "number";
            case SymbolInformationDTO.KIND_BOOLEAN:
                return "boolean";
        }
        return "property";
    }


}

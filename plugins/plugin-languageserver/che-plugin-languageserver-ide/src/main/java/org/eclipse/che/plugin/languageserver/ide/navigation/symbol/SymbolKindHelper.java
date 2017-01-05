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
package org.eclipse.che.plugin.languageserver.ide.navigation.symbol;

import io.typefox.lsapi.SymbolKind;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.plugin.languageserver.ide.LanguageServerResources;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides icons and text description for {@link SymbolKind}
 * 
 * @author Evgen Vidolob
 */
@Singleton
public class SymbolKindHelper {


    private final LanguageServerResources resources;

    private final Map<SymbolKind, SVGResource> iconMap = new HashMap<>();

    @Inject

    public SymbolKindHelper(LanguageServerResources resources) {
        this.resources = resources;
        //TODO Replace markWarning() image with proper images
        iconMap.put(SymbolKind.Method, resources.methodItem());
        iconMap.put(SymbolKind.Function, resources.textItem());
        iconMap.put(SymbolKind.Constructor, resources.textItem());
        iconMap.put(SymbolKind.Variable, resources.variableItem());
        iconMap.put(SymbolKind.Class, resources.classItem());
        iconMap.put(SymbolKind.Interface, resources.interfaceItem());
        iconMap.put(SymbolKind.Namespace, resources.textItem());
        iconMap.put(SymbolKind.Package, resources.textItem());
        iconMap.put(SymbolKind.Module, resources.moduleItem());
        iconMap.put(SymbolKind.Property, resources.propertyItem());
        iconMap.put(SymbolKind.Enum, resources.enumItem());
        iconMap.put(SymbolKind.String, resources.textItem());
        iconMap.put(SymbolKind.File, resources.textItem());
        iconMap.put(SymbolKind.Array, resources.textItem());
        iconMap.put(SymbolKind.Number, resources.textItem());
        iconMap.put(SymbolKind.Boolean, resources.textItem());
        iconMap.put(SymbolKind.Field, resources.fieldItem());
        iconMap.put(SymbolKind.Constant, resources.textItem());
    }

    public SVGResource getIcon(SymbolKind kind) {
        if (iconMap.containsKey(kind)) {
            return iconMap.get(kind);
        }
        return null;
    }

    public String from(SymbolKind kind) {
        switch (kind) {
            case Method:
                return "method";
            case Function:
                return "function";
            case Constructor:
                return "constructor";
            case Variable:
                return "variable";
            case Class:
                return "class";
            case Interface:
                return "interface";
            case Namespace:
                return "namespace";
            case Package:
                return "package";
            case Module:
                return "module";
            case Property:
                return "property";
            case Enum:
                return "enum";
            case String:
                return "string";
            case File:
                return "file";
            case Array:
                return "array";
            case Number:
                return "number";
            case Boolean:
                return "boolean";
        }
        return "property";
    }


}

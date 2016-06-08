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
package org.eclipse.che.plugin.languageserver.ide.editor.codeassist;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.plugin.languageserver.ide.LanguageServerResources;
import org.eclipse.che.plugin.languageserver.shared.lsapi.CompletionItemDTO;
import org.vectomatic.dom.svg.ui.SVGResource;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Evgen Vidolob
 */
@Singleton
public class CompletionImageProvider {

    private final LanguageServerResources resources;

    private Map<Integer, SVGResource> imageMap = new HashMap<>();

    @Inject
    public CompletionImageProvider(LanguageServerResources resources) {
        this.resources = resources;
        //TODO add missed icons
        //no icon for keyword kind
        imageMap.put(CompletionItemDTO.KIND_TEXT, resources.textItem());
        imageMap.put(CompletionItemDTO.KIND_METHOD, resources.methodItem());
//        imageMap.put(CompletionItemDTO.KIND_FUNCTION, );
//        imageMap.put(CompletionItemDTO.KIND_CONSTRUCTOR, );
        imageMap.put(CompletionItemDTO.KIND_FIELD, resources.fieldItem());
        imageMap.put(CompletionItemDTO.KIND_VARIABLE, resources.variableItem());
        imageMap.put(CompletionItemDTO.KIND_CLASS, resources.classItem());
        imageMap.put(CompletionItemDTO.KIND_INTERFACE, resources.interfaceItem());
        imageMap.put(CompletionItemDTO.KIND_MODULE, resources.moduleItem());
        imageMap.put(CompletionItemDTO.KIND_PROPERTY, resources.propertyItem());
//        imageMap.put(CompletionItemDTO.KIND_UNIT, );
        imageMap.put(CompletionItemDTO.KIND_VALUE, resources.valueItem());
        imageMap.put(CompletionItemDTO.KIND_ENUM, resources.enumItem());
        imageMap.put(CompletionItemDTO.KIND_SNIPPET, resources.snippetItem());
//        imageMap.put(CompletionItemDTO.KIND_COLOR, );
        imageMap.put(CompletionItemDTO.KIND_FILE, resources.fileItem());
//        imageMap.put(CompletionItemDTO.KIND_REFERENCE, );
    }

    public Icon getIcon(Integer completionKind) {
        return new Icon("", imageMap.get(completionKind));
    }
}

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
package org.eclipse.che.plugin.languageserver.ide.editor.codeassist;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import org.eclipse.che.ide.api.icon.Icon;
import org.eclipse.che.plugin.languageserver.ide.LanguageServerResources;
import org.eclipse.lsp4j.CompletionItemKind;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Evgen Vidolob */
@Singleton
public class CompletionImageProvider {

  private final LanguageServerResources resources;

  private Map<CompletionItemKind, SVGResource> imageMap = new HashMap<>();

  @Inject
  public CompletionImageProvider(LanguageServerResources resources) {
    this.resources = resources;
    // TODO add missed icons
    // no icon for keyword kind
    imageMap.put(CompletionItemKind.Text, resources.textItem());
    imageMap.put(CompletionItemKind.Method, resources.methodItem());
    //        imageMap.put(CompletionItemKind.KIND_FUNCTION, );
    //        imageMap.put(CompletionItemKind.KIND_CONSTRUCTOR, );
    imageMap.put(CompletionItemKind.Field, resources.fieldItem());
    imageMap.put(CompletionItemKind.Variable, resources.variableItem());
    imageMap.put(CompletionItemKind.Class, resources.classItem());
    imageMap.put(CompletionItemKind.Interface, resources.interfaceItem());
    imageMap.put(CompletionItemKind.Module, resources.moduleItem());
    imageMap.put(CompletionItemKind.Property, resources.propertyItem());
    //        imageMap.put(CompletionItemKind.KIND_UNIT, );
    imageMap.put(CompletionItemKind.Value, resources.valueItem());
    imageMap.put(CompletionItemKind.Enum, resources.enumItem());
    imageMap.put(CompletionItemKind.Snippet, resources.snippetItem());
    //        imageMap.put(CompletionItemKind.KIND_COLOR, );
    imageMap.put(CompletionItemKind.File, resources.fileItem());
    //        imageMap.put(CompletionItemKind.KIND_REFERENCE, );
  }

  public Icon getIcon(CompletionItemKind completionKind) {
    return new Icon("", imageMap.get(completionKind));
  }
}

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
package org.eclipse.che.plugin.languageserver.ide;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import org.eclipse.che.ide.ui.list.SimpleList;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Anatolii Bazko */
public interface LanguageServerResources extends ClientBundle {

  LanguageServerResources INSTANCE = GWT.create(LanguageServerResources.class);

  @Source({"languageserver.css", "org/eclipse/che/ide/api/ui/style.css"})
  LSCss css();

  @Source({
    "QuickOpenList.css",
    "org/eclipse/che/ide/ui/constants.css",
    "org/eclipse/che/ide/api/ui/style.css"
  })
  QuickOpenListCss quickOpenListCss();

  @Source("svg/file.svg")
  SVGResource file();

  @Source("svg/category.svg")
  SVGResource category();

  @Source("svg/taskmrk.svg")
  SVGResource taskMark();

  @Source("svg/mark-error.svg")
  SVGResource markError();

  @Source("svg/mark-warning.svg")
  SVGResource markWarning();

  @Source("svg/import.svg")
  SVGResource importItem();

  @Source("svg/codeassist/class.svg")
  SVGResource classItem();

  @Source("svg/codeassist/interface.svg")
  SVGResource interfaceItem();

  @Source("svg/codeassist/enum_type.svg")
  SVGResource enumItem();

  @Source("svg/codeassist/package.svg")
  SVGResource moduleItem();

  @Source("svg/codeassist/field.svg")
  SVGResource fieldItem();

  @Source("svg/codeassist/method.svg")
  SVGResource methodItem();

  @Source("svg/codeassist/generic_file.svg")
  SVGResource fileItem();

  @Source("svg/codeassist/local.svg")
  SVGResource variableItem();

  @Source("svg/codeassist/template.svg")
  SVGResource snippetItem();

  @Source("svg/codeassist/text.svg")
  SVGResource textItem();

  @Source("svg/codeassist/property.svg")
  SVGResource propertyItem();

  @Source("svg/codeassist/value.svg")
  SVGResource valueItem();

  @Source("svg/find.svg")
  SVGResource findIcon();

  interface LSCss extends CssResource {

    @ClassName("overview-mark-warning")
    String overviewMarkWarning();

    @ClassName("overview-mark-error")
    String overviewMarkError();

    @ClassName("overview-mark-task")
    String overviewMarkTask();

    @ClassName("mark-element")
    String markElement();

    @ClassName("codeassistant-detail")
    String codeassistantDetail();

    @ClassName("codeassistant-highlight")
    String codeassistantHighlight();
  }

  interface QuickOpenListCss extends SimpleList.Css {
    int menuListBorderPx();

    String listItem();

    String listBase();

    String listContainer();

    @ClassName("search-match")
    String searchMatch();

    String groupSeparator();
  }
}

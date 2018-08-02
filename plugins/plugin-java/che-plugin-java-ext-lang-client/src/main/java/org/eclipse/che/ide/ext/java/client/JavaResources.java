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
package org.eclipse.che.ide.ext.java.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import org.vectomatic.dom.svg.ui.SVGResource;

/**
 * @author Evgen Vidolob
 * @author Vlad Zhukovskyi
 */
public interface JavaResources extends ClientBundle {
  JavaResources INSTANCE = GWT.create(JavaResources.class);

  @Source("java.css")
  JavaCss css();

  @Source("svg/annotation_type.svg")
  SVGResource annotationItem();

  @Source("org/eclipse/che/ide/ext/java/client/images/class.svg")
  SVGResource svgClassItem();

  @Source("org/eclipse/che/ide/ext/java/client/images/interface.svg")
  SVGResource interfaceItem();

  @Source("svg/enum_type.svg")
  SVGResource enumItem();

  @Source("org/eclipse/che/ide/ext/java/client/images/default_field.svg")
  SVGResource defaultField();

  @Source("org/eclipse/che/ide/ext/java/client/images/private_field.svg")
  SVGResource privateField();

  @Source("org/eclipse/che/ide/ext/java/client/images/protected_field.svg")
  SVGResource protectedField();

  @Source("org/eclipse/che/ide/ext/java/client/images/public_field.svg")
  SVGResource publicField();

  @Source("org/eclipse/che/ide/ext/java/client/images/package.svg")
  SVGResource packageItem();

  @Source("org/eclipse/che/ide/ext/java/client/images/default_method.svg")
  SVGResource defaultMethod();

  @Source("svg/private_method.svg")
  SVGResource privateMethod();

  @Source("svg/protected_method.svg")
  SVGResource protectedMethod();

  @Source("svg/publicMethod.svg")
  SVGResource publicMethod();

  @Source("org/eclipse/che/ide/ext/java/client/images/template.svg")
  SVGResource template();

  @Source(
      "org/eclipse/che/ide/ext/java/client/internal/text/correction/proposals/correction_change.svg")
  SVGResource correctionChange();

  @Source("org/eclipse/che/ide/ext/java/client/internal/text/correction/proposals/remove.svg")
  SVGResource remove();

  @Source(
      "org/eclipse/che/ide/ext/java/client/internal/text/correction/proposals/correction_cast.svg")
  SVGResource correctionCast();

  @Source("org/eclipse/che/ide/ext/java/client/internal/text/correction/proposals/local.svg")
  SVGResource localVar();

  @Source(
      "org/eclipse/che/ide/ext/java/client/internal/text/correction/proposals/correction_delete_import.svg")
  SVGResource correctionDeleteImport();

  @Source("org/eclipse/che/ide/ext/java/client/internal/text/correction/proposals/import.svg")
  SVGResource importItem();

  @Source("org/eclipse/che/ide/ext/java/client/internal/text/correction/proposals/add.svg")
  SVGResource add();

  @Source(
      "org/eclipse/che/ide/ext/java/client/internal/text/correction/proposals/remove_correction.svg")
  SVGResource correctionRemove();

  @Source(
      "org/eclipse/che/ide/ext/java/client/internal/text/correction/proposals/jexception_obj.svg")
  SVGResource exceptionProp();

  @Source("org/eclipse/che/ide/ext/java/client/internal/text/correction/proposals/javadoc.svg")
  SVGResource javadoc();

  @Source("internal/text/correction/proposals/correction_rename.svg")
  SVGResource linkedRename();

  @Source("org/eclipse/che/ide/ext/java/client/images/taskmrk.svg")
  SVGResource taskMark();

  @Source("svg/mark-error.svg")
  SVGResource markError();

  @Source("svg/mark-warning.svg")
  SVGResource markWarning();

  @Source("svg/javaFile.svg")
  SVGResource javaFile();

  @Source("svg/jsfFile.svg")
  SVGResource jsfFile();

  @Source("svg/jspFile.svg")
  SVGResource jspFile();

  @Source("svg/update-dependencies.svg")
  SVGResource updateDependencies();

  @Source("svg/category/java.svg")
  SVGResource javaCategoryIcon();

  @Source("svg/externalLibraries.svg")
  SVGResource externalLibraries();

  @Source("svg/jarFileIcon.svg")
  SVGResource jarFileIcon();

  @Source("svg/sourceFolder.svg")
  SVGResource sourceFolder();

  @Source("svg/testFolder.svg")
  SVGResource testFolder();

  @Source("svg/openDeclaration.svg")
  SVGResource openDeclaration();

  @Source("svg/quickDocumentation.svg")
  SVGResource quickDocumentation();

  @Source("svg/searchMatch.svg")
  SVGResource searchMatch();

  @Source("svg/file-navigation.svg")
  SVGResource fileNavigation();
}

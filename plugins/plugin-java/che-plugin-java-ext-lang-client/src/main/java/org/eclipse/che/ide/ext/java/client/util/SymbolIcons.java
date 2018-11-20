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
package org.eclipse.che.ide.ext.java.client.util;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.jdt.ls.extension.api.Visibility;
import org.eclipse.che.jdt.ls.extension.api.dto.ExtendedSymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.vectomatic.dom.svg.ui.SVGResource;

public class SymbolIcons {
  private static final Map<Visibility, Map<SymbolKind, SVGResource>> resources;

  static {
    resources =
        new ImmutableMap.Builder<Visibility, Map<SymbolKind, SVGResource>>()
            .put(
                Visibility.PUBLIC,
                new ImmutableMap.Builder<SymbolKind, SVGResource>()
                    .put(SymbolKind.Package, JavaResources.INSTANCE.packageItem())
                    .put(SymbolKind.Class, JavaResources.INSTANCE.svgClassItem())
                    .put(SymbolKind.Method, JavaResources.INSTANCE.publicMethod())
                    .put(SymbolKind.Field, JavaResources.INSTANCE.publicField())
                    .put(SymbolKind.Constructor, JavaResources.INSTANCE.publicMethod())
                    .put(SymbolKind.Enum, JavaResources.INSTANCE.enumItem())
                    .put(SymbolKind.Interface, JavaResources.INSTANCE.interfaceItem())
                    .put(SymbolKind.Constant, JavaResources.INSTANCE.publicField())
                    .put(SymbolKind.EnumMember, JavaResources.INSTANCE.publicField())
                    .build())
            .put(
                Visibility.PROTECTED,
                new ImmutableMap.Builder<SymbolKind, SVGResource>()
                    .put(SymbolKind.Package, JavaResources.INSTANCE.packageItem())
                    .put(SymbolKind.Class, JavaResources.INSTANCE.svgClassItem())
                    .put(SymbolKind.Method, JavaResources.INSTANCE.protectedMethod())
                    .put(SymbolKind.Field, JavaResources.INSTANCE.protectedField())
                    .put(SymbolKind.Constructor, JavaResources.INSTANCE.protectedMethod())
                    .put(SymbolKind.Enum, JavaResources.INSTANCE.enumItem())
                    .put(SymbolKind.Interface, JavaResources.INSTANCE.interfaceItem())
                    .put(SymbolKind.Constant, JavaResources.INSTANCE.protectedField())
                    .put(SymbolKind.EnumMember, JavaResources.INSTANCE.protectedField())
                    .build())
            .put(
                Visibility.PACKAGE,
                new ImmutableMap.Builder<SymbolKind, SVGResource>()
                    .put(SymbolKind.Package, JavaResources.INSTANCE.packageItem())
                    .put(SymbolKind.Class, JavaResources.INSTANCE.svgClassItem())
                    .put(SymbolKind.Method, JavaResources.INSTANCE.defaultMethod())
                    .put(SymbolKind.Field, JavaResources.INSTANCE.defaultField())
                    .put(SymbolKind.Constructor, JavaResources.INSTANCE.defaultMethod())
                    .put(SymbolKind.Enum, JavaResources.INSTANCE.enumItem())
                    .put(SymbolKind.Interface, JavaResources.INSTANCE.interfaceItem())
                    .put(SymbolKind.Constant, JavaResources.INSTANCE.defaultField())
                    .put(SymbolKind.EnumMember, JavaResources.INSTANCE.defaultField())
                    .build())
            .put(
                Visibility.PRIVATE,
                new ImmutableMap.Builder<SymbolKind, SVGResource>()
                    .put(SymbolKind.Package, JavaResources.INSTANCE.packageItem())
                    .put(SymbolKind.Class, JavaResources.INSTANCE.svgClassItem())
                    .put(SymbolKind.Method, JavaResources.INSTANCE.privateMethod())
                    .put(SymbolKind.Field, JavaResources.INSTANCE.privateField())
                    .put(SymbolKind.Constructor, JavaResources.INSTANCE.privateMethod())
                    .put(SymbolKind.Enum, JavaResources.INSTANCE.enumItem())
                    .put(SymbolKind.Interface, JavaResources.INSTANCE.interfaceItem())
                    .put(SymbolKind.Constant, JavaResources.INSTANCE.privateField())
                    .put(SymbolKind.EnumMember, JavaResources.INSTANCE.privateField())
                    .build())
            .build();
  }

  public SVGResource get(ExtendedSymbolInformation symbol) {
    Visibility visiblity = symbol.getVisiblity();
    return resources
        .get(visiblity == null ? Visibility.PACKAGE : visiblity)
        .get(symbol.getInfo().getKind());
  }
}

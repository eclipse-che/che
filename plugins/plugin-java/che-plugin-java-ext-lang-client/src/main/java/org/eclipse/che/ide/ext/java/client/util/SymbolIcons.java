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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.eclipse.che.ide.ext.java.client.JavaResources;
import org.eclipse.che.jdt.ls.extension.api.Visibility;
import org.eclipse.che.jdt.ls.extension.api.dto.ExtendedSymbolInformation;
import org.eclipse.lsp4j.SymbolKind;
import org.vectomatic.dom.svg.ui.SVGResource;

public class SymbolIcons {
  private static final Map<Visibility, Map<SymbolKind, SVGResource>> resources;

  static {
    resources =
        map(
            put(
                Visibility.PUBLIC,
                map(
                    put(SymbolKind.Package, JavaResources.INSTANCE.packageItem()),
                    put(SymbolKind.Class, JavaResources.INSTANCE.svgClassItem()),
                    put(SymbolKind.Method, JavaResources.INSTANCE.publicMethod()),
                    put(SymbolKind.Field, JavaResources.INSTANCE.publicField()),
                    put(SymbolKind.Constructor, JavaResources.INSTANCE.publicMethod()),
                    put(SymbolKind.Enum, JavaResources.INSTANCE.enumItem()),
                    put(SymbolKind.Interface, JavaResources.INSTANCE.interfaceItem()),
                    put(SymbolKind.Field, JavaResources.INSTANCE.publicField()),
                    put(SymbolKind.Constant, JavaResources.INSTANCE.publicField()),
                    put(SymbolKind.EnumMember, JavaResources.INSTANCE.publicField()))),
            put(
                Visibility.PROTECTED,
                map(
                    put(SymbolKind.Package, JavaResources.INSTANCE.packageItem()),
                    put(SymbolKind.Class, JavaResources.INSTANCE.svgClassItem()),
                    put(SymbolKind.Method, JavaResources.INSTANCE.protectedMethod()),
                    put(SymbolKind.Field, JavaResources.INSTANCE.protectedField()),
                    put(SymbolKind.Constructor, JavaResources.INSTANCE.protectedMethod()),
                    put(SymbolKind.Enum, JavaResources.INSTANCE.enumItem()),
                    put(SymbolKind.Interface, JavaResources.INSTANCE.interfaceItem()),
                    put(SymbolKind.Field, JavaResources.INSTANCE.protectedField()),
                    put(SymbolKind.Constant, JavaResources.INSTANCE.protectedField()),
                    put(SymbolKind.EnumMember, JavaResources.INSTANCE.protectedField()))),
            put(
                Visibility.PACKAGE,
                map(
                    put(SymbolKind.Package, JavaResources.INSTANCE.packageItem()),
                    put(SymbolKind.Class, JavaResources.INSTANCE.svgClassItem()),
                    put(SymbolKind.Method, JavaResources.INSTANCE.defaultMethod()),
                    put(SymbolKind.Field, JavaResources.INSTANCE.defaultField()),
                    put(SymbolKind.Constructor, JavaResources.INSTANCE.defaultMethod()),
                    put(SymbolKind.Enum, JavaResources.INSTANCE.enumItem()),
                    put(SymbolKind.Interface, JavaResources.INSTANCE.interfaceItem()),
                    put(SymbolKind.Field, JavaResources.INSTANCE.defaultField()),
                    put(SymbolKind.Constant, JavaResources.INSTANCE.defaultField()),
                    put(SymbolKind.EnumMember, JavaResources.INSTANCE.defaultField()))),
            put(
                Visibility.PRIVATE,
                map(
                    put(SymbolKind.Package, JavaResources.INSTANCE.packageItem()),
                    put(SymbolKind.Class, JavaResources.INSTANCE.svgClassItem()),
                    put(SymbolKind.Method, JavaResources.INSTANCE.privateMethod()),
                    put(SymbolKind.Field, JavaResources.INSTANCE.privateField()),
                    put(SymbolKind.Constructor, JavaResources.INSTANCE.privateMethod()),
                    put(SymbolKind.Enum, JavaResources.INSTANCE.enumItem()),
                    put(SymbolKind.Interface, JavaResources.INSTANCE.interfaceItem()),
                    put(SymbolKind.Field, JavaResources.INSTANCE.privateField()),
                    put(SymbolKind.Constant, JavaResources.INSTANCE.privateField()),
                    put(SymbolKind.EnumMember, JavaResources.INSTANCE.privateField()))));
  }

  public SVGResource get(ExtendedSymbolInformation symbol) {
    Visibility visiblity = symbol.getVisiblity();
    return resources
        .get(visiblity == null ? Visibility.PACKAGE : visiblity)
        .get(symbol.getInfo().getKind());
  }

  private static <K, V> Consumer<Map<K, V>> put(K key, V value) {
    return new Consumer<Map<K, V>>() {

      @Override
      public void accept(Map<K, V> map) {
        map.put(key, value);
      }
    };
  }

  @SafeVarargs
  private static <K, V> Map<K, V> map(Consumer<Map<K, V>>... entries) {
    HashMap<K, V> result = new HashMap<>(entries.length);
    for (Consumer<Map<K, V>> entry : entries) {
      entry.accept(result);
    }
    return result;
  }
}

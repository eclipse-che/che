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
package org.eclipse.che.ide.editor.orion.client.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.gwt.inject.client.multibindings.GinMultibinder;
import com.google.inject.TypeLiteral;
import org.eclipse.che.ide.api.editor.defaulteditor.EditorBuilder;
import org.eclipse.che.ide.api.editor.texteditor.EditorWidgetFactory;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.editor.orion.client.ContentAssistWidgetFactory;
import org.eclipse.che.ide.editor.orion.client.OrionEditorBuilder;
import org.eclipse.che.ide.editor.orion.client.OrionEditorWidget;
import org.eclipse.che.ide.editor.orion.client.jso.OrionCodeEditWidgetOverlay;
import org.eclipse.che.ide.editor.orion.client.jso.OrionEditorOptionsOverlay;
import org.eclipse.che.ide.editor.orion.client.signature.SignatureWidgetFactory;

@ExtensionGinModule
public class OrionEditorGinModule extends AbstractGinModule {

  @Override
  protected void configure() {

    bind(EditorBuilder.class).to(OrionEditorBuilder.class);

    install(
        new GinFactoryModuleBuilder()
            .build(new TypeLiteral<EditorWidgetFactory<OrionEditorWidget>>() {}));

    bind(OrionCodeEditWidgetOverlay.class).toProvider(OrionCodeEditWidgetProvider.class);
    bind(OrionEditorOptionsOverlay.class).toProvider(OrionEditorOptionsOverlayProvider.class);

    install(new GinFactoryModuleBuilder().build(ContentAssistWidgetFactory.class));
    install(new GinFactoryModuleBuilder().build(SignatureWidgetFactory.class));

    GinMultibinder.newSetBinder(binder(), OrionPlugin.class)
        .addBinding()
        .to(JavaHighlightingOrionPlugin.class);

    // GinMultibinder.newSetBinder(binder(),
    // OrionPlugin.class).addBinding().to(LanguageServerHighlightingOrionPlugin.class);
  }
}

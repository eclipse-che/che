/*******************************************************************************
 * Copyright (c) 2014-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.editor.orion.client.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.editor.orion.client.ContentAssistWidgetFactory;
import org.eclipse.che.ide.editor.orion.client.OrionEditorBuilder;
import org.eclipse.che.ide.editor.orion.client.OrionEditorExtension;
import org.eclipse.che.ide.editor.orion.client.OrionEditorModule;
import org.eclipse.che.ide.editor.orion.client.OrionEditorPresenter;
import org.eclipse.che.ide.editor.orion.client.OrionEditorWidget;
import org.eclipse.che.ide.editor.orion.client.jso.OrionKeyBindingModule;
import org.eclipse.che.ide.api.editor.defaulteditor.EditorBuilder;
import org.eclipse.che.ide.api.editor.texteditor.EditorModule;
import org.eclipse.che.ide.api.editor.texteditor.EditorWidgetFactory;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorPresenter;
import org.eclipse.che.ide.api.editor.texteditor.TextEditorPresenterFactory;
import org.eclipse.che.ide.requirejs.ModuleHolder;

@ExtensionGinModule
public class OrionEditorGinModule extends AbstractGinModule {

    @Override
    protected void configure() {
        // Bind the Orion EditorWidget factory
        install(new GinFactoryModuleBuilder().build(new TypeLiteral<EditorWidgetFactory<OrionEditorWidget>>() {}));
        bind(new TypeLiteral<EditorModule<OrionEditorWidget>>() {}).to(OrionEditorModule.class);
        bind(OrionKeyBindingModule.class).toProvider(OrionEditorExtension.class);
        install(new GinFactoryModuleBuilder().build(ContentAssistWidgetFactory.class));

        install(new GinFactoryModuleBuilder()
                        .implement(new TypeLiteral<TextEditorPresenter<OrionEditorWidget>>() {
                        }, OrionEditorPresenter.class)
                        .build(new TypeLiteral<TextEditorPresenterFactory<OrionEditorWidget>>() {
                        }));

        bind(EditorBuilder.class).to(OrionEditorBuilder.class);

        bind(ModuleHolder.class).in(Singleton.class);
    }
}

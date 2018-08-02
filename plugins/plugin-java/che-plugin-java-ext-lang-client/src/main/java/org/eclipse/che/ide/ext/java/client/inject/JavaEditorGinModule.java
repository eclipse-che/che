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
package org.eclipse.che.ide.ext.java.client.inject;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.gwt.inject.client.assistedinject.GinFactoryModuleBuilder;
import org.eclipse.che.ide.api.editor.formatter.ContentFormatter;
import org.eclipse.che.ide.api.extension.ExtensionGinModule;
import org.eclipse.che.ide.ext.java.client.editor.JavaAnnotationModelFactory;
import org.eclipse.che.ide.ext.java.client.editor.JavaCodeAssistProcessorFactory;
import org.eclipse.che.ide.ext.java.client.editor.JavaFormatter;
import org.eclipse.che.ide.ext.java.client.editor.JavaPartitionScanner;
import org.eclipse.che.ide.ext.java.client.editor.JavaPartitionerFactory;
import org.eclipse.che.ide.ext.java.client.editor.JavaQuickAssistProcessorFactory;
import org.eclipse.che.ide.ext.java.client.editor.JavaReconcileUpdateOperation;
import org.eclipse.che.ide.ext.java.client.editor.JavaReconcilerStrategyFactory;
import org.eclipse.che.ide.ext.java.client.editor.JsJavaEditorConfigurationFactory;

@ExtensionGinModule
public class JavaEditorGinModule extends AbstractGinModule {

  @Override
  protected void configure() {
    install(new GinFactoryModuleBuilder().build(JavaCodeAssistProcessorFactory.class));
    install(new GinFactoryModuleBuilder().build(JavaQuickAssistProcessorFactory.class));
    install(new GinFactoryModuleBuilder().build(JsJavaEditorConfigurationFactory.class));
    install(new GinFactoryModuleBuilder().build(JavaReconcilerStrategyFactory.class));
    install(new GinFactoryModuleBuilder().build(JavaAnnotationModelFactory.class));
    bind(ContentFormatter.class).to(JavaFormatter.class);
    bind(JavaPartitionScanner.class);
    bind(JavaPartitionerFactory.class);
    bind(JavaReconcileUpdateOperation.class).asEagerSingleton();
  }
}

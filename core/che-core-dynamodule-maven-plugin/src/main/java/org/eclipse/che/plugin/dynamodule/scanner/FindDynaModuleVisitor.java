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
package org.eclipse.che.plugin.dynamodule.scanner;

import org.eclipse.che.inject.DynaModule;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/** ASM visitor used to search the {@link DynaModule} annotations in the classes. */
public final class FindDynaModuleVisitor extends ClassVisitor {

  /** Descriptor object of the DynaModule annotation. */
  public static final String DYNAMODULE_DESCRIPTOR = Type.getDescriptor(DynaModule.class);

  /** Name of the class scanned */
  private String classname;

  /** true if {@link DynaModule} is found */
  private boolean found;

  /** Default constructor. */
  public FindDynaModuleVisitor() {
    super(Opcodes.ASM7);
  }

  /**
   * visit the class and keep the name of the class.
   *
   * @param version the version
   * @param access access (public, ...)
   * @param name class name
   * @param signature signature
   * @param superName super class name
   * @param interfaces implemented interfaces name
   */
  @Override
  public void visit(
      final int version,
      final int access,
      final String name,
      final String signature,
      final String superName,
      final String[] interfaces) {
    classname = name.replace("/", ".");
  }

  /**
   * If we visit the matched {@Link DynaModule} annotation, keep it.
   *
   * @param name annotation name
   * @param visible annotation visibility
   * @return a visitor
   */
  @Override
  public AnnotationVisitor visitAnnotation(final String name, final boolean visible) {
    if (DYNAMODULE_DESCRIPTOR.equals(name) && visible) {
      found = true;
    }
    return null;
  }

  /**
   * It's true if the class is annotated with {@link DynaModule}
   *
   * @return true if annotated, else false
   */
  public boolean isDynaModule() {
    return found;
  }

  /**
   * Gets the name of the class that has been visited
   *
   * @return the name of the class
   */
  public String getClassname() {
    return classname;
  }
}

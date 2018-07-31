/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.dynamodule;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.RETURN;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Set;
import org.eclipse.che.inject.ModuleFinder;
import org.eclipse.che.plugin.dynamodule.scanner.DynaModuleScanner;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * Generates the class implementing the ModuleFinder interface.
 *
 * @author Florent Benoit
 */
public class DynaModuleListByteCodeGenerator {

  /** Package used to generate the class */
  private static final String PACKAGE_NAME = "org/eclipse/che/dynamodule/";

  /** URLS to parse */
  private Collection<URL> urls;

  /** Name of the class to generate */
  private String className = "DynaModuleList";

  /** Pattern for excluding some files. */
  private String[] additionalSkipResources;

  /** Bytecode generated that is corresponding to the generated class. */
  private byte[] classToGenerate;

  /** Instance of the scanner */
  private DynaModuleScanner dynaModuleScanner;

  /** Directory used to unpack war files */
  private File unpackedDirectory;

  /** Scan jar in war files. */
  private boolean scanJarInWarDependencies;

  /** Setup a new generator */
  public DynaModuleListByteCodeGenerator() {
    this.dynaModuleScanner = new DynaModuleScanner();
  }

  /**
   * Search all classes annotated with {@link org.eclipse.che.inject.DynaModule} and generate a
   * class that will provide all these modules
   */
  protected void init() {
    dynaModuleScanner.setAdditionalSkipResources(additionalSkipResources);
    dynaModuleScanner.setUnpackedDirectory(unpackedDirectory);
    dynaModuleScanner.setScanJarInWarDependencies(scanJarInWarDependencies);

    urls.forEach(
        url -> {
          try {
            dynaModuleScanner.scan(url);
          } catch (URISyntaxException | IOException e) {
            throw new IllegalStateException("Unable to initialize the scanner", e);
          }
        });

    dynaModuleScanner.stats();
    generateClass(dynaModuleScanner.getDynaModuleClasses());
  }

  public void setClassName(String className) {
    this.className = className;
  }

  public void setUrls(Collection<URL> urls) {
    this.urls = urls;
  }

  protected void generateClass(Set<String> guiceModules) {
    ClassWriter cw = new ClassWriter(COMPUTE_FRAMES);
    MethodVisitor methodVisitor;

    // interface to implement
    String moduleFinderInternalClassName = Type.getType(ModuleFinder.class).getInternalName();

    cw.visit(
        52,
        ACC_PUBLIC + ACC_SUPER,
        PACKAGE_NAME.concat(className),
        null,
        "java/lang/Object",
        new String[] {moduleFinderInternalClassName});

    // default constructor
    methodVisitor = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
    methodVisitor.visitCode();
    methodVisitor.visitVarInsn(ALOAD, 0);
    methodVisitor.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    methodVisitor.visitInsn(RETURN);
    methodVisitor.visitMaxs(0, 0);
    methodVisitor.visitEnd();

    // implements getModules method
    final MethodVisitor getModulesMethodVisitor =
        cw.visitMethod(
            ACC_PUBLIC,
            "getModules",
            "()Ljava/util/List;",
            "()Ljava/util/List<Lcom/google/inject/Module;>;",
            null);
    getModulesMethodVisitor.visitCode();
    getModulesMethodVisitor.visitTypeInsn(NEW, "java/util/ArrayList");
    getModulesMethodVisitor.visitInsn(DUP);
    getModulesMethodVisitor.visitMethodInsn(
        INVOKESPECIAL, "java/util/ArrayList", "<init>", "()V", false);
    getModulesMethodVisitor.visitVarInsn(ASTORE, 1);

    guiceModules.forEach(
        moduleClassName -> {
          String moduleInternalName = moduleClassName.replace(".", "/");
          // add each modules
          getModulesMethodVisitor.visitVarInsn(ALOAD, 1);
          getModulesMethodVisitor.visitTypeInsn(NEW, moduleInternalName);
          getModulesMethodVisitor.visitInsn(DUP);
          getModulesMethodVisitor.visitMethodInsn(
              INVOKESPECIAL, moduleInternalName, "<init>", "()V", false);
          getModulesMethodVisitor.visitMethodInsn(
              INVOKEINTERFACE, "java/util/List", "add", "(Ljava/lang/Object;)Z", true);
          getModulesMethodVisitor.visitInsn(POP);
        });

    // end of the method getModules()
    getModulesMethodVisitor.visitVarInsn(ALOAD, 1);
    getModulesMethodVisitor.visitInsn(ARETURN);
    getModulesMethodVisitor.visitMaxs(0, 0);
    getModulesMethodVisitor.visitEnd();

    cw.visitEnd();

    this.classToGenerate = cw.toByteArray();
  }

  /** Execute this generator. */
  public byte[] execute() {
    init();
    return this.classToGenerate;
  }

  public void setSkipResources(String[] additionalSkipResources) {
    this.additionalSkipResources = additionalSkipResources;
  }

  protected DynaModuleScanner getDynaModuleScanner() {
    return dynaModuleScanner;
  }

  public void setUnpackedDirectory(File unpackedDirectory) {
    this.unpackedDirectory = unpackedDirectory;
  }

  public void setScanJarInWarDependencies(boolean scanJarInWarDependencies) {
    this.scanJarInWarDependencies = scanJarInWarDependencies;
  }
}

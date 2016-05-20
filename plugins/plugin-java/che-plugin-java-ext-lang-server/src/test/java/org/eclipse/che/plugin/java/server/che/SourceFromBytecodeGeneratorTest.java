/*******************************************************************************
 * Copyright (c) 2012-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.plugin.java.server.che;


import org.eclipse.che.plugin.java.server.SourcesFromBytecodeGenerator;
import org.eclipse.jdt.core.IType;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Evgen Vidolob
 */
// TODO: rework after new Project API
@Ignore
public class SourceFromBytecodeGeneratorTest extends BaseTest {


    private IType type;
    private IType zipFileSystem;

    @Before
    public void findType() throws Exception {
        type = project.findType("com.sun.nio.zipfs.ZipFileStore");
        zipFileSystem = project.findType("com.sun.nio.zipfs.ZipFileSystem");
    }

    @Test
    public void testClassComment() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(type);
        assertThat(source).isNotNull().isNotEmpty()
                  .contains("// Failed to get sources. Instead, stub sources have been generated.")
                  .contains("// Implementation of methods is unavailable.");
    }

    @Test
    public void testPackageDeclaration() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(type);
        assertThat(source).contains("package com.sun.nio.zipfs;");
    }

    @Test
    public void testClassDeclaration() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(type);
        assertThat(source).contains("public class ZipFileStore extends java.nio.file.FileStore {");
    }

    @Test
    public void testFieldsDeclaration() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(type);
        assertThat(source).contains("    private final com.sun.nio.zipfs.ZipFileSystem zfs;");
    }

    @Test
    public void testFieldsDeclaration2() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        assertThat(source).contains("    private boolean readOnly;");
    }

    @Test
    public void testFieldsDeclaration3() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        assertThat(source).contains("    private final boolean createNew;");
    }

    @Test
    public void testFieldsDeclaration4() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        assertThat(source).contains("    private static final java.util.Set<java.lang.String> supportedFileAttributeViews;");
    }

    @Test
    public void testFieldsDeclaration5() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        assertThat(source).contains("    private static final java.lang.String GLOB_SYNTAX = \"glob\";");
    }

    @Test
    public void testFieldsDeclaration6() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        assertThat(source).contains("    private static byte[] ROOTPATH;");
    }

    @Test
    public void testFieldsDeclaration7() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        assertThat(source).contains("    private java.util.LinkedHashMap<com.sun.nio.zipfs.ZipFileSystem.IndexNode,com.sun.nio.zipfs.ZipFileSystem.IndexNode> inodes;");
    }

    @Test
    public void testFieldsDeclaration8() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        assertThat(source).contains("    private com.sun.nio.zipfs.ZipFileSystem.IndexNode root;");
    }

    @Test
    public void testFieldsDeclaration9() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        assertThat(source).contains("    private final int MAX_FLATER = 20;");
    }

    @Test
    public void testConstructorDeclaration() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        assertThat(source).contains("    ZipFileSystem(com.sun.nio.zipfs.ZipFileSystemProvider arg0, java.nio.file.Path arg1, java.util.Map<java.lang.String,?> arg2) throws java.io.IOException { /* compiled code */ }");
    }

    @Test
    public void testMethodDeclaration() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        assertThat(source).contains("    public java.nio.file.spi.FileSystemProvider provider() { /* compiled code */ }");
    }

    @Test
    public void testMethodDeclaration2() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        assertThat(source).contains("    public com.sun.nio.zipfs.ZipPath getPath(java.lang.String arg0, java.lang.String[] arg1) { /* compiled code */ }");
    }

    @Test
    public void testMethodDeclaration3() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        assertThat(source).contains("    void createDirectory(byte[] arg0, java.nio.file.attribute.FileAttribute<?>[] arg1) throws java.io.IOException { /* compiled code */ }");
    }

    @Test
    public void testGenericMethodDeclaration() throws Exception {
        IType iType = project.findType("com.sun.nio.zipfs.ZipFileStore");
        String source = new SourcesFromBytecodeGenerator().generateSource(iType);
        assertThat(source).contains("public <V extends java.nio.file.attribute.FileStoreAttributeView> V getFileStoreAttributeView(java.lang.Class<V> arg0) { /* compiled code */ }");
    }

    @Test
    public void testEnumDeclaration() throws Exception {
        IType enumType = project.findType("javax.servlet.DispatcherType");
        String source = new SourcesFromBytecodeGenerator().generateSource(enumType);
        assertThat(source).contains("\n" +
                                               "public final enum DispatcherType {\n" +
                                               "    FORWARD, INCLUDE, REQUEST, ASYNC, ERROR;\n" +
                                               "\n" +
                                               "    public static javax.servlet.DispatcherType[] values() { /* compiled code */ }\n" +
                                               "\n" +
                                               "    public static javax.servlet.DispatcherType valueOf(java.lang.String name) { /* " +
                                               "compiled code */ }\n" +
                                               "\n" +
                                               "    private DispatcherType() { /* compiled code */ }\n" +
                                               "\n" +
                                               "}");
    }

    @Test
    public void testInnerTypeDeclaration() throws Exception {
        String source = new SourcesFromBytecodeGenerator().generateSource(zipFileSystem);
        assertThat(source).contains("    private static class ExChannelCloser {\n" +
                                               "        java.nio.file.Path path;\n" +
                                               "        java.nio.channels.SeekableByteChannel ch;\n" +
                                               "        java.util.Set<java.io.InputStream> streams;\n" +
                                               "\n" +
                                               "        ExChannelCloser(java.nio.file.Path arg0, java.nio.channels.SeekableByteChannel " +
                                               "arg1, java.util.Set<java.io.InputStream> arg2) { /* compiled code */ }\n" +
                                               "\n" +
                                               "    }");
    }


    @Test
    public void testInterfaceDeclaration() throws Exception {
        IType interfaceType = project.findType("java.lang.CharSequence");
        String source = new SourcesFromBytecodeGenerator().generateSource(interfaceType);
        assertThat(source).contains("public interface CharSequence {");
    }

    @Test
    public void testInterfaceMethodDeclaration() throws Exception {
        IType interfaceType = project.findType("java.lang.CharSequence");
        String source = new SourcesFromBytecodeGenerator().generateSource(interfaceType);
        assertThat(source).contains("    public int length();").contains("    public char charAt(int arg0);")
                  .contains("    public java.lang.CharSequence subSequence(int arg0, int arg1);")
                  .contains("    public java.lang.String toString();");
    }

    @Test
    public void testTypeExtendsGeneric() throws Exception {
        IType interfaceType = project.findType("com.sun.nio.zipfs.ZipDirectoryStream");
        String source = new SourcesFromBytecodeGenerator().generateSource(interfaceType);
        assertThat(source).contains(
        "public class ZipDirectoryStream implements java.nio.file.DirectoryStream<java.nio.file.Path> {\n" +
        "    private final com.sun.nio.zipfs.ZipFileSystem zipfs;\n" +
        "    private final byte[] path;\n" +
        "    private final java.nio.file.DirectoryStream.Filter<? super java.nio.file.Path> filter;\n" +
        "    private volatile boolean isClosed;\n" +
        "    private volatile java.util.Iterator<java.nio.file.Path> itr;\n" +
        "\n" +
        "    ZipDirectoryStream(com.sun.nio.zipfs.ZipPath arg0, java.nio.file.DirectoryStream.Filter<? super java.nio.file.Path> " +
        "arg1) throws java.io.IOException { /* compiled code */ }\n" +
        "\n" +
        "    public synchronized java.util.Iterator<java.nio.file.Path> iterator() { /* compiled code */ }\n" +
        "\n" +
        "    public synchronized void close() throws java.io.IOException { /* compiled code */ }\n" +
        "\n"+
        "}");
    }

    @Test
    public void testGenericInterface() throws Exception {
        IType interfaceType = project.findType("com.google.gwt.user.client.rpc.AsyncCallback");
        String source = new SourcesFromBytecodeGenerator().generateSource(interfaceType);
        assertThat(source).contains("public interface AsyncCallback<T> {\n" +
                                               "\n" +
                                               "    public void onFailure(java.lang.Throwable arg0);\n" +
                                               "\n" +
                                               "    public void onSuccess(T arg0);\n" +
                                               "\n" +
                                               "}");
    }

    @Test
    public void testAnnotation() throws Exception {
        IType interfaceType = project.findType("com.google.gwt.core.client.SingleJsoImpl");
        String source = new SourcesFromBytecodeGenerator().generateSource(interfaceType);
        assertThat(source).contains("public @interface SingleJsoImpl {\n");
    }

    @Test
    public void testAnnotationMethod() throws Exception {
        IType interfaceType = project.findType("com.google.gwt.core.client.SingleJsoImpl");
        String source = new SourcesFromBytecodeGenerator().generateSource(interfaceType);
        assertThat(source).contains("    public java.lang.Class<? extends com.google.gwt.core.client.JavaScriptObject> value();\n");
    }

    @Test
    public void testAnnotationsOnAnnotation() throws Exception {
        IType interfaceType = project.findType("com.google.gwt.core.client.SingleJsoImpl");
        String source = new SourcesFromBytecodeGenerator().generateSource(interfaceType);
        assertThat(source).contains("@java.lang.annotation.Retention(value=java.lang.annotation.RetentionPolicy.RUNTIME)\n")
                  .contains("@java.lang.annotation.Target(value=java.lang.annotation.ElementType.TYPE)\n");
    }
    @Test
    public void testAnnotationsOnMethod() throws Exception {
        IType interfaceType = project.findType("java.util.Date");
        String source = new SourcesFromBytecodeGenerator().generateSource(interfaceType);
        assertThat(source).contains("@java.lang.Deprecated\n    public Date(java.lang.String arg0)");

    }
}

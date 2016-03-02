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
package org.eclipse.che.ide.ext.java;

import org.eclipse.che.ide.ext.java.shared.OpenDeclarationDescriptor;
import org.eclipse.che.jdt.JavaNavigation;
import org.eclipse.che.jdt.SourcesFromBytecodeGenerator;
import org.junit.Ignore;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

/**
 * @author Evgen Vidolob
 */
public class FindDeclarationTest extends BaseTest {

    private JavaNavigation navigation = new JavaNavigation(new SourcesFromBytecodeGenerator());

    @Test
    public void testFindClassIsNotNullOrEmpty() throws Exception {
        OpenDeclarationDescriptor declaration = navigation.findDeclaration(project, "java.lang.String", 3669);
        assertThat(declaration).isNotNull();
    }



    @Test
    public void testFindClassShouldReturnBinaryPath() throws Exception {
        OpenDeclarationDescriptor declaration = navigation.findDeclaration(project, "org.eclipse.che.test.MyClass", 855);
        assertThat(declaration).isNotNull();
        assertThat(declaration.getOffset()).isNotNull();
        assertThat(declaration.getLength()).isNotNull();
        assertThat(declaration.isBinary()).isTrue();
        assertThat(declaration.getPath()).isEqualTo("java.lang.String");
    }

    @Test
    public void testFindClassShouldReturnSourcePath() throws Exception {
        OpenDeclarationDescriptor declaration = navigation.findDeclaration(project, "org.eclipse.che.test.TestClass", 60);
        assertThat(declaration).isNotNull();
        assertThat(declaration.isBinary()).isFalse();
        assertThat(declaration.getPath()).isEqualTo("/test/src/main/java/org/eclipse/che/test/MyClass.java");
    }

    @Test
    public void testConstructorParam() throws Exception {
        OpenDeclarationDescriptor declaration = navigation.findDeclaration(project, "zzz.Z", 115);
        assertThat(declaration).isNotNull();
        assertThat(declaration.isBinary()).isFalse();
        assertThat(declaration.getOffset()).isEqualTo(75);
    }



}

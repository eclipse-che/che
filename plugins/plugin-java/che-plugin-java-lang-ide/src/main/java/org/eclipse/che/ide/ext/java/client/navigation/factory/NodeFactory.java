/*******************************************************************************
 * Copyright (c) 2012-2017 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.client.navigation.factory;

import com.google.inject.assistedinject.Assisted;

import org.eclipse.che.ide.ext.java.client.navigation.node.FieldNode;
import org.eclipse.che.ide.ext.java.client.navigation.node.InitializerNode;
import org.eclipse.che.ide.ext.java.client.navigation.node.MethodNode;
import org.eclipse.che.ide.ext.java.client.navigation.node.TypeNode;
import org.eclipse.che.ide.ext.java.shared.dto.model.CompilationUnit;
import org.eclipse.che.ide.ext.java.shared.dto.model.Field;
import org.eclipse.che.ide.ext.java.shared.dto.model.Initializer;
import org.eclipse.che.ide.ext.java.shared.dto.model.Method;
import org.eclipse.che.ide.ext.java.shared.dto.model.Type;

/**
 * Factory for creating an element for the Navigation tree.
 *
 * @author Valeriy Svydenko
 */
public interface NodeFactory {
    /**
     * Create type node. The core of this node is {@link Type}.
     *
     * @param type
     *         source type for the node
     * @param compilationUnit
     *         an entire Java compilation unit the parent of type
     * @param showInheritedMembers
     *         <code>true</code> if inherited members are shown
     * @param isFromSuper
     *         <code>true</code> if a member is inherited
     * @return instance of {@link TypeNode}
     */
    TypeNode create(Type type,
                    CompilationUnit compilationUnit,
                    @Assisted("showInheritedMembers") boolean showInheritedMembers,
                    @Assisted("isFromSuper") boolean isFromSuper);

    /**
     * Create method node. The core of this node is {@link Method}.
     *
     * @param method
     *         represents of the method
     * @param showInheritedMembers
     *         <code>true</code> if inherited members are shown
     * @param isFromSuper
     *         <code>true</code> if a member is inherited
     * @return instance of {@link MethodNode}
     */
    MethodNode create(Method method,
                      @Assisted("showInheritedMembers") boolean showInheritedMembers,
                      @Assisted("isFromSuper") boolean isFromSuper);

    /**
     * Create filed node. The core of this node is {@link Field}.
     *
     * @param filed
     *         represents of the filed
     * @param showInheritedMembers
     *         <code>true</code> if inherited members are shown
     * @param isFromSuper
     *         <code>true</code> if a member is inherited
     * @return instance of {@link FieldNode}
     */
    FieldNode create(Field filed,
                     @Assisted("showInheritedMembers") boolean showInheritedMembers,
                     @Assisted("isFromSuper") boolean isFromSuper);

    /**
     * Create method node. The core of this node is {@link Method}.
     *
     * @param initializer
     *         represents of the initializer block
     * @param showInheritedMembers
     *         <code>true</code> if inherited members are shown
     * @param isFromSuper
     *         <code>true</code> if a member is inherited
     * @return instance of {@link InitializerNode}
     */
    InitializerNode create(Initializer initializer,
                           @Assisted("showInheritedMembers") boolean showInheritedMembers,
                           @Assisted("isFromSuper") boolean isFromSuper);
}

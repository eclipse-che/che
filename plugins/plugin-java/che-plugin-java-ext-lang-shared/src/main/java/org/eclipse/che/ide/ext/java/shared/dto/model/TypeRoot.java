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
package org.eclipse.che.ide.ext.java.shared.dto.model;

/**
 * Represents an entire Java type root (either an <code>CompilationUnit</code> or an <code>ClassFile
 * </code>).
 *
 * @author Evgen Vidolob
 */
public interface TypeRoot extends JavaElement, Openable {}

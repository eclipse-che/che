/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.ide.ext.java.jdt;

/** Definition of Java partitioning and its partitions. */
//TODO move to java extensions
public interface JavaPartitions {

    /** The identifier of the Java partitioning. */
    String JAVA_PARTITIONING = "___java_partitioning";  //$NON-NLS-1$

    /** The identifier of the single-line (JLS2: EndOfLineComment) end comment partition content type. */
    String JAVA_SINGLE_LINE_COMMENT = "__java_singleline_comment"; //$NON-NLS-1$

    /** The identifier multi-line (JLS2: TraditionalComment) comment partition content type. */
    String JAVA_MULTI_LINE_COMMENT = "__java_multiline_comment"; //$NON-NLS-1$

    /** The identifier of the Javadoc (JLS2: DocumentationComment) partition content type. */
    String JAVA_DOC = "__java_javadoc"; //$NON-NLS-1$

    /** The identifier of the Java string partition content type. */
    String JAVA_STRING = "__java_string"; //$NON-NLS-1$

    /** The identifier of the Java character partition content type. */
    String JAVA_CHARACTER = "__java_character";  //$NON-NLS-1$
}

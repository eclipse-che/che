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
package org.eclipse.che.ide.ext.java.client.editor;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.che.ide.ext.java.jdt.JavaPartitions;
import org.eclipse.che.ide.api.editor.partition.DefaultPartitioner;
import org.eclipse.che.ide.api.editor.partition.DocumentPartitioner;
import org.eclipse.che.ide.api.editor.partition.DocumentPositionMap;
import org.eclipse.che.ide.api.editor.partition.PartitionScanner;
import org.eclipse.che.ide.api.editor.partition.PartitionerFactory;

/**
 * Factory of document partitioner for java documents.
 */
public class JavaPartitionerFactory implements PartitionerFactory {

    /** Array with legal content types. */
    public static final List<String> LEGAL_CONTENT_TYPES = Collections.unmodifiableList(Arrays.asList(
            JavaPartitions.JAVA_DOC,
            JavaPartitions.JAVA_MULTI_LINE_COMMENT,
            JavaPartitions.JAVA_SINGLE_LINE_COMMENT,
            JavaPartitions.JAVA_STRING,
            JavaPartitions.JAVA_CHARACTER
    ));

    private final PartitionScanner scanner;

    @Inject
    public JavaPartitionerFactory(final JavaPartitionScanner scanner,
                                  final DocumentPositionMap documentPositionMap) {
        this.scanner = scanner;
    }

    @Override
    public DocumentPartitioner create(final DocumentPositionMap documentPositionMap) {
        return new DefaultPartitioner(this.scanner,
                                      LEGAL_CONTENT_TYPES,
                                      documentPositionMap);
    }

}

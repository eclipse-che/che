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
package org.eclipse.che.api.vfs.server.impl.memory;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.eclipse.che.api.vfs.server.search.LuceneSearcher;

/**
 * In-memory implementation of LuceneSearcher.
 *
 * @author <a href="mailto:aparfonov@codenvy.com">Andrey Parfonov</a>
 */
public class MemoryLuceneSearcher extends LuceneSearcher {
    public MemoryLuceneSearcher() {
        super();
    }

    @Override
    protected Directory makeDirectory() {
        return new RAMDirectory();
    }
}

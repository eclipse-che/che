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
package org.eclipse.che.api.git.shared;

import org.eclipse.che.dto.shared.DTO;

import java.util.List;

/**
 * Request to show changes between commits. Use {@link #commitA} and {@link #commitB} to specify values for comparison.
 * <ul>
 * <li>If both are omitted then view changes between index and working tree.</li>
 * <li>If both are specified then view changes between two commits.</li>
 * <li>If {@link #commitA} is specified ONLY then behavior is dependent on state of {@link #cached}. If
 * <code>cached==false<code> then view changes between specified commit and working tree. If
 * <code>cached==true<code> then view changes between specified commit and index.</li>
 * </ul>
 *
 * @author <a href="mailto:andrey.parfonov@exoplatform.com">Andrey Parfonov</a>
 * @version $Id: DiffRequest.java 22817 2011-03-22 09:17:52Z andrew00x $
 */
@DTO
public interface DiffRequest extends GitRequest {
    /** Type of diff output. */
    public enum DiffType {
        /** Only names of modified, added, deleted files. */
        NAME_ONLY("--name-only"),
        /**
         * Names staus of modified, added, deleted files.
         * <p/>
         * Example:
         * <p/>
         * <p/>
         * <pre>
         * D   README.txt
         * A   HOW-TO.txt
         * </pre>
         */
        NAME_STATUS("--name-status"),
        RAW("--raw");

        private final String value;

        private DiffType(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }


        @Override
        public String toString() {
            return value;
        }
    }

    /** @return filter of file to show diff. It may be either list of file names or name of directory to show all files under them */
    List<String> getFileFilter();
    
    void setFileFilter(List<String> fileFilter);
    
    DiffRequest withFileFilter(List<String> fileFilter);

    /** @return type of diff output */
    DiffType getType();
    
    void setType(DiffType type);
    
    DiffRequest withType(DiffType type);

    /** @return <code>true</code> if renames must not be showing in diff result */
    boolean isNoRenames();
    
    void setNoRenames(boolean isNoRenames);
    
    DiffRequest withNoRenames(boolean noRenames);

    /** @return limit of showing renames in diff output. This attribute has sense if {@link #noRenames} is <code>false</code> */
    int getRenameLimit();
    
    void setRenameLimit(int renameLimit);
    
    DiffRequest withRenameLimit(int renameLimit);

    /** @return first commit to view changes */
    String getCommitA();
    
    void setCommitA(String commitA);
    
    DiffRequest withCommitA(String commitA);

    /** @return second commit to view changes */
    String getCommitB();
    
    void setCommitB(String commitB);
    
    DiffRequest withCommitB(String commitB);

    /**
     * @return if <code>false</code> (default) view changes between {@link #commitA} and working tree otherwise between {@link #commitA}
     *         and index
     */
    boolean isCached();
    
    void setCached(boolean isCached);
    
    DiffRequest withCached(boolean isCached);
}
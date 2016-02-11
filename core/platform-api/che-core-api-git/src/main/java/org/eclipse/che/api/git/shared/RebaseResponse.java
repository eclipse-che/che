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


import java.util.List;

import org.eclipse.che.dto.shared.DTO;

/**
 * Info received from rebase response
 *
 * @author Dror Cohen
 */
@DTO
public interface RebaseResponse {
    public enum RebaseStatus {
        OK("OK"), 
        ABORTED("Aborted"), 
        FAST_FORWARD("Fast-forward"), 
        ALREADY_UP_TO_DATE("Already up-to-date"), 
        FAILED("Failed"), 
        MERGED("Merged"), 
        CONFLICTING("Conflicting"), 
        STOPPED("Stopped"),
        UNCOMMITTED_CHANGES("Uncommitted Changes"), 
        NOT_SUPPORTED("Not-yet-supported");
        
        private final String value;

		private RebaseStatus(String value) {
            this.value = value;
        }
		
        public String getValue() {
            return value;
        }
    }
    
    public RebaseStatus getStatus();
	
    /* @return files that has conflicts. Empty array if there is no conflicts */
    public List<String> getConflicts();
	
    /* @return files that failed to merge. Empty array if there is aren't any */
    public List<String> getFailed();
}
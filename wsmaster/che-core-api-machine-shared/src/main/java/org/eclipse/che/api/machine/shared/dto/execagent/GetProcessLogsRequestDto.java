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
package org.eclipse.che.api.machine.shared.dto.execagent;

import org.eclipse.che.dto.shared.DTO;

/**
 * @author Dmitry Kuleshov
 */
@DTO
public interface GetProcessLogsRequestDto {
    Integer getPid();
    GetProcessLogsRequestDto withPid(Integer pid);

    String getFrom();
    GetProcessLogsRequestDto withFrom(String from);

    String getTill();
    GetProcessLogsRequestDto withTill(String till);

    String getFormat();
    GetProcessLogsRequestDto withFormat(String format);

    Integer getLimit();
    GetProcessLogsRequestDto withLimit(Integer limit);

    Integer getSkip();
    GetProcessLogsRequestDto withSkip(Integer limit);
}

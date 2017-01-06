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
package org.eclipse.che.api.git;

import org.eclipse.che.api.git.shared.GitUser;
import org.eclipse.che.api.git.shared.Log;
import org.eclipse.che.api.git.shared.Revision;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author andrew00x
 */
public class LogPage implements Log, InfoPage {
    // The same as C git does.
    private static final String     DATE_FORMAT = "EEE MMM dd HH:mm:ss yyyy ZZZZZ";

    private static final DateFormat dateFormat;
    
    protected List<Revision> commits;
    
    static {
        dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        TimeZone timeZone = TimeZone.getDefault();
        dateFormat.setTimeZone(timeZone);
    }

    public LogPage(List<Revision> commits) {
        this.commits = commits;
    }

    /** @see InfoPage#writeTo(java.io.OutputStream) */
    @Override
    public void writeTo(OutputStream out) throws IOException {
        PrintWriter writer = new PrintWriter(out);
        DateFormat df = (DateFormat)dateFormat.clone();
        for (Revision commit : commits) {
            writer.format("commit %s\n", commit.getId());

            GitUser commiter = commit.getCommitter();
            if (commiter != null) {
                writer.format("Author: %1$s <%2$s>\n", commiter.getName(), commiter.getEmail());
            }

            long commitTime = commit.getCommitTime();
            if (commitTime > 0) {
                writer.format("Date:   %s\n", df.format(new Date(commitTime)));
            }

            writer.println();

            // Message with indent.
            String[] lines = commit.getMessage().split("\n");
            for (String line : lines) {
                writer.format("    %s\n", line);
            }

            writer.println();
        }
        writer.flush();
    }

    /** {@inheritDoc} */
    @Override
    public List<Revision> getCommits() {
        return commits;
    }
}

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
package org.eclipse.che.api.factory.server.model.impl;

import org.eclipse.che.api.core.model.factory.Policies;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

/**
 * Data object for {@link Policies}.
 *
 * @author Anton Korneta
 */
@Embeddable
public class PoliciesImpl implements Policies {

    @Column(name = "referer")
    private String referer;

    @Column(name = "match_reopen")
    private String match;

    @Column(name = "creation_strategy")
    private String create;

    @Column(name = "until")
    private Long until;

    @Column(name = "since")
    private Long since;

    public PoliciesImpl() {}

    public PoliciesImpl(String referer,
                        String match,
                        String create,
                        Long until,
                        Long since) {
        this.referer = referer;
        this.match = match;
        this.create = create;
        this.until = until;
        this.since = since;
    }

    public PoliciesImpl(Policies policies) {
        this(policies.getReferer(),
             policies.getMatch(),
             policies.getCreate(),
             policies.getUntil(),
             policies.getSince());
    }

    @Override
    public String getReferer() {
        return referer;
    }

    public void setReferer(String referer) {
        this.referer = referer;
    }

    @Override
    public String getMatch() {
        return match;
    }

    public void setMatch(String match) {
        this.match = match;
    }

    @Override
    public String getCreate() {
        return create;
    }

    public void setCreate(String create) {
        this.create = create;
    }

    @Override
    public Long getUntil() {
        return until;
    }

    public void setUntil(Long until) {
        this.until = until;
    }

    @Override
    public Long getSince() {
        return since;
    }

    public void setSince(Long since) {
        this.since = since;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PoliciesImpl)) return false;
        final PoliciesImpl other = (PoliciesImpl)obj;
        return Objects.equals(referer, other.referer)
               && Objects.equals(match, other.match)
               && Objects.equals(create, other.create)
               && Objects.equals(until, other.until)
               && Objects.equals(since, other.since);
    }

    @Override
    public int hashCode() {
        int result = 7;
        result = 31 * result + Objects.hashCode(referer);
        result = 31 * result + Objects.hashCode(match);
        result = 31 * result + Objects.hashCode(create);
        result = 31 * result + Objects.hashCode(until);
        result = 31 * result + Objects.hashCode(since);
        return result;
    }

    @Override
    public String toString() {
        return "PoliciesImpl{" +
               "referer='" + referer + '\'' +
               ", match='" + match + '\'' +
               ", create='" + create + '\'' +
               ", until=" + until +
               ", since=" + since +
               '}';
    }
}

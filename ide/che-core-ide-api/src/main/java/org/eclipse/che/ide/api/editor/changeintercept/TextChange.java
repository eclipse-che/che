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
package org.eclipse.che.ide.api.editor.changeintercept;

import org.eclipse.che.ide.api.editor.text.TextPosition;

/**
 * The description of a change to be examined and processed through {@link TextChangeInterceptor}s.
 */
public final class TextChange {

    /** The start of the change. */
    private final TextPosition from;
    /** The end of the change. */
    private final TextPosition to;
    /** The new content. */
    private final String newText;

    /**
     * Constructor for {@link TextChange}.<br>
     * It is private, {@link TextChange} instances should be created using {@link Builder}.
     * @param from the start of the change
     * @param to the end of the change
     * @param inserted the new content
     */
    private TextChange(final TextPosition from, final TextPosition to, final String inserted) {
        this.from = from;
        this.to = to;
        this.newText = inserted;
    }

    /**
     * Returns the start of the change.
     * @return the start of the change.
     */
    public TextPosition getFrom() {
        return this.from;
    }

    /**
     * Returns the end of the change.
     * @return the end of the change.
     */
    public TextPosition getTo() {
        return this.to;
    }

    /**
     * Returns the new content.
     * @return the new content.
     */
    public String getNewText() {
        return this. newText;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("TextChange [from=").append(from)
                .append(", to=").append(to)
                .append(", newText=").append(newText)
                .append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((from == null) ? 0 : from.hashCode());
        result = prime * result + ((newText == null) ? 0 : newText.hashCode());
        result = prime * result + ((to == null) ? 0 : to.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TextChange other = (TextChange)obj;
        if (from == null) {
            if (other.from != null) {
                return false;
            }
        } else if (!from.equals(other.from)) {
            return false;
        }
        if (newText == null) {
            if (other.newText != null) {
                return false;
            }
        } else if (!newText.equals(other.newText)) {
            return false;
        }
        if (to == null) {
            if (other.to != null) {
                return false;
            }
        } else if (!to.equals(other.to)) {
            return false;
        }
        return true;
    }

    /**
     * Builder for {@link TextChange}.
     */
    public static final class Builder {

        /** The start of the change. */
        private TextPosition from;
        /** The end of the change. */
        private TextPosition to;
        /** The new content. */
        private String newText;

        /**
         * Sets the start position of the change.
         * @param from the new value
         * @return this object (builder)
         */
        public Builder from(final TextPosition from) {
            this.from = from;
            return this;
        }

        /**
         * Sets the end position of the change.
         * @param from the new value
         * @return this object (builder)
         */
        public Builder to(final TextPosition to) {
            this.to = to;
            return this;
        }

        /**
         * Sets the new text for the change.
         * @param from the new value
         * @return this object (builder)
         */
        public Builder insert(final String inserted) {
            this.newText = inserted;
            return this;
        }

        /**
         * Creates the instance of {@link TextChange} using the values that where provided.
         * @return the {@link TextChange}
         */
        public TextChange build() {
            return new TextChange(this.from, this.to, this.newText);
        }
    }
}

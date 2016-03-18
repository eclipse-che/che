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
package org.eclipse.che.api.project.server;

import org.eclipse.che.api.core.ServerException;

import java.util.Properties;
import java.util.Set;

/**
 * Stores additional information about single project.
 *
 * @author andrew00x
 */
public class ProjectMisc {
    static final String UPDATED = "updated";
    static final String CREATED = "created";
    static final String CONTENT_ROOT = "contentRoot";

    private final InternalMisc data;
    private final Project      project;

    public ProjectMisc(Properties properties, Project project) {
        this.data = new InternalMisc(properties);
        this.project = project;
    }

    public ProjectMisc(Project project) {
        this.data = new InternalMisc();
        this.project = project;
    }

    final Project getProject() {
        return project;
    }

    public long getModificationDate() {
        return data.getLong(UPDATED, -1L);
    }

    public long getCreationDate() {
        return data.getLong(CREATED, -1L);
    }

    public String getContentRoot() {
        return data.get(CONTENT_ROOT);
    }

    public void setContentRoot(String contentRoot) {
        data.set(CONTENT_ROOT, contentRoot);
    }

    public void setModificationDate(long date) {
        data.setLong(UPDATED, date);
    }

    public void setCreationDate(long date) {
        data.setLong(CREATED, date);
    }

    public void save() throws ServerException {
        project.saveMisc(this);
    }

    boolean isUpdated() {
        return data.isUpdated();
    }

    Properties asProperties() {
        return data.properties;
    }

    private static class InternalMisc {
        final Properties properties;
        boolean updated;

        boolean isUpdated() {
            synchronized (properties) {
                return updated;
            }
        }

        InternalMisc() {
            this(new Properties());
        }

        InternalMisc(Properties properties) {
            this.properties = properties;
        }

        String get(String name) {
            return properties.getProperty(name);
        }

        void set(String name, String value) {
            if (name == null) {
                throw new IllegalArgumentException("The name of property may not be null. ");
            }
            if (value == null) {
                properties.remove(name);
            } else {
                properties.setProperty(name, value);
            }
            synchronized (properties) {
                updated = true;
            }
        }

        boolean getBoolean(String name) {
            return getBoolean(name, false);
        }

        boolean getBoolean(String name, boolean defaultValue) {
            final String str = get(name);
            return str == null ? defaultValue : Boolean.parseBoolean(str);
        }

        void setBoolean(String name, boolean value) {
            set(name, String.valueOf(value));
        }

        int getInt(String name) {
            return getInt(name, 0);
        }

        int getInt(String name, int defaultValue) {
            final String str = get(name);
            if (str == null)
                return defaultValue;
            try {
                return Integer.parseInt(str);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        void setInt(String name, int value) {
            set(name, String.valueOf(value));
        }

        long getLong(String name) {
            return getLong(name, 0L);
        }

        long getLong(String name, long defaultValue) {
            final String str = get(name);
            if (str == null)
                return defaultValue;
            try {
                return Long.parseLong(str);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        void setLong(String name, long value) {
            set(name, String.valueOf(value));
        }

        float getFloat(String name) {
            return getFloat(name, 0.0F);
        }

        float getFloat(String name, float defaultValue) {
            final String str = get(name);
            if (str == null)
                return defaultValue;
            try {
                return Float.parseFloat(str);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        void setFloat(String name, float value) {
            set(name, String.valueOf(value));
        }


        double getDouble(String name) {
            return getDouble(name, 0.0);
        }

        double getDouble(String name, double defaultValue) {
            final String str = get(name);
            if (str == null)
                return defaultValue;
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }

        void setDouble(String name, double value) {
            set(name, String.valueOf(value));
        }

        Set<String> getNames() {
            return properties.stringPropertyNames();
        }
    }
}

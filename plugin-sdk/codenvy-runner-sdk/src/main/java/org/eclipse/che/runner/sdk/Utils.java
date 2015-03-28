/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.runner.sdk;

import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.core.util.LineConsumer;
import org.eclipse.che.api.core.util.ListLineConsumer;
import org.eclipse.che.api.core.util.DownloadPlugin;
import org.eclipse.che.api.core.util.HttpDownloadPlugin;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.api.core.util.ValueHolder;
import org.eclipse.che.api.project.server.Constants;
import org.eclipse.che.api.project.shared.dto.ProjectDescriptor;
import org.eclipse.che.commons.lang.IoUtil;
import org.eclipse.che.ide.commons.GwtXmlUtils;
import org.eclipse.che.ide.maven.tools.MavenUtils;
import org.eclipse.che.ide.maven.tools.Model;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A smattering of useful methods.
 *
 * @author Artem Zatsarynnyy
 */
class Utils {
    private static DownloadPlugin downloadPlugin = new HttpDownloadPlugin();

    /** Not instantiable. */
    private Utils() {
    }

    /** Returns URL to get Tomcat binary distribution. */
    static URL getTomcatBinaryDistribution() throws IOException {
        URL tomcatDistributionUrl = Thread.currentThread().getContextClassLoader().getResource("tomcat.zip");
        if (tomcatDistributionUrl == null) {
            throw new IOException("Unable to get Tomcat binary distribution.");
        }
        return tomcatDistributionUrl;
    }

    /** Returns URL to get Codenvy Platform binary distribution. */
    static URL getCodenvyPlatformBinaryDistribution() throws IOException {
        URL codenvyPlatformDistributionUrl = Thread.currentThread().getContextClassLoader().getResource("CodenvyPlatform.zip");
        if (codenvyPlatformDistributionUrl == null) {
            throw new IOException("Unable to get Codenvy Platform binary distribution.");
        }
        return codenvyPlatformDistributionUrl;
    }

    /** Download project to the specified destination folder. */
    static java.io.File exportProject(ProjectDescriptor projectDescriptor, java.io.File destinationFolder) throws IOException {
        List<Link> projectLinks = projectDescriptor.getLinks();
        final Link exportZipLink = getLinkByRel(Constants.LINK_REL_EXPORT_ZIP, projectLinks);

        final ValueHolder<IOException> errorHolder = new ValueHolder<>();
        final ValueHolder<java.io.File> resultHolder = new ValueHolder<>();
        downloadPlugin.download(exportZipLink.getHref(), destinationFolder, new DownloadPlugin.Callback() {
            @Override
            public void done(java.io.File downloaded) {
                resultHolder.set(downloaded);
            }

            @Override
            public void error(IOException e) {
                errorHolder.set(e);
            }
        });
        final IOException ioError = errorHolder.get();
        if (ioError != null) {
            throw ioError;
        }
        return resultHolder.get();
    }

    private static Link getLinkByRel(String rel, List<Link> links) {
        for (Link link : links) {
            if (rel.equals(link.getRel())) {
                return link;
            }
        }
        return null;
    }

    /**
     * Builds project with Maven from the specified sources.
     *
     * @param sourcesPath
     *         path to the folder that contains project sources to build
     * @param artifactNamePattern
     *         name pattern of the artifact to return
     * @return {@link java.util.zip.ZipFile} that represents a built artifact
     */
    static ZipFile buildProjectFromSources(Path sourcesPath, String artifactNamePattern) throws IOException, InterruptedException {
        final String[] command = new String[]{MavenUtils.getMavenExecCommand(), "clean", "package"};
        ProcessBuilder processBuilder = new ProcessBuilder(command).directory(sourcesPath.toFile()).redirectErrorStream(true);
        Process process = processBuilder.start();
        ListLineConsumer consumer = new ListLineConsumer();
        ProcessUtil.process(process, consumer, LineConsumer.DEV_NULL);
        process.waitFor();
        if (process.exitValue() != 0) {
            throw new IOException(consumer.getText());
        }
        return new ZipFile(IoUtil.findFile(artifactNamePattern, sourcesPath.resolve("target").toFile()));
    }

    /**
     * Read extension descriptor from the specified JAR.
     *
     * @param zipFile
     *         JAR file with Codenvy Extension
     * @return {@link ExtensionDescriptor}
     * @throws IOException
     *         if can not read specified JAR file
     * @throws IllegalArgumentException
     *         if specified JAR does not contains a valid Codenvy Extension
     */
    static ExtensionDescriptor getExtensionFromJarFile(ZipFile zipFile) throws IOException {
        try {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            ZipEntry gwtXmlEntry = null;
            ZipEntry pomEntry = null;
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    if (entry.getName().endsWith(GwtXmlUtils.GWT_MODULE_XML_SUFFIX)) {
                        gwtXmlEntry = entry;
                    } else if (entry.getName().endsWith("pom.xml")) {
                        pomEntry = entry;
                    }
                }
                // have both entries
                if (pomEntry != null && gwtXmlEntry != null) {
                    break;
                }
            }

            // TODO: consider Codenvy extensions validator
            if (gwtXmlEntry == null || pomEntry == null) {
                throw new IllegalArgumentException(String.format("%s is not a valid Codenvy Extension", zipFile.getName()));
            }

            String gwtModuleName = gwtXmlEntry.getName();
            gwtModuleName = gwtModuleName.substring(0, gwtModuleName.length() - GwtXmlUtils.GWT_MODULE_XML_SUFFIX.length());
            Model pom = Model.readFrom(zipFile.getInputStream(pomEntry));
            List<String> sourceDirectories = MavenUtils.getSourceDirectories(pom);
            sourceDirectories.addAll(MavenUtils.getResourceDirectories(pom));
            for (String src : sourceDirectories) {
                if (gwtModuleName.startsWith(src))
                    gwtModuleName = gwtModuleName.replace(src,"");
            }
            gwtModuleName = gwtModuleName.replace(java.io.File.separatorChar, '.');
            if (gwtModuleName.startsWith("."))
                gwtModuleName = gwtModuleName.replaceFirst(".","");
            return new ExtensionDescriptor(gwtModuleName, MavenUtils.getGroupId(pom), pom.getArtifactId(), MavenUtils.getVersion(pom));
        } finally {
            zipFile.close();
        }
    }

    static class ExtensionDescriptor {
        String gwtModuleName;
        String groupId;
        String artifactId;
        String version;

        ExtensionDescriptor(String gwtModuleName, String groupId, String artifactId, String version) {
            this.gwtModuleName = gwtModuleName;
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }
    }
}

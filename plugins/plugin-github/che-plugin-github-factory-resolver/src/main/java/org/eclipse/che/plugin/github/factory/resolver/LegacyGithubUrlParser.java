package org.eclipse.che.plugin.github.factory.resolver;

import org.eclipse.che.plugin.urlfactory.URLChecker;

import javax.inject.Inject;

/**
 * Support old dockerfila and factory filenames;
 *
 * @author Max Shaposhnik
 */
public class LegacyGithubUrlParser extends GithubUrlParser {

    URLChecker urlChecker;

    @Inject
    public LegacyGithubUrlParser(URLChecker urlChecker) {
        this.urlChecker = urlChecker;
    }

    @Override
    public GithubUrl parse(String url) {
        GithubUrl githubUrl = super.parse(url);
        if (!urlChecker.exists(githubUrl.dockerFileLocation())) {
            githubUrl.dockerfileFilename(".codenvy.dockerfile");
        }

        if (!urlChecker.exists(githubUrl.factoryJsonFileLocation())) {
            githubUrl.factoryFilename(".codenvy.json");
        }
        return githubUrl;
    }
}

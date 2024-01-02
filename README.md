[![Dev](https://img.shields.io/static/v1?label=Open%20in&message=Che%20dogfooding%20server%20(with%20VS%20Code)&logo=eclipseche&color=FDB940&labelColor=525C86)](https://che-dogfooding.apps.che-dev.x6e0.p1.openshiftapps.com/#https://github.com/eclipse/che?che-editor=che-incubator/che-code/insiders)
[![Dev](https://img.shields.io/static/v1?label=Open%20in&message=Che%20dogfooding%20server%20(with%20Theia)&logo=eclipseche&color=FDB940&labelColor=525C86)](https://che-dogfooding.apps.che-dev.x6e0.p1.openshiftapps.com/#https://github.com/eclipse/che?che-editor=eclipse/che-theia/next)

### Eclipse Che Server has moved!

If you're looking for the sources of the Eclipse Che Server, it has been relocated here:

https://github.com/eclipse-che/che-server


<div id="header" align="center">

[![Eclipse Che - Eclipse Next-Generation IDE](https://raw.githubusercontent.com/eclipse/che/assets/eclipseche.png)](
https://eclipse.dev/che/)

**Next-generation container development platform, developer workspace server and cloud IDE**

[![Eclipse License](https://img.shields.io/badge/license-Eclipse-brightgreen.svg)](https://github.com/codenvy/che/blob/master/LICENSE)
<a href="https://sonarcloud.io/dashboard?id=org.eclipse.che%3Ache-parent%3Amaster">
<img src="https://sonarcloud.io/images/project_badges/sonarcloud-black.svg" width="94" height="20" href="" />
</a>

*Che is Kubernetes-native and places everything the developer needs into containers in Kube pods including dependencies, embedded containerized runtimes, a web IDE, and project code. This makes workspaces distributed, collaborative, and portable to run anywhere Kubernetes runs ... [Read More](https://eclipse.dev/che/features/)*

</div>

![Eclipse Che](https://raw.githubusercontent.com/eclipse/che/assets/screenshoft_che7-quarkus-demo.png)

---

**Visit website at: https://eclipse.dev/che/** and documentation at: https://eclipse.dev/che/docs

- [**Getting Started**](#getting-started)
- [**Using Eclipse Che**](#using-eclipse-che)
- [**Feedback and Community**](#feedback-and-community)
- [**Contributing**](#contributing)
- [**Roadmap**](#roadmap)
- [**License**](#license)

---

### Getting Started
Here you can find links on how to get started with Eclipse Che:
- [Use Eclipse Che online](https://eclipse.dev/che/getting-started/cloud/)
- [Run Eclipse Che on your own Kubernetes cluster](https://eclipse.dev/che/docs/stable/administration-guide/preparing-the-installation/)


### Using Eclipse Che
Here you can find references to useful documentation and hands-on guides to learn how to get the most of Eclipse Che:
- [Customize Che workspaces for your projects](https://eclipse.dev/che/docs/stable/end-user-guide/customizing-workspace-components/)
- [Automatically run VSCode Extensions in Che workspaces](https://eclipse.dev/che/docs/stable/end-user-guide/microsoft-visual-studio-code-open-source-ide/#automating-installation-of-microsoft-visual-studio-code-extensions-at-workspace-startup)
- [Starting a workspace from a Git repository URL](https://eclipse.dev/che/docs/stable/end-user-guide/starting-a-workspace-from-a-git-repository-url/)
- [Making a workspace portable using a devfile](https://eclipse.dev/che/docs/stable/end-user-guide/devfile-introduction/)
- [Configure your instance of Che](https://eclipse.dev/che/docs/stable/administration-guide/checluster-custom-resource-fields-reference/) using the [CheCluster Kubernetes Custom Resource](https://doc.crds.dev/github.com/eclipse-che/che-operator)
- [Use and customize the embedded VSCode extensions registry.](https://eclipse.dev/che/docs/stable/administration-guide/extensions-for-microsoft-visual-studio-code-open-source/#adding-or-removing-extensions-in-the-embedded-open-vsx-registry-instance)

### Feedback and Community
We love to hear from users and developers. Here are the various ways to get in touch with us:
* **Support:** You can ask questions, report bugs, and request features using [GitHub issues](https://github.com/eclipse/che/issues).
* **Public Chat:** Join the public [eclipse-che](https://communityinviter.com/apps/ecd-tools/join-the-community) Mattermost channel to discuss with community and contributors.
* **Twitter:** [@eclipse_che](https://twitter.com/eclipse_che)
* **Mailing List:** [che-dev@eclipse.org](https://accounts.eclipse.org/mailing-list/che-dev)
* **Weekly Meetings:** Join us in our [Che community meeting](https://github.com/eclipse/che/wiki/Che-Dev-Meetings) every second monday.


### Contributing
If you are interested in fixing issues and contributing directly to the code base:
- :bug: [Submitting bugs](https://github.com/eclipse/che/issues/new/choose)
- :page_facing_up: [Contributor license agreement](https://github.com/eclipse/che/wiki/Eclipse-Contributor-Agreement)
- :checkered_flag: [Development workflows](./CONTRIBUTING.md)
- :pencil: [Improve docs](https://github.com/eclipse-che/che-docs)
- :building_construction: [Che architecture](https://eclipse.dev/che/docs/stable/administration-guide/architecture-overview/)
- :octocat: [Che repositories](./CONTRIBUTING.md#other-che-repositories)
- :sparkles: [Good first issue for new contributors](https://github.com/eclipse/che/wiki/Labels#new-contributors)


#### Extending Eclipse Che
- [Customize the default dev tooling container (the universal developer image or UDI).](https://github.com/devfile/developer-images/)
- [Customize the list of getting started samples.](https://eclipse.dev/che/docs/stable/administration-guide/configuring-getting-started-samples/)
- [Add your own editor definition.](https://github.com/eclipse-che/che-plugin-registry/blob/main/che-editors.yaml)

### Roadmap
We maintain the [Che roadmap](https://github.com/eclipse/che/wiki/Roadmap) in the open way. We welcome anyone to ask question and contribute to the roadmap by joining our [community meetings](https://github.com/eclipse/che/wiki/Che-Dev-Meetings).

### License
Che is open sourced under the Eclipse Public License 2.0.

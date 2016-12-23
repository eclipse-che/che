---
tags: [ "eclipse" , "che" ]
title: Factories
excerpt: "Convert Chefiles for execution by factories"
layout: docs
permalink: /:categories/factories/
---
{% include base.html %}
A factory is a Codenvy concept that automates the generation or loading of a workspace using URLs. Codenvy is a hosted version of Eclipse Che that you can deploy onto your own servers or make use of at Codenvy's systems at codenvy.com.

Factories make it possible to execute many of the automation capabilities contained within a Chefile, but in a purely remote syntax. Factories are URLs that you give to others, that when executed by other developers, will generate new workspaces in those acceptors' accounts with cloned projects and ready-to-go commands. Factories are wrapped with policies so that the Factory owner can control when, how, and who is able to make use of the Factory without the acceptors' having to pre-configure any software on their computer.

You can create Chefiles for a local directory from an existing Factory. Or, you can have Codenvy automatically generate a Factory for a source repository that has a Chefile in the root of the repository. Think of factory as a way for you to allow remote users to execute `che dir up` against a repository without those users having to install anything first.

## Create Factory From Chefile
You can create a factory to load within Codenvy from an existing Chefile. In the directory that has your Chefile execute:
```shell  
che dir factory\
```

#!/usr/bin/groovy
@Library('github.com/fabric8io/fabric8-pipeline-library@master')
def canaryVersion = "1.0.${env.BUILD_NUMBER}"
def utils = new io.fabric8.Utils()
mavenNode {
  checkout scm
  if (utils.isCI()){

    mavenCI{}
    
  } else if (utils.isCD()){
    echo 'NOTE: running pipelines for the first time will take longer as build and base docker images are pulled onto the node'
    container(name: 'maven') {

      stage('Build Release'){
        mavenCanaryRelease {
          version = canaryVersion
        }
      }
    }
  }
}
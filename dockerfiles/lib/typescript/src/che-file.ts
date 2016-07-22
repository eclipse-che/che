/*
 * Copyright (c) 2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */

// imports
import {RemoteIp} from './remoteip';
import {RecipeBuilder} from './recipebuilder';
import {Workspace} from './workspace';
import {WorkspaceDto} from './dto/workspacedto';
import {Websocket} from './websocket';


export class CheFile {

  // grab default hostname from the remote ip component
  DEFAULT_HOSTNAME: string;

  debug: boolean = false;
  times: number = 10;

  // gloabl var
  waitDone = false;
  che: { hostname: string, server: any };
  dockerContent;

  // requirements
  path = require('path');
  http = require('http');
  fs = require('fs');
  vm = require('vm');
  readline = require('readline');
  exec = require('child_process').exec;

  // init folder/files variables
  currentFolder: string = this.path.resolve('./');
  folderName: any;
  cheFile : any;
  dotCheFolder : any;
  confFolder : any;
  workspacesFolder : any;
  chePropertiesFile : any;

  mode;
  args : Array<string>;

  constructor(args) {
    this.args = args;

    this.DEFAULT_HOSTNAME = new RemoteIp().getIp();
    this.che =  {hostname: this.DEFAULT_HOSTNAME, server: 'tmp'};


    this.currentFolder = this.path.resolve(args[0]);
    this.folderName = this.path.basename(this.currentFolder);
    this.cheFile = this.path.resolve(this.currentFolder, 'chefile');
    this.dotCheFolder = this.path.resolve(this.currentFolder, '.che');
    this.confFolder = this.path.resolve(this.dotCheFolder, 'conf');
    this.workspacesFolder = this.path.resolve(this.dotCheFolder, 'workspaces');
    this.chePropertiesFile = this.path.resolve(this.confFolder, 'che.properties');

  }


  startsWith(value:string, searchString: string) : boolean {
    return value.substr(0, searchString.length) === searchString;
  }





  run() {
    if (this.args.length == 0) {
      console.log('only init and up commands are supported.');
      return;
    } else if ('init' === this.args[1]) {
      this.init();
    } else if ('up' === this.args[1]) {
      this.init();
      this.up();
    } else {
      console.log('Invalid arguments ' + this.args +': Only init and up commands are supported.');
      return;
    }

  }

  parse() {

    try {
      this.fs.statSync(this.cheFile);
      // we have a file
    } catch (e) {
      console.log('No chefile defined, use default settings');
      return;
    }

    // load the chefile script if defined
    var script_code = this.fs.readFileSync(this.cheFile);

    // setup the bindings for the script
    this.che.server =  {};
    this.che.server.ip = this.che.hostname;

    // create sandboxed object
    var sandbox = { "che": this.che, "console": console};

    var script = this.vm.createScript(script_code);
    script.runInNewContext(sandbox);

    if (this.debug) {
      console.log('Che file parsing object is ', this.che);
    }
  }


  init() {
    // needs to create folders
    this.initCheFolders();
    this.setupConfigFile();

    console.log('Che configuration initialized in ' + this.dotCheFolder );
  }

  up() {
    this.parse();

    // test if conf is existing
    try {
      var statsPropertiesFile = this.fs.statSync(this.chePropertiesFile);
    } catch (e) {
      console.log('No che configured. che init has been done ?');
      return;
    }

    console.log('Starting che');
    // needs to invoke docker run
    this.cheBoot();

    this.dockerContent = new RecipeBuilder().getDockerContent();

    // loop to check startup (during 30seconds)
    this.waitCheBoot(this);
  }



// Create workspace based on the remote hostname and workspacename
// if custom docker content is provided, use it
  createWorkspace(remoteHostname, workspaceName, dockerContent) {
    var options = {
      hostname: remoteHostname,
      port: 8080,
      path: '/api/workspace?account=',
      method: 'POST',
      headers: {
        'Accept': 'application/json, text/plain, */*',
        'Content-Type': 'application/json;charset=UTF-8'
      }
    };
    var req = this.http.request(options, (res) => {
      res.on('data', (body) => {

        if (res.statusCode == 201) {
          // workspace created, continue
          this.displayUrlWorkspace(JSON.parse(body));
        } else {
          // error
          console.log('Invalid response from the server side. Aborting');
          console.log('response was ' + body);
        }
      });
    });
    req.on('error', (e) => {
      console.log('problem with request: ' + e.message);
    });

    var workspace = {
      "defaultEnv": "default",
      "commands": [],
      "projects": [],
      "environments": [{
        "machineConfigs": [{
          "dev": true,
          "servers": [],
          "envVariables": {},
          "limits": {"ram": 2500},
          "source": {"type": "dockerfile", "content": dockerContent},
          "name": "default",
          "type": "docker",
          "links": []
        }], "name": "default"
      }],
      "name": workspaceName,
      "links": [],
      "description": null
    };


    req.write(JSON.stringify(workspace));
    req.end();

  }

  displayUrlWorkspace(workspace) {

    var found = false;
    var i = 0;
    var links = workspace.links;
    while (i < links.length && !found) {
      // display the ide url link
      var link = links[i];
      if (link.rel === 'ide url') {
        found = true;
        console.log('Open browser to ' + link.href);
      }
      i++;

    }

    if (!found) {
      console.log('Workspace successfully started but unable to find workspace link');
    }


  }

  initCheFolders() {

    // create .che folder
    try {
      this.fs.mkdirSync(this.dotCheFolder, 0o744);
    } catch (e) {
      // already exist
    }

    // create .che/workspaces folder
    try {
      this.fs.mkdirSync(this.workspacesFolder, 0o744);
    } catch (e) {
      // already exist
    }

    // create .che/conf folder

    try {
      this.fs.mkdirSync(this.confFolder, 0o744);
    } catch (e) {
      // already exist
    }


    // copy configuration file

    try {
      var stats = this.fs.statSync(this.chePropertiesFile);
    } catch (e) {
      // file does not exist, copy it
      this.fs.writeFileSync(this.chePropertiesFile, this.fs.readFileSync(this.path.resolve(__dirname, 'che.properties')));
    }

  }


  setupConfigFile() {
    // need to setup che.properties file with workspaces folder

    // update che.user.workspaces.storage
    this.updateConfFile('che.user.workspaces.storage', this.workspacesFolder);

    // update extra volumes
    this.updateConfFile('machine.server.extra.volume', this.currentFolder + ':/projects/' + this.folderName);

  }

  updateConfFile(propertyName, propertyValue) {

    var content = '';
    var foundLine = false;
    this.fs.readFileSync(this.chePropertiesFile).toString().split('\n').forEach((line) => {

      var updatedLine;



      if (this.startsWith(line, propertyName)) {
        foundLine = true;
        updatedLine = propertyName + '=' + propertyValue + '\n';
      } else {
        updatedLine = line  + '\n';
      }

      content += updatedLine;
    });

    // add property if not present
    if (!foundLine) {
      content += propertyName + '=' + propertyValue + '\n';
    }

    this.fs.writeFileSync(this.chePropertiesFile, content);

  }

  cheBoot() {

    var commandLine: string = 'docker run ' +
        ' -v /var/run/docker.sock:/var/run/docker.sock' +
        ' -e CHE_DATA_FOLDER=' + this.workspacesFolder +
        ' -e CHE_CONF_FOLDER=' + this.confFolder +
        ' codenvy/che-launcher:nightly start';

    if (this.debug) {
      console.log('Executing command line', commandLine);
    }
    var child = this.exec(commandLine , function callback(error, stdout, stderr) {
          //console.log('error is ' + error, stdout, stderr);
        }
    );

    //if (debug) {
    child.stdout.on('data', (data) => {
      console.log(data.toString());
    });
    //}

  }


// test if can connect on port 8080
  waitCheBoot(self: CheFile) {

    if(self.times < 1) {
      return;
    }
    //console.log('wait che on boot', times);
    var options = {
      hostname: self.che.hostname,
      port: 8080,
      path: '/api/workspace',
      method: 'GET',
      headers: {
        'Accept': 'application/json, text/plain, */*',
        'Content-Type': 'application/json;charset=UTF-8'
      }
    };
    if (self.debug) {
      console.log('using che ping options', options, 'and docker content', self.dockerContent);
    }

    var req = self.http.request(options, (res) => {
      res.on('data', (body) => {

        if (res.statusCode === 200 && !self.waitDone) {
          self.waitDone = true;
          if (self.debug) {
            console.log('status code is 200, creating workspace');
          }
          self.createWorkspace(self.che.hostname, 'local', self.dockerContent);
        }
      });
    });
    req.on('error', (e) => {
      if (self.debug) {
        console.log('with request: ' + e.message);
      }
    });


    req.end();


    self.times--;
    if (self.times > 0 && !self.waitDone) {
      setTimeout(self.waitCheBoot, 5000, self);
    }


  }
}

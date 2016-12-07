/*
 * Copyright (c) 2016-2016 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 */
import {ContainerVersion} from "./container-version";


/**
 * Defines a way to get ssh key (private key and public key)
 * @author Florent Benoit
 */
export class SSHGenerator {


   generateKey() : any {
       let map : Map<string,string> = new Map<string, string>();
       var execSync = require('child_process').execSync;
       let output:string = execSync('docker run --rm --entrypoint /bin/sh codenvy/alpine_jdk8 -c "sudo /usr/bin/ssh-keygen -t rsa -A && echo PRIVATE_KEY_START && sudo cat /etc/ssh/ssh_host_rsa_key && echo PRIVATE_KEY_END && echo PUBLIC_KEY_START && sudo cat /etc/ssh/ssh_host_rsa_key.pub &&echo PUBLIC_KEY_END"').toString();

       // now grab private key
       let isPrivateKey : boolean = false;
       let isPublicKey : boolean = false;
       let publicKey: string = '';
       let privateKey : string = '';
       let lines : Array<string> = output.split("\n");
       let i : number = 0;

       // TODO : use regexp there
       while (i < lines.length) {
         let line = lines[i];
         if (line === 'PRIVATE_KEY_START') {           
             isPrivateKey = true;
             i++;
             continue;
         } else if (line === 'PRIVATE_KEY_END') {
             isPrivateKey = false;
             i++;
             continue;
         } else if (line === 'PUBLIC_KEY_START') {
             isPrivateKey = false;
             isPublicKey = true;
             i++;
             continue;
         } else if (line === 'PUBLIC_KEY_END') {
             isPublicKey = false;
             i++;
             continue;
         }
         // line could have been moved
         line = lines[i];


         if (isPrivateKey) {
            if (privateKey.length > 0) {
                privateKey = privateKey + '\n' + line;
            } else {
                privateKey = line;
            }
         }

        if (isPublicKey) {
            if (publicKey.length > 0) {
                publicKey = publicKey + '\n' + line;
            } else {
                publicKey = line;
            }
        }
         i++;
       }

    map.set('private', privateKey);
    map.set('public', publicKey);
    
    return map;
   }

}


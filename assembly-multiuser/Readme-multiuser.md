### Getting Started with multiuser Che
To run an multiuser Che version, the following steps are required after building the branch:
 - Rebuild init, cli and che images (in the given sequence). To to that, proceed to folder _dockerfiles/<inage_name>_ and run _build.sh_
 - Run che usual way using cli, with additional parameters:  `-e CHE_MULTIUSER=true` and `--skip:pull --skip:nightly`  
   Full command example:
   `docker run -it --rm -v /var/run/docker.sock:/var/run/docker.sock -v /home/user/.che:/data -e CHE_MULTIUSER=true eclipse/che-cli:nightly start --skip:pull --skip:nightly`
 - MacOS users may need to edit _che.env_ file in the data folder, changing `CHE_HOST` and `CHE_KEYCLOAK_AUTH-SERVER-URL` values to their specific IP.
 
When start is succeeded, the following containers should be created:  
 - che, exposing 8080 port;
 - che_keycloak, exposing 5050 port;  
 - che_postgres, exposing 5432 port;  

## How to Build Plugins?

In plugin root directory, run:

`mvn clean install`

## How to Add Custom Plugin to Modules Build Cycle?

Add a module to plugins parent pom.xml:

`<module>your-plugin-name</module>`

## How to Add a Custom Plugin to Che Assembly?

See: [Building Custom Assemblies] (https://eclipse-che.readme.io/docs/plug-ins#add-plug-in-by-referencing-from-che)

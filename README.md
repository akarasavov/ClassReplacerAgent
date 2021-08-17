# Class replace agent

The agent uses class name selector to find all classes that should be replaced in the original jar.

### External Dependencies

- javassist(compile+runtime). The library is distributed under the triple license of the MPL, the LGPL, and the Apache License
- junit(testing only)

### How to build the agent

`mvn clean package`

### How to run the agent

`java -javaagent:agentPath.jar=classNameSelector=selectorValue,classCount=Integer,overrideJar=Path,logsDir=Path -jar jarPath`

*classNameSelector parameter* 

Class name selector defines what classes should be replaced in the original jar. For example, if `classNameSelector=Point` agent replaces all classes that contain Point in their class name.

*overrideJar parameter*

Defines path to overrode classes. The override jar should contain all classes that classNameSelector can potentially select.

*classCount parameter*

Defines maximum number of classes that can be replaced. The class transformation stops once the parameter is exceeded 

*logsDir parameter*

Agent uses `logsDir` as a root folder for the debug logs file. If `logsDir` is not defined agent will use System.out to print the debug logs.

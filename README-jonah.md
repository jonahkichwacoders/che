# Che with Debug Server protocol

To build and run:

0. Create an empty directory. (On Windows do this in `%userprofile%` somewhere)
1. Set-up normal build environment for Che: https://github.com/eclipse/che/wiki/Development-Workflow
2. Build dependencies:
```
# Debugger Server Protocol implementation in Java
git clone git@github.com:tracymiranda/dsp4e.git
mvn -f dsp4e/org.eclipse.dsp4j clean install

# Eclipse Che Dependencies
git clone --branch dsp git@github.com:jonahkichwacoders/che-dependencies.git
mvn -f che-dependencies clean install
```
3. Build Che:
```
# Clone
git clone --branch dsp git@github.com:jonahkichwacoders/che.git
# Build DSP plug-ins
mvn -f che/plugins/plugin-dsp clean install
# Create assembly
mvn -f che/assembly clean install
```
4. Run Che:
```
docker run --rm -it -v /var/run/docker.sock:/var/run/docker.sock -v $PWD/che-dsp-data:/data -v $PWD/che:/repo eclipse/che:5.18.0 start
```
5. Create and run a new stack in Che with recipe `FROM jonahkichwacoders/mockdebug`
6. Create a project called `readme` with a file called `readme.md` in it.
7. Create a new Debug Server Protocl debug configuration and debug away.
   1. Command: `/usr/bin/node`
   2. Argument: `/home/user/andreweinand.mock-debug-0.23.0/out/mockDebug.js`
   3. JSON:
```
{
	"type": "mock",
	"request": "launch",
	"name": "Mock Debug",
	"program": "/projects/readme/readme.md",
	"stopOnEntry": true,
	"trace": true
}
```

# Dev Build command

These are some commands I use to rebuild parts of Che

```
# assemble server only
time (\
mvn -f che/plugins/plugin-dsp/che-plugin-dsp-server fmt:format \
&& mvn -f che/plugins/plugin-dsp/che-plugin-dsp-server clean install -Pfast \
&& mvn -f che/assembly/assembly-wsagent-war clean install \
&& mvn -f che/assembly/assembly-wsagent-server clean install \
&& mvn -f che/assembly/assembly-main clean install \
&& docker run --privileged --rm -it -v /var/run/docker.sock:/var/run/docker.sock -v $PWD/che-dsp-data:/data -v $PWD/che:/repo eclipse/che:5.18.0 restart --fast \
) 2>&1 | ts

# assemble everything
time (\
mvn -f che/plugins/plugin-dsp fmt:format \
&& mvn -f che/plugins/plugin-dsp clean install -Pfast \
&& mvn -f che/assembly/clean install \
&& docker run --privileged --rm -it -v /var/run/docker.sock:/var/run/docker.sock -v $PWD/che-dsp-data:/data -v $PWD/che:/repo eclipse/che:5.18.0 restart --fast \
) 2>&1 | ts

```

# Build Che IDE faster

To reduce number of permutations, you can edit ide/che-core-ide-generators/src/main/resources/org/eclipse/che/util/gwt.xml.template and uncomment the commented out XML.

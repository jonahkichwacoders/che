# Che with Debug Server protocol

To build and run:

0. Create an empty directory. (On Windows do this in `%userprofile%` somewhere)
1. Set-up normal build environment for Che: https://github.com/eclipse/che/wiki/Development-Workflow
2. Build dependencies:
```
# Debugger Server Protocol implementation in Java
git clone https://github.com/jonahkichwacoders/lsp4j.git
./gradlew build createLocalMavenRepo
mvn -f releng install

# Eclipse Che Dependencies
git clone --branch renesascloud_mar_2017 https://github.com/jonahkichwacoders/che-dependencies.git
mvn -f che-dependencies clean install
```
3. Build Che:
```
# Clone
git clone --branch renesascloud_mar_2017 https://github.com/jonahkichwacoders/che.git
time ( mvn install)  2>&1 | ts
```
4. Run Che:
```
docker run --rm -it -v /var/run/docker.sock:/var/run/docker.sock -v $PWD/che-dsp-data:/data -v $PWD/che:/repo eclipse/che:nightly start
```
4. Edit che.env to use priviledged mode (`CHE_DOCKER_PRIVILEGED=true`) (and `CHE_SINGLE_PORT=true`?)
5. Create and run a new stack in Che with recipe `FROM jonahkichwacoders/mockdebug`
6. Create a project with a file called `readme.md` in it.
7. Create a new Debug Server Protocl debug configuration and debug away.
   1. Command: `/home/user/node/node-v8.9.1-linux-x64/bin/node`
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
11. Create `cproj/main.c` with:

```
#include <stdio.h>

int func1(int a);

int main(void)
{
    printf("Hello, World!\n");
    for (int a = 0; a < 5; a++)
    {
        int b = func1(a);
        printf("%d * %d = %d\n", a, a, b);
    }
}

int func1(int a)
{
    return a * a;
}
```

9. Compile it with `gcc -Wall -pthread -g -o main main.c`
10. Launch config settings:
   1. `/home/user/node/node-v8.9.1-linux-x64/bin/node`
   2. `/home/user/vscode/extensions/webfreak.debug-0.21.2/out/src/gdb.js`
   3. params:
```
        {
            "name": "Launch C program",
            "type": "gdb",
            "request": "launch",
            "target": "/projects/cproj/main",
            "cwd": "/projects/cproj"
        }
```

11. Create `goproj/main.go` with:
```
package main

import "fmt"

func main() {
  fmt.Println("Hello, World!")
  for a := 0; a < 5; a++ {
    b := func1(a)
    fmt.Printf("%d * %d = %d\n", a, a, b)
  }
}

func func1(a int) int {
  return a * a
}
```
12. Launch config settings:
   1. `/home/user/node/node-v8.9.1-linux-x64/bin/node`
   2. `/home/user/vscode/extensions/lukehoban.go-0.6.76/out/src/debugAdapter/goDebug.js`
   3. params:
```
        {
            "name": "Launch go file",
            "type": "go",
            "request": "launch",
            "mode": "debug",
            "program": "/projects/goproj/main.go"
        }
```

13. Create `pythonproj/main.py` with:
```
def func1(a):
  return a * a

def main():
  print("Hello, World!")
  for a in range(5):
    b = func1(a)
    print("%d * %d = %d" % (a, a, b))



if __name__ == "__main__":
  main()

```
12. Launch config settings:
   1. `/home/user/node/node-v8.9.1-linux-x64/bin/node`
   2. `/home/user/vscode/extensions/ms-python.python-2018.1.0/out/client/debugger/Main.js`
   3. params:
```
        {
            "name": "Launch python file",
            "type": "python",
            "request": "launch",
            "stopOnEntry": true,
            "program": "/projects/pythonproj/main.py",
            "cwd": "",
            "console":"none",
            "env": {}
        }
```

# Dev Build command

These are some commands I use to rebuild parts of Che

```
docker run --privileged --rm -it -v /var/run/docker.sock:/var/run/docker.sock -v $PWD/che-dsp-data:/data -v $PWD/che:/repo eclipse/che:nightly start
docker run --privileged --rm -it -e CHE_PORT=9090 -v /var/run/docker.sock:/var/run/docker.sock -v $PWD/che-6.0-dsp-data:/data eclipse/che:6.0.0 start
docker run --privileged --rm -it -e CHE_PORT=9090 -v /var/run/docker.sock:/var/run/docker.sock -v $PWD/che-nightly-dsp-data:/data eclipse/che:nightly start

```

```

# DSP IDE only changes
time ( \
(cd che && git commit -m"WIP" --all) ; \
   unbuffer mvn -f che fmt:format install -pl :che-plugin-dsp-shared,:che-plugin-dsp-ide,:che-plugin-dsp-server,:che-ide-full,:che-ide-gwt-app -Pfast -Dskip-enforce -Dskip-validate-sources \
&& unbuffer mvn -f che/assembly clean install -Pfast \
&& docker run --privileged --rm -it -v /var/run/docker.sock:/var/run/docker.sock -v $PWD/che-dsp-data:/data -v $PWD/che:/repo eclipse/che:nightly restart --fast \
)  2>&1 | ts

# Debugger IDE only changes
time ( \
   (cd che && git add --all && git commit -m"WIP" --allow-empty) \
&& unbuffer mvn -f che fmt:format -pl :che-plugin-debugger-ide,:che-core-api-debug-shared,:che-plugin-debugger-ide,:che-ide-full -Pfast \
&& (cd che && git commit -m"WIP - format" --all --allow-empty) \
&& unbuffer mvn -f che install -pl :che-plugin-debugger-ide,:che-core-api-debug-shared,:che-plugin-debugger-ide,:che-ide-full,:che-ide-gwt-app -Dskip-enforce -Dskip-validate-sources -Pfast \
&& unbuffer mvn -f che/assembly clean install -Dskip-enforce -Dskip-validate-sources -Pfast \
&& docker run --privileged --rm -it -v /var/run/docker.sock:/var/run/docker.sock -v $PWD/che-dsp-data:/data -v $PWD/che:/repo eclipse/che:nightly restart --fast \
)  2>&1 | ts

# DSP Server only changes
time ( \
(cd che && git commit -m"WIP" --all) ; \
   mvn -f che fmt:format install -pl :che-plugin-dsp-shared,:che-plugin-dsp-server -Pfast \
&& mvn -f che/assembly clean install -Pfast \
&& docker run --privileged --rm -it -v /var/run/docker.sock:/var/run/docker.sock -v $PWD/che-dsp-data:/data -v $PWD/che:/repo eclipse/che:nightly restart --fast \
)  2>&1 | ts


mvn gwt:codeserver -pl :che-ide-gwt-app -am -Dskip-enforce -Dskip-validate-sources -Pfast
mvn gwt:codeserver -pl :che-ide-gwt-app -am -Dmaven.main.skip -Dmaven.resources.skip -Dche.dto.skip -Dskip-enforce -Dskip-validate-sources -Pfast


java -agentlib:jdwp=transport=dt_socket,server=y,address=8000,suspend=y org.eclipse.che.testthis.Main
```


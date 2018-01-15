# Che with Debug Server protocol

To build and run:

0. Create an empty directory. (On Windows do this in `%userprofile%` somewhere)
1. Set-up normal build environment for Che: https://github.com/eclipse/che/wiki/Development-Workflow
2. Build dependencies:
```
# Debugger Server Protocol implementation in Java
git clone git@github.com:jonahkichwacoders/lsp4j.git
./gradlew build createLocalMavenRepo
mvn -f releng install

# Eclipse Che Dependencies
git clone --branch dsp6 git@github.com:jonahkichwacoders/che-dependencies.git
mvn -f che-dependencies clean install
```
3. Build Che:
```
# Clone
git clone --branch dsp6 git@github.com:jonahkichwacoders/che.git
mvn install
```
4. Run Che:
```
docker run --rm -it -v /var/run/docker.sock:/var/run/docker.sock -v $PWD/che-dsp-data:/data -v $PWD/che:/repo eclipse/che:nightly start
```
4. Edit che.env to use priviledged mode (`CHE_DOCKER_PRIVILEGED=true`) 
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
8. Add a main.c to the project, for example:

```
/*******************************************************************************
 * Copyright (c) 2017 Kichwa Coders Ltd. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
#include <stdio.h>
#include <string.h>
#include <pthread.h>
#include <stdlib.h>
#include <unistd.h>


#define FOREVER
#define NUMTHREADS 3

pthread_t tid[NUMTHREADS];
int threadNums[NUMTHREADS];

void *doSomeThing(void *arg) {
  int threadNum = *(int *)arg;
  printf("Running thread %d\n", threadNum);

  // Fake some work
  for (int i = 0; i < 0xfffffff; i++)
    ;

  printf("Finishing thread %d\n", threadNum);
  return NULL;
}

int main(void) {
  int err = setvbuf(stdout, NULL, _IONBF, 0);
  if (err != 0) {
      printf("Failed to setvbuf: %s\n", strerror(err));
  }

#ifdef FOREVER
  while (1) {
#endif
  for (int i = 0; i < NUMTHREADS; i++) {
    threadNums[i] = i;
    int err = pthread_create(&tid[i], NULL, &doSomeThing, &threadNums[i]);
    if (err != 0) {
      printf("Failed to create thread %d: %s\n", i, strerror(err));
    } else {
      printf("Created thread %d\n", i);
    }
  }

  printf("Joining threads\n");
  for (int i = 0; i < NUMTHREADS; i++) {
    int err = pthread_join(tid[i], NULL);
    if (err != 0) {
      printf("Failed to join thread %d: %s\n", i, strerror(err));
    } else {
      printf("Joined thread %d\n", i);
    }
  }
#ifdef FOREVER
  printf("Sleeping before restarting.\n");
  sleep(3);
  }
#endif

  
  printf("Main thread done\n");
  return 0;
}
```
9. Compile it with `gcc -Wall -pthread -g -o main main.c`
10. Launch config settings:
   1. `/home/user/node/node-v8.9.1-linux-x64/bin/node`
   2. `/home/user/vscode/extensions/webfreak.debug-0.21.2/out/src/gdb.js`
   3. params:
```
        {
            "type": "gdb",
            "request": "launch",
            "name": "Launch Program",
            "target": "/projects/readme/main",
            "cwd": "/projects/readme"
        }
```

# Dev Build command

These are some commands I use to rebuild parts of Che

```
docker run --privileged --rm -it -v /var/run/docker.sock:/var/run/docker.sock -v $PWD/che-dsp-data:/data -v $PWD/che:/repo eclipse/che:nightly start

```

```

# DSP IDE only changes
time ( \
(cd che && git commit -m"WIP" --all) ; \
   mvn -f che fmt:format install -pl :che-plugin-dsp-shared,:che-plugin-dsp-ide,:che-plugin-dsp-server,:che-ide-full,:che-ide-gwt-app -Pfast \
&& mvn -f che/assembly clean install -Pfast \
&& docker run --privileged --rm -it -v /var/run/docker.sock:/var/run/docker.sock -v $PWD/che-dsp-data:/data -v $PWD/che:/repo eclipse/che:nightly restart --fast \
)  2>&1 | ts

# DSP Server only changes
time ( \
(cd che && git commit -m"WIP" --all) ; \
   mvn -f che fmt:format install -pl :che-plugin-dsp-shared,:che-plugin-dsp-server -Pfast \
&& mvn -f che/assembly clean install -Pfast \
&& docker run --privileged --rm -it -v /var/run/docker.sock:/var/run/docker.sock -v $PWD/che-dsp-data:/data -v $PWD/che:/repo eclipse/che:nightly restart --fast \
)  2>&1 | ts

```


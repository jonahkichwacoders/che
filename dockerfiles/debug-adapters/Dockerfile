FROM eclipse/stack-base:ubuntu

RUN sudo apt-get update && \
	sudo apt-get install bzip2 make mono-runtime bsdmainutils build-essential gdb gcc python -y && \
    sudo rm -rf /var/lib/apt/lists/*

# Install pre-packaged VSCode extensions
# This tar was created by installing desired extensions in VSCode and then tarring
# the ~/.vscode/extensions directory
ADD vscode-extensions.tar.bz2 /home/user/vscode/

# Install Node (node from https://nodejs.org/en/)
ADD node-v8.9.1-linux-x64.tar.xz /home/user/node/

# Install Go (tar from https://golang.org/dl/)
ADD go1.10.linux-amd64.tar.gz /home/user/

ENV GOROOT=/home/user/go
ENV GOPATH=/home/user/go-user
ENV PATH=$GOROOT/bin:$GOPATH/bin:$PATH
RUN mkdir $GOPATH

# Install the go packages needed for the go vscode extension to operate.
RUN go get -u -v github.com/uudashr/gopkgs/cmd/gopkgs
RUN go get -u -v golang.org/x/tools/blog/atom
RUN go get -u -v golang.org/x/tools/present
RUN go get -u -v golang.org/x/tools/go/ast/astutil
RUN go get -u -v golang.org/x/tools/container/intsets
RUN go get -u -v golang.org/x/tools/go/buildutil
RUN go get -u -v golang.org/x/tools/go/types/typeutil
RUN go get -u -v golang.org/x/tools/godoc/vfs
RUN go get -u -v golang.org/x/tools/godoc/redirect
RUN go get -u -v golang.org/x/tools/godoc/static
RUN go get -u -v golang.org/x/tools/godoc/util
RUN go get -u -v golang.org/x/tools/godoc/vfs/httpfs
RUN go get -u -v golang.org/x/tools/godoc/vfs/gatefs
RUN go get -u -v golang.org/x/tools/godoc/vfs/mapfs
RUN go get -u -v golang.org/x/tools/godoc/vfs/zipfs
RUN go get -u -v golang.org/x/tools/playground
RUN go get -u -v golang.org/x/tools/blog
RUN go get -u -v golang.org/x/tools/go/loader
RUN go get -u -v golang.org/x/tools/go/ssa
RUN go get -u -v golang.org/x/tools/go/ssa/ssautil
RUN go get -u -v golang.org/x/tools/go/callgraph
RUN go get -u -v golang.org/x/tools/go/pointer
RUN go get -u -v golang.org/x/tools/godoc/analysis
RUN go get -u -v golang.org/x/tools/godoc
RUN go get -u -v github.com/derekparker/delve/cmd/dlv

FROM ${BUILD_ORGANIZATION}/${BUILD_PREFIX}-debug-adapters:${BUILD_TAG}

# This is the full toolchain for building and debugging the RZ/G board. 
# It is possibly available pre-built from somewhere (The windows version is on https://www.renesas.com/en-eu/products/microcontrollers-microprocessors/rz/rzg/rzg1m.html)
# This version was built using https://elinux.org/RZ-G/Boards/Yocto_2.0
# Due to the size of the sh file it is not in source control
ADD poky-glibc-x86_64-core-image-weston-sdk-cortexa15hf-vfp-neon-toolchain-2.0.1.sh /home/user/

RUN sudo chmod +x /home/user/poky-glibc-x86_64-core-image-weston-sdk-cortexa15hf-vfp-neon-toolchain-2.0.1.sh && sudo /home/user/poky-glibc-x86_64-core-image-weston-sdk-cortexa15hf-vfp-neon-toolchain-2.0.1.sh

# Set-up the environment that would have been created if ". /opt/poky/2.0.1/environment-setup-cortexa15hf-vfp-neon-poky-linux-gnueabi" had been done
# We want to do as few ENV commands as possible as that limits how many intermediary images we have, but 
# because many other variables depend on SDKTARGETSYSROOT, we need to finalize it first
ENV SDKTARGETSYSROOT=/opt/poky/2.0.1/sysroots/cortexa15hf-vfp-neon-poky-linux-gnueabi
ENV PATH=/opt/poky/2.0.1/sysroots/x86_64-pokysdk-linux/usr/bin:/opt/poky/2.0.1/sysroots/x86_64-pokysdk-linux/usr/bin/../x86_64-pokysdk-linux/bin:/opt/poky/2.0.1/sysroots/x86_64-pokysdk-linux/usr/bin/arm-poky-linux-gnueabi:/opt/poky/2.0.1/sysroots/x86_64-pokysdk-linux/usr/bin/arm-poky-linux-uclibc:/opt/poky/2.0.1/sysroots/x86_64-pokysdk-linux/usr/bin/arm-poky-linux-musl:$PATH\
     CCACHE_PATH=/opt/poky/2.0.1/sysroots/x86_64-pokysdk-linux/usr/bin:/opt/poky/2.0.1/sysroots/x86_64-pokysdk-linux/usr/bin/../x86_64-pokysdk-linux/bin:/opt/poky/2.0.1/sysroots/x86_64-pokysdk-linux/usr/bin/arm-poky-linux-gnueabi:/opt/poky/2.0.1/sysroots/x86_64-pokysdk-linux/usr/bin/arm-poky-linux-uclibc:/opt/poky/2.0.1/sysroots/x86_64-pokysdk-linux/usr/bin/arm-poky-linux-musl:$CCACHE_PATH\
     PKG_CONFIG_SYSROOT_DIR=$SDKTARGETSYSROOT\
     PKG_CONFIG_PATH=$SDKTARGETSYSROOT/usr/lib/pkgconfig\
     CONFIG_SITE=/opt/poky/2.0.1/site-config-cortexa15hf-vfp-neon-poky-linux-gnueabi\
     OECORE_NATIVE_SYSROOT="/opt/poky/2.0.1/sysroots/x86_64-pokysdk-linux"\
     OECORE_TARGET_SYSROOT="$SDKTARGETSYSROOT"\
     OECORE_ACLOCAL_OPTS="-I /opt/poky/2.0.1/sysroots/x86_64-pokysdk-linux/usr/share/aclocal"\
     PYTHONHOME=/opt/poky/2.0.1/sysroots/x86_64-pokysdk-linux/usr\
     CC="arm-poky-linux-gnueabi-gcc  -march=armv7-a -mfloat-abi=hard -mfpu=neon -mtune=cortex-a15 --sysroot=$SDKTARGETSYSROOT"\
     CXX="arm-poky-linux-gnueabi-g++  -march=armv7-a -mfloat-abi=hard -mfpu=neon -mtune=cortex-a15 --sysroot=$SDKTARGETSYSROOT"\
     CPP="arm-poky-linux-gnueabi-gcc -E  -march=armv7-a -mfloat-abi=hard -mfpu=neon -mtune=cortex-a15 --sysroot=$SDKTARGETSYSROOT"\
     AS="arm-poky-linux-gnueabi-as "\
     LD="arm-poky-linux-gnueabi-ld  --sysroot=$SDKTARGETSYSROOT"\
     GDB=arm-poky-linux-gnueabi-gdb\
     STRIP=arm-poky-linux-gnueabi-strip\
     RANLIB=arm-poky-linux-gnueabi-ranlib\
     OBJCOPY=arm-poky-linux-gnueabi-objcopy\
     OBJDUMP=arm-poky-linux-gnueabi-objdump\
     AR=arm-poky-linux-gnueabi-ar\
     NM=arm-poky-linux-gnueabi-nm\
     M4=m4\
     TARGET_PREFIX=arm-poky-linux-gnueabi-\
     CONFIGURE_FLAGS="--target=arm-poky-linux-gnueabi --host=arm-poky-linux-gnueabi --build=x86_64-linux --with-libtool-sysroot=$SDKTARGETSYSROOT"\
     CFLAGS=" -O2 -pipe -g -feliminate-unused-debug-types"\
     CXXFLAGS=" -O2 -pipe -g -feliminate-unused-debug-types"\
     LDFLAGS="-Wl,-O1 -Wl,--hash-style=gnu -Wl,--as-needed"\
     CPPFLAGS=""\
     KCFLAGS="--sysroot=$SDKTARGETSYSROOT"\
     OECORE_DISTRO_VERSION="2.0.1"\
     OECORE_SDK_VERSION="2.0.1"\
     ARCH=arm\
     CROSS_COMPILE=arm-poky-linux-gnueabi-

# Some tools (such as Che's default GDB integration) don't allow you to specify the path to GDB itself, to make it easier, as this image is an RZG image,
# make "gdb" on the path the same as the poky GDB.
RUN sudo cp /opt/poky/2.0.1/sysroots/x86_64-pokysdk-linux/usr/bin/arm-poky-linux-gnueabi/arm-poky-linux-gnueabi-gdb /opt/poky/2.0.1/sysroots/x86_64-pokysdk-linux/usr/bin/arm-poky-linux-gnueabi/gdb

RUN sudo apt-get update && \
     sudo apt-get install qemu bzip2 make -y && \
    sudo rm -rf /var/lib/apt/lists/*

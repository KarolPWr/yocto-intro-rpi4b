#!/bin/bash
# uruchamia kontener crops/poky z katalogiem ~/yocto-kurs podmontowanym jako /workdir
# instalacja: cp start_yocto_env.sh ~/ && chmod +x ~/start_yocto_env.sh

docker run --rm --net=host -e HISTFILE=/workdir/.bash_history -it \
    -v "$HOME/yocto-kurs":/workdir \
    crops/poky:ubuntu-22.04 --workdir=/workdir

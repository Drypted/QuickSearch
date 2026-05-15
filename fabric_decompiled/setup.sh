#!/bin/zsh

# fabric source is added as a submodule using command
# git submodule add https://github.com/FabricMC/fabric-api.git fabric_decompiled/src

# reloading
git submodule update --init --recursive --depth 1

cd src

# check out the 1.21.11 tag
git fetch origin tag "0.141.3+1.21.11" --no-tags
git checkout "0.141.3+1.21.11"

cd ..
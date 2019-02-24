#!/bin/bash

set -ex

rm -fr deps.txt
./gradlew allDeps --configuration releaseCompileClasspath > deps.txt

#!/bin/sh
set -e
BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
MAVEN_VERSION=3.9.11
MAVEN_HOME="$HOME/.m2/wrapper/dists/apache-maven-$MAVEN_VERSION"
MAVEN_BIN="$MAVEN_HOME/bin/mvn"
if [ ! -x "$MAVEN_BIN" ]; then
  mkdir -p "$MAVEN_HOME"
  tmp="$MAVEN_HOME/maven.zip"
  curl -fsSL "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.zip" -o "$tmp"
  unzip -q "$tmp" -d "$MAVEN_HOME"
  mv "$MAVEN_HOME/apache-maven-$MAVEN_VERSION"/* "$MAVEN_HOME/"
  rmdir "$MAVEN_HOME/apache-maven-$MAVEN_VERSION"
  rm -f "$tmp"
fi
exec "$MAVEN_BIN" "$@"

#!/bin/sh

cd
apk update
apk add git

git clone "$REPO" gedcom
touch gedcom/SERVE_PUBLIC_GED_FILES

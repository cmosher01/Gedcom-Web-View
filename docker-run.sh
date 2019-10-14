#!/bin/sh

cd

i=$((0))
while [ ! -r gedcom/SERVE_PUBLIC_GED_FILES ] ; do
    echo "Waiting for file gedcom/SERVE_PUBLIC_GED_FILES to be readable..."
    sleep 2
    i=$((i+1))
    if [ $i -gt 10 ] ; then
        echo "Timeout waiting for file gedcom/SERVE_PUBLIC_GED_FILES."
        exit 1
    fi
done

exec "$@"

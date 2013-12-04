#!/bin/bash

./dalmatian.sh install

PACKAGES="dnsutils"
echo "Also installing the packages \" "$PACKAGES" \" if not already installed."
for i in "$PACKAGES"
	do sudo apt-get install $i;
done

../client/install-bottle.sh

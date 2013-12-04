#!/bin/sh
echo "Installing python-setuptools, and the python \'bottle\' library"
sudo apt-get install python-setuptools
sudo easy_install -U bottle

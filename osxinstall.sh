#!/bin/sh
#
# This is an attempt to make an installer for Smartphone Pentest Framework
# for OSX, tested on OSX10.8 and should work on OSX10.7
# @ThomasLandJr
#######################################################################


## Install needed packages

echo "$(tput setaf 1)\nInstallin pyserial, pexpect and MySQL-python for python\n"; echo "$(tput sgr0)"
echo "$(tput setaf 1)#########################################\n"; echo "$(tput sgr0)"
echo $cwd;

pip install pyserial
pip install pexpect
sudo pip install MySQL-python

wget http://dl.google.com/android/android-sdk_r21-linux.tgz;
tar -xvzf android-sdk_r21-linux.tgz;
export PATH=${PATH}:${PWD}/android-sdk-linux/tools:${PWD}/android-sdk-linux/platform-tools
android update sdk --no-ui --filter platform-tool
android update sdk --no-ui --filter android-4
android update sdk --no-ui --filter addon-google_apis-google-4
android update sdk --no-ui --filter android-14
android update sdk --no-ui --filter addon-google_apis-google-14
echo "$(tput setaf 1)Creating first time 'framework' database (empty)\n"; echo "$(tput sgr0)"
wget http://cdn.mysql.com/Downloads/MySQL-5.6/mysql-5.6.12-osx10.7-x86_64.dmg
hdiutil mount mysql-5.6.12-osx10.7-x86_64.dmg
sudo installer -package /Volumes/mysql-5.6.12-osx10.7-x86_64/mysql-5.6.12-osx10.7-x86_64.pkg -target "/Volumes/Macintosh HD"
sudo installer -package /Volumes/mysql-5.6.12-osx10.7-x86_64/MySQLStartupItem.pkg -target "/Volumes/Macintosh HD"
hdiutil unmount /Volumes/mysql-5.6.12-osx10.7-x86_64
PATH=$PATH:/usr/local/mysql/bin

sudo /usr/local/mysql/bin/mysqld_safe
/usr/local/mysql/bin/mysqladmin -u root password toor
/usr/local/mysql/bin/mysqladmin -u root -ptoor create framework;
sudo apachectl start
sudo ln -s /usr/local/mysql/lib/libmysqlclient.18.dylib /usr/lib/libmysqlclient.18.dylib
echo "$(tput setaf 1)\n\nInstall of Smartphone Pentest Framework is complete!\n"; echo "$(tput sgr0)"
echo "$(tput setaf 1)You need to edit your config file to your liking in the following location:${PWD}/frameworkconsole/config\n"; echo "$(tput sgr0)"

echo "$(tput setaf 1)Once config is setup you can either run python framework.py in the frameworkconsole directory,\n"; echo "$(tput sgr0)"
echo "$(tput setaf 1)This concludes the install.. enjoy!"; echo "$(tput sgr0)"



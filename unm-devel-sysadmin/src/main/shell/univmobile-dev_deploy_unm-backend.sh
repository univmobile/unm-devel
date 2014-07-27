#!/bin/sh

sudo su - tomcat -c "bin/shutdown.sh"

sudo rm -rf /usr/local/apache-tomcat/webapps/testSP/

sudo cp unm-backend.war /usr/local/apache-tomcat/webapps/testSP.war

sudo su - tomcat -c "bin/startup.sh"

sudo apache2ctl restart

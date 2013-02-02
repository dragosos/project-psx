@echo off
@title Login Server
set CLASSPATH=.;dist\psx.jar;dist\mina-core.jar;dist\slf4j-api.jar;dist\slf4j-jdk14.jar;dist\mysql-connector-java-bin.jar;dist\jep.jar
java -Xmx100m -Dwzpath=wz\ -Drecvops=recvops.properties -Dsendops=sendops.properties -Dip=db.properties.ip -Djavax.net.ssl.keyStore=filename.keystore -Djavax.net.ssl.keyStorePassword=passwd -Djavax.net.ssl.trustStore=filename.keystore -Djavax.net.ssl.trustStorePassword=passwd net.login.LoginServer
pause

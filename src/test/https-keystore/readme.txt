The keystore resides here for testing purposes.

User: Junit Test
Password: jetty9

command on how the keystore file is created with java's keytool tool:
keytool -keystore keystore -alias jetty -genkey -keyalg RSA -validity 36000 -sigalg SHA256withRSA -ext 'SAN=dns:localhost'

See here for documentation:
http://www.eclipse.org/jetty/documentation/current/configuring-ssl.html



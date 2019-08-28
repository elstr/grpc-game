# gRPC workshop

Simple Tic Tac Toe game

Some quick guidelines
* `src/main/proto` contains the .proto files   
* Run `./gradlew generateProto` to generate client, server and message classes, run this command after you  


Run server `./gradlew -PmainClass=nl.toefel.server.ServerMain execute`

Run client `./gradlew -PmainClass=nl.toefel.application.ClientMain execute`


### Scaling issues on Linux

See https://stackoverflow.com/questions/26182460/javafx-8-hidpi-support
 
Solution:

`gsettings set org.gnome.desktop.interface scaling-factor 2`
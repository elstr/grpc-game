# gRPC workshop

Some quick guidelines
* `src/main/proto` contains the .proto files   
* Run `./gradlew generateProto` to generate client, server and message classes, run this command after you  
* Each package below `nl.toefel` contains an example. For example `nl.toefel.chatroom`
* Runnable classes end with Main.
* The server stubs are implemented with classnames ending witn 'Controller'
* Run `./gradlew idea` or `./gradlew eclipse` to configure your IDE to detect the generated code directories.
     
The react frontend-application is located in the root directory `front-end`
     

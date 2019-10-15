# gRPC game workshop

**REQUIREMENTS: java 11**

The workshop contains a simple tic-tac-toe game (boter kaas en eieren). Multiple players can join and play against each other.

There are two components: a client and and a server. Clients connect to the server using gRPC and execute calls such as `testConnection()`, `createPlayer()`, `listPlayers()` and `playGame()`. 

Most of the code is already finished. The only thing that needs to be implemented is the client side gRPC layer.

#### Starting instructions

Run ServerMain.java from your IDE or 

    ./gradlew runServer

Run ClientMain.java from your IDE or 

    ./gradlew runClient

The client will open a JavaFX window.

# Exercises

### Introduction

Take a look at [tic_tac_toe.proto](src/main/proto/tic_tac_toe.proto) This file already contains the protocol buffer message types that will be used, and a service definition. The server is already implemented using this definition. 

If you change anything in this file, you should regenerate the code using:

    ./gradlew generateProto

The next exercises will require you to implement the client step by step.

### 0. Start the server

You can start the server from the IDE by running ServerMain.java or 

    ./gradlew runServer
    
The next assignments assume you keep the server running. 

### 1. Setting up a connection

Open the file [GrpcController.java](src/main/java/nl/toefel/tictactoe/client/controller/GrpcController.java) and 
implement the `connectToServer()` method. 

**Step 1**: create a `ManagedChannel` using a `ManagedChanelBuilder` to build a channel that connects to the given host and port.

Once you have channel, you can create a client for a service defined in a .proto file. 
We only have one service defined and it is called TicTacToe.
 
**Step 2**: create a blocking stup using the `TicTacToeGrpc.newBlockingStub()` class and call `testConnection()` on that stub. This will execute a call to the server! 

If anything goes wrong, an exception will be thrown and a visual dialog will pop-up. Otherwise
everything is OK.

**Step 3**: Store the `ManagedChannel` instance in the state, using `state.setGrpcConnection(grpcConnection)`.

Run the client and test your code by running ClientMain.java or 

    .gradlew runClient 

And click the connect button!

### 2. Create a player

Build a `CreatePlayerRequest` the given playerName in it. 

Then create a newBlockingstub(), use the channel from exercise 1 which can be retrieved from the state: `state.getGrpcConnection()`

You should, get a player object back, store this object in the state using `state.setMyself()`.


### 3. Initialize the bi-directional stream

This time, **do not create a blockingStub**, but a normal stub. This stub exposes the PlayGame method. PlayGame is not available on the blocking stub because it contains a bi-directional stream
which is per definition not blocking. 

The stub has many sort of builder methods. The one you should use now is `.withCallCredentials()` and pass it a new `PlayerIdCredentials` object. The ID passed into the PlayerIdCredentials should equal `state.getMyself().getId()`. Call credentials will be sent as metadata before each call. 

now call playGame() and store the result in the state using `state.setGameCommandStream()`.

TODO instruct the content of onNext() onError() and onCompleted().

### 4. Implement listPlayers()

### 5. Implemented startGameAgainstPlayer()

### 6. Change the listPlayer method a server side streaming call


### JavaFX Scaling issues 4k displays using Linux

See https://stackoverflow.com/questions/26182460/javafx-8-hidpi-support
 
Solution for gnome based distro's:

    gsettings set org.gnome.desktop.interface scaling-factor 2
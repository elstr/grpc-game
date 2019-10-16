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

The next exercises will require you to implement the client side, step by step.

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
 
**Step 2**: create a blocking stub using the `TicTacToeGrpc.newBlockingStub()` class and call `testConnection()` on that stub. This will execute a call to the server! 

If anything goes wrong, an exception will be thrown and a visual dialog will pop-up. Otherwise
everything is OK.

**Step 3**: Store the `ManagedChannel` instance in the state, using `state.setGrpcConnection(grpcConnection)`.

Run the client and test your code by running ClientMain.java or 

    .gradlew runClient 

And click the connect button!

### 2. Create a player

Open the file [GrpcController.java](src/main/java/nl/toefel/tictactoe/client/controller/GrpcController.java) and 
implement the `createPlayer()` method. 

**Step 1** Build a `CreatePlayerRequest` the given playerName in it. 

**Step 2** Create a `TicTacToeGrpc.newBlockingstub()`, use the channel from exercise 1 which can be retrieved from the state: `state.getGrpcConnection()`

**Step 3** Call the create player method on the stub. You should, get a player object back, store this object in the state using `state.setMyself(returnedPlayer)`.

Test it out running the game and click the Join button, you should see your name appear next to it. 

### 3. Initialize the bi-directional stream

Open the file [GrpcController.java](src/main/java/nl/toefel/tictactoe/client/controller/GrpcController.java) and 
implement the `initializeGameStream()` method. 

You might have noticed that the blockingstub you created in the previous exercises does not contain the `playGame()` method from our TicTacToe service. That's because `playGame()` is a bi-directional streaming call, which is by definition not blocking. 
 
**Step 1** Create a `TicTacToeGrpc.newStub()` instead of a blockingStub(). This stub exposes the PlayGame method. 

**Step 2** The stubs have many sort of builder methods. The one you should use now is `.withCallCredentials()` and pass it a new `PlayerIdCredentials` object. The ID passed into the PlayerIdCredentials should equal `state.getMyself().getId()`. Call credentials will be sent as metadata before each call. The server will use it to identify which player is joining the game.

**Step 3** Issue a call to `playGame()` The input parameter is a StreamObserver. Tou can create an anonymous class of the StreamObserver interface, giving you 3 methods to implement `onNext()`, `onError()`, `onCompleted()`. This methods can be implemented with only one line each:

    onNext()      should be implemented:     state.onGameEvent(gameEvent)
    onError()     should be implemented:     state.onGameStreamError(throwable)
    onCompleted() should be implemented:     state.onGameStreamCompleted()
    
**Step 4** Store the stream returned by `playGame()` in the state by using `state.setGameCommandStream()`. The game uses this stream to send game commands to the server. 

### 4. Implement listPlayers

Open the file [GrpcController.java](src/main/java/nl/toefel/tictactoe/client/controller/GrpcController.java) and 
implement the `listPlayers()` method. 


**Step 1** Create a stub (whichever you like) and call listPlayers. Remember you always need an input parameter, even if it is empty. Make sure to also send the PlayerIdCredentials!
 
**Step 2** Store the list of players in `state.setPlayers()`

### 5. Implemented startGameAgainstPlayer

Open the file [GrpcController.java](src/main/java/nl/toefel/tictactoe/client/controller/GrpcController.java) and 
implement the `startGameAgainstPlayer()` method.

This method receives the opponent you want to challenge. You can get yourself using `state.getMyself()`. 

**Step 1** Create GameCommand and StartGame command.

**Step 2** Send this game command to the server using `state.getGameCommandStream().onNext()`

### 6. Implement make board move

This is the last exercise, then we will be able to play the game.

Open the file [GrpcController.java](src/main/java/nl/toefel/tictactoe/client/controller/GrpcController.java) and 
implement the `makeBoardMove()` method. 

**Step 1** Create GameCommand with the received BoardMove. 

**Step 2** Send this game command to the server using `state.getGameCommandStream().onNext()`


### 7. Change the listPlayer method a server side streaming call

Extra credit :)

Change listPlayer to a server side streaming call. 

**Step 1** Update the TicTacToe service definition in the tic_tac_toe.proto file. Make the return type a stream of Player objects.

**Step 2** generate the code `./gradlew generateProto`

**Step 3** Update the server code

**Step 4** Update the client code 


### JavaFX Scaling issues 4k displays using Linux

See https://stackoverflow.com/questions/26182460/javafx-8-hidpi-support
 
Solution for gnome based distro's:

    gsettings set org.gnome.desktop.interface scaling-factor 2
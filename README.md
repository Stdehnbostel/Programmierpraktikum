todo:

server:
*) fix login bug (User won't be able to send / receive messages after logging back into an already existing account, likely due to mismatched DataOutput/DataInput streams)
 possible fix: store DataOutput / Input streams in ServerThread class, then overwrite the out and in variables with the already existing ones. 
*) clean termination of ServerSocket, disconnection of clients.
*) refactoring

client:
*) enable to choose a server
*) refactoring

*) make it possible to send pdf and image files

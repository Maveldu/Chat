#Chat server
>Small online chat for thin client.
##What does it do?
This server allows a thin client (telnet) to connect (port 2016) in order to chat with other clients.
The client can use different chat rooms and have access to different commands.
###Available commands :
For any user
```
[Message] – Send a message to everyone in your chatroom
/? – List informations about the server, also list the available commands
/all [Message] – Send a message to every client (whatever the room they're in)
/w [Receiver] [Message] – Send a private message
/re [Message] – Reply to the last private message received
/bye – Disconnect
/goto [Salle de chat] – Move to another chatroom
/listroom – List the available rooms
/listuser – List connected ursers
/lista – List the global messages history
/listr – List the room messages history
/listw – List the private messages history
/admin – Log as administrator
```
For admin only
```
/shutdown – Shutdown the server
/kick [Utilisateur] – Force a client to quit
/listr [Salle de chat] – List the messages history of a specified room
/listw [Utilisateur] – List the messages history of a specified user
/moveto [Utilisateur] [Salle de chat] – Force a specidied user to go to a specified room
/newroom [Nom] – Make a new chatroom
/delroom [Nom] – Delete a chatroom
```


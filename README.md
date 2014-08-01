XRMI
====

XRMI Networking Library

Usage:

new RMIConnection(socket, bind)
// If you bind some object to the connection

new RMIConnect(socket).getBind()
// You get the binded object from the other side of the socket

Example.

public interface Agent{
    public String getName();
}

Server:
ServerSocket serverSocket=new ServerSocket(1234);
while(true){
    Agent agent=new RMIConnection(serverSocket.accpet()).getBind();
    System.out.println(agent.getName() + " is connected";
}

Client:
new RMIConnection(new Socket("localhost", 1234), new Agent(){
    @Override
    public String getName(){
        return "Client";
    }
});

Notice only two kind of objects can be trasferred through XRMI
1. Objects that implements Serializable
2. Objects that implments a interface (directly or indirectly) and is not Serializable
3. Notice that if objects is not Serializable, the received object is a dynamic stub generated by XRMI, and you only can call methods of the FIRST interface of the object transferred. These objects' method calls are performed as remote method invocation, so be careful to cache as many data as you can, to decrease the network band needed.

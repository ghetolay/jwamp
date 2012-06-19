What's JWamp ?
--------------
--------------

JWamp is a java implementation of the WebSocket subprotocol [WAMP][wamp].
Since on WebSocket there is no differences between client and server except for the initiation of the connection, the same jar work for both client and server architecture only the start point differs.

**This do not include a WebSocket implementation** but it's easy to adapt to any WebSocket implementation.
Actually it's only compatible for jetty WebSocket and as you can see on sources the jetty's specific part is only 3 small classes.
If you need it to work with a particularly WebSocket implementation create a issue and I'll work on it.

This is the very first version so for now it's only meant to be tested, **there is no stable version yet**.

Since I'm still working on the Javadoc and wiki here is a quick tutorial to keep starting :  

Tutorial
--------
--------

Configuration
-------------

First add jetty-websocket to your maven dependencies : 
```xml
  <dependency>
     <groupId>org.eclipse.jetty</groupId>
     <artifactId>jetty-websocket</artifactId> 
  </dependency>
```
For an embedded jetty server add : 
```
  <dependency>
      <groupId>org.eclipse.jetty</groupId>
      <artifactId>jetty-webapp</artifactId>
   </dependency>
```
Factory
-------

Simply use the jetty factory : 
```java
  WampJettyFactory wampFact = WampJettyFactory.getInstance();
```

Client connection
-----------------

for a client connection there is two way of connection : 

The first one will block until the connection is made and a welcome message is received or a timeout expire :

```java
  WampWebSocket wamp = wampFact.connect(new URI("ws://URL"));
```

The second one will call the listener once the welcome message is received. There is no timeout for the welcome message but you can still close the connection if the listener is not called after a amout of time.
The WampConnection received should only be used to close the connection, use the WampWebSocket from the listener to start communication.

```java
  ResultListener<WampWebSocket> rl = new ResultListener<WampWebSocket>() {

    @Override
    public void onResult(WampWebSocket result) {
					
    }
  };
			
  WampConnection connection = wampFact.connect(new URI("ws://URL"), rl);
```


Server Connection
-----------------

For the server just get the jetty handler (below explanation about wamp.xml): 

```java
   InputStream is = new FileInputStream("wamp.xml");
   WampJettyHandler wampHandler = wampFact.newJettyHandler(new DefaultWampParameter.SimpleServerParameter(is));
```


WampParameter and WampMessageHandler
------------------------------------

WampConnection work with a collection of WampMessageHandler, each message handler will handle one or multiple type of message like CALL,CALLRESULT, SUBSCRIBE, PUBLISH....
Each WampMessageHandler should or should not be reinstanciated/reinitiated for each connection that's why I created WampParameter with the method getnewHandler().
WampParameter manage the creation of handlers (ressources sharing...).

DefaultWampParameter contains 4 subclasses for the most commons needs.
It separated Wamp protocol in 4 group of messages : 

* RPC Sender handle CALLRESULT and CALLRESULTERROR messages and send CALL messages.
* RPC Manager handle CALL messages and send CALLRESULT and CALLRESULTERROR messages.
* Event Subscriber handle EVENT messages and send SUBSCRIBE, UNSUBSCRIBE and PUBLISH messages.
* Event Manager handler SUBSCRIBE, UNSUBSCRIBE and PUBLISH messages and send Event messages.

WELCOME messages are handled by the connection and for now PREFIX messages are not supported.

So the 4 classes are : 

* SimpleClientParameter handling RPC Sender and Event Subscriber.
* FullClientParameter handling the 4 groups.
* SimpleServerParameter handling RPC Manager and Event Manager.
* FullServerParameter handling the 4 groups

The difference between FullClientParameter and FullServerParameter if the creation of the Event Manager.
For interoperability with other Wamp implementation a client should only handle RPC Sender and Event Sender and a server RPC Manager and Event Manager.


Wamp.xml
--------

Wamp.xml is the easiest way of mapping actions (RPC/Event) to message handlers.
It's a first shot so it very simple. Here is a example : 

```xml
  <jwamp xmlns="http://www.example.org/jwamp" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="jwamp.xsd">
	<rpc>
		<action id="Test" class="com.example.TestCallAction"/>
	</rpc>
	<event>
		<action id="TestEvent" class="com.example.TestEventAction"/>
                <subscribe>Event1,Event2,Event3</subscribe>
	</event>
  </jwamp>
```

You don't need to have both rpc and event elements.
RPC classes must implements CallAction.
Event classes must implements EventAction or extends AbstractEventAction wich is simplier to use.

The subscribe element is optional and is used for Event subscriber, it contains a list of event the connection will automatically subscribe at.



This are my first steps on open project so don't hesitate to send me your feedbacks or questions ! 

[wamp]: http://wamp.ws/

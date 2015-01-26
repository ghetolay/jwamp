#---- **Project no more maintained** ----

What's jwamp ?
==============

JWamp is a java implementation of the WebSocket subprotocol [WAMP V1][wamp].  
Since on WebSocket there is no differences between client and server except for the initiation of the connection, the same API work for both client and server architecture.

**This do not include a WebSocket implementation** but it's easy to adapt to any WebSocket implementation.  
For the moment it's only compatible with jetty WebSocket and as you can see on sources the jetty's specific part is only 3 small classes.  
If you need it to work with a particulary WebSocket implementation just ask and I'll work on it.

This is the very first version so for now it's only meant to be tested, **there is no stable version yet**.

Since I'm still working on the Javadoc and wiki, here is a quick tutorial to keep starting :

Tutorial
========

Maven Configuration
-------------

First you must enable SNAPSHOT from sonatype in your pom.xml or settings.xml like this :
```xml
<repository>
	<id>snapshots-repo</id>
	<url>https://oss.sonatype.org/content/repositories/snapshots</url>
	<snapshots>
		<enabled>true</enabled>
	</snapshots>
</repository>
```

then add jwamp dependency : 
```xml
<dependency>
	<groupId>com.github.ghetolay</groupId>
	<artifactId>jwamp</artifactId>
	<version>0.0.1-SNAPSHOT</version>
</dependency>
```
To use jwamp with jetty-websocket add : 
```xml
  <dependency>
     <groupId>org.eclipse.jetty.websocket</groupId>
     <artifactId>websocket-client</artifactId> 
     <version>9.0.0.RC2</version>
  </dependency>
```
or
```xml
  <dependency>
     <groupId>org.eclipse.jetty.websocket</groupId>
     <artifactId>websocket-server</artifactId>
     <version>9.0.0.RC2</version>
  </dependency>
```

For an embedded jetty server add : 
```xml
  <dependency>
      <groupId>org.eclipse.jetty</groupId>
      <artifactId>jetty-webapp</artifactId>
      <version>9.0.0.RC2</version>
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
You can configure 2 kind of timeout on the WampJettyFactory : 
* IdleTimeout : Standard connection timeout.
* waitTimeout : It's the amount of time we wait for a welcome message once the connection is made. It provide infinite waiting if the other side accept the connection but doesn't support WAMP protocol, in this case if you did not set a waitTimeout the connect function will wait forever... (default behavior)

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
   WampWebSocketHandler wampHandler = wampFact.newWebsocketHandler(new DefaultWampParameter.SimpleServerParameter(is));
```


WampParameter and WampMessageHandler
------------------------------------

WampConnection work with a collection of WampMessageHandler, each message handler will handle one or multiple type of message like CALL,CALLRESULT, SUBSCRIBE, PUBLISH....  
Each WampMessageHandler should or should not be reinstanciated/reinitiated for each connection that's why I created WampParameter with the method getnewHandler(). This method manage the creation of handlers (ressources sharing...).

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
  <jwamp xmlns="https://github.com/ghetolay/jwamp" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="https://github.com/ghetolay/jwamp https://raw.github.com/ghetolay/jwamp/master/jwamp.xsd">
	<rpc>
		<action id="CallAction1" class="com.example.TestCallAction"/>
		<action id="CallAction2" class="com.example.TestCallAction2"/>
		<action id="SpecialAction" class="com.example.SpecialAction"/>
	</rpc>
	<event>
		<action id="Event1" class="com.example.TestEventAction"/>
		<action id="Event2" class="com.example.TestEventAction2"/>
		<action id="SpecialAction" class="com.example.SpecialAction"/>
                <subscribe>Event1,Event2,Event3</subscribe>
	</event>
  </jwamp>
```

You don't need to have both rpc and event elements.  
RPC classes must implements CallAction.  
Event classes must implements EventAction or extends AbstractEventAction wich is simplier to use.  

The subscribe element is optional and is used for Event subscriber, it contains a list of event the connection will automatically subscribe at.  
  
Test
----

see [here][test-readme].  
  
  
  
These are my first steps on open project so don't hesitate to send me your feedbacks or questions ! 

[wamp]: http://wamp.ws/spec/wamp1/
[test-readme]: jwamp/tree/master/src/test/resources

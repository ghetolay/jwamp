/**
*Copyright [2012] [Ghetolay]
*
*Licensed under the Apache License, Version 2.0 (the "License");
*you may not use this file except in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing, software
*distributed under the License is distributed on an "AS IS" BASIS,
*WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*See the License for the specific language governing permissions and
*limitations under the License.
*/
package com.github.ghetolay.jwamp;


import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.github.ghetolay.jwamp.event.WampEventSubscriber;
import com.github.ghetolay.jwamp.event.WampSubscription;
import com.github.ghetolay.jwamp.event.EventResult;
import com.github.ghetolay.jwamp.message.WampArguments;
import com.github.ghetolay.jwamp.message.SerializationException;
import com.github.ghetolay.jwamp.message.WampCallResultMessage;
import com.github.ghetolay.jwamp.rpc.CallException;
import com.github.ghetolay.jwamp.rpc.WampRPCSender;
import com.github.ghetolay.jwamp.utils.ResultListener;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class WampWebSocket {

  public static final String URI_WAMP_BASE = "http://api.wamp.ws/";
  public static final String URI_WAMP_ERROR = URI_WAMP_BASE + "error#";
  public static final String URI_WAMP_PROCEDURE = URI_WAMP_BASE + "procedure#";
  public static final String URI_WAMP_TOPIC = URI_WAMP_BASE + "topic#";
  public static final String URI_WAMP_ERROR_GENERIC = URI_WAMP_ERROR + "generic";
  public static final String URI_WAMP_ERROR_INTERNAL = URI_WAMP_ERROR + "internal";
  private static final String HASH_ALGORITHM = "HmacSHA256";
  public static final long DEFAULT_TIMEOUT = 5000;

  private WampConnection connection;
	
	private WampRPCSender rpcSender;
	private WampEventSubscriber eventSubscriber;
	
	public WampWebSocket(WampConnection connection) {
		this.connection = connection;
	}

	public WampConnection getConnection(){
		return connection;
	}
	
	private WampRPCSender getRPCSender() throws UnsupportedWampActionException{
		//Refresh in case it was removed from the connection
		if(rpcSender == null || connection.containsMessageHandler(rpcSender)){
			rpcSender = connection.getMessageHandler(WampRPCSender.class);
			if(rpcSender == null)
				throw new UnsupportedWampActionException();
		}
		return rpcSender;
	}
	
	private WampEventSubscriber getEventSubscriber() throws UnsupportedWampActionException{
		//Refresh in case it was removed from the connection
		if(eventSubscriber == null || connection.containsMessageHandler(eventSubscriber)){
			eventSubscriber = connection.getMessageHandler(WampEventSubscriber.class);
			if(eventSubscriber == null)
				throw new UnsupportedWampActionException();
		}
		return eventSubscriber;
	}
	
	public WampArguments simpleCall(String procId, Object... args) throws IOException, TimeoutException, UnsupportedWampActionException, SerializationException, CallException{
		return getRPCSender().call(procId, DEFAULT_TIMEOUT, args);
	}
	
	//TODO issue this method can be miss-call
	public WampArguments call(String procId, long timeout, Object... args) throws IOException, TimeoutException, UnsupportedWampActionException, SerializationException, CallException{
		return getRPCSender().call(procId, timeout, args);
	}
	
	public String call(String procId, ResultListener<WampCallResultMessage> listener, long timeout, Object... args) throws IOException, UnsupportedWampActionException, SerializationException, CallException{
		return getRPCSender().call(procId, listener, timeout, args);
	}
	
	public WampArguments call(String procId, long timeout, Object args) throws IOException, TimeoutException, UnsupportedWampActionException, SerializationException, CallException{
		return getRPCSender().call(procId, timeout, args);
	}
	
	public String call(String procId, ResultListener<WampCallResultMessage> listener, long timeout, Object args) throws IOException, UnsupportedWampActionException, SerializationException, CallException{
		return getRPCSender().call(procId, listener, timeout, args);
	}
	
	public void subscribe(String topicId) throws IOException, SerializationException, UnsupportedWampActionException{
		getEventSubscriber().subscribe(topicId);
	}
	
	public void subscribe(String topicId, ResultListener<WampArguments> listener) throws IOException, SerializationException, UnsupportedWampActionException{
		getEventSubscriber().subscribe(topicId, listener);
	}
	
	public void subscribe(WampSubscription subscription) throws IOException, UnsupportedWampActionException, SerializationException{
		getEventSubscriber().subscribe(subscription);
	}
	
	public void subscribe(WampSubscription subscription, ResultListener<WampArguments> listener) throws IOException, UnsupportedWampActionException, SerializationException{
		getEventSubscriber().subscribe(subscription, listener);
	}
	
	public void unsubscribe(String topicId) throws IOException, UnsupportedWampActionException, SerializationException{
		getEventSubscriber().unsubscribe(topicId);
	}
	
	public void unsubscribeAll() throws IOException, UnsupportedWampActionException{
		getEventSubscriber().unsubscribeAll();
	}
	
	public void publish(String topicId, Object event) throws IOException, UnsupportedWampActionException, SerializationException{
		getEventSubscriber().publish(topicId, event);
	}
	public void publish(String topicId, Object event, boolean excludeMe) throws IOException, UnsupportedWampActionException, SerializationException{
		getEventSubscriber().publish(topicId, event, excludeMe);
	}
	public void publish(String topicId, Object event, boolean excludeMe, List<String> eligible) throws IOException, UnsupportedWampActionException, SerializationException{
		getEventSubscriber().publish(topicId, event, excludeMe, eligible);
	}
	public void publish(String topicId, Object event, List<String> exclude, List<String> eligible) throws IOException, UnsupportedWampActionException, SerializationException{
		getEventSubscriber().publish(topicId, event, exclude, eligible);
	}
	
	public void setGlobalEventListener(ResultListener<EventResult> listener){
		getEventSubscriber().setGlobalListener(listener);
	}

  public String authSignature(String authChallenge, String authSecret) throws SignatureException {
    try {
      Key sk = new SecretKeySpec(authSecret.getBytes(), HASH_ALGORITHM);
      Mac mac = Mac.getInstance(sk.getAlgorithm());
      mac.init(sk);
      final byte[] hmac = mac.doFinal(authChallenge.getBytes());
      //return Base64.encodeToString(hmac,Base64.NO_WRAP);
      return Base64.getEncoder().encodeToString(hmac);
    } catch (NoSuchAlgorithmException e) {
      throw new SignatureException("error building signature, no such algorithm in device " + HASH_ALGORITHM);
    } catch (InvalidKeyException e) {
      throw new SignatureException("error building signature, invalid key " + HASH_ALGORITHM);
    }
  }

  public void authenticate(String authKey, String authSecret)
          throws IOException, TimeoutException, UnsupportedWampActionException, SerializationException, CallException, SignatureException {
    authenticate(authKey, authSecret, DEFAULT_TIMEOUT);
  }

  public void authenticate(String authKey, String authSecret, long timeout)
          throws IOException, TimeoutException, UnsupportedWampActionException, SerializationException, CallException, SignatureException {
    WampArguments res = call(URI_WAMP_PROCEDURE + "authreq", timeout, authKey);
    String challenge = res.nextObject().asString();
    String sig = authSignature(challenge, authSecret);
    call(URI_WAMP_PROCEDURE + "auth", timeout, sig);
  }
}

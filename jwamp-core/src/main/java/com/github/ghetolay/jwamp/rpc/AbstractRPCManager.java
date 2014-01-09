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
package com.github.ghetolay.jwamp.rpc;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.ghetolay.jwamp.WampConnection;
import com.github.ghetolay.jwamp.WampMessageHandler;
import com.github.ghetolay.jwamp.message.WampArguments;
import com.github.ghetolay.jwamp.message.SerializationException;
import com.github.ghetolay.jwamp.message.WampCallMessage;
import com.github.ghetolay.jwamp.message.WampMessage;
import com.github.ghetolay.jwamp.message.output.OutputWampCallErrorMessage;
import com.github.ghetolay.jwamp.message.output.OutputWampCallResultMessage;

/**
 * @author ghetolay
 *
 */
public abstract class AbstractRPCManager implements WampMessageHandler{

	protected final Logger log = LoggerFactory.getLogger(getClass());

	private WampConnection conn;

	private ExecutorService executor = Executors.newFixedThreadPool(25);

	public void onConnected(WampConnection connection) {
		conn = connection;
	}

	public void onClose(String sessionId, int closeCode) {
		conn = null;
	}

	public boolean onMessage(String sessionId, WampMessage message) {
		if(message.getMessageType() == WampMessage.CALL){
			RunnableAction action = getRunnableAction(sessionId, (WampCallMessage)message);
			if(action != null){
				executor.execute(action);
				return true;
			}
		}

		return false;
	}

	/**
	 * any result will be sent.
	 * 
	 * @param callId
	 * @param result
	 * @throws IOException
	 * @throws SerializationException
	 */
	private void sendResult(String callId, Object... result) throws IOException, SerializationException{

		OutputWampCallResultMessage resultMsg = new OutputWampCallResultMessage();

		if(result != null && result.length > 0){
			if(result.length == 1)
				resultMsg.setResult(result[0]);
			else
				resultMsg.setResult(result);
		}
		
		resultMsg.setCallId(callId);


		conn.sendMessage(resultMsg);
	}

	private void sendError(String callId, String procId, CallException e) throws IOException, SerializationException{
		conn.sendMessage( new OutputWampCallErrorMessage(callId, procId,e.getErrorDescription(), e.getErrorDetails()) );
	}
	
	private void sendErrorFromException(String callId, String procId, Throwable e) throws IOException, SerializationException{
		OutputWampCallErrorMessage errorMsg = new OutputWampCallErrorMessage(callId, procId,e.getLocalizedMessage());
		if(!e.getMessage().equals(e.getLocalizedMessage()))
			errorMsg.setErrorDetails(e.getMessage());

		conn.sendMessage(errorMsg);
	}

	protected abstract RunnableAction getRunnableAction(String sessionId, WampCallMessage message);

	public abstract class RunnableAction implements Runnable, CallResultSender{

		protected String sessionId;
		protected WampCallMessage message;

		public RunnableAction(String sessionId, WampCallMessage message) {
			this.sessionId = sessionId;
			this.message = message;
		}

		protected abstract void excuteAction(String sessionID, WampArguments args, CallResultSender sender) throws CallException, Throwable;

		public void run(){ 

			try{	
				try {
					excuteAction(sessionId, message.getArguments(), this);
				} catch (CallException ce){
					if(log.isDebugEnabled())
						log.debug("action " + message.getCallId() + " returning CallException " + ce.getErrorDescription() + ", " + ce.getErrorDetails());
					
					sendError(message.getCallId(), message.getProcId(), ce);
					return;
				} catch (Throwable e) {
				
					if(log.isDebugEnabled())
						log.debug("Error on action " + message.getCallId(),e);

					sendErrorFromException(message.getCallId(), message.getProcId(), e);
					return;
				}
			}catch(Exception e){
				if(log.isDebugEnabled())
					log.debug("Unable to send response for call action " + message.getProcId() + " with id " + message.getCallId(),e);
			}
		}
		
		public boolean sendResult(Object... result){
			try {
				AbstractRPCManager.this.sendResult(message.getCallId(),result);
				return true;
			} catch (Exception e) {
				if(log.isDebugEnabled())
					log.debug("Unable to send response for call action with id " + message.getCallId(),e);
			} 
			
			return false;
		}
		
	}
}



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
package com.github.ghetolay.testutils;

import java.net.URI;

import com.github.ghetolay.jwamp.message.WampCallMessage;
import com.github.ghetolay.jwamp.message.WampCallResultMessage;
import com.github.ghetolay.jwamp.rpc.CallAction;
import com.github.ghetolay.jwamp.rpc.CallException;
import com.github.ghetolay.jwamp.session.WampSession;
import com.github.ghetolay.jwamp.utils.ObjectHolderFactory;
import com.github.ghetolay.jwamp.utils.URIBuilder;

public class EchoAction implements CallAction{

	public static URI uri = URIBuilder.uri("http://example.com/echo");
	
	@Override
	public WampCallResultMessage handleCall(WampSession session, WampCallMessage msg) throws CallException {
		if ( msg.getArgs().size() != 0)
			return WampCallResultMessage.create(msg.getCallId(), msg.getArgs().get(0));

		return WampCallResultMessage.create(msg.getCallId(), ObjectHolderFactory.VOID);
	}
	
}

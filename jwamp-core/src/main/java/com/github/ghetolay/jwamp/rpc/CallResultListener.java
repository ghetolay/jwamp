/**
 * 
 */
package com.github.ghetolay.jwamp.rpc;

import com.github.ghetolay.jwamp.message.WampCallErrorMessage;
import com.github.ghetolay.jwamp.message.WampCallResultMessage;

/**
 * @author Kevin
 *
 */
public interface CallResultListener {
	public void onSuccess(WampCallResultMessage msg);
	public void onError(WampCallErrorMessage msg);
	public void onTimeout();
}

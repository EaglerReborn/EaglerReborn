/*
 * MIT License
 * 
 * Copyright (c) 2018 Calder Young (LAX1DUDE)
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 */

package me.scovel.vigler.auth;

import java.io.IOException;

import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.request.HttpRequestWithBody;

public class Requestler {

	public static HttpRequestWithBody startRequest(String endpoint) {
		return Unirest.post("https://authserver.mojang.com/"+endpoint).header("Content-Type", "application/json");
	}

	public static void handleResponseCode(HttpResponse<JsonNode> response) throws Throwable {
		if(response.getStatus() < 200 || response.getStatus() >= 300) {
			JSONObject json = response.getBody().getObject();
			if(json.has("error")) {
				String error   = json.getString("error");
				String message = json.optString("errorMessage");
				String cause   = json.optString("cause");
				
				Throwable t;
				if("ForbiddenOperationException".equals(error)) {
					t = new ForbiddenOperationException(message);
				}else if("IllegalArgumentException".equals(error)) {
					t = new IllegalArgumentException(message);
				}else if("TooManyRequestsException".equals(error)) {
					t = new TooManyRequestsException(message);
				}else {
					t = new EaglerException(message);
				}
				
				if(cause != null) {
					if("UserMigratedException".equals(cause)) {
						t.initCause(new UserMigratedException());
					}
				}
				
				throw t;
			}
		}
	}

	static class TooManyRequestsException extends IOException {
		private static final long serialVersionUID = 1L;
		public TooManyRequestsException(String msg) {
			super(msg);
		}
	}
	
	static class UserMigratedException extends Exception {
		private static final long serialVersionUID = 1L;
		
		public UserMigratedException() {
			super("User Migrated");
		}
	}
	

	static class ForbiddenOperationException extends Exception {
		private static final long serialVersionUID = 1L;
		
		public ForbiddenOperationException(String cause) {
			super(cause);
		}
	}
	
	static class EaglerException extends Exception {
		private static final long serialVersionUID = 1L;
		
		public EaglerException(String cause) {
			super(cause);
		}
	}
}

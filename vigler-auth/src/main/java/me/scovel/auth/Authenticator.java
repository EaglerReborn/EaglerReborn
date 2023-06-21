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
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.json.JSONArray;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.exceptions.UnirestException;

public class Authenticator {
	
	private String clientToken;
	private String accessToken;
	private String userID;
	private UserProfile selectedProfile;
	private Map<String,String> userProperties;
	private Game game;
	
	private Consumer<String> loghandler = (dee) -> {};
	
	public String getClientToken() {
		return clientToken;
	}

	public String getAccessToken() {
		return accessToken;
	}
	
	public void seLogHandler(Consumer<String> handler) {
		this.loghandler = handler;
	}
	
	/**
	 * this is not what you pass to minecraft, the uuid to pass to minecraft is in .
	 */
	public String getUserID() {
		return userID;
	}

	public Map<String, String> getUserProperties() {
		return userProperties;
	}

	public Game getGame() {
		return game;
	}

	public String getUsername() {
		return username;
	}

	void setClientToken(String clientToken) {
		this.clientToken = clientToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	void setUserID(String userID) {
		this.userID = userID;
	}

	void setSelectedProfile(UserProfile selectedProfile) {
		this.selectedProfile = selectedProfile;
	}

	void setUserProperties(Map<String, String> userProperties) {
		this.userProperties = userProperties;
	}

	void setGame(Game game) {
		this.game = game;
	}

	public Authenticator(String clientToken, Game game) {
		this.clientToken = clientToken;
		this.game = game;
		this.userProperties = new HashMap();
	}
	
	public Authenticator(JSONObject json, Game game) {
		this.game = game;
		this.userProperties = new HashMap();
		this.load(json);
	}
	
	/**
	 * Minecraft will default
	 */
	public Authenticator(String clientToken) {
		this.clientToken = clientToken;
		this.userProperties = new HashMap();
		this.game = Game.MINECRAFTv1;
	}
	
	/**
	 * Minecraft will default
	 */
	public Authenticator(JSONObject json) {
		this.game = Game.MINECRAFTv1;
		this.userProperties = new HashMap();
		this.load(json);
	}
	
	private void load(JSONObject json) {
		this.username          = json.optString("username");
		this.clientToken       = json.optString("clientToken");
		this.accessToken       = json.optString("accessToken");
		this.selectedProfile   = json.has("selectedProfile") ? new UserProfile(json.getJSONObject("selectedProfile")) : null;
		this.userID            = json.optString("userId");
		if(json.has("selectedProfile")) {
			this.userProperties.clear();
			json.getJSONObject("userProperties").toMap().forEach((dee, vile) -> this.userProperties.put(dee, (String) vile));
		}
		this.clearEmptyVariables();
	}
	
	public JSONObject saveToJSON() {
		this.clearEmptyVariables();
		return (new JSONObject())
				.put("username", this.username)
				.put("clientToken", this.clientToken)
				.put("accessToken", this.accessToken)
				.put("selectedProfile", this.selectedProfile == null ? null : this.selectedProfile.json())
				.put("userId", this.userID)
				.put("userProperties", this.userProperties);
	}

	private String username = null;
	private String password = null;

	public void setUsernamePassword(String username, String password) {
		this.username = username;
		this.password = password;
	}

	public void setUsernamePasswordAndLogin(String username, String password) throws AuthenticationException {
		this.username = username;
		this.password = password;
		this.loginWithPassword();
	}

	/**
	 * Required Set Variables: username, password
	 */
	public void loginWithPassword() throws AuthenticationException {
		try {
			loghandler.accept("Logging in with username and password...");
			HttpResponse<JsonNode> response = Requestler.startRequest("authenticate")
					.body(new JSONObject()
							.put("agent", this.game.agent)
							.put("username", this.username)
							.put("password", this.password)
							.put("clientToken", this.clientToken)
							.put("requestUser", true)).asJson();
			Requestler.handleResponseCode(response);
			
			handleLoginResponse(response.getBody().getObject());
			
		} catch (Throwable t) {
			throw new AuthenticationException("Could not preform request!", t);
		}
	}
	
	/**
	 * Required Set Variables: accessToken
	 */
	public void loginWithToken() throws AuthenticationException {
		if(!isTokenValid()) {
			this.forceTokenRefresh();
		}
	}
	
	public void forceTokenRefresh() throws AuthenticationException {
		try {
			loghandler.accept("Refreshing access token...");
			HttpResponse<JsonNode> response = Requestler.startRequest("refresh")
					.body(new JSONObject()
							.put("accessToken", this.accessToken)
							.put("clientToken", this.clientToken)
							.put("requestUser", true)).asJson();
			Requestler.handleResponseCode(response);
			
			handleLoginResponse(response.getBody().getObject());
		} catch (Throwable t) {
			throw new AuthenticationException("Could not preform request!", t);
		}
	}
	
	private void handleLoginResponse(JSONObject jsonObject) {
		this.accessToken = jsonObject.getString("accessToken");
		this.clientToken = jsonObject.getString("clientToken");
		this.selectedProfile = new UserProfile(jsonObject.getJSONObject("selectedProfile"));
		JSONObject userJson = jsonObject.getJSONObject("user");
		this.userID = userJson.getString("id");
		if(userJson.has("properties")) {
			this.userProperties.clear();
			userJson.getJSONArray("properties").forEach((yeeeee) -> {JSONObject j = (JSONObject)yeeeee; this.userProperties.put(j.getString("name"), j.getString("value"));});
		}
	}
	
	public boolean ensureLoggedIn() {
		this.clearEmptyVariables();
		if(this.accessToken != null && this.selectedProfile != null) {
			if(!this.isTokenValid()) {
				try {
					this.forceTokenRefresh();
					return true;
				}catch(AuthenticationException e) {
					try {
						if((e.getCause() instanceof IOException) || (e.getCause() instanceof UnirestException)) { //no internet
							if(this.selectedProfile != null) {
								return true;
							}
						}else {
							if(this.username != null && this.password != null) {
								this.loginWithPassword();
								return true;
							}
						}
					}catch(AuthenticationException e2) {
						loghandler.accept("Could not log in with username and password: "+e2.toString()+"  "+((e2.getCause() != null) ? e2.getCause().toString() : null));
					}
				}
			}else {
				return true;
			}
		}else {
			try {
				if(this.username != null && this.password != null) {
					this.loginWithPassword();
					return true;
				}
			}catch(AuthenticationException e2) {
				loghandler.accept("Could not log in with username and password: "+e2.toString()+"  "+((e2.getCause() != null) ? e2.getCause().toString() : null));
			}
		}
		return false;
	}
	
	private static boolean isEmpty(String string) {
		return string == null || string.trim().length() == 0;
	}
	
	private void clearEmptyVariables() {
		if(isEmpty(this.clientToken)) this.clientToken = null;
		if(isEmpty(this.accessToken)) this.accessToken = null;
		if(isEmpty(this.userID)) this.userID = null;
		if(isEmpty(this.username)) this.username = null;
		if(isEmpty(this.password)) this.password = null;
	}
	
	/**
	 * Required Set Variables: accessToken
	 */
	public boolean isTokenValid() {
		try {
			HttpResponse<JsonNode> response = Requestler.startRequest("validate")
					.body(new JSONObject()
							.put("accessToken", this.accessToken)
							.put("clientToken", this.clientToken)).asJson();
			
			Requestler.handleResponseCode(response);
			
			loghandler.accept("Current access token is valid");
			
			return true;
		} catch (Throwable t) {
			loghandler.accept("Access token has expired (got exception: "+t.toString()+")");
			return false;
		}
	}
	
	public void setUsernamePasswordAndLogout(String username, String password) throws AuthenticationException {
		this.username = username;
		this.password = password;
		this.logout();
	}
	
	/**
	 * Required Set Variables: username, password
	 */
	public void logout() throws AuthenticationException {
		try {
			loghandler.accept("Logging user out...");
			HttpResponse<JsonNode> response = Requestler.startRequest("signout")
					.body(new JSONObject()
							.put("username", this.username)
							.put("password", this.password)).asJson();
			this.selectedProfile = null;
			this.accessToken = null;
			Requestler.handleResponseCode(response);
			setClientToken(UUID.randomUUID().toString()); //no headache for people using accounts from their launcher
		} catch (Throwable t) {
			throw new AuthenticationException("Could not preform request!", t);
		}
	}
	
	/**
	 * Required Set Variables: accessToken
	 */
	public void invalidateToken() throws AuthenticationException {
		try {
			loghandler.accept("Invalidating current access token...");
			HttpResponse<JsonNode> response = Requestler.startRequest("invalidate")
					.body(new JSONObject()
							.put("accessToken", this.accessToken)
							.put("clientToken", this.clientToken)).asJson();
			this.selectedProfile = null;
			this.accessToken = null;
			Requestler.handleResponseCode(response);
			setClientToken(UUID.randomUUID().toString()); //no headache for people using accounts from their launcher
		} catch (Throwable t) {
			throw new AuthenticationException("Could not preform request!", t);
		}
	}
	
	public UserProfile getSelectedProfile() {
		return this.selectedProfile;
	}
	
	/**
	 * For minecraft
	 */
	public String getUserPropertiesString() {
		JSONObject obj = new JSONObject();
		if(this.userProperties != null && this.userProperties.size() > 0) {
			this.userProperties.forEach((dee, vile) -> obj.put(dee, (new JSONArray()).put(vile)));
		}
		return obj.toString();
	}
}

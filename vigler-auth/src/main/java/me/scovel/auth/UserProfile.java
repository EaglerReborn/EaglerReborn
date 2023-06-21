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

import java.nio.charset.Charset;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONObject;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;

public class UserProfile {
	
	public final String uuid;
	public final String playername;
	public final boolean legacy;
	
	public UserProfile(JSONObject json) {
		this.uuid = json.getString("id");
		this.playername = json.getString("name");
		this.legacy = json.optBoolean("legacy", false);
	}

	public UserProfile(String playerName2, String uuid2, boolean legacy2) {
		this.playername = playerName2;
		this.uuid = uuid2;
		this.legacy = legacy2;
	}

	public JSONObject json() {
		return (new JSONObject()).put("id", this.uuid).put("name", this.playername).put("legacy", this.legacy);
	}
	
	public String getSkinURL() {
		try {
			HttpResponse<JsonNode> response = Unirest.get("https://sessionserver.mojang.com/session/minecraft/profile/"+this.uuid).asJson();
			Requestler.handleResponseCode(response);
			JSONArray j = response.getBody().getObject().getJSONArray("properties");
			for(Object o : j) {
				JSONObject o2 = (JSONObject) o;
				if(o2.getString("name").equals("textures")) {
					JSONObject t = new JSONObject(new String(Base64.decodeBase64(o2.getString("value")), Charset.forName("UTF8")));
					return t.getJSONObject("textures").getJSONObject("SKIN").getString("url");
				}
			}
			throw new IllegalArgumentException("sessionserver.mojang.com did not return a skin");
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}

}

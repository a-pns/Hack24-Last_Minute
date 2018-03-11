package com.a_pns.hack24.entitymatch.controller;

import java.util.Arrays;
import java.util.Iterator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@Component
public class OpenCalaisController {

	@Autowired
	private RestTemplate HTTPClient;

	private String openCalaisApiUrl = "https://api.thomsonreuters.com/permid/calais";
	// Content-Type - mandatory
	private String contentType = "text/html";
	// outputFormat
	private String outputFormat = "application/json";
	// x-ag-access-token - mandatory
	private String xAgAccessToken = "qikmMMCy8qF1muU89uZJV2O5fFAlELnd";
	// x-calais-language
	private String xCalaisLanguage = "English";
	// x-calais-selectiveTags
	private String xCaliasSelectiveTags = "";
	
	///// MOre info API
	private String moreInfoApiURL = "http://localhost:8081/";
	
	@CrossOrigin(origins = "chrome-extension://hmlfkdkddlkpaefaifgnmfnejhfjeifh")
	@RequestMapping(name = "/", method = RequestMethod.POST)
	public ResponseEntity<String> parseContentAndReturnInformation(@RequestBody String body) throws JSONException {
		JSONObject calais = sendOpenCalaisRequest(body);
		JSONObject entities = parseResponseToGetEntities(calais);
		JSONObject entityInformation = sendInfoGatheringRequest(entities);
		String taggedHtml = processHtmlAddTags(body, entities.getJSONArray("entities"));
		JSONObject jsonResponse = createResponse(taggedHtml, entityInformation);
		return new ResponseEntity<String>(jsonResponse.toString(), HttpStatus.OK);
	}

	public JSONObject sendOpenCalaisRequest(String content) throws JSONException {
		HttpEntity entity;
		HttpHeaders headers = new HttpHeaders();
		headers.add("Content-Type", contentType);
		headers.add("outputFormat", outputFormat);
		headers.add("x-ag-access-token", xAgAccessToken);
		headers.add("x-calais-language", xCalaisLanguage);

		entity = new HttpEntity<>(content, headers);

		ResponseEntity<String> response = HTTPClient.postForEntity(openCalaisApiUrl, entity, String.class);
		JSONObject json = new JSONObject(response.getBody());
		return json;
	}

	public JSONObject parseResponseToGetEntities(JSONObject response) throws JSONException {
		JSONObject responseJson = new JSONObject();
		JSONArray arrayOfEntities = new JSONArray();
		Iterator responseKeys = response.keys();
		int indexNum = 0;
		while (responseKeys.hasNext()) {
			String key = (String) responseKeys.next();
			if (response.get(key) != null
					&& "entities".equals(response.getJSONObject(key).opt("_typeGroup"))
					&& Arrays.asList("MedicalCondition", "MedicalTreatment")
					.contains(response.getJSONObject(key).opt("_type"))) {
				JSONObject entity = response.getJSONObject(key);
				JSONArray instances = entity.getJSONArray("instances");
				String name = entity.getString("name");
				String type = entity.getString("_type");

				JSONObject newEntity = new JSONObject();
				JSONArray prefixes = new JSONArray();
				JSONArray suffixes = new JSONArray();
				for (int i = 0; i < instances.length(); i++) 
				{
					{
						JSONObject tmpJsonObj = instances.getJSONObject(i);
						prefixes.put(tmpJsonObj.getString("prefix"));
						suffixes.put(tmpJsonObj.getString("suffix"));
					}

					newEntity.put("textBeforeRefs", prefixes);
					newEntity.put("textAfterRefs", suffixes);
				}
				newEntity.put("name", name);
				newEntity.put("type", type);
				newEntity.put("tag", "entity" + indexNum);
				arrayOfEntities.put(newEntity);
				indexNum++;
			}
		}
		responseJson.put("entities", arrayOfEntities);
		return responseJson;
	}
	
	public JSONObject processItalicizedQueries(JSONObject entities, String requestBody)
	{
		
		return entities;
	}
	
	public JSONObject sendInfoGatheringRequest(JSONObject entities) throws JSONException
	{
		HttpEntity entity;
		HttpHeaders headers = new HttpHeaders();
		entity = new HttpEntity<>(entities.toString(), headers);

		ResponseEntity<String> response = HTTPClient.postForEntity(moreInfoApiURL, entity, String.class);
		JSONObject json = new JSONObject(response.getBody());
		return json;
	}
	
	public String processHtmlAddTags(String content, JSONArray entities) throws JSONException
	{
		String className = "entity";
		for (int i = 0; i < entities.length(); i++)
		{
			JSONObject entity = entities.getJSONObject(i);
			JSONArray prefixes = entity.getJSONArray("textBeforeRefs");
			JSONArray suffixes = entity.getJSONArray("textAfterRefs");
			content = content.replace(
					entity.getString("name")
					,
					"<span class=\""+className + i + "\">" + entity.getString("name") + "</span>");
		}
		content = content.replace("\r\n", "");
		content = content.replace("\n", "");
		content = content.replace("\r", "");
		return content;
	}
	
	public JSONObject createResponse(String html, JSONObject entityInformation) throws JSONException
	{
		JSONObject json = new JSONObject();
		json.put("html", html);
		json.put("entities", entityInformation);
		return json;
	}
}

package com.a_pns.hack24.entitymatch.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		processItalicizedQueries(entities, body);
		JSONObject entityInformation = sendInfoGatheringRequest(entities);
		//String taggedHtml = processHtmlAddTags(body, entities.getJSONArray("entities"));
		JSONObject jsonResponse = createResponse(entityInformation);
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
				&& 
					(
						"socialTag".equals(response.getJSONObject(key).opt("_typeGroup"))
						||
						(
								"entities".equals(response.getJSONObject(key).opt("_typeGroup"))
							&&
								Arrays.asList("MedicalCondition", "MedicalTreatment", "Technology").contains(response.getJSONObject(key).opt("_type"))
						)
					)
				)
				
			{
				JSONObject entity = response.getJSONObject(key);
				String name = entity.getString("name");
				String type = null;
				JSONObject newEntity = new JSONObject();
				if (response.getJSONObject(key).getString("_typeGroup").equalsIgnoreCase("socialtag"))
				{
					type = entity.getString("_typeGroup");
					JSONArray array1 = new JSONArray();
					array1.put("");
					JSONArray array2 = new JSONArray();
					array2.put("");
					newEntity.put("textBeforeRefs", array1);
					newEntity.put("textAfterRefs", array2);
				}
				else
				{
					JSONArray instances = entity.getJSONArray("instances");
					type = entity.getString("_type");

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
				}
				newEntity.put("name", name);
				newEntity.put("type", type);
				newEntity.put("tag", "entity" + indexNum);
				newEntity.put("tag_id", indexNum);
				arrayOfEntities.put(newEntity);
				indexNum++;
			}
		}
		responseJson.put("entities", arrayOfEntities);
		return responseJson;
	}
	
	public JSONObject processItalicizedQueries(JSONObject entities, String requestBody)
	{
		Pattern italicRegex = Pattern.compile("<i>(.+?)</i>");

		Set<String> matchedValues = new HashSet<String>();
		Matcher matcher = italicRegex.matcher(requestBody);
	    while (matcher.find()) {
	    	matchedValues.add(matcher.group(1).trim());
	    }

	    JSONArray entityArray =  entities.getJSONArray("entities");	
	    Set<String> names = new HashSet<String>();
	    for (int i = 0; i < entityArray.length(); i++)
		{
	    	names.add(entityArray.getJSONObject(i).getString("name").trim());
		}
	    int highestNumber = 0;
	   	for (int j = 0; j < entityArray.length(); j++)
	   	{
	   		int num = entityArray.getJSONObject(j).getInt("tag_id");
	   		if (num > highestNumber)
	   			highestNumber = num;
	   	}
	    
	   	int index = highestNumber + 1;
	    matchedValues.removeAll(names);
	    for (String value: matchedValues)
	    {
	    	if (value.split(" ").length <= 3 && value.trim().length() > 1)
	    	{
		    	JSONObject newEntity = new JSONObject();
		    	newEntity.put("name", value);
		    	newEntity.put("type", "Other");
		    	JSONArray array1 = new JSONArray();
		    	array1.put("");
				newEntity.put("textBeforeRefs", array1);
		    	JSONArray array2 = new JSONArray();
		    	array2.put("");
				newEntity.put("textAfterRefs", array2);
				newEntity.put("tag", "entity" + index);
				newEntity.put("tag_id", index);
		    	entityArray.put(newEntity);
		    	index++;
	    	}
	    }
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
	
/*	public String processHtmlAddTags(String content, JSONArray entities) throws JSONException
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
	}*/
	
	public JSONObject createResponse(JSONObject entityInformation) throws JSONException
	{
		JSONObject json = new JSONObject();
		json.put("entities", entityInformation);
		return json;
	}
	
}

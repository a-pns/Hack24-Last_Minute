import http.client, urllib.parse
import json


subscriptionKey = 'PLEASE_REPLACE_WITH_SUBSCRIPTION_KEY'
host = 'api.cognitive.microsoft.com'
path = '/bing/v7.0/entities/'

count = '10'
offset = '0'
safesearch = 'Moderate'

def search_bing_api(entities):
    entities = entities['entities']
    query = []

    for i in range(len(entities)):
        queryString = entities[i]['name']
        json_results = send_bing_api_call(queryString, 'en-GB')

        if(json_results.get('entities',None)):
            entities[i]['description'] = json_results['entities']['value'][0]['description']
        else:
            entities[i]['description'] = break_down_query_and_get_result(queryString)

def send_bing_api_call(query, mkt):

    params = '?q=' + urllib.parse.quote(query) + '&mkt=' + mkt + '&count=' + count + '&offset=' + offset + '&safesearch=' + safesearch
    headers = {'Ocp-Apim-Subscription-Key': subscriptionKey}
    conn = http.client.HTTPSConnection (host)
    conn.request ("GET", path+params, None, headers)
    response = conn.getresponse()

    print (path+params)

    return json.loads(response.read())

def break_down_query_and_get_result(query):
    # remove and abreviations like e.g, etc
    terms_array = query.split();
    description = ""
    is_first_description = True
    for term in terms_array:
        result = send_bing_api_call(term, 'en-GB')
        if(result.get('entities',None)):
            if is_first_description:
                is_first_description = False
            else:
                description = description + "<br>"
            description = description + term + " - " + result['entities']['value'][0]['description']
        else:
            result = send_bing_api_call(term, 'en-US')
            if(result.get('entities', None)):
                if is_first_description:
                    is_first_description = False
                else:
                    description = description + "<br>"
                description = description + term + " - " + result['entities']['value'][0]['description']
    return description

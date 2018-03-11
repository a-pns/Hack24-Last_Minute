import json
from flask import jsonify
from flask import request
# rename dummyMethod to your method
from app.search import search_bing_api
from app.elife_api import search_entities_on_elife

def search_entities():
    data = request.get_data()
    decodedData = data.decode('UTF-8')
    jsonBody = json.loads(decodedData)
    # Replace the dummyMethod
    search_bing_api(jsonBody)
    search_entities_on_elife(jsonBody)
    return jsonify(jsonBody)

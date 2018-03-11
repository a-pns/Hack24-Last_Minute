import json
from flask import jsonify
from flask import request
# rename dummyMethod to your method
from app.search import dummyMethod
from app.elife_api import search_entities_on_elife

def search_entities():
    data = request.get_data()
    decodedData = data.decode('UTF-8')
    jsonBody = json.loads(decodedData)
    # Replace the dummyMethod
    jsonToReturn = dummyMethod(jsonBody)
    search_entities_on_elife(jsonToReturn)
    return jsonify(jsonToReturn)

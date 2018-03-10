import json
from flask import jsonify
from flask import request
# rename dummyMethod to your method
from app.search import dummyMethod

def search_entities():
    data = request.get_data()
    decodedData = data.decode('UTF-8')
    jsonBody = json.loads(decodedData)
    # Replace the dummyMethod
    jsonToReturn = dummyMethod(jsonBody)
    return jsonify(jsonToReturn)

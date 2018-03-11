# -*- coding: utf-8 -*-

import http.client, urllib.parse
import json

# **********************************************
# *** Update or verify the following values. ***
# **********************************************

# Replace the subscriptionKey string value with your valid subscription key.
subscriptionKey = '5a3cd3d67c67466ca30a2816510913e1'

host = 'api.cognitive.microsoft.com'
path = '/bing/v7.0/entities/'

mkt = 'en-GB'
count = '10'
offset = '0'
safesearch = 'Moderate'


array = '{"entities":[{"name":"leptospiral infection","tag":"entity0","type":"MedicalCondition","textBeforeRefs":["sterilizing immunity for the animals from "],"textAfterRefs":["<\/body>"]},{"name":"inflammatory cells","tag":"entity1","type":"MedicalCondition","textBeforeRefs":["livers were infiltrated by various "],"textAfterRefs":[", indicating moderate to severe hepatitis. Focal"]},{"name":"immunizations","tag":"entity3","type":"MedicalTreatment","textBeforeRefs":["studies were tested for the ability of the ","et al., 2000 ). The hamster group that received "],"textAfterRefs":[" to generate an effective humoral response using"," containing recombinant chimeric protein,"]},{"name":"severe hepatitis","tag":"entity4","type":"MedicalCondition","textBeforeRefs":["inflammatory cells, indicating moderate to "],"textAfterRefs":[". Focal necrosis was also found in parenchymal"]},{"name":"Focal necrosis","tag":"entity5","type":"MedicalCondition","textBeforeRefs":["cells, indicating moderate to severe hepatitis. "],"textAfterRefs":[" was also found in parenchymal hepatocytes,"]},{"name":"e.g. multifocal pulmonary ecchymoses","tag":"entity6","type":"MedicalCondition","textBeforeRefs":["lesions on spirochete-targeted organs ("],"textAfterRefs":[", icteric liver and enlarged kidney). Lung"]},{"name":"immunization","tag":"entity10","type":"MedicalTreatment","textBeforeRefs":["analysis for LigB Ig-like domains utilized in ","with the negative control, LigB12. LigB10-B7-B7 ","kidney, and urinary bladder tissue from all ","all other hamsters from PBS, LigB7 and LigB10 "],"textAfterRefs":[" trials.(A) Far-UV circular dichroism (CD)"," led to an increase in post-booster secondary"," groups were examined by real-time quantitative"," groups presented severe clinical signs and"]},{"name":"immunization","tag":"entity10","type":"MedicalTreatment","textBeforeRefs":["analysis for LigB Ig-like domains utilized in ","with the negative control, LigB12. LigB10-B7-B7 ","kidney, and urinary bladder tissue from all ","all other hamsters from PBS, LigB7 and LigB10 "],"textAfterRefs":[" trials.(A) Far-UV circular dichroism (CD)"," led to an increase in post-booster secondary"," groups were examined by real-time quantitative"," groups presented severe clinical signs and"]},{"name":"Severe tubulointerstitial nephritis","tag":"entity11","type":"MedicalCondition","textBeforeRefs":["loss of normal tissue integrity ( Figure 7D ). "],"textAfterRefs":[" with locally extensive hemorrhage in uriniferous"]},{"name":"severe fibrosis","tag":"entity12","type":"MedicalCondition","textBeforeRefs":["by lymphoplasmacytic cells infiltration and "],"textAfterRefs":[". In contrast, all hamsters immunized with"]}]}'

data = json.loads(array)

list_one = data['entities']

query = []

for i in range(len(list_one)):

	query.append(list_one[i]['name'])

for i in range(len(query)):
	params = '?q=' + urllib.parse.quote (query[i]) + '&mkt=' + mkt + '&count=' + count + '&offset=' + offset + '&safesearch=' + safesearch
#urllib.parse.quote 

	print("\n"+path+params+"\n")

	def get_suggestions ():
	    headers = {'Ocp-Apim-Subscription-Key': subscriptionKey}
	    conn = http.client.HTTPSConnection (host)
	    conn.request ("GET", path+params, None, headers)
	    response = conn.getresponse ()
	    return response.read ()

	result = get_suggestions()

	json_results = json.loads(result)

	if(json_results.get('entities',None)):
		list_one[i]['description'] = json_results['entities']['value'][0]['description']

print(list_one)		
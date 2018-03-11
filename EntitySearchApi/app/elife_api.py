import requests
elife_search_api = 'http://api.elifesciences.org/search'
def search_elife_api(term):
    params = {"for":term,"per-page":10}
    resp = requests.get(url=elife_search_api, params=params)
    data = resp.json()
    return data["items"]

def search_entities_on_elife(entities):
    entityArray = entities['entities']
    for entity in entityArray:
        items = search_elife_api(entity['name'])
        e_life_links = []
        entity["eLife Links"] = e_life_links
        for item in items:
            item = {"title":item["title"], "link":"https://elifesciences.org/articles/" + item["id"]}
            e_life_links.append(item)

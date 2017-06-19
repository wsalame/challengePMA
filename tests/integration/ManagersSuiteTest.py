import requests
import json

def test_findClosestManager_in_Beirut_find_Barcelona():
    url='http://localhost:4567/managers/find?lat=33&lon=35'
    response = requests.get(url)

    if(response.ok):
        jData = json.loads(response.content)
        assert jData['hostname'] == 'barcelona.is.awesome.com'
    else:
        response.raise_for_status()

def test_findClosestManager_in_Toronto_find_Montreal():
    url='http://localhost:4567/managers/find?lat=43.761539&lon=-79.411079'
    response = requests.get(url)

    if(response.ok):
        jData = json.loads(response.content)
        assert jData['hostname'] == 'pmng1.mtl-east.ampme.com'
    else:
        response.raise_for_status()

if __name__ == '__main__':
    # Find closest manager
    # GET /managers/find?lng=[longitude]&lat=[latitude]
    test_findClosestManager_in_Beirut_find_Barcelona()
    test_findClosestManager_in_Toronto_find_Montreal()
    print 'All passed'
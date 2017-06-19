import requests
import json


def test_createPartyInSherbrooke():
    url = 'http://localhost:5151/parties?lat=45.4&lon=-71.88'
    body = '''
    {
        "device_id": "sherby #1"
    }
    '''
    response = requests.post(url, data=body)

    if (response.ok):
        jData = json.loads(response.content)
        assert jData['party_code'] is not None and jData['participant_id'] == "sherby #1"
        return jData['party_code']
    else:
        response.raise_for_status()


def test_createPartyInLaval():
    url = 'http://localhost:5151/parties?lat=45.55396771389642&lon=-73.69972229003906'
    body = '''
    {
        "device_id": "iphone marc bergevin laval"
    }
    '''
    response = requests.post(url, data=body)

    if (response.ok):
        jData = json.loads(response.content)
        assert jData['party_code'] is not None and jData['participant_id'] == "iphone marc bergevin laval"
        return jData['party_code']
    else:
        response.raise_for_status()
        assert False


def test_createPartyInParis():
    url = 'http://localhost:5151/parties?lat=48.864&lon=2.3490'
    body = '''
    {
        "device_id": "du coup je suis a paris"
    }
    '''
    response = requests.post(url, data=body)

    if (response.ok):
        jData = json.loads(response.content)
        assert jData['party_code'] is not None and jData['participant_id'] == "du coup je suis a paris"
        return jData['party_code']
    else:
        response.raise_for_status()


def test_joinPartyInLaval_userOne(party_code_laval):
    url = 'http://localhost:5151/parties/%s/participants?lat=45.56&lon=-73.69' % party_code_laval
    body = '''
       {
           "device_id": "galaxy s5 carrefour laval"
       }
    '''
    response = requests.put(url, data=body)

    if (response.ok):
        jData = json.loads(response.content)
        assert jData['party_code'] is not None and jData['participant_id'] == "galaxy s5 carrefour laval" and jData[
                                                                                                                  'number_guests'] == 1
    else:
        response.raise_for_status()


def test_joinPartyInLaval_userTwo(party_code_laval):
    url = 'http://localhost:5151/parties/%s/participants?lat=44.56&lon=-73.69' % party_code_laval
    body = '''
       {
           "device_id": "wifi colossus"
       }
    '''
    response = requests.put(url, data=body)

    if (response.ok):
        jData = json.loads(response.content)
        assert jData['party_code'] is not None and jData['participant_id'] == "wifi colossus" and jData[
                                                                                                      'number_guests'] == 2
    else:
        response.raise_for_status()


def test_discoveryInMontreal_showOneParty(size):
    url = 'http://localhost:5151/parties/find?lat=45.500&lon=-73.5&size=%s' % size

    response = requests.get(url)

    if (response.ok):
        jData = json.loads(response.content)

        assert "iphone marc bergevin laval" in jData['nearby_parties']
    else:
        response.raise_for_status()


def test_discoveryInBarcelona_showTwoParty(size):
    url = 'http://localhost:5151/parties/find?lat=41.390205&lon=2.154007&size=%s' % size

    response = requests.get(url)

    if (response.ok):
        jData = json.loads(response.content)

        assert "du coup je suis a paris" in jData['nearby_parties'] and "sherby #1" in jData['nearby_parties']
    else:
        response.raise_for_status()


def test_listParticipants_partyLaval(party_code_laval):
    url = 'http://localhost:5151/parties/%s/participants' % party_code_laval

    response = requests.get(url)
    if (response.ok):
        jData = json.loads(response.content)
        assert jData['participants']['primary_device_id'] == "iphone marc bergevin laval"
        assert "galaxy s5 carrefour laval" in jData['participants']['guests'] and len(jData['participants']['guests']) == 1
    else:
        response.raise_for_status()

def test_leavingPartyInLaval(party_code_laval):
    url = 'http://localhost:5151/parties/%s?lat=44.56&lon=-73.69' % party_code_laval
    body = '''
       {
           "device_id": "wifi colossus"
       }
       '''
    requests.delete(url, data=body)

if __name__ == '__main__':
    # Create parties
    test_createPartyInSherbrooke()
    party_code_laval = test_createPartyInLaval()
    test_createPartyInParis()

    # Join party
    test_joinPartyInLaval_userOne(party_code_laval)
    test_joinPartyInLaval_userTwo(party_code_laval)

    # Discovery
    test_discoveryInMontreal_showOneParty(1)
    test_discoveryInBarcelona_showTwoParty(2)

    # Participant leaving and Participant listing
    test_leavingPartyInLaval(party_code_laval)
    test_listParticipants_partyLaval(party_code_laval)

    print 'All passed'

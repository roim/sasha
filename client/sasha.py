from bottle import *
import json
import urllib2

@get('/search')
def search():
    if 'q' in request.query:
        search_hits = json.loads(urllib2.urlopen("http://localhost:8080/search?" + request.query_string).read())
        return template('results', search_hits=search_hits)
    else:
        random_quote = json.loads(urllib2.urlopen("http://quotes.stormconsultancy.co.uk/random.json").read())
        return template('search', quote=random_quote)

run(host='localhost', port=80)

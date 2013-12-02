from bottle import *
import json
import urllib2

@get('/search')
def search():
    if 'q' in request.query:
        search_hits = json.loads(urllib2.urlopen("http://localhost:8080/search?" + request.query_string).read())
        return template('results', search_hits=search_hits)
    else:
        return static_file('search.html', root='.')

run(host='localhost', port=80)

from bottle import *
import json
import urllib2

@get('/search')
def search():
    search_hits = {}
    random_quote = ""
    file = ""
    extension = request.query["ext"] if 'ext' in request.query else ""

    if 'q' in request.query:
        file = request.query["q"]
        search_hits = json.loads(urllib2.urlopen("http://localhost:8080/search?" + request.query_string).read())
    else:
        random_quote = json.loads(urllib2.urlopen("http://quotes.stormconsultancy.co.uk/random.json").read())

    return template('search', search_hits=search_hits, file=file, extension=extension, quote=random_quote)

run(host='localhost', port=80)

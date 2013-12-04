from bottle import *
import json
import urllib2

@get('/search')
def search():
    file = request.query.get("q", "")
    extension = request.query.get("ext", "")

    search_hits = {}
    random_quote = ""
    if 'q' in request.query:
        search_hits = json.loads(urllib2.urlopen("http://localhost:8080/search?" + request.query_string).read())
    else:
        random_quote = json.loads(urllib2.urlopen("http://quotes.stormconsultancy.co.uk/random.json").read())

    return template('search', search_hits=search_hits, file=file, extension=extension, quote=random_quote)

run(host='0.0.0.0', port=80)

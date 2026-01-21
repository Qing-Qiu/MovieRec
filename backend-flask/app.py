from flask import Flask
from flask import request
from flask import abort
from flask_cors import CORS
from gevent import pywsgi
import json
from kw import KwApi

app = Flask(__name__)
cors = CORS(app)


@app.route('/search')
def kuwoAPI():
    key = request.args.get('key')
    pn = request.args.get('pn', 0)
    # rn = 30 # Default in KwApi
    
    # Restore legacy behavior: frontend sends 1-based index, API expects 0-based
    try:
        page_idx = int(pn) - 1
        if page_idx < 0: page_idx = 0
    except:
        page_idx = 0

    result = KwApi.search(key, pn=page_idx)
    return json.dumps(result, ensure_ascii=False)


@app.route('/mp3')
def ridKuwoAPI():
    rid = request.args.get('rid')
    
    # Try 320k first
    final_url = KwApi.get_mp3_url(rid=rid, br='320kmp3')
    
    if final_url:
        return str(final_url)
    
    print(f"High Quality (320k) failed for {rid}, trying 128k...")
    
    # Fallback to 128k
    final_url = KwApi.get_mp3_url(rid=rid, br='128kmp3')
    
    if final_url:
        return str(final_url)
    
    print(f"MP3 retrieval failed for {rid}")
    abort(500)


@app.route('/lrc')
def lrcKuwoAPI():
    rid = request.args.get('rid')
    lrc_list = KwApi.get_lrc(rid)
    return json.dumps(lrc_list)


if __name__ == '__main__':
    server = pywsgi.WSGIServer(('0.0.0.0', 5000), app)
    server.serve_forever()

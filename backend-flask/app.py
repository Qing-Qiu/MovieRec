from flask import Flask
from flask import request
from flask import abort
from flask_cors import CORS
from gevent import pywsgi
import requests
import json
from kw import kwFirstUrl
import re

app = Flask(__name__)
cors = CORS(app)


@app.route('/search')
def kuwoAPI():
    key = request.args.get('key')
    pn = request.args.get('pn')
    rn = 30
    url = f'http://search.kuwo.cn/r.s?pn={int(pn) - 1}&rn={rn}&all={key}&ft=music&newsearch=1&alflac=1&itemset=web_2013&client=kt&cluster=0&vermerge=1&rformat=json&encoding=utf8&show_copyright_off=1&pcmp4=1&ver=mbox&plat=pc&vipver=MUSIC_9.2.0.0_W6&devid=11404450&newver=1&issubtitle=1&pcjson=1'
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/115.0.0.0 Safari/537.36 Edg/115.0.1901.188'
    }
    responseText = requests.get(url=url, headers=headers).text
    search = []
    try:
        responseJson = json.loads(responseText)
        for song in responseJson.get("abslist"):
            if song.get('web_albumpic_short') != '':
                if "120" in song.get('web_albumpic_short'):
                    pic = f'https://img4.kuwo.cn/star/albumcover/{song.get("web_albumpic_short").replace("120", "300")}'
                else:
                    pic = f'https://img4.kuwo.cn/star/albumcover/{song.get("web_albumpic_short")}'
            else:
                if "120" in song.get('web_artistpic_short'):
                    pic = f'https://img1.kuwo.cn/star/starheads/{song.get("web_artistpic_short").replace("120", "300")}'
                else:
                    pic = f'https://img1.kuwo.cn/star/starheads/{song.get("web_artistpic_short")}'
            tempList = {
                'name': song.get('SONGNAME'),
                'artist': song.get('ARTIST'),
                'rid': int(song.get('DC_TARGETID')),
                'pic': pic
            }
            search.append(tempList)
        return json.dumps(obj={'total': responseJson.get('TOTAL'), 'list': search}, ensure_ascii=False)
    except Exception as e:
        print(f'Server Error: {format(str(e))}')
        print(responseText)
        return abort(500)


@app.route('/mp3')
def ridKuwoAPI():
    rid = request.args.get('rid')
    # First try with high quality (320kmp3) - Default
    url = kwFirstUrl(rid=rid, br='320kmp3')
    headers = {
        "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36 Edg/110.0.1587.50",
        "csrf": "96Y8RG5X3X64",
        "Referer": "https://www.kuwo.cn"
    }
    
    music_url = ""
    try:
        music_url = requests.get(url=url, headers=headers, timeout=3).text
    except requests.Timeout:
        print('获取mp3链接超时，正在重试……')
        music_url = requests.get(url=url, headers=headers).text

    # Check if we got a valid URL
    pattern = r'url=(.*)'
    match = re.search(pattern, music_url)
    
    if match and match.group(1):
        final_url = match.group(1)
        print(f'已获取到高音质(320k) mp3文件链接 from 320k attempt=>{str(final_url)}')
        return str(final_url)
    
    print("高音质(320k)获取失败，尝试降级到标准音质(128k)...")
    
    # Fallback to standard quality (128kmp3)
    url_fallback = kwFirstUrl(rid=rid, br='128kmp3')
    try:
        music_url = requests.get(url=url_fallback, headers=headers, timeout=3).text
    except requests.Timeout:
         music_url = requests.get(url=url_fallback, headers=headers).text
         
    match_fallback = re.search(pattern, music_url)
    if match_fallback and match_fallback.group(1):
        final_url = match_fallback.group(1)
        print(f'已获取到标准音质(128k) mp3文件链接=>{str(final_url)}')
        return str(final_url)
    else:
        print("未找到URL (320k and 128k failed)")
        print('Error Info:\n' + music_url)
        abort(500)


@app.route('/lrc')
def lrcKuwoAPI():
    rid = request.args.get('rid')
    url = f'http://m.kuwo.cn/newh5/singles/songinfoandlrc?musicId={rid}&httpsStatus=1&reqId=1c3ccf60-f4a2-11ed-b93d-c5042ed5dae3'
    headers = {
        'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/113.0.0.0 Safari/537.36 Edg/113.0.1774.42'
    }
    try:
        response_json = requests.get(url=url, headers=headers, timeout=3).json()
    except requests.Timeout:
        print('获取歌词超时，正在重试……')
        response_json = requests.get(url=url, headers=headers).json()
        
    lrc = []
    if response_json.get("data"):
        lrc = response_json["data"].get("lrclist", [])
        print('已获取到歌词\n\n')
    else:
        print('无歌词数据\n\n')
        
    return json.dumps(lrc)


if __name__ == '__main__':
    server = pywsgi.WSGIServer(('0.0.0.0', 5000), app)
    server.serve_forever()

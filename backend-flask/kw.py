import base64
import copy
import requests
import json
import re
import random
import threading
import time
import uuid

class KwApi:
    DES_MODE_DECRYPT = 1

    arrayE = [
        31, 0, DES_MODE_DECRYPT, 2, 3, 4, -1, -1, 3, 4, 5, 6, 7, 8, -1, -1, 7, 8, 9, 10, 11, 12, -1, -1, 11, 12, 13, 14,
        15, 16, -1, -1, 15, 16, 17, 18, 19, 20, -1, -1, 19, 20, 21, 22, 23, 24, -1, -1, 23, 24, 25, 26, 27, 28, -1, -1,
        27, 28, 29, 30, 31, 30, -1, -1
    ]

    arrayIP = [
        57, 49, 41, 33, 25, 17, 9, DES_MODE_DECRYPT, 59, 51, 43, 35, 27, 19, 11, 3, 61, 53, 45, 37, 29, 21, 13, 5, 63,
        55, 47, 39, 31, 23, 15, 7, 56, 48, 40, 32, 24, 16, 8, 0, 58, 50, 42, 34, 26, 18, 10, 2, 60, 52, 44, 36, 28, 20,
        12, 4, 62, 54, 46, 38, 30, 22, 14, 6
    ]

    arrayIP_1 = [
        39, 7, 47, 15, 55, 23, 63, 31, 38, 6, 46, 14, 54, 22, 62, 30, 37, 5, 45, 13, 53, 21, 61, 29, 36, 4, 44, 12, 52,
        20, 60, 28, 35, 3, 43, 11, 51, 19, 59, 27, 34, 2, 42, 10, 50, 18, 58, 26, 33, DES_MODE_DECRYPT, 41, 9, 49, 17,
        57, 25, 32, 0, 40, 8, 48, 16, 56, 24
    ]
    arrayLs = [1, 1, 2, 2, 2, 2, 2, 2, 1, 2, 2, 2, 2, 2, 2, 1]
    arrayLsMask = [0, 0x100001, 0x300003]
    arrayMask = [2 ** i for i in range(64)]
    arrayMask[-1] *= -1
    arrayP = [
        15, 6, 19, 20, 28, 11, 27, 16,
        0, 14, 22, 25, 4, 17, 30, 9,
        1, 7, 23, 13, 31, 26, 2, 8,
        18, 12, 29, 5, 21, 10, 3, 24,
    ]
    arrayPC_1 = [
        56, 48, 40, 32, 24, 16, 8, 0,
        57, 49, 41, 33, 25, 17, 9, 1,
        58, 50, 42, 34, 26, 18, 10, 2,
        59, 51, 43, 35, 62, 54, 46, 38,
        30, 22, 14, 6, 61, 53, 45, 37,
        29, 21, 13, 5, 60, 52, 44, 36,
        28, 20, 12, 4, 27, 19, 11, 3,
    ]
    arrayPC_2 = [
        13, 16, 10, 23, 0, 4, -1, -1,
        2, 27, 14, 5, 20, 9, -1, -1,
        22, 18, 11, 3, 25, 7, -1, -1,
        15, 6, 26, 19, 12, 1, -1, -1,
        40, 51, 30, 36, 46, 54, -1, -1,
        29, 39, 50, 44, 32, 47, -1, -1,
        43, 48, 38, 55, 33, 52, -1, -1,
        45, 41, 49, 35, 28, 31, -1, -1,
    ]
    matrixNSBox = [[
        14, 4, 3, 15, 2, 13, 5, 3,
        13, 14, 6, 9, 11, 2, 0, 5,
        4, 1, 10, 12, 15, 6, 9, 10,
        1, 8, 12, 7, 8, 11, 7, 0,
        0, 15, 10, 5, 14, 4, 9, 10,
        7, 8, 12, 3, 13, 1, 3, 6,
        15, 12, 6, 11, 2, 9, 5, 0,
        4, 2, 11, 14, 1, 7, 8, 13, ], [
        15, 0, 9, 5, 6, 10, 12, 9,
        8, 7, 2, 12, 3, 13, 5, 2,
        1, 14, 7, 8, 11, 4, 0, 3,
        14, 11, 13, 6, 4, 1, 10, 15,
        3, 13, 12, 11, 15, 3, 6, 0,
        4, 10, 1, 7, 8, 4, 11, 14,
        13, 8, 0, 6, 2, 15, 9, 5,
        7, 1, 10, 12, 14, 2, 5, 9, ], [
        10, 13, 1, 11, 6, 8, 11, 5,
        9, 4, 12, 2, 15, 3, 2, 14,
        0, 6, 13, 1, 3, 15, 4, 10,
        14, 9, 7, 12, 5, 0, 8, 7,
        13, 1, 2, 4, 3, 6, 12, 11,
        0, 13, 5, 14, 6, 8, 15, 2,
        7, 10, 8, 15, 4, 9, 11, 5,
        9, 0, 14, 3, 10, 7, 1, 12, ], [
        7, 10, 1, 15, 0, 12, 11, 5,
        14, 9, 8, 3, 9, 7, 4, 8,
        13, 6, 2, 1, 6, 11, 12, 2,
        3, 0, 5, 14, 10, 13, 15, 4,
        13, 3, 4, 9, 6, 10, 1, 12,
        11, 0, 2, 5, 0, 13, 14, 2,
        8, 15, 7, 4, 15, 1, 10, 7,
        5, 6, 12, 11, 3, 8, 9, 14, ], [
        2, 4, 8, 15, 7, 10, 13, 6,
        4, 1, 3, 12, 11, 7, 14, 0,
        12, 2, 5, 9, 10, 13, 0, 3,
        1, 11, 15, 5, 6, 8, 9, 14,
        14, 11, 5, 6, 4, 1, 3, 10,
        2, 12, 15, 0, 13, 2, 8, 5,
        11, 8, 0, 15, 7, 14, 9, 4,
        12, 7, 10, 9, 1, 13, 6, 3, ], [
        12, 9, 0, 7, 9, 2, 14, 1,
        10, 15, 3, 4, 6, 12, 5, 11,
        1, 14, 13, 0, 2, 8, 7, 13,
        15, 5, 4, 10, 8, 3, 11, 6,
        10, 4, 6, 11, 7, 9, 0, 6,
        4, 2, 13, 1, 9, 15, 3, 8,
        15, 3, 1, 14, 12, 5, 11, 0,
        2, 12, 14, 7, 5, 10, 8, 13, ], [
        4, 1, 3, 10, 15, 12, 5, 0,
        2, 11, 9, 6, 8, 7, 6, 9,
        11, 4, 12, 15, 0, 3, 10, 5,
        14, 13, 7, 8, 13, 14, 1, 2,
        13, 6, 14, 9, 4, 1, 2, 14,
        11, 13, 5, 0, 1, 10, 8, 3,
        0, 11, 3, 5, 9, 4, 15, 2,
        7, 8, 12, 15, 10, 7, 6, 12, ], [
        13, 7, 10, 0, 6, 9, 5, 15,
        8, 4, 3, 10, 11, 14, 12, 5,
        2, 11, 9, 6, 15, 12, 0, 3,
        4, 1, 14, 13, 1, 2, 7, 8,
        1, 2, 12, 15, 10, 4, 0, 3,
        13, 14, 6, 9, 7, 8, 9, 6,
        15, 1, 5, 12, 3, 10, 14, 5,
        8, 7, 11, 0, 4, 13, 2, 11, ],
    ]

    SECRET_KEY = b'ylzsxkwm'
    USER_AGENTS = [
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Edg/120.0.0.0 Safari/537.36',
    ]
    RETRY_STATUS_CODES = {429, 500, 502, 503, 504}
    CACHE_TTL_SECONDS = {
        'search': 90,
        'mp3': 240,
        'lrc': 300,
    }
    _cache = {}
    _cache_lock = threading.Lock()

    @classmethod
    def cache_get(cls, namespace, key):
        cache_key = (namespace, key)
        now = time.monotonic()
        with cls._cache_lock:
            cached = cls._cache.get(cache_key)
            if not cached:
                return None

            expires_at, value = cached
            if expires_at <= now:
                cls._cache.pop(cache_key, None)
                return None

            return copy.deepcopy(value)

    @classmethod
    def cache_set(cls, namespace, key, value):
        ttl = cls.CACHE_TTL_SECONDS.get(namespace)
        if not ttl:
            return

        with cls._cache_lock:
            cls._cache[(namespace, key)] = (time.monotonic() + ttl, copy.deepcopy(value))

    @classmethod
    def request_get(cls, url, retries=3, backoff=0.35, **kwargs):
        headers = dict(kwargs.pop('headers', {}) or {})
        headers.setdefault('User-Agent', random.choice(cls.USER_AGENTS))
        kwargs.setdefault('timeout', 8)

        last_error = None
        for attempt in range(retries):
            session = requests.Session()
            session.trust_env = False
            try:
                response = session.get(url, headers=headers, **kwargs)
                response.content
                if response.status_code in cls.RETRY_STATUS_CODES and attempt < retries - 1:
                    last_error = requests.HTTPError(f'HTTP {response.status_code} from {url}')
                    time.sleep(backoff * (2 ** attempt) + random.uniform(0, 0.18))
                    continue
                return response
            except requests.RequestException as error:
                last_error = error
                if attempt >= retries - 1:
                    raise
                time.sleep(backoff * (2 ** attempt) + random.uniform(0, 0.18))
            finally:
                session.close()

        if last_error:
            raise last_error
        raise requests.RequestException(f'GET failed: {url}')

    @classmethod
    def bit_transform(cls, arr_int, n, l):
        l2 = 0
        for i in range(n):
            if arr_int[i] < 0 or (l & cls.arrayMask[arr_int[i]] == 0):
                continue
            l2 |= cls.arrayMask[i]
        return l2

    @classmethod
    def DES64(cls, longs, l):
        out = 0
        SOut = 0
        pR = [0] * 8
        pSource = [0, 0]
        sbi = 0
        t = 0
        L = 0
        R = 0
        out = cls.bit_transform(cls.arrayIP, 64, l)
        pSource[0] = 0xFFFFFFFF & out
        pSource[1] = (-4294967296 & out) >> 32
        for i in range(16):
            R = pSource[1]
            R = cls.bit_transform(cls.arrayE, 64, R)
            R ^= longs[i]
            for j in range(8):
                pR[j] = 255 & R >> j * 8
            SOut = 0
            for sbi in range(7, -1, -1):
                SOut <<= 4
                SOut |= cls.matrixNSBox[sbi][pR[sbi]]

            R = cls.bit_transform(cls.arrayP, 32, SOut)
            L = pSource[0]
            pSource[0] = pSource[1]
            pSource[1] = L ^ R
        pSource = pSource[::-1]
        out = -4294967296 & pSource[1] << 32 | 0xFFFFFFFF & pSource[0]
        out = cls.bit_transform(cls.arrayIP_1, 64, out)
        return out

    @classmethod
    def sub_keys(cls, l, longs, n):
        l2 = cls.bit_transform(cls.arrayPC_1, 56, l)
        for i in range(16):
            l2 = ((l2 & cls.arrayLsMask[cls.arrayLs[i]]) << 28 -
                  cls.arrayLs[i] | (l2 & ~cls.arrayLsMask[cls.arrayLs[i]]) >> cls.arrayLs[i])
            longs[i] = cls.bit_transform(cls.arrayPC_2, 64, l2)
        j = 0
        while n == 1 and j < 8:
            l3 = longs[j]
            longs[j], longs[15 - j] = longs[15 - j], longs[j]
            j += 1

    @classmethod
    def encrypt(cls, msg, key=SECRET_KEY):
        if isinstance(msg, str):
            msg = msg.encode()
        if isinstance(key, str):
            key = key.encode()
        assert (isinstance(msg, bytes))
        assert (isinstance(key, bytes))

        l = 0
        for i in range(8):
            l = l | key[i] << i * 8

        j = len(msg) // 8
        arrLong1 = [0] * 16
        cls.sub_keys(l, arrLong1, 0)
        arrLong2 = [0] * j
        for m in range(j):
            for n in range(8):
                arrLong2[m] |= msg[n + m * 8] << n * 8

        arrLong3 = [0] * ((1 + 8 * (j + 1)) // 8)
        for i1 in range(j):
            arrLong3[i1] = cls.DES64(arrLong1, arrLong2[i1])

        arrByte1 = msg[j * 8:]
        l2 = 0
        for i1 in range(len(msg) % 8):
            l2 |= arrByte1[i1] << i1 * 8
        arrLong3[j] = cls.DES64(arrLong1, l2)

        arrByte2 = [0] * (8 * len(arrLong3))
        i4 = 0
        for l3 in arrLong3:
            for i6 in range(8):
                arrByte2[i4] = (255 & l3 >> i6 * 8)
                i4 += 1
        return arrByte2

    @classmethod
    def base64_encrypt(cls, msg):
        b1 = cls.encrypt(msg)
        b2 = bytearray(b1)
        s = base64.encodebytes(b2)
        return s.replace(b'\n', b'').decode()

    @classmethod
    def get_mp3_url(cls, rid, br='320kmp3'):
        rid = cls.normalize_rid(rid)
        if not rid:
            return None

        cache_key = (rid, br)
        cached_url = cls.cache_get('mp3', cache_key)
        if cached_url:
            return cached_url

        # Construct the query URL
        query = f"user=0&android_id=0&prod=kwplayer_ar_8.5.5.0&corp=kuwo&newver=3&vipver=8.5.5.0&source=kwplayer_ar_8.5.5.0_apk_keluze.apk&p2p=1&notrace=0&type=convert_url2&br={br}&format=flac|mp3|aac&sig=0&rid={rid}&priority=bitrate&loginUid=0&network=WIFI&loginSid=0&mode=download"
        encrypted_query = cls.base64_encrypt(query)
        url = f'http://mobi.kuwo.cn/mobi.s?f=kuwo&q={encrypted_query}'
        
        # We need to fetch this URL to get the ACTUAL mp3 link (which is in the body of the response)
        try:
           headers = {
               "csrf": "96Y8RG5X3X64",
               "Referer": "https://www.kuwo.cn"
           }
           # Try fetching
           resp = cls.request_get(url, headers=headers, timeout=6, retries=2)
           resp.raise_for_status()
           pattern = r'url=(.*)'
           match = re.search(pattern, resp.text)
           if match and match.group(1):
               final_url = match.group(1).strip()
               cls.cache_set('mp3', cache_key, final_url)
               return final_url
           else:
               # Try to parse properties if it's line separated
               for line in resp.text.splitlines():
                   if line.startswith('url='):
                       final_url = line.split('=', 1)[1].strip()
                       cls.cache_set('mp3', cache_key, final_url)
                       return final_url
        except:
            return None 
        return None

    @classmethod
    def search(cls, key, pn=0, rn=30):
        key = (key or '').strip()
        try:
            pn = max(int(pn), 0)
        except (TypeError, ValueError):
            pn = 0
        try:
            rn = max(int(rn), 1)
        except (TypeError, ValueError):
            rn = 30

        cache_key = (key, pn, rn)
        cached_result = cls.cache_get('search', cache_key)
        if cached_result is not None:
            return cached_result

        url = 'http://search.kuwo.cn/r.s'
        params = {
            'pn': pn,
            'rn': rn,
            'all': key or '',
            'ft': 'music',
            'newsearch': '1',
            'alflac': '1',
            'itemset': 'web_2013',
            'client': 'kt',
            'cluster': '0',
            'vermerge': '1',
            'rformat': 'json',
            'encoding': 'utf8',
            'show_copyright_off': '1',
            'pcmp4': '1',
            'ver': 'mbox',
            'plat': 'pc',
            'vipver': 'MUSIC_9.2.0.0_W6',
            'devid': '11404450',
            'newver': '1',
            'issubtitle': '1',
            'pcjson': '1',
        }
        headers = {
            'Accept': 'application/json,text/plain,*/*',
            'Referer': 'https://www.kuwo.cn/',
        }
        try:
            response = cls.request_get(url=url, params=params, headers=headers, timeout=8)
            response.raise_for_status()
            response.encoding = 'utf-8'
            responseJson = json.loads(response.text)
            
            search_results = []
            if responseJson.get("abslist"):
                for song in responseJson.get("abslist"):
                    try:
                        # Extract images
                        pic = ""
                        if song.get('web_albumpic_short'):
                             p = song.get('web_albumpic_short')
                             if "120" in p: p = p.replace("120", "300")
                             pic = f'https://img4.kuwo.cn/star/albumcover/{p}'
                        elif song.get('web_artistpic_short'):
                             p = song.get('web_artistpic_short')
                             if "120" in p: p = p.replace("120", "300")
                             pic = f'https://img1.kuwo.cn/star/starheads/{p}'
                        
                        search_results.append({
                            'name': song.get('SONGNAME'),
                            'artist': song.get('ARTIST'),
                            'rid': int(song.get('DC_TARGETID')),
                            'pic': pic,
                            'album': song.get('ALBUM')
                        })
                    except:
                        continue
            
            result = {'total': responseJson.get('TOTAL'), 'list': search_results}
            cls.cache_set('search', cache_key, result)
            return result
        except Exception as e:
            print(f"Search API Error: {e}")
            return {'total': 0, 'list': []}

    @classmethod
    def normalize_rid(cls, rid):
        rid_text = str(rid).strip()
        return rid_text if rid_text.isdigit() else None

    @classmethod
    def extract_lrc_list(cls, data):
        if not isinstance(data, dict):
            return []

        payload = data.get("data") or {}
        lrc_list = payload.get("lrclist") or []
        return lrc_list if isinstance(lrc_list, list) else []

    @classmethod
    def fetch_lrc_direct(cls, rid, base_url):
        params = {
            'musicId': rid,
        }
        if 'newh5' in base_url:
            params.update({
                'httpsStatus': '0' if base_url.startswith('http://') else '1',
                'reqId': str(uuid.uuid4()),
            })
        headers = {
            'Accept': 'application/json,text/plain,*/*',
            'Referer': 'https://www.kuwo.cn/' if 'openapi' in base_url else f'https://m.kuwo.cn/yinyue/{rid}',
        }

        response = cls.request_get(
            base_url,
            params=params,
            headers=headers,
            timeout=6,
            retries=3,
        )
        response.raise_for_status()
        response.encoding = 'utf-8'
        try:
            data = response.json()
        except ValueError:
            data = json.loads(response.text)
        return cls.extract_lrc_list(data)

    @classmethod
    def fetch_lrc_subprocess(cls, rid):
        import subprocess
        import sys
        import os

        # Keep this as a fallback path because Kuwo's lyric endpoint sometimes
        # behaves differently in Flask's long-lived process than in a fresh one.
        script = """
import json
import os
import sys
import urllib.parse
import urllib.request
import uuid

def extract_lrc_list(data):
    payload = data.get('data') or {}
    lrc_list = payload.get('lrclist') or []
    return lrc_list if isinstance(lrc_list, list) else []

def fetch():
    for key in ('HTTP_PROXY', 'HTTPS_PROXY', 'ALL_PROXY', 'http_proxy', 'https_proxy', 'all_proxy'):
        os.environ.pop(key, None)

    rid = sys.argv[1]
    sources = (
        ('https://www.kuwo.cn/openapi/v1/www/lyric/getlyric', None),
        ('https://kuwo.cn/openapi/v1/www/lyric/getlyric', None),
        'https://m.kuwo.cn/newh5/singles/songinfoandlrc',
        'http://m.kuwo.cn/newh5/singles/songinfoandlrc',
    )

    for source in sources:
        if isinstance(source, tuple):
            base_url, https_status = source
        else:
            base_url = source
            https_status = '0' if base_url.startswith('http://') else '1'

        query = {
            'musicId': rid,
        }
        if https_status is not None:
            query.update({
                'httpsStatus': https_status,
                'reqId': str(uuid.uuid4()),
            })

        params = urllib.parse.urlencode(query)
        headers = {
            'Accept': 'application/json,text/plain,*/*',
            'Referer': 'https://www.kuwo.cn/' if 'openapi' in base_url else f'https://m.kuwo.cn/yinyue/{rid}',
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
        }
        try:
            req = urllib.request.Request(f'{base_url}?{params}', headers=headers)
            with urllib.request.urlopen(req, timeout=6) as response:
                data = json.loads(response.read().decode('utf-8'))
            lrc_list = extract_lrc_list(data)
            if lrc_list:
                print(json.dumps(lrc_list, ensure_ascii=False))
                return
        except Exception:
            continue

    print('[]')

if __name__ == '__main__':
    fetch()
"""
        try:
            env = os.environ.copy()
            for key in ('HTTP_PROXY', 'HTTPS_PROXY', 'ALL_PROXY', 'http_proxy', 'https_proxy', 'all_proxy'):
                env.pop(key, None)

            result = subprocess.run(
                [sys.executable, "-c", script, rid],
                capture_output=True,
                text=True,
                timeout=14,
                encoding='utf-8',
                errors='replace',
                env=env
            )
            return json.loads(result.stdout.strip() or '[]')
        except Exception:
            return []

    @classmethod
    def get_lrc(cls, rid):
        rid = cls.normalize_rid(rid)
        if not rid:
            return []

        cached_lrc = cls.cache_get('lrc', rid)
        if cached_lrc is not None:
            return cached_lrc

        lyric_urls = (
            'https://www.kuwo.cn/openapi/v1/www/lyric/getlyric',
            'https://kuwo.cn/openapi/v1/www/lyric/getlyric',
            'https://m.kuwo.cn/newh5/singles/songinfoandlrc',
            'http://m.kuwo.cn/newh5/singles/songinfoandlrc',
        )
        for base_url in lyric_urls:
            try:
                lrc_list = cls.fetch_lrc_direct(rid, base_url)
                if lrc_list:
                    cls.cache_set('lrc', rid, lrc_list)
                    return lrc_list
            except Exception:
                continue

        lrc_list = cls.fetch_lrc_subprocess(rid)
        if lrc_list:
            cls.cache_set('lrc', rid, lrc_list)
        return lrc_list

def kwFirstUrl(rid, br='320kmp3'):
    query = f"user=0&android_id=0&prod=kwplayer_ar_8.5.5.0&corp=kuwo&newver=3&vipver=8.5.5.0&source=kwplayer_ar_8.5.5.0_apk_keluze.apk&p2p=1&notrace=0&type=convert_url2&br={br}&format=flac|mp3|aac&sig=0&rid={rid}&priority=bitrate&loginUid=0&network=WIFI&loginSid=0&mode=download"
    encrypted = KwApi.base64_encrypt(query)
    # Return the wrapped URL as before
    return f'http://mobi.kuwo.cn/mobi.s?f=kuwo&q={encrypted}'

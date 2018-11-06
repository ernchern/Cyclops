#!/usr/bin/env python3

import requests, json

KEY_LENGTH = 8
KEY_NAME = 'key.txt'
SERVER_URL = "http://143.248.140.158:6780/bike/location"

key_file = open(KEY_NAME, 'r')
key = key_file.read(KEY_LENGTH)

payload =   {
            "key" : key,
            "location" : {
                "lat"  : 0,
                "long" : 0
                }
            }

for x in range(5):
    payload["location"]["lat"] = x
    payload["location"]["long"] = 2*x
                
    print("PAYLOAD:", payload)

    r = requests.post(SERVER_URL, json=payload)

    print(r.status_code, r.reason)
    # print(r.headers)
    print(r.text[:100])


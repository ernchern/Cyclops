#!/usr/bin/env python3

import requests, json
import random 

KEY_LENGTH = 8
KEY_NAME = 'key.txt'
SERVER_URL = "http://143.248.140.158:6780/bike/location"
rand_lat = random.randint(-90,90)
rand_long = random.randint(-180,180)


def gps_simulator():
    
    return (rand_lat, rand_long) if random.randint(1,10)!=1 else (random.randint(-90,90),random.randint(-180,180))

random.seed()

key_file = open(KEY_NAME, 'r')
key = key_file.read(KEY_LENGTH)

payload =   {
            "key" : key,
            "location" : {
                "lat"  : 0,
                "long" : 0
                }
            }

for x in range(20):
    
    coordinates = gps_simulator()
    payload["location"]["lat"] = coordinates[0]
    payload["location"]["long"] = coordinates[1]
                
    print("PAYLOAD:", payload)

    r = requests.post(SERVER_URL, json=payload)

    print(r.status_code, r.reason)
    # print(r.headers)
    print(r.text[:100])


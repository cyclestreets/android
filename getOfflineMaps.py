# Uses Python 3.
#
# You will first need to run:
#     `pip install requests PyFunctional`
#
# Then run:
#     `python getOfflineMaps.py`
#
# and an updated offline map JSON file will be generated.

# TODO: Turn this into a Gradle task... or maybe even an occasional Java execution, just like the Blog!!!!!

import json
import requests
import re
from functional import seq

def get_mb(size):
    unit = size[-1]
    amount = float(size[:-1])
    if unit == 'K':
        return 1
    if unit == 'G':
        return round(amount * 1024)
    if unit == 'M':
        return round(amount)

# At the moment, this just gets a list of current European maps.
r = requests.get('http://ftp-stud.hs-esslingen.de/pub/Mirrors/download.mapsforge.org/maps/v4/europe/')

# print(r.text)

row_regex = re.compile('<tr>.*?<a href="([^"]*?\.map)">.*?(\d{4}-\d{2}-\d{2}).*?<td align="right">\s*([\d\.]*[MGK])</td>.*?</tr>')

rows = row_regex.findall(r.text, re.MULTILINE)

out = seq(rows) \
    .map(lambda x: {'path': x[0], 'lastModified': x[1], 'sizeMb': get_mb(x[2])}) \
    .list()

out_json = json.dumps(out, indent=2)

with open('offline_maps.json', 'w') as f:
    f.write(out_json)

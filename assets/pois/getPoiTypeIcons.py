#!/usr/bin/env python2

# Retrieve POI type icons and save them to the 'drawable' folder.
#
# This gets done as part of the Travis build, to verify no changes have been made.
# When changes are made, this should be run manually and the checked-in files updated.
#
# Can target the API like https://api.cyclestreets.net/v2/pois.types?key=redacted&icons=64
# with a number of icon sizes for different densities, to achieve 16dp, as follows.
#
# 64 = xxxhdpi
# 48 = xxhdpi
# 32 = xhdpi
# 24 = hdpi
# 16 = mdpi
#
# However, testing showed that Android does a better job of dynamic downscaling than the static
# 48px icons, so we don't store the xxhdpi ones.
#
# TODO: upgrade this back to Python 3 as soon as the Travis image can be upgraded from "trusty".

import glob
import os
import requests

# Note that it's important to use our app's key, specifically.  Other keys will return a different
# set of POI types.
apikey = 'redacted'
with open('../../libraries/cyclestreets-core/src/test/resources/cyclestreets-api.key', 'r') as apikey_file:
    apikey = apikey_file.read().replace('\n', '')
session = requests.Session()
session.params.update({'key': apikey})

host = 'https://api.cyclestreets.net/v2'
api = 'pois.types'

iconSizes = { 64: 'xxxhdpi', 32: 'xhdpi', 24: 'hdpi', 16: 'mdpi'}

# Clear out previously-downloaded icons
existingPoiIconFiles = glob.glob('../../libraries/cyclestreets-core/src/main/res/drawable-*/poi_*.png')
for file in existingPoiIconFiles:
    os.remove(file)

# Get the icons
for size, density in iconSizes.items():
    print('Processing size {} ({}):'.format(size, density)),
    r = session.get(url='{}/{}'.format(host, api), params={ 'icons': size }, verify=False)
    r.raise_for_status()
    data = r.json()
    types = data['types']

    for id, type in types.items():
        # Remove base64 encoding
        decoded_img = type['icon'].decode('base64')
        with open('../../libraries/cyclestreets-core/src/main/res/drawable-{}/poi_{}.png'.format(density, id), 'wb') as fh:
            fh.write(decoded_img)
        print('.'),

    print(' done')

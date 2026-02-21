import json
import sys

page_to_check = int(sys.argv[1]) if len(sys.argv) > 1 else 2

with open('app/src/main/assets/data/hafs_smart_v8.json', 'r') as f:
    data = json.load(f)

for item in data:
    if item['page'] == page_to_check:
        print(f"ID {item['id']} Sura {item['sura_no']} Aya {item['aya_no']} Page {item['page']}")

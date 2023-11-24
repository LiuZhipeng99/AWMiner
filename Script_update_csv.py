import csv
import sys

import requests
def txt2csv():
    # repos文件变更txt->csv->更多字段->更新和重组字段代码
    txt_file = 'c_repos_sorted_test.txt'
    csv_file = 'c_repos_200_test.csv'
    rows = []
    with open(txt_file, 'r') as f:
        reader1 = csv.reader(f, delimiter='\t')
        rows = list(reader1)

    with open(csv_file, 'w', newline='') as f:
        writer = csv.writer(f)
        writer.writerows(rows)

# 记得替换为你的GitHub令牌
github_token = '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'
headers = {'Authorization': f'token {github_token}'}
with open(sys.argv[1], 'r') as csvinput:
    with open("output2.csv", 'a', newline='',encoding='utf8') as csvoutput:
        writer = csv.writer(csvoutput)
        writer.writerow(['Repository Name', 'Repository URL', 'Default Branch', 'Star Count' , 'Description', 'Update_date'])
        for row in csv.reader(csvinput):
            name = row[0]
            url = row[1]
            repo_url = url.replace('https://github.com/', '').replace('.git', '')
            response = requests.get(f'https://api.github.com/repos/{repo_url}', headers=headers)
            print(repo_url)
            if response.status_code == 200:
                data = response.json()
                # print(data)
                writer.writerow([name,url.replace('.git', ''), data['default_branch'],data['stargazers_count'],data['description'],data['updated_at']])

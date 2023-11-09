import csv,sys,os,time
from git import Repo
from tqdm import tqdm


def clone_if_not_exists(url,destination_path):
    if os.path.exists(destination_path):
        print(destination_path, " exist")
    else:
        try:
            Repo.clone_from(url, destination_path)
            print(f"Clone successful: {url}")
        except Exception as e:
            print(f"Clone failed: {url}")
            print(e)

def clone_if_not_exists2(url,destination_path):
    if os.path.exists(destination_path):
        return
    else:
        try:
            Repo.clone_from(url, destination_path)
        except Exception as e:
            print(e)

def txt2csv():
    #
    txt_file = 'c_repos_sorted_test.txt'
    csv_file = 'c_repos_200_test.csv'
    rows = []
    with open(txt_file, 'r') as file:
        reader = csv.reader(file, delimiter='\t')
        rows = list(reader)

    with open(csv_file, 'w', newline='') as file:
        writer = csv.writer(file)
        writer.writerows(rows)

total_rows= 0
with open(sys.argv[1], 'r') as file:
    reader = csv.reader(file)
    total_rows = sum(1 for row in reader)  # 获取总行数

if __name__ == '__main__':
    # txt2csv()
    with open(sys.argv[1], 'r') as file:
        reader = csv.reader(file)
        for row in tqdm(reader, total=total_rows, ncols=80, desc="Cloning"):
            clone_if_not_exists2(row[2], "./tmp_github/" + row[3])
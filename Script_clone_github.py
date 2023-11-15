import csv, sys, os, time
from git import Repo
from tqdm import tqdm
import asyncio

def txt2csv():
    #
    txt_file = 'c_repos_sorted_test.txt'
    csv_file = 'c_repos_200_test.csv'
    rows = []
    with open(txt_file, 'r') as f:
        reader1 = csv.reader(f, delimiter='\t')
        rows = list(reader1)

    with open(csv_file, 'w', newline='') as f:
        writer = csv.writer(f)
        writer.writerows(rows)



async def clone_if_not_exists(row):
    url, destination_path = row[1] , "./tmp_github/" + row[0]
    if os.path.exists(destination_path):
        # print(destination_path, " exist")
        pass
    else:
        try:
            Repo.clone_from(url, destination_path)
            print(f"Clone successful: {url}")
        except Exception as e:
            print(f"Clone failed: {url}")
            print(e)

async def main():
    with open(sys.argv[1], 'r') as file:
        rows = list(csv.reader(file))
        total_rows = len(rows)

    # with tqdm(total=total_rows, ncols=80, desc="Git Cloning") as pbar:
    #     async def update_progress(future):
    #         pbar.update(1)

    tasks = []
    for row in rows:
        task = asyncio.create_task(clone_if_not_exists(row))
        # task.add_done_callback(update_progress)
        tasks.append(task)

    await asyncio.gather(*tasks)



if __name__ == '__main__':
    # txt2csv()
    asyncio.run(main())
    print("clone done.")


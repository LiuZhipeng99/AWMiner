import csv
import os
import shutil

csv_file_path = 'c_repos_star_200_test.csv'  # Define the csv file path here

with open(csv_file_path, 'r') as file:
    reader = csv.reader(file)
    for row in reader:
        repo_name = row[0]
        source_directory_1 = f'ActionableWarning/{repo_name}.json'
        source_directory_2 = f'NonActionableWarning/{repo_name}.json'

        if os.path.exists(source_directory_1):
            shutil.move(source_directory_1, './3/')
        
        if os.path.exists(source_directory_2):
            shutil.move(source_directory_2, './4/')
    print(111)
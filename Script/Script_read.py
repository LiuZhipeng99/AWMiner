import os
import json

def read_json_files(folder_path):
    json_data = []
    
    for filename in os.listdir(folder_path):
        if filename.endswith('.json'):
            file_path = os.path.join(folder_path, filename)
            with open(file_path, 'r', encoding='utf-8') as file:
                try:
                    data = json.load(file)
                    json_data.extend(data)
                except json.JSONDecodeError:
                    print(f"Error decoding JSON in file: {file_path}")
    
    return json_data


folder_ = '../GeneratedDataset/V1_TMP/GeneratedDataset-cqu1/'  # 替换为实际的GeneratedDataset文件夹路径



Acw_json_list = read_json_files(folder_ + "/ActionableWarning")
NAcw_json_list = read_json_files(folder_ + "/NonActionableWarning")
print("AW & NAW") # ActionableWarning 目前总数
print( len(Acw_json_list), len(NAcw_json_list)) # ActionableWarning 目前总数

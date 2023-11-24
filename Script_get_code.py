import os
import json

def read_json_files(folder_path):
    json_data = []
    
    for filename in os.listdir(folder_path):
        if filename.endswith('.json'):
            file_path = os.path.join(folder_path, filename)
            with open(file_path, 'r') as file:
                try:
                    data = json.load(file)
                    json_data.extend(data)
                except json.JSONDecodeError:
                    print(f"edu.cqu.zhipeng.parser.Error decoding JSON in file: {file_path}")
    
    return json_data

ACW_folder_path = './GeneratedDataset/ActionableWarning'  # 替换为实际的文件夹路径
NACW_folder_path = './GeneratedDataset/NonActionableWarning'




Acw_json_list = read_json_files(ACW_folder_path)
print(len(Acw_json_list)) # ActionableWarning 目前总数
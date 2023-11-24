import re, time, os
import hashlib

#Procname.hashable_name函数解析过程：先区分语言类型，如ObjC类取第一个:分隔前的，且其Non_verbose打印方式F.fprintf fmt "%s%s%s" (Typ.Name.name osig.class_name) ： osig.method_name
def hashable_name(proc_name):
    # 分语言、再构造带:的字符串包含参数、文件名、方法名等信息 （比如复杂构造模式test::lambda.cpp:10:15::operator()）
    return proc_name.split(':')[0]
def compute_hash(severity, bug_type, proc_name, file, qualifier): # 核心思想就是拼接一个问题的特征表示（目前想到的情景只有同文件下的行数变化，目录变化文件名不变）
    num_regexp = re.compile(r"\(:\)[0-9]+")
    qualifier_regexp = re.compile(r"(line |column |:|parameter |\$)[0-9]+")

    base_filename = os.path.basename(file)
    hashable_procedure_name = hashable_name(proc_name)

    location_independent_proc_name = re.sub(
        num_regexp, "$_", hashable_procedure_name)

    location_independent_qualifier = re.sub(
        qualifier_regexp, "$_", qualifier)
# 以使哈希不变 当在文件中移动源代码时
# (* Removing the line,column, line and column in lambda's name
#          (e.g. test::lambda.cpp:10:15::operator()),
#          and infer temporary variable (e.g., n$67) information from the
#          error message as well as the index of the annonymmous class to make the hash invariant
#          when moving the source code in the file *)
    data = (
        severity,
        bug_type,
        location_independent_proc_name,
        base_filename,
        location_independent_qualifier
    )

    hash_object = hashlib.sha256()
    hash_object.update(str(data).encode('utf-8'))
    hash_value = hash_object.hexdigest()

    return hash_value

import json
json_data = '''
{
    "bug_type": "UNINITIALIZED_VALUE",
    "qualifier": "The value read from bundle_pkt_type was never initialized.",
    "severity": "ERROR",
    "line": 8543,
    "column": 14,
    "procedure": "ngtcp2_conn_write_connection_close",
    "procedure_start_line": 8483,
    "file": "repository/ngtcp2/lib/ngtcp2_conn.c",
    "bug_trace": [
        {
            "level": 0,
            "filename": "repository/ngtcp2/lib/ngtcp2_conn.c",
            "line_number": 8543,
            "column_number": 14,
            "description": ""
        }
    ],
    "key": "ngtcp2_conn.c|ngtcp2_conn_write_connection_close|UNINITIALIZED_VALUE",
    "hash": "707e999c302ff4783ab864b4422b7dfb",
    "bug_type_hum": "Uninitialized Value"
}
'''

# 解析JSON数据
data = json.loads(json_data)

# 提取所需字段
severity = data['severity']
bug_type = data['bug_type']
proc_name = data['procedure']
file = data['file']
qualifier = data['qualifier']

# 调用函数计算哈希值
hash_value = compute_hash(severity, bug_type, proc_name, file, qualifier)

print("Hash Value:", hash_value)
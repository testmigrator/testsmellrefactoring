import re

from testsmell.buildindex.code_repo_v5 import refactoring_test_one_qa
from testsmell.loading.loading import loading_detection_result
from testsmell.loading.process import read_code_from_filepath, remove_prefix_from_path
import os
import json

def read_file_content(file_path):
    try:
        with open(file_path, 'r', encoding='utf-8') as file:
            return file.read()
    except FileNotFoundError:
        return "File not found."
    except IOError:
        return "Error reading file."


def parseRefactorCode(code_string):
    pattern = r"```java(.*?)```"
    matches = re.findall(pattern, code_string, re.DOTALL)

    java_codes = []
    for java_code in matches:
        java_codes.append(java_code)

    refactored_code = "\n".join(java_codes)
    return refactored_code


def process_one_json(json_file_path):
    json_data_list = loading_detection_result(json_file_path)
    filename = os.path.basename(json_file_path).replace(".json", "")
    current_working_directory = os.getcwd()
    refactoring_result_list = []
    index = 0
    test_file_prefix = ""
    test_file_suffix = ""
    for json_data in json_data_list:
        index = index + 1
        test_code = json_data["testCode"]
        module_name = remove_prefix_from_path(json_data["testClassPathName"])
        # test_file_name = read_code_from_filepath(json_data["testFilename"])
        test_file_path = json_data["testFilepath"]
        test_file_name = json_data["testFilename"]
        test_file_prefix = json_data["testFilePrefix"]
        test_file_suffix = json_data["testFileSuffix"]
        split_file_name = json_data["splitFilename"]
        smell_list = json_data["smellList"]

        # smell type
        smell_type_desc_list = []
        smell_refactoring_rule_list = []
        smell_refactoring_checkpoint_list = []
        for smell in smell_list:
            file_path = current_working_directory + "/smelltype" + "/" + smell + ".txt"
            smell_type_desc_list.append(read_file_content(file_path))

            file_path = current_working_directory + "/refactor_rules" + "/" + smell + ".txt"
            smell_refactoring_rule_list.append(read_file_content(file_path))

            file_path = current_working_directory + "/checkpoints" + "/" + smell + ".txt"
            smell_refactoring_checkpoint_list.append(read_file_content(file_path))
        # invoke gpt
        refactoring_result = refactoring_test_one_qa(filename, index, test_code, module_name, smell_list,
                                                     smell_type_desc_list,
                                                     smell_refactoring_rule_list, smell_refactoring_checkpoint_list)

        res_str = refactoring_result.json(ensure_ascii=False)
        response = json.loads(res_str)['content']

        refactoring_result_code = parseRefactorCode(response)

        refactoring_result_list.append(refactoring_result_code)

    refactor_code_str = "\n".join(refactoring_result_list)
    new_test_file_str = "\n".join([test_file_prefix, refactor_code_str, test_file_suffix])

    file_name = filename + ".java"
    with open(file_name, 'w') as file:
        file.write(f"{new_test_file_str}\n")
        file.write("\n")


def run(project_name):
    json_path_prefix = "/TestSmellRefactoring/json/"
    json_path = json_path_prefix + project_name
    json_files = []

    json_files.append(json_path)

    # for root, dirs, files in os.walk(json_path):
    #     for file in files:
    #         if file.endswith('.json'):
    #             file_path = os.path.join(root, file)
    #             json_files.append(file_path)

    for json_file in json_files:
        process_one_json(json_file)
        print(json_file, " process done")


if __name__ == '__main__':
    project_name = "jfreechart-master/AbstractRendererTest.json"
    run(project_name)

import json

def loading_detection_result(json_file_path):
    try:
        with open(json_file_path, 'r', encoding='utf-8') as file:
            data = json.load(file)
            return data
    except FileNotFoundError:
        print(f"file not found : {json_file_path}")
    except json.JSONDecodeError:
        print(f"format error: {json_file_path}")
    except Exception as e:
        print(f"loading error: {e}")


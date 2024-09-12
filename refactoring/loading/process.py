def remove_prefix_from_path(path):
    prefix = "src/main/java"
    if not path:
        return ""
    if prefix in path:
        # Remove the prefix and everything before it
        return path.split(prefix, 1)[1].replace(".java", "")
    else:
        return path.replace(".java", "")

def read_code_from_filepath(java_file_path):
    try:
        with open(java_file_path, 'r', encoding='utf-8') as file:
            java_file_content = file.read()

    except FileNotFoundError:
        print(f"file not found: {java_file_path}")
    except IOError:
        print(f"loading error: {java_file_path}")

    java_file_content = remove_import_statements(java_file_content)
    return java_file_content


def remove_import_statements(content):
    # Split the content into lines
    lines = content.split('\n')
    # Filter out lines that start with 'import'
    filtered_lines = [line for line in lines if not line.strip().startswith('import')]
    # Join the lines back into a single string
    return '\n'.join(filtered_lines)

# complete code
import re
import os

def read_log_file(file_path):
    """
    Reads the hs_err_pid*.log file and extracts the corrupted cache information.

    Args:
        file_path (str): The path to the hs_err_pid*.log file.

    Returns:
        dict: A dictionary containing the corrupted cache information.
    """
    try:
        with open(file_path, 'r') as file:
            content = file.read()
            match = re.search(r'Problematic frame:\s*([^\n]+)', content)
            if match:
                return {
                    'corrupted_cache': match.group(1).split()[1],
                    'file_path': file_path
                }
            else:
                return None
    except FileNotFoundError:
        print(f"Error: The file {file_path} does not exist.")
        return None
    except Exception as e:
        print(f"Error: An error occurred while reading the file {file_path}. {str(e)}")
        return None

def main():
    # Get the list of hs_err_pid*.log files
    log_files = [f for f in os.listdir('.') if f.startswith('hs_err_pid')]

    # Iterate over the log files and extract the corrupted cache information
    corrupted_caches = []
    for log_file in log_files:
        corrupted_cache = read_log_file(log_file)
        if corrupted_cache:
            corrupted_caches.append(corrupted_cache)

    # Print the corrupted cache information
    for corrupted_cache in corrupted_caches:
        print(f"Corrupted cache: {corrupted_cache['corrupted_cache']}")

if __name__ == "__main__":
    main()
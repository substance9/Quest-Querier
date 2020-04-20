#!/usr/bin/python3
import os
import subprocess
import time
import pathlib
from datetime import datetime
from signal import signal, SIGINT
from sys import exit

PROJECT_DIR_PATH = "/home/guoxi/Workspace/quest/querier"

DB_PORT = 10009
PROJECT_NAME_BASE = "quest_"


def call_cmd(cmd_arr):
    print("Calling" + str(cmd_arr))
    subprocess.call(cmd_arr)

# def call_cmd_no_blocking(cmd_arr, output_file):
#     print("Calling" + str(cmd_arr))
#     proc = subprocess.Popen(cmd_arr, stdout=output_file)
#     return proc


def runexp(duration,experiment_id,enc_key,secret,enc_table_name,output_path,query_type,query_start_time,query_end_time,query_device):
    os.chdir(PROJECT_DIR_PATH)
    subprocess.call(["gradle","clean"])
    subprocess.call(["gradle","fatJar"])

    now = datetime.now()
    experiment_id = now.strftime("%Y-%m-%d-%H-%M-%S")

    output_dir = PROJECT_DIR_PATH + "/results/" + "dur_" + str(duration) +  "|expID_" + experiment_id
    pathlib.Path(output_dir).mkdir(parents=True, exist_ok=True)

    db_port = DB_PORT
    call_cmd(["java", "-jar" ,"-Xmx32g", "build/libs/querier-all-0.1.jar", \
                        "-d", str(duration),\
                        "-x", experiment_id,\
                        "-k", enc_key,\
                        "-s", secret,\
                        "-p", str(db_port),\
                        "-n", enc_table_name,\
                        "-o", output_dir,\
                        "-q", str(query_type),\
                        "-b", query_start_time,
                        "-e", query_end_time,
                        "-v", query_device])


# Variables per experiment:
dur = 15 # IMPORTANT PARAMETER: delta duration in minutes
key = "tippersquest"
secret = "questsecret"
exp_id = "test"
enc_t_name = "quest_15_1000000"

# Query parameters
query_type = 1  # 1 for Location Trace, 2 for User Trace, 3 for Social Distance, 4 for Crowd Flow
query_start_time = "2018-10-04 13:01:00.000000"  # Please use the exact format
query_end_time = "2018-10-04 13:25:00.000000"
query_device = "63efb80af376b19ae55cbb45a3051267959aba1c"


out_path = PROJECT_DIR_PATH + "/results"
runexp(duration=dur,experiment_id=exp_id,enc_key=key,secret=secret,enc_table_name=enc_t_name,output_path=out_path,\
        query_type=query_type,query_start_time=query_start_time,query_end_time=query_end_time,query_device=query_device)
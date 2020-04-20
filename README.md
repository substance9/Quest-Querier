## Software Requirements

**Supported OS**: Linux / macOS

**Required Softwares**:

- Bash
- Gradle: (version: 6.0+)
- Java (JDK): (version: 11+)
- Python 3.6.9


## Brief Intro

Querier Module generates encrypted SQL query according to the input plain-text query. It queries DB using generated SQL query and parse the encrypted data to get the result for the query


## Run the experiment

Please follow the following steps to set up the environment and run the experiments:

1. Create the following folder in the project directory (if not exist):
   - `./results/`

2. Initialize Docker volume for persistent DB storage and start PostgreSQL DB container
   - `cd util`
   - `./start_db_container.sh` 

3. Setup correct path variables according to the path where you put the project directory root:
   - in `./run_exp.py`: 
     - Modify `PROJECT_DIR_PATH` variable: the absolute path to the project root folder
     - Modify `DB_PORT` variable: The port that will be used for PostgreSQL DB port

4. Modify the experiment parameters in `./run_exp.py` at the bottom of the file (arguments in `runexp()` function)

5. Run the experiment by executing: `./run_exp.py`



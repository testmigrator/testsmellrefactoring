# Test Smell Refactoring
> This is the basic implementation of our submission in FSE 2025:
- [Description](#Description)
- [Project Structure](#ProjectStructure)
- [Datasets](#Datasets)
- [Usage](#Usage)


## Description
> Test smells, often introduced due to poor design practices and lack of domain knowledge, can degrade
the quality of test code, making it difficult to maintain and update. Manually refactoring of test smells is
time-consuming and error-prone, highlighting the necessity for automated approaches. Current rule-based
refactoring methods often fail when encountering scenarios not covered by predefined rules, demonstrating
poor adaptability. In this paper, we propose a novel approach called UTRefactor, a context-enhanced, LLM-
based framework for automatic test smell refactoring in Java projects. UTRefactor extracts relevant context
from test code and leverages an external knowledge base that includes test smell definitions, descriptions,
and DSL-based refactoring rules. By simulating the manual refactoring process through a chain-of-thought
approach, UTRefactor guides the LLM to eliminate test smells in a step-by-step process, ensuring both
accuracy and consistency throughout the refactoring. Additionally, we implement a checkpoint mechanism to
facilitate comprehensive refactoring, particularly when multiple smells are present. We evaluate UTRefactor
on 879 tests from six open-source Java projects, reducing the number of test smells from 2,375 to 265,
achieving an 89% reduction. UTRefactor outperforms direct LLM-based refactoring methods by 61.82% in
smell elimination and significantly surpasses the performance of a rule-based test smell refactoring tool. Our
results demonstrate the effectiveness of UTRefactor in enhancing test code quality while minimizing manual
involvement.
## ProjectStructure
```
GUIMigrator
├── datasets  - Experimental data.
├── detection  - tsDetector tool.
├── refactoring  - This directory contains the definitions of Java test smells, the refactoring DSL, prompt generation, and the source code for refactoring tests using LLM (Llama3-70B).
│   ├── loading -  Contains utility classes for some common file operations.
│   ├── prompt_generation -  Loads test smells, refactoring rules, generates prompts, and refactors tests.
├── src  - Project source code.
│   ├── entity -  Includes project source code structure, etc.
│   ├── utils - Common utilities for logging, file operations, etc.
│   └── service
│       ├── preprocess - Extracts tests and related contextual information and performs preprocessing. 
│       ├── detection - Detects smells present in the tests. 
│       └── refactor - Partial refactoring process of the tests.
└── resources - Project configuration files.
```

##  Datasets
Complete dataset：please refer to `datasets` directory.
Here you can find all the source code files for the projects mentioned in the paper.

##  Usage
### Step 1: Task Parameter Configuration
Supplement parameter configurations in the `task.properties` file.

You need to locate the `task.properties` file in the `src/main/resources` directory, and then set the projectPath to the root path of the project you wish to refactor.

Execute the `Main` class.

The tool will automatically parse and detect, and preprocess the unit tests from the project directory.

### Step 2: Test Refactoring
Execute the `refactoring_test.py`.

This process will automatically refactor the test code with 'smell'. Note, please change the paths used in the code to the actual paths in your computer environment.



You can find a prompt in the `motivation_example_prompt.txt` file, and use an LLM to quickly see the effect of a test refactoring.

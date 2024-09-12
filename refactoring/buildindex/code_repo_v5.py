import os
from langchain.chains.combine_documents import create_stuff_documents_chain
from langchain.chains.history_aware_retriever import create_history_aware_retriever
from langchain.chains.retrieval import create_retrieval_chain
from langchain_community.document_loaders import DirectoryLoader, TextLoader
from langchain_community.llms.tongyi import Tongyi
from langchain_core.prompts import ChatPromptTemplate
from langchain_text_splitters import RecursiveCharacterTextSplitter, Language
from langchain_chroma import Chroma
from langchain_huggingface import HuggingFaceEmbeddings
# from langchain_community.llms import Tongyi
from langchain_core.prompts import PromptTemplate


from langchain_openai import ChatOpenAI

os.environ["DASHSCOPE_API_KEY"] = "sk-"

REPO_PATH = "/"
JAVA_SRC_PATH = REPO_PATH + "/src/main/java"
SMELL_DOC_PATH = REPO_PATH + "/docs/smells"
RULE_DOC_PATH = REPO_PATH + "/docs/rules"
CHUNK_SIZE = 2000
CHUNK_OVERLAP = 200
# EMBEDDINGS_MODEL = HuggingFaceEmbeddings()
LLM_MODEL = Tongyi(model_name="llama3-70b-instruct", temperature=0)

RETRIEVER_SEARCH_TYPE = "mmr"
RETRIEVER_K = 8

documents_cache = {}
texts_cache = {}
db_cache = None

llm_model = ChatOpenAI(
    api_key=os.getenv("DASHSCOPE_API_KEY"),
    base_url="https://dashscope.aliyuncs.com/compatible-mode/v1",
    model="llama3-70b-instruct"
    )

system_role = """
You are a java testing expert, and now you need to refactor the test code which contains bad test code smells.
"""

def build_prompt_total(test_code, context, smell_list, smell_desc_list, refactoring_dsl,
                       smell_refactoring_checkpoint_list):
    prompt_template = """
    Based on the provided test context information, you are required to refactor the test code. Use a Chain-of-Thought approach to break down the problem, understand the intent of the test, recognize the code smells present in the test code, comprehend the refactoring rules and measures, and refactor the test code accordingly.
    
    Test Code: 
    {test_code}  
    Context of the Test Code:   
    {context}   
    The test has the following code smells:  
    {smell_list}   
    The explanation for each smell is:   
    {smell_desc_list}    
    The corresponding refactoring DSL to eliminate the bad smell is:  
    {refactoring_dsl}   
    
    During the refactoring process, it is necessary to ensure that all code smells are addressed. checkpoints:
    {smell_refactoring_checkpoint_list}    
    
    Instructions**:  
    1. **Understand the test intent**: Make sure you understand the design intent of the test.  
    2. **Understand the code smells**: Make sure you understand the insight of the existing code smells.
    3. **Understand the corresponding refactoring DSL**: Make sure you understand the corresponding refactoring DSL and how the test code should be refactored.
    4. **Refactoring the test code**: For the above analysis, refactor the test and provide the refactored test code. Make sure not to miss any code smell. 
    Provide only the complete refactored code, including the test and any additional variable and method definitions, strictly following the refactoring steps, without any other responses.
    """
    template = PromptTemplate(
        input_variables=["system_role", "test_code", "context", "smell_list", "smell_desc_list", "refactoring_dsl",
                         "smell_refactoring_checkpoint_list"],
        template=prompt_template
    )

    final_prompt = template.format(
        system_role=system_role,
        test_code=test_code,
        context=context,
        smell_list=smell_list,
        smell_desc_list=smell_desc_list,
        refactoring_dsl=refactoring_dsl,
        smell_refactoring_checkpoint_list=smell_refactoring_checkpoint_list
    )

    return final_prompt


def build_prompt_1(source_code, context):
    prompt_template = """
    {system_role}
    Given the test code and its context, please provide a brief summary of the design intent of the test. 
    Only provide a concise summary, without any additional modifications or suggestions.
    Test Code: 
    {source_code}
    Context of the Test Code: 
    {context}
    Summary:
    """
    template = PromptTemplate(
        input_variables=["system_role", "source_code", "context"],
        template=prompt_template
    )

    final_prompt = template.format(
        system_role=system_role,
        source_code=source_code,
        context=context
    )

    return final_prompt


def build_prompt_2(test_code, intent, smell_list, smell_desc_list):
    prompt_template = """
    Please provide a brief insight of the existing code smells.
    Only provide a concise summary, without any additional modifications or suggestions.
    The test code: 
    {test_code} 
    The design intent is: 
    {intent}
    The test has the following code smells:
    {smell_list}
    The explanation for each smell is:
    {smell_desc_list}
    """
    template = PromptTemplate(
        input_variables=["test_code", "intent", "smell_list", "smell_desc_list"],
        template=prompt_template
    )

    final_prompt = template.format(
        test_code=test_code,
        intent=intent,
        smell_list=smell_list,
        smell_desc_list=smell_desc_list,
    )

    return final_prompt


def build_prompt_3(test_code, smell, refactoring_dsl):
    prompt_template = """
    Please refer to the test code and the corresponding refactoring approach to provide an analysis and conclusion on how the test code should be refactored.
    Only provide a concise summary, without any additional modifications or suggestions, or code examples.
    For the test code and the existing smell,
    {test_code}
    {smell}
    The corresponding refactoring DSL to eliminate the bad smell is:
    {refactoring_dsl}
    """
    template = PromptTemplate(
        input_variables=["test_code", "smell", "refactoring_dsl"],
        template=prompt_template
    )

    final_prompt = template.format(
        test_code=test_code,
        smell=smell,
        refactoring_dsl=refactoring_dsl
    )

    return final_prompt


def build_prompt_4(test_code, refactoring_steps):
    prompt_template = """
    For the above analysis, refactor the test and provide the refactored test code.
    Only provide the refactored test code, strictly following the refactoring steps, without any other responses.
    {test_code} 
    {refactoring_steps}
    """
    template = PromptTemplate(
        input_variables=["test_code", "refactoring_steps"],
        template=prompt_template
    )

    final_prompt = template.format(
        test_code=test_code,
        refactoring_steps=refactoring_steps
    )

    return final_prompt


def load_documents(repo_path, glob_pattern, doc_type):
    if doc_type not in documents_cache:
        loader = DirectoryLoader(
            repo_path,
            glob=glob_pattern,
            loader_cls=TextLoader,
            use_multithreading=True
        )
        documents_cache[doc_type] = loader.load()
    return documents_cache[doc_type]


def split_documents(documents, language):
    if language not in texts_cache:
        splitter = RecursiveCharacterTextSplitter.from_language(
            language=language, chunk_size=CHUNK_SIZE, chunk_overlap=CHUNK_OVERLAP
        )
        texts_cache[language] = splitter.split_documents(documents)
    return texts_cache[language]


# def create_db(texts):
#     global db_cache
#     if db_cache is None:
#         db_cache = Chroma.from_documents(texts, EMBEDDINGS_MODEL)
#     return db_cache


def create_prompt_templates():
    search_prompt = ChatPromptTemplate.from_messages(
        [
            ("placeholder", "{chat_history}"),
            ("user", "{input}"),
            (
                "user",
                "Given the above conversation, generate a search query to look up to get information relevant to the conversation",
            ),
        ]
    )

    answer_prompt = ChatPromptTemplate.from_messages(
        [
            (
                "system",
                "Answer the user's questions based on the below context:\\\\n\\\\n{context}",
            ),
            ("placeholder", "{chat_history}"),
            ("user", "{input}"),
        ]
    )
    return search_prompt, answer_prompt


def create_qa_chain(llm, db, search_prompt, answer_prompt):
    retriever = db.as_retriever(
        search_type=RETRIEVER_SEARCH_TYPE,
        search_kwargs={"k": RETRIEVER_K},
    )
    retriever_chain = create_history_aware_retriever(llm, retriever, search_prompt)
    document_chain = create_stuff_documents_chain(llm, answer_prompt)
    return create_retrieval_chain(retriever_chain, document_chain)


def get_specific_test_documents(test_code_path, smell_type, rule_type):
    test_code_documents = load_documents(test_code_path, "*.java", "test_code")
    test_code_texts = split_documents(test_code_documents, Language.JAVA)

    smell_documents = load_documents(SMELL_DOC_PATH, f"*{smell_type}*.txt", "smells")
    smell_texts = split_documents(smell_documents, Language.JAVA)

    rule_documents = load_documents(RULE_DOC_PATH, f"*{rule_type}*.txt", "rules")
    rule_texts = split_documents(rule_documents, Language.JAVA)

    return test_code_texts + smell_texts + rule_texts


def refactoring_test_qa(task_idx, llm, test_code, context, smell_list, smell_desc_list, refactoring_dsl,
                        smell_refactoring_checkpoint_list):
    question = build_prompt_total(test_code, context, smell_list, smell_desc_list, refactoring_dsl,
                                  smell_refactoring_checkpoint_list)

    file_name = "refactoring_log_prompt_" + str(task_idx) + ".txt"
    with open(file_name, 'w') as file:
        file.write("Prompt:\n")
        file.write(question)
        file.write("\n")

    result = llm.invoke(question)
    return result


def step1_understand_test_context(llm, test_code, context):
    question = build_prompt_1(test_code, context)
    result = llm.invoke(question)
    return result


def step2_understand_test_code_smell(llm, test_code, intent, smell_list, smell_desc_list):
    question = build_prompt_2(test_code, intent, smell_list, smell_desc_list)
    result = llm.invoke(question)
    return result


def step3_understand_test_code_refactoring_rules(llm, test_code, smell, refactoring_dsl):
    question = build_prompt_3(test_code, smell, refactoring_dsl)
    result = llm.invoke(question)
    return result


def step4_refactoring_test_code(llm, test_code, refactoring_steps):
    question = build_prompt_4(test_code, refactoring_steps)
    result = llm.invoke(question)
    return result


# To create a Python function that writes the input parameters and intermediate steps to a text file, we can use the following code:


def refactoring_test_one_qa(filename, task_idx, test_code, context, smell_list, smell_desc_list, refactoring_dsl,
                            smell_refactoring_checkpoint_list):
    file_name = "refactoring_log_" + filename + "_" + str(task_idx) + ".txt"

    # Open the file in write mode
    with open(file_name, 'w') as file:
        # Write the input parameters to the file
        file.write("Input Parameters:\n")
        file.write(f"Test Code: {test_code}\n")
        file.write(f"Context: {context}\n")
        file.write(f"Smell List: {smell_list}\n")
        file.write(f"Smell Description List: {smell_desc_list}\n")
        file.write(f"Refactoring DSL: {refactoring_dsl}\n")
        file.write(f"Checkpoints: {smell_refactoring_checkpoint_list}\n")
        file.write("\n")

        refactored_code = refactoring_test_qa(task_idx, llm_model, test_code, context, smell_list, smell_desc_list,
                                              refactoring_dsl, smell_refactoring_checkpoint_list)
        file.write(f"Refactored Code: {refactored_code}\n")
        file.write("\n")

    # Return the refactored code
    return refactored_code


def refactoring_test_multi_qa(task_idx, test_code, context, smell_list, smell_desc_list, refactoring_dsl):
    file_name = "refactoring_log_" + str(task_idx) + ".txt"

    # Open the file in write mode
    with open(file_name, 'w') as file:
        # Write the input parameters to the file
        file.write("Input Parameters:\n")
        file.write(f"Test Code: {test_code}\n")
        file.write(f"Context: {context}\n")
        file.write(f"Smell List: {smell_list}\n")
        file.write(f"Smell Description List: {smell_desc_list}\n")
        file.write(f"Refactoring DSL: {refactoring_dsl}\n")
        file.write("\n")

        # Step 1: Understand test context
        file.write("Step 1: Understand Test Context\n")
        step1_result = step1_understand_test_context(llm_model, test_code, context)
        file.write(f"Step 1 Result: {step1_result}\n")
        file.write("\n")

        # Step 2: Understand test code smell
        file.write("Step 2: Understand Test Code Smell\n")
        step2_result = step2_understand_test_code_smell(llm_model, test_code, step1_result, smell_list, smell_desc_list)
        file.write(f"Step 2 Result: {step2_result}\n")
        file.write("\n")

        # Step 3: Understand test code refactoring rules
        file.write("Step 3: Understand Test Code Refactoring Rules\n")
        step3_result = step3_understand_test_code_refactoring_rules(llm_model, test_code, step2_result, refactoring_dsl)
        file.write(f"Step 3 Result: {step3_result}\n")
        file.write("\n")

        # Step 4: Refactoring test code
        file.write("Step 4: Refactoring Test Code\n")
        refactored_code = step4_refactoring_test_code(llm_model, test_code, step3_result)
        file.write(f"Refactored Code: {refactored_code}\n")
        file.write("\n")

    # Return the refactored code
    return refactored_code


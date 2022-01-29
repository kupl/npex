# NPEX
NPEX is an automated program repair system for Null Pointer Exceptions (NPE) in Java. The key feature of NPEX is that it automatically inferences repair specification of the buggy program and uses the inferred specification to validate patches. This feature enables NPEX to repair an NPE bug with only the buggy program and the corresponding NPE stack trace. In contrast to NPEX, the state-of-the-art NPE repair techniques requires test case written by developers as additional inputs for patch validation. The key idea of NPEX is to learn a statistical model that predicts how developers would handle NPEs by mining null-handling patterns from existing codebases, and to use a variant of symbolic execution that can infer the repair specification from the buggy program using the model.

# Installation Guides
## Requirements
  * Java 15+
  * Maven 3.6+
  * Ocaml 4.10+
  * Python 3.8+

## Building Instructions
1. Clone the source codes from GitHub and go into the cloned directory:
```
$ git clone https://github.com/kupl/NPEX --recursive && cd ./NPEX
```
Please don't forget the `--recursive` option because we manage our specification inference & validation module
as git submodules. The option will automatically checkouts its latest version.

2. Build NPEX Java modules. We implemented those modules using the maven build system.
Please run the following command:
```
$ mvn install
```

3. Build the specification inference & validation module, which is implemented on top of the [Infer](https://github.com/facebook/infer) framework. 
Please run the following command:
```
$ cd npex-analyzer && ./build-infer.sh java
```



# Getting Started
We explain how use NPEX to fix a given NPE.
Let's consider fixing NPE in [aries-jpa_7712046](benchmarks/Ours/aries-jpa_7712046).
If you type ```mvn test``` at the project, the test will fail with NPE.
To fix this NPE, we need to first prepare inputs of NPEX.

## Step 1. Preparing Inputs
NPEX takes the following three inputs:
* npe.json: Information of the given NPE.
  * filepath, line, npe_class, npe_method of the given NPE
  * deref_field: last field name of null pointer (e.g., "x" for x, "f" for x.f())
* traces.json: NPE Stack trace.
* Buggy program builded by Infer and Spoon.

For example, NPEX takes the following two inputs for aries-jpa_7712046.
```
npe.json = {
    "filepath": "jpa-blueprint/src/main/java/org/apache/aries/jpa/blueprint/impl/AnnotationScanner.java",
    "line": 39,
    "npe_class": "AnnotationScanner",
    "npe_method": "parseClass",
    "deref_field": "cl",
}
```
```
traces.json =
[
    {
        "filepath": "jpa-blueprint/src/main/java/org/apache/aries/jpa/blueprint/impl/AnnotationScanner.java",
        "line": 39,
        "method_name": "parseClass"
    },
    {
        "filepath": "jpa-blueprint/src/main/java/org/apache/aries/jpa/blueprint/impl/AnnotationScanner.java",
        "line": 33,
        "method_name": "getJpaAnnotatedMembers"
    },
    {
        "filepath": "jpa-blueprint/src/test/java/org/apache/aries/jpa/blueprint/impl/AnnotationScannerTest.java",
        "line": 96,
        "method_name": "getFactoryTest"
    }
]
```
For npe.json, we need to manually write the information of NPE. For traces.json, we provide a script for parsing the 
stack trace of NPE. For example, we can get ```traces.json``` by the following stack trace and the command.
```
stack_trace.txt = 
	java.lang.NullPointerException: null
        at org.apache.aries.jpa.blueprint.impl.AnnotationScanner.parseClass(AnnotationScanner.java:39)
        at org.apache.aries.jpa.blueprint.impl.AnnotationScanner.getJpaAnnotatedMembers(AnnotationScanner.java:33)
        at org.apache.aries.jpa.blueprint.impl.AnnotationScannerTest.getFactoryTest(AnnotationScannerTest.java:96)
```
``` npex prepare --parse_trace stack_trace.txt```

Then, we also need to build the buggy program with Infer and Spoon.
```
npex prepare --build
```
Currently, we only support a java program with Maven2 build system.

## Step 2. Fault Localization
Then we perform fault localization by the given ```npe.json``` and the stack trace ```traces.json```.
```
npex run --localize
```
Then, localized NPEs (JSON file with prefix `npe_`) will be generated. 
For the example, the following JSONs are generated:
```
npe_AnnotationScanner.java_30.json
npe_AnnotationScanner.java_32.json 
npe_AnnotationScanner.java_33.json 
npe_AnnotationScanner.java_38.json 
npe_AnnotationScanner.java_39.json
```

## Step 3. Patch Enumeration
Then, we enumerate patches for all the localized NPEs. The following command will takes all `npe*.json` and enumerate patches.
```
npex run --enumerate
```
The generated patches are located at ```patches``` directory.


## Step 4. Instantiate Null Handling Model
To infer a specification, we need a null handling model which is a map from NPE expression to alternative expression. 
```
npex run --predict
```
Then, a null handling model is generated at ```model.json```.
This procedure uses a learned classifier to instantiate a null handling model for a given program.
The learned classifier is located at ```tools/npex/example/classifier-example```

## Step 5. Specification Inference & Patch Validation
The last step of NPEX is to infer a specification and validate patches by the inferred specification.
```
npex run --validate
```
Then the validation result is stored at ```result.json```.
In this example, the results is
```
{
    "number_of_patches": 27,
    "time_to_verify": 172.13991022109985,
    "time_to_inference": 0.6845083236694336,
    "time_to_capture_original": 0.0,
    "time_to_capture_patches": 162.69369459152222,
    "verified_patches": [
        "SkipBreakStrategy_33-32_1"
    ],
    "rejected_patches": [
        "ReplacePointerStrategy_32_0",
        ...
    ]
}
```


     
# License
This project is licensed under the terms of MIT license.

# Issues
If you encounter any issues, please refer to the [Issue page](https://github.com/kupl/npex/issues) as the solutions already exists.  
If you don't get any solutions from the page, please report it as a new issue for public interest.  
The email address you can contact is as follows:  

* **Junhee Lee** : junhee_lee@korea.ac.kr
* **Seongjoon Hong** : seongjoon@korea.ac.kr

We are welcome to any questions or comments for our lovely project! 

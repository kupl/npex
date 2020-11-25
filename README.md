# NPEX (Null Pointer Exception X)

## Intro

NPEX is an open-source patch recommendation system for Java Null Pointer Exceptions (NPE). NPEX helps developers to maintain their projects **NPE-free** by automatically generating **compilable** patches for NPE. Our project is on top of [**Spoon**](https://github.com/INRIA/spoon) framework.

## Build Instruction

NPEX uses [**maven**](https://maven.apache.org) build system. NPEX can be installed by the one-line-command:
```
mvn install
```

## How to use
To use NPEX, please type the following command :
```
java -jar "[NPEX_path] -patch [project] [NPE]"
````

The following table describes the **mandatory** arguments for NPEX:  
Name       | Description
---------- | -----------
NPEX_path  | path to NPEX jar file (executable file)
project    | path to the project directory
NPE        | path to the npe info file in json format

After running the command above, NPEX generates candidate patches in "patches" directory.


## Detail

NPEX requires specific NPE info file in json format. This info file describes the NPE error of the target project.
The following block is the example of NPE info file, and the table explains each argument in detail.

```
{
      "filepath": "./Main.java",
      "line": 39,
      "last_access": "foo", 
      "npe_class": "A"
}
```

Name        | Description
----------- | -----------
filepath    | the relative path to the file where NPE occurs
line        | the exact line number where NPE occurs
last_access | the last access expression of the null pointer. It can be a variable, a name of field, or a name of method.<br> For example, when NPE occurs at x.foo().g, the last_access is foo.
npe_class   | the class where NPE occurs, discard the package-path.<br>For example, if the exact class org.apache.A, only leaves the name of class, A.

     
## License
This project is licensed under the MIT license.

## Issues
If you find any issues, please refer to the [Issue page](https://github.com/kupl/npex/issues) as the solutions already exists.  
If you don't get any solutions from the page, please report it as a new issue.  
The email address you can contact is as follows:  

* **Junhee Lee** : junhee_lee@korea.ac.kr
* **Seongjoon Hong** : seongjoon_hong@korea.ac.kr

We are welcome to any questions or comments for our lovely project! 

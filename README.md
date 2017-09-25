# cs753

Project Repository for Team 7

We use Gradle as a build system to build a particular assignment first make sure the `mainClassName` in `build.gradle` points to the correct `Main.kt` file you want to run. 

Gradle requires you to have Java our project requires you to have Java 1.8 installed. 

On a Linux system use the provided `gradlew` shell script if on Windows use the `gradlew.bat` batch file. Below we assume a Linux system, but replacing the shell script for the batch file will work on Windows. Initialize the Gradle environment by running ```./gradlew``` or ```./gradlew init```.

Then simply build the project with ```./gradlew build```

Afterwards you run the project with ```./gradlew run```, please read below to see if an assignment has specific instructions.

# a1

Lucene Index for TREC Complex Answer Retreival

The default main class for this assignment is ```edu.unh.cs.ir.a1.MainKt```

Run this assignment with ```./gradlew run -Parg1="absolute/path/to/data/file"```

For example, if you're in the directory that contains your `resources` folder with your data then run: ```./gradlew run -Parg1="/resources/data/test200/train.test200.cbor.paragraphs"```

# a2

Evaluation Measures and TREC_EVAL

The default main class for this assignment is ```edu.unh.cs.ir.a2.MainKt```

There are two modes you can run our assignment in they are ```-init``` and ```-eval```

The ```init``` mode generates the results files in the parent (```../```) directory that you run the code from, they will be called ```cs753luceneDefault.results``` and ```cs753termFrequency.results```.

Once these files are generated you can then run the ```eval``` mode to run the evaluation measures: RPrecision, MAP, and NDCG@20

For example, to generate the results files:

```./gradlew run -Parg1="-init" -Parg2="/absolute/path/to/train.test200.cbor.paragraph" -Parg3="/absolute/path/to/train.test200.cbor.outlines" ```

To run the evaluations on the default Lucene scoring function:

```./gradlew run -Parg1="-eval" -Parg2="/aboslute/path/to/train.test200.cbor.article.qrels" -Parg3="../cs753luceneDefault.results"```

# a3
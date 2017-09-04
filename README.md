# cs753

Project Repository for Team 7

We use Gradle as a build system to build a particular assignment first make sure the `mainClassName` in `build.gradle` points to the correct `Main.kt` file you want to run. On a Linux system use the provided `gradlew` shell script if on Windows use the `gradlew.bat` batch file. Below we assume a Linux system, but replacing the shell script for the batch file will work on Windows.

Then simply build the project with ```./gradlew build```

Afterwards you run the project with ```./gradlew run```, please read below to if an assignment has a specific instructions.

# a1

Lucene Index for TREC Complex Answer Retreival

The default main class for this assingment is ```edu.unh.cs.ir.a1.MainKt```

Run this assignment with ```./gradlew run -Parg1="path/to/data/file"```

For example: ```./gradlew run -Parg1="/home/user/resources/data/test200/train.test200.cbor.paragraphs"```

# a2

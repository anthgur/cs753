package edu.unh.cs.ir.tools

class Evaluator(qRelDataReader: DataReader, resultsDataReader: DataReader) {

    private val relevantDocuments = qRelDataReader.readQRelFile()
    private val testResults = resultsDataReader.readResultsFile()

    fun calculateRPrecision() {
        relevantDocuments.forEach{ key, value ->
            println("Key $key \n \t\t\t $value")
        }
    }

}
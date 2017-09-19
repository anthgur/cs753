package edu.unh.cs.ir.tools

class Evaluator(qRelDataReader: DataReader, resultsDataReader: DataReader) {

    private val relevantDocuments = qRelDataReader.readQRelFile()
    private val testResults = resultsDataReader.readResultsFile()

    fun calculateRPrecision() : Float {
        val numberOfQueries = testResults.size.toFloat()
        var sumOfPrecisions = 0.toFloat()
        relevantDocuments.forEach { query, relevantDocList ->
            var currentPrecisionSum = 0.toFloat()
            val n = relevantDocList.size
            val resultSet = testResults[query]
            for((docID) in resultSet!!.slice(IntRange(0,n))) {
                if (relevantDocList.contains(qRelDataEntry(docID, true))){
                    currentPrecisionSum +=
                }
            }
        }
        return sumOfPrecisions / numberOfQueries
    }

    fun printData() {
        relevantDocuments.forEach { key, value -> println("Key $key \n \t\t\t $value") }
        testResults.forEach { key, value -> println("Key $key \n \t\t\t $value") }
    }

}
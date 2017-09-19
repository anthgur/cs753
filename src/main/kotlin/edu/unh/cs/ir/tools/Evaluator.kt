package edu.unh.cs.ir.tools

class Evaluator(qRelDataReader: DataReader, resultsDataReader: DataReader) {

    private val relevantDocuments = qRelDataReader.readQRelFile()
    private val testResults = resultsDataReader.readResultsFile()

    fun calculateRPrecision() : Double {
        val numberOfQueries = testResults.size.toDouble()
        var sumOfPrecisions = 0.toDouble()
        relevantDocuments.forEach { query, relevantDocList ->
            var currentPrecisionSum = 0.toDouble()
            var n = relevantDocList.size
            val resultSet = testResults[query]
            if (resultSet != null) {
                if (resultSet.size < n) {
                    n = resultSet.size
                }
                for ((docID) in resultSet.slice(IntRange(0, n-1))) {
                    if (relevantDocList.contains(qRelDataEntry(docID, true))) {
                        currentPrecisionSum += 1.toDouble()
//                        println("match! $currentPrecisionSum for $query")
                    }
                }
                currentPrecisionSum /= relevantDocList.size.toDouble()
//                println("precision for $query is $currentPrecisionSum")
                sumOfPrecisions += currentPrecisionSum
            }
        }
        return sumOfPrecisions / numberOfQueries
    }

    fun printData() {
        relevantDocuments.forEach { key, value -> println("Key $key \n \t\t\t $value") }
        testResults.forEach { key, value -> println("Key $key \n \t\t\t $value") }
    }

}
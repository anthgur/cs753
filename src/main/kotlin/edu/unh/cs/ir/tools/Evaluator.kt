package edu.unh.cs.ir.tools

class Evaluator(qRelDataReader: DataReader, resultsDataReader: DataReader) {

    private val relevantDocuments = qRelDataReader.readQRelFile()
    private val testResults = resultsDataReader.readResultsFile()

    fun calculateRPrecision(): Double {
        val numberOfQueries = testResults.size.toDouble()
        var sumOfPrecisions = 0.0
        relevantDocuments.forEach { query, relevantDocList ->
            var currentPrecisionSum = 0.0
            var n = relevantDocList.size
            val resultSet = testResults[query]
            if (resultSet != null) {
                if (resultSet.size < n) {
                    n = resultSet.size
                }
                for ((docID) in resultSet.slice(IntRange(0, n - 1))) {
                    if (relevantDocList.contains(qRelDataEntry(docID, true))) {
                        currentPrecisionSum += 1.0
                    }
                }
                currentPrecisionSum /= relevantDocList.size.toDouble()
                sumOfPrecisions += currentPrecisionSum
            }
        }
        return sumOfPrecisions / numberOfQueries
    }

    fun calculateMeanAveragePrecision(): Double {
        val numberOfQueries = testResults.size.toDouble()
        var sumOfAveragePrecisions = 0.toDouble()
        relevantDocuments.forEach { query, relevantDocList ->
            var truePositives = 0.0
            var currentAveragePrecision = 0.0
            val resultSet = testResults[query]
            var n = 0
            if (resultSet != null) {
                n = resultSet.size
                for ((docID) in  resultSet.slice(IntRange(0, n - 1))) {
                   if (relevantDocList.contains(qRelDataEntry(docID, true))) {
                       truePositives += 1.0
                   }
                }
                currentAveragePrecision = truePositives / relevantDocList.size.toDouble()
                sumOfAveragePrecisions += currentAveragePrecision
            }

        }
        return sumOfAveragePrecisions / numberOfQueries
    }

    fun printData() {
        relevantDocuments.forEach { key, value -> println("Key $key \n \t\t\t $value") }
        testResults.forEach { key, value -> println("Key $key \n \t\t\t $value") }
    }

}
package edu.unh.cs.ir.tools

class Evaluator(qRelDataReader: DataReader, resultsDataReader: DataReader) {

    private val relevantDocuments = qRelDataReader.readQRelFile()
    private val testResults = resultsDataReader.readResultsFile()

    fun calculateRPrecision(): Double {
        val numberOfQueries = relevantDocuments.size.toDouble()
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
        val numberOfQueries = relevantDocuments.size.toDouble()
        var sumOfAveragePrecisions = 0.toDouble()
        relevantDocuments.forEach { query, relevantDocList ->
            val retrievedDocuments = testResults[query]
            if (retrievedDocuments != null) {
                val averagePrecision = calculateAveragePrecision(relevantDocList, retrievedDocuments)
//                println("averagePrecision of $query is $averagePrecision")
                sumOfAveragePrecisions += averagePrecision
            }
        }
        return sumOfAveragePrecisions / numberOfQueries
    }

    private fun calculateAveragePrecision(relevantDocuments: ArrayList<qRelDataEntry>,
                                          retrievedDocuments: ArrayList<resultsDataEntry>): Double {
        val numberOfRelevantDocuments = relevantDocuments.size
//        println("relevant documents: $numberOfRelevantDocuments")
        var truePositives = 0.0
        var sumOfPrecisionsAtRelevantDocuments = 0.0
        retrievedDocuments.forEachIndexed { r, (docID) ->
            if (relevantDocuments.contains(qRelDataEntry(docID,true))) {
                truePositives += 1.0
                sumOfPrecisionsAtRelevantDocuments += truePositives / (r.toDouble()+1)
//                println("match! precision@${r.toDouble()} is ${truePositives/(r.toDouble()+1)}")
            }
        }
//        println("averagePrecision $sumOfPrecisionsAtRelevantDocuments / ${numberOfRelevantDocuments.toDouble()}")
        return sumOfPrecisionsAtRelevantDocuments / numberOfRelevantDocuments.toDouble()
    }

    fun printData() {
        relevantDocuments.forEach { key, value -> println("Key $key \n \t\t\t $value") }
        testResults.forEach { key, value -> println("Key $key \n \t\t\t $value") }
    }

}
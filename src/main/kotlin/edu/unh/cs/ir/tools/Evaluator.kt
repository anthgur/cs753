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

    fun calculateRPrecisionError(mean: Double): Double {
        val numberOfQueries = relevantDocuments.size.toDouble()
        var varianceSum = 0.0
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
                varianceSum += (currentPrecisionSum - mean) * (currentPrecisionSum - mean)
            }
        }
        return Math.sqrt(varianceSum / (numberOfQueries - 1.0)) * (1.0 / Math.sqrt(numberOfQueries))
    }

    fun calculateMeanAveragePrecision(): Double {
        val numberOfQueries = relevantDocuments.size.toDouble()
        var sumOfAveragePrecisions = 0.0
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

    fun calculateMeanAveragePrecisionError(mean: Double): Double {
        val numberOfQueries = relevantDocuments.size.toDouble()
        var varianceSum = 0.0
        relevantDocuments.forEach { query, relevantDocList ->
            val retriedDocuments = testResults[query]
            if (retriedDocuments != null) {
                val averagePrecision = calculateAveragePrecision(relevantDocList, retriedDocuments)
                varianceSum += (averagePrecision - mean) * (averagePrecision - mean)
            }
        }
        return Math.sqrt(varianceSum / (numberOfQueries - 1.0)) * (1.0 / Math.sqrt(numberOfQueries))
    }

    fun calculateNormalizedDiscountCumulativeGain(k: Int = 20): Double {
        val numberOfQueries = relevantDocuments.size.toDouble()
        var sumOfNDCG = 0.0
        relevantDocuments.forEach { query, relevantDocList ->
            val retrievedDocuments = testResults[query]
            if (retrievedDocuments != null) {
                val nDCG = calculateDiscountCumulativeGain(k, retrievedDocuments, query)
                val iNDCG = calculateIdealCumulativeGain(k, relevantDocList)
                sumOfNDCG += nDCG / iNDCG
            }
        }
        return sumOfNDCG / numberOfQueries
    }

    fun calculateNormalizedDiscountCumulativeGainError(mean: Double, k: Int = 20): Double {
        val numberOfQueries = relevantDocuments.size.toDouble()
        var varianceSum = 0.0
        relevantDocuments.forEach { query, relevantDocList ->
            val retrievedDocuments = testResults[query]
            if (retrievedDocuments != null) {
                val nDCG = calculateDiscountCumulativeGain(k, retrievedDocuments, query)
                val iNDCG = calculateIdealCumulativeGain(k, relevantDocList)
                varianceSum += ((nDCG / iNDCG) - mean) * ((nDCG / iNDCG) - mean)
            }
        }
        return Math.sqrt(varianceSum / (numberOfQueries - 1.0)) * (1.0 / Math.sqrt(numberOfQueries))
    }

    private fun calculateAveragePrecision(relevantDocs: ArrayList<qRelDataEntry>,
                                          retrievedDocs: ArrayList<resultsDataEntry>): Double {
        val numberOfRelevantDocuments = relevantDocs.size
//        println("relevant documents: $numberOfRelevantDocuments")
        var truePositives = 0.0
        var sumOfPrecisionsAtRelevantDocuments = 0.0
        retrievedDocs.forEachIndexed { r, (docID) ->
            if (relevantDocs.contains(qRelDataEntry(docID, true))) {
                truePositives += 1.0
                sumOfPrecisionsAtRelevantDocuments += truePositives / (r.toDouble() + 1)
//                println("match! precision@${r.toDouble()} is ${truePositives/(r.toDouble()+1)}")
            }
        }
//        println("averagePrecision $sumOfPrecisionsAtRelevantDocuments / ${numberOfRelevantDocuments.toDouble()}")
        return sumOfPrecisionsAtRelevantDocuments / numberOfRelevantDocuments.toDouble()
    }

    private fun calculateIdealCumulativeGain(k: Int = 20, relevantDocs: ArrayList<qRelDataEntry>): Double {
        var sumOfDiscountCumulativeGain = 0.0
        var currentAdditionsToTheSum = 0
        relevantDocs.forEachIndexed { m, _ ->
            if (currentAdditionsToTheSum < k) {
                sumOfDiscountCumulativeGain += (Math.pow(2.0, 1.0) - 1.0) / (Math.log(1.0 + m + 1) / Math.log(2.0))
            }
            currentAdditionsToTheSum++
        }
        return sumOfDiscountCumulativeGain
    }

    private fun calculateDiscountCumulativeGain(k: Int = 20, retrievedDocuments: ArrayList<resultsDataEntry>,
                                                query: String): Double {
        var sumOfDiscountCumulativeGain = 0.0
        var currentAdditionsToTheSum = 0
        val relevantDocs = relevantDocuments[query]
        retrievedDocuments.forEachIndexed { m, (docID) ->
            if (relevantDocs != null) {
                if (relevantDocs.contains(qRelDataEntry(docID, true)) && currentAdditionsToTheSum < k) {
                    sumOfDiscountCumulativeGain += (Math.pow(2.0, 1.0) - 1.0) / (Math.log(1.0 + m + 1) / Math.log(2.0))
                }
            }
            currentAdditionsToTheSum++
        }
        return sumOfDiscountCumulativeGain
    }

    fun printData() {
        relevantDocuments.forEach { key, value -> println("Key $key \n \t\t\t $value") }
        testResults.forEach { key, value -> println("Key $key \n \t\t\t $value") }
    }

}
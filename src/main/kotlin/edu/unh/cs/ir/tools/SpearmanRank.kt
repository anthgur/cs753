package edu.unh.cs.ir.tools

class SpearmanRank(resultsDataReader: DataReader, luceneDataReader: DataReader) {

    private val testResults = resultsDataReader.readResultsFile()
    private val luceneResults = luceneDataReader.readResultsFile()

    fun calculateRank(): Double {
        val numberOfQueries = luceneResults.size
        var sumOfSpearmanRanks = 0.0
        luceneResults.forEach { query, luceneDocList ->
            val testDocList = testResults[query]
            if (testDocList != null) {
                sumOfSpearmanRanks += calculateSpearmanRank(testDocList, luceneDocList)
            }
        }
//        println("rank: $sumOfSpearmanRanks / $numberOfQueries")
        return 1 - (sumOfSpearmanRanks / numberOfQueries)
    }

    private fun calculateSpearmanRank(testDocList: ArrayList<resultsDataEntry>,
                                      luceneDocList: ArrayList<resultsDataEntry>): Double {
        var sumOfDistances = 0.0
        luceneDocList.forEachIndexed { index, resultsDataEntry ->
            sumOfDistances += calculateDistance(index.toDouble(), resultsDataEntry, testDocList)
        }
        val n = Math.max(testDocList.size, luceneDocList.size)
//        println("spearman: 6 * $sumOfDistances / ($n * (($n * $n) - 1))")
        return if (n > 1) {
            (6.0 * sumOfDistances) / (n * ((n * n) - 1.0))
        } else {
            0.0
        }
    }

    private fun calculateDistance(leftHandSide: Double, resultsDataEntry: resultsDataEntry,
                                  testDocList: ArrayList<resultsDataEntry>): Double {
        var rankRightHandSide = 0.0
        testDocList.forEach {
            if (it.docID == resultsDataEntry.docID) {
                rankRightHandSide = it.rank.toDouble()
            }
        }
//        println("distance: $rankLeftHandSide - $rankRightHandSide")
        return Math.max(leftHandSide - rankRightHandSide, rankRightHandSide - leftHandSide)
    }


}
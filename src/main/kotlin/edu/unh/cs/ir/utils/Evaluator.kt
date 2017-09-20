package edu.unh.cs.ir.utils

class Evaluator(qrels: Reader, results: Reader){

    val relMap = qrels.readQRelFile()
    val resMap = results.readResultsFile()

    fun PrecisionAtR() : Double
    {
        var totalSum = 0.0
        relMap.forEach { query, docList ->
            var n = docList.size
            val R = n.toDouble()
            val resList = resMap[query]
            if (resList != null)
            {
                var localSum = 0.0
                if (n > resList.size)
                    n = resList.size

                for (i in IntRange(0, n - 1)) {
                    if (docList.contains(qRelDataEntry(resList[i].docID, true)))
                        localSum += 1.0
                }

                totalSum += (localSum / R)
            }
        }
        return totalSum / relMap.size.toDouble()
    }

    fun MAP()
    {

    }

    fun iDCG()
    {

    }

    fun DCG()
    {

    }

    fun NDCG20()
    {

    }
}
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

    fun MAP() : Double
    {
        var AP = 0.0
        relMap.forEach { query, docList ->
            val R = docList.size.toDouble()
            val resList = resMap[query]
            var localSum = 0.0
            var tp = 0.0
            if (resList != null)
            {
                resList.forEach {
                    if (docList.contains(qRelDataEntry(it.docID, true)))
                    {
                        tp += 1.0
                        localSum += (tp / (it.rank + 1))
                    }
                }
            }
            AP += localSum / R
        }

        return (AP / relMap.size.toDouble())
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
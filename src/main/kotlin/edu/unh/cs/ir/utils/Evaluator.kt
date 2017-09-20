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

    fun iDCG(query: String) : Double
    {
        var totalSum = 0.0
        var k = 20
        val relDocs = relMap[query]
        if (relDocs != null) {
            if (k > relDocs.size)
                k = relDocs.size
            for (i in IntRange(0, k)) {
                totalSum += ((Math.pow(2.0,1.0) - 1) / (Math.log(1.0+i+1.0)))
            }
        }
        return totalSum
    }

    fun DCG(query: String) : Double
    {
        var totalSum = 0.0
        var k = 20
        val resDocs = resMap[query]
        if (resDocs != null) {
            if (k > resDocs.size)
                k = resDocs.size
            for (i in IntRange(0, k - 1)) {
                if (relMap[query]!!.contains(qRelDataEntry(resDocs[i].docID, true)))
                    totalSum += ((Math.pow(2.0,1.0) - 1) / (Math.log(1.0+i+1.0)))
                else
                    totalSum += ((Math.pow(2.0,0.0) - 1) / (Math.log(1.0+i+1.0)))
            }
        }
        return totalSum
    }

    fun NDCG20() : Double
    {
        var totalSum = 0.0
        relMap.forEach { query, docList ->
            totalSum += ((1/iDCG(query))*DCG(query))
        }
        return totalSum / relMap.size.toDouble()
    }
}
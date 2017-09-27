package edu.unh.cs.ir.tools

import org.apache.lucene.search.similarities.BasicStats
import org.apache.lucene.search.similarities.SimilarityBase

class LNCLTN : SimilarityBase() {
    var currentDoc = 0
    var maxDocId = 0
    var documentVector = ArrayList<ArrayList<Double>>()
    var queryVector = ArrayList<Double>()

    fun calculateQueryVector(invertedIndex: InvertedIndex, queryTokens : ArrayList<String>)
    {
        invertedIndex.forEach { term, _ ->
            if (queryTokens.contains(term))
            {
                val tf = invertedIndex.calculateQueryFrequency(queryTokens, term)
                val N = invertedIndex.getNumberOfDocs().toDouble()
                val df = invertedIndex.getDocFrequency(term)

                var l = 0.0
                if (tf > 0)
                    l = 1 + Math.log10(tf)

                var t = 0.0
                if ((N/df) > 0)
                    t = Math.log10(N/df)

                queryVector.add(l * t)
            }
            else
                queryVector.add(0.0)
        }
    }

    fun calculateDocumentVector(invertedIndex: InvertedIndex, docId : Int)
    {
        documentVector.add(ArrayList<Double>())
        invertedIndex.forEach { term, docList ->
            val tf = invertedIndex.getTermFrequency(docId, term).toDouble()

            var l = 0.0
            if (tf > 0)
                l = 1 + Math.log10(tf)

            documentVector[docId].add(l)
        }
    }

    fun normalizeVectorC(vector: ArrayList<Double>) : ArrayList<Double>
    {
        var normalVector = ArrayList<Double>()
        var sumOfSqs = 0.0
        vector.forEach {
            sumOfSqs += it * it
        }

        val c = 1.0 / Math.sqrt(sumOfSqs)

        vector.forEach {
            normalVector.add( it * c )
        }

        return normalVector
    }

    override fun score(stats: BasicStats?, freq: Float, docLen: Float): Float {
        var dot = 0.0
        if (currentDoc < maxDocId) {
            val normalDocVector = normalizeVectorC(documentVector[currentDoc])
            assert(queryVector.size == normalDocVector.size)
            normalDocVector.forEachIndexed { index, value ->
                dot += value + queryVector[index]
            }

            currentDoc++
        }
        return dot.toFloat()
    }

    override fun toString(): String {
        return "LNC.LTN"
    }
}
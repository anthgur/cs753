package edu.unh.cs.ir.tools

import org.apache.lucene.search.similarities.BasicStats
import org.apache.lucene.search.similarities.SimilarityBase

class BNNBNN: SimilarityBase() {
    var currentDoc = 0
    var maxDocId = 0
    var documentVector = ArrayList<ArrayList<Double>>()
    var queryVector = ArrayList<Double>()

    fun calculateQueryVector(invertedIndex: InvertedIndex, queryTokens : ArrayList<String>)
    {
        invertedIndex.forEach { term, _ ->
            if (queryTokens.contains(term))
            {
                queryVector.add(1.0)
            }
            else
                queryVector.add(0.0)
        }
    }

    fun calculateDocumentVector(invertedIndex: InvertedIndex, docId : Int)
    {
        documentVector.add(ArrayList<Double>())
        invertedIndex.forEach { term, docList ->
            val tf = invertedIndex.getTermFrequency(docId, term)
            if (tf >= 1) {
                documentVector[docId].add(1.0)
            }
            else
                documentVector[docId].add(0.0)
        }
    }

    override fun score(stats: BasicStats?, freq: Float, docLen: Float): Float {
        var dot = 0.0
        if (currentDoc < maxDocId) {
            assert(queryVector.size == documentVector[currentDoc].size)
            documentVector[currentDoc].forEachIndexed { index, value ->
                dot += (value + queryVector[index])
            }
            currentDoc++
        }
        return dot.toFloat()
    }

    override fun toString(): String {
        return "BNN.BNN"
    }
}
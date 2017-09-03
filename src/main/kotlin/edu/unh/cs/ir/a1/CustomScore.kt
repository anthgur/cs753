package edu.unh.cs.ir.a1

import org.apache.lucene.search.similarities.BasicStats
import org.apache.lucene.search.similarities.SimilarityBase

class CustomScore : SimilarityBase
{
    constructor() : super()

    override fun toString() : String
    {
        return "scoring from class"
    }

    override fun score(stats: BasicStats?, freq: Float, docLen: Float): Float
    {
        return stats!!.totalTermFreq.toFloat()
    }
}
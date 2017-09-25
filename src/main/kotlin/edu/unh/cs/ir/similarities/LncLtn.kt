package edu.unh.cs.ir.similarities

import org.apache.lucene.search.similarities.BasicStats
import org.apache.lucene.search.similarities.SimilarityBase

class Lnc : SimilarityBase() {
    override fun score(stats: BasicStats?, freq: Float, docLen: Float): Float {
        val lValue = (1.0 + log2((stats!!.totalTermFreq).toDouble())).toFloat()
        val dValue = 1f
        val nValue = stats.valueForNormalization
        return (lValue * dValue) / (nValue)
    }

    override fun toString() = "TF: (1 + log(tf)) | IDF: 1.0 | Normalize: Cosine"
}

class Ltn : SimilarityBase() {
    override fun score(stats: BasicStats?, freq: Float, docLen: Float): Float {
        val lValue = (1.0 + log2((stats!!.totalTermFreq).toDouble())).toFloat()
        val dValue = (log2((stats.numberOfDocuments/stats.docFreq).toDouble())).toFloat()
        val nValue = 1f
        return (lValue * dValue) / (nValue)
    }

    override fun toString() = "TF: (1 + log(tf)) | IDF: log(N/df) | Normalize: 1.0"
}


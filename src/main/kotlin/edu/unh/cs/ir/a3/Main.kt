package edu.unh.cs.ir.a3

import org.apache.lucene.search.similarities.BasicStats
import org.apache.lucene.search.similarities.SimilarityBase

class LncLtn : SimilarityBase() {
    override fun toString(): String {
        return "TF-IDF with lcn.ltn scoring."
    }

    override fun score(stats: BasicStats?, freq: Float, docLen: Float): Float {
        fun l(freq: Float): Float {
            return (1 + log2(freq.toDouble())).toFloat()
        }

        return l(freq)
    }
}

fun main(args: Array<String>) {

}
package edu.unh.cs.ir.tools

import edu.unh.cs.ir.data.DocumentFrequency

class InvertedIndex : HashMap<String, ArrayList<DocumentFrequency>>() {

    fun addToIndex(token: String, currentIndexDocID: Int) {
        if (this[token] != null) {
            if (this[token]!![this[token]!!.size - 1].id == currentIndexDocID.toString()) {
                this[token]!![this[token]!!.size - 1].frequency += 1
            } else {
                val newDocFrequency = DocumentFrequency(currentIndexDocID.toString(), 1)
                this[token]?.add(newDocFrequency)
            }
        } else if (this[token] == null) {
            val newDocFrequencyList = ArrayList<DocumentFrequency>()
            val newDocFrequency = DocumentFrequency(currentIndexDocID.toString(), 1)
            newDocFrequencyList.add(newDocFrequency)
            this[token] = newDocFrequencyList
        }
    }

    private fun getMaxTermFrequency(list: ArrayList<DocumentFrequency>): Double {
        var maxTermFrequency = 0.0
        list.forEach { (_, freq) ->
            if (maxTermFrequency < freq) {
                maxTermFrequency = freq.toDouble()
            }
        }
        return maxTermFrequency
    }

    fun generateDocumentVector(type: TFIDF_DOC_TYPE, docID: Int): ArrayList<Double> {
        val documentVector = ArrayList<Double>()
        this.forEach { term, docList ->
            when (type) {
                TFIDF_DOC_TYPE.LNC -> {
                    val lValue = doLogarithm(getTermFrequency(docID, term).toDouble())
                    val nValue = 1.0
                    documentVector.add(lValue * nValue)
                }
                TFIDF_DOC_TYPE.BNN -> {
                    val bValue = doBoolean(getTermFrequency(docID, term).toDouble())
                    val nValue = 1.0
                    documentVector.add(bValue * nValue)
                }
                TFIDF_DOC_TYPE.ANC -> {
                    val aValue = doAugmented(getTermFrequency(docID, term).toDouble(), getMaxTermFrequency(docList))
                    val nValue = 1.0
                    documentVector.add(aValue * nValue)
                }
            }
        }
        return documentVector
    }

//    fun generate

    fun normalizeVector(vector: ArrayList<Double>, type: TFIDF_DOC_TYPE): ArrayList<Double> {
        val normalizedVector = ArrayList<Double>()
        val normalizationFactor = doCosine(vector)
        when (type) {
            TFIDF_DOC_TYPE.LNC -> {
                vector.forEachIndexed { _, entry ->
                    normalizedVector.add(entry * normalizationFactor)
                }
            }
            TFIDF_DOC_TYPE.BNN -> {
                return vector
            }
            TFIDF_DOC_TYPE.ANC -> {
                vector.forEachIndexed { _, entry ->
                    normalizedVector.add(entry * normalizationFactor)
                }
            }
        }
        return normalizedVector
    }

    fun getNumberOfDocs() = this.size

    fun getDocFrequency(token: String) = this[token]?.size ?: 0

    fun getTermFrequency(docID: Int, token: String): Int {
        this[token]?.forEach { (id, freq) ->
            if (docID.toString() == id) {
                return freq
            }
        }
        return 0
    }

    fun printIndexWithLargeList(size: Int) {
        this.forEach { token, list ->
            if (list.size >= size) {
                print("$token : ")
                list.forEach { (id, freq) ->
                    print("($id|$freq)->")
                }
                print("END")
                println()
            }
        }
    }

    fun printEntireIndex() {
        this.forEach { token, list ->
            print("$token : ")
            list.forEach { (id, freq) ->
                print("($id|$freq)->")
            }
            print("END")
            println()
        }
    }

    fun printIndexList(listToDisplay: String) {
        this.forEach { token, list ->
            if (token == listToDisplay) {
                print("$token : ")
                list.forEach { (id, freq) ->
                    print("($id|$freq)->")
                }
                print("END")
                println()
            }
        }
    }

    // Document TF-IDF Functions
    fun doLogarithm(termFrequency: Double) = 1.0 + Math.log(termFrequency)

    fun doBoolean(termFrequency: Double) = if (termFrequency > 0) 1 else 0
    fun doAugmented(termFrequency: Double, maxTermFrequency: Double) = 0.5 + ((0.5 * termFrequency) / maxTermFrequency)

    fun doInvTerm(documentFrequency: Double) = Math.log(getNumberOfDocs() / documentFrequency)
    fun doProbInvTerm(documentFrequency: Double): Double {
        return if (Math.log((getNumberOfDocs() - documentFrequency) / documentFrequency) <= 0) 0.0
        else Math.log((getNumberOfDocs() - documentFrequency) / documentFrequency)
    }

    fun doCosine(vector: ArrayList<Double>): Double {
        var sumOfSquares = 0.0
        vector.forEach { entry -> sumOfSquares += (entry * entry) }
        return 1.0 / (Math.sqrt(sumOfSquares))
    }


}

enum class TFIDF_DOC_TYPE {
    LNC, BNN, ANC
}

enum class TFIDF_QUERY_TYPE {
    LTN, BNN, APC
}
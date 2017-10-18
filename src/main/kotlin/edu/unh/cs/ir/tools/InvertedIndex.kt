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
                    val aValue = doAugmented(getTermFrequency(docID, term).toDouble(),
                            getMaxTermFrequency(docList))
                    val nValue = 1.0
                    documentVector.add(aValue * nValue)
                }
            }
        }
        return documentVector
    }

    fun generateQueryVector(tokenizedQuery: ArrayList<String>, type: TFIDF_QUERY_TYPE): ArrayList<Double> {
        val queryVector = ArrayList<Double>()
        this.forEach { term, _ ->
            if (tokenizedQuery.contains(term)) {
                when (type) {
                    TFIDF_QUERY_TYPE.LTN -> {
                        val lValue = doQueryLogarithm(tokenizedQuery, term)
                        val tValue = doInvTerm(getDocFrequency(term).toDouble())
                        queryVector.add(lValue * tValue)
                    }
                    TFIDF_QUERY_TYPE.BNN -> {
                        val bValue = doQueryBoolean(tokenizedQuery, term)
                        val nValue = 1.0
                        queryVector.add(bValue * nValue)
                    }
                    TFIDF_QUERY_TYPE.APC -> {
                        val aValue = doQueryAugmented(tokenizedQuery, term)
                        val pValue = doProbInvTerm(getDocFrequency(term).toDouble())
                        queryVector.add(aValue * pValue)
                    }
                }
            } else {
                queryVector.add(0.0)
            }
        }


        return queryVector
    }

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

    private fun getNumberOfDocs() = this.size

    private fun getDocFrequency(token: String) = this[token]?.size ?: 0

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
    private fun doLogarithm(termFrequency: Double): Double {
        return if (termFrequency > 0) {
            1.0 + Math.log(termFrequency)
        } else {
            0.0
        }
    }

    private fun doBoolean(termFrequency: Double) = if (termFrequency > 0) 1 else 0

    private fun doAugmented(termFrequency: Double, maxTermFrequency: Double): Double {
        return if (termFrequency > 0 && maxTermFrequency > 0) {
            0.5 + ((0.5 * termFrequency) / maxTermFrequency)
        } else {
            0.0
        }
    }

    private fun doInvTerm(documentFrequency: Double): Double {
        return if (documentFrequency > 0) {
            Math.log(getNumberOfDocs() / documentFrequency)
        } else {
            0.0
        }
    }

    private fun doProbInvTerm(documentFrequency: Double): Double {
        return if (documentFrequency > 0) {
            if (Math.log((getNumberOfDocs() - documentFrequency) / documentFrequency) <= 0) 0.0
            else Math.log((getNumberOfDocs() - documentFrequency) / documentFrequency)
        } else {
            0.0
        }
    }

    private fun doCosine(vector: ArrayList<Double>): Double {
        var sumOfSquares = 0.0
        vector.forEach { entry -> sumOfSquares += (entry * entry) }
        return 1.0 / (Math.sqrt(sumOfSquares))
    }

    // Query TF-IDF Functions
    private fun calculateQueryFrequency(tokenizedQuery: ArrayList<String>, queryTerm: String): Double {
        var frequency = 0.0
        tokenizedQuery.forEach { term ->
            if (term == queryTerm) {
                frequency += 1.0
            }
        }
        return frequency
    }

    private fun doQueryLogarithm(tokenizedQuery: ArrayList<String>, queryTerm: String): Double {
        val termFrequency = calculateQueryFrequency(tokenizedQuery, queryTerm)
        return if (termFrequency > 0) {
            1.0 + Math.log(termFrequency)
        } else {
            0.0
        }
    }

    private fun doQueryBoolean(tokenizedQuery: ArrayList<String>, queryTerm: String): Double {
        val termFrequency = calculateQueryFrequency(tokenizedQuery, queryTerm)
        return if (termFrequency > 0) {
            1.0
        } else {
            0.0
        }
    }

    private fun doQueryAugmented(tokenizedQuery: ArrayList<String>, queryTerm: String): Double {
        val termFrequency = calculateQueryFrequency(tokenizedQuery, queryTerm)
        return if (termFrequency > 0) {
            0.5 + ((0.5 * termFrequency) / termFrequency)
        } else {
            0.0
        }
    }


}

enum class TFIDF_DOC_TYPE {
    LNC, BNN, ANC
}

enum class TFIDF_QUERY_TYPE {
    LTN, BNN, APC
}
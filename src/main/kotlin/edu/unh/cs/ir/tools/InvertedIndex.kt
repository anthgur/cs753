package edu.unh.cs.ir.tools

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

    fun calculateQueryFrequency(tokenizedQuery: ArrayList<String>, queryTerm: String): Double {
        var frequency = 0.0
        tokenizedQuery.forEach { term ->
            if (term == queryTerm) {
                frequency += 1.0
            }
        }
        return frequency
    }

    fun getTermFrequency(docID: Int, token: String): Int {
        this[token]?.forEach { (id, freq) ->
            if (docID.toString() == id) {
                return freq
            }
        }
        return 0
    }

    fun getNumberOfDocs() = this.size

    fun getDocFrequency(token: String) = this[token]?.size ?: 0

    fun getMaxTermFrequency(list: ArrayList<DocumentFrequency>): Double {
        var maxTermFrequency = 0.0
        list.forEach { (_, freq) ->
            if (maxTermFrequency < freq) {
                maxTermFrequency = freq.toDouble()
            }
        }
        return maxTermFrequency
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
}

data class DocumentFrequency(val id: String, var frequency: Int)
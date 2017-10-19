package edu.unh.cs.ir.tools

class BigramIndex : HashMap<Int, HashMap<String, HashMap<String, Int>>>(){
    fun addToIndex(docId: Int, wi: String, wiMinus1: String) {
        if (this[docId] != null) {
            if (this[docId]!![wi] != null) {
                if (this[docId]!![wi]!![wiMinus1] != null)
                    this[docId]!![wi]!![wiMinus1] = this[docId]!![wi]!![wiMinus1]!! + 1
                else
                    this[docId]!![wi]!![wiMinus1] = 1
            } else {
                val newFreqMap = HashMap<String, Int>()
                newFreqMap[wiMinus1] = 1
                this[docId]!![wi] = newFreqMap
            }
        } else if (this[docId] == null) {
            val newBigramMap = HashMap<String, HashMap<String, Int>>()
            newBigramMap[wi] = HashMap<String, Int>()
            newBigramMap[wi]!![wiMinus1] = 1
            this[docId] = newBigramMap
        }
    }

    fun getFrequency(docId: Int, wi: String, wiMinus1: String) : Int{
        if (this[docId] == null)
            return 0
        if (this[docId]!![wi] == null)
            return 0
        if (this[docId]!![wi]!![wiMinus1] == null)
            return 0
        return this[docId]!![wi]!![wiMinus1]!!
    }
}
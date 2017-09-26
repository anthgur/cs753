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
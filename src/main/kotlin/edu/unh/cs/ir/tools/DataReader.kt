package edu.unh.cs.ir.tools

import java.io.FileInputStream
import java.util.*

data class qRelDataEntry(val queryID: String, val docID: String, val isRelevant: Boolean)
data class resultsDataEntry(val queryID: String, val docID: String, val rank: Int, val score: Double, val meta: String)

/**
 * Reads a specific file type, assumes that the file given
 * has the correct format, which is only checked via file extension.
 * @param fileName String the name of the data file to be read
 */
class DataReader(private val fileName: String) {

    private val inputFile = FileInputStream(fileName)

    fun readQRelFile() : ArrayList<qRelDataEntry> {
        if(!fileName.endsWith(".qrels")) {
            throw InputMismatchException("Incorrect file format, expected .qrels!")
        }
        val listOfQRelDataEntries = ArrayList<qRelDataEntry>()
        val qRelFileScanner = Scanner(inputFile)
        while(qRelFileScanner.hasNext()) {
            val queryID = qRelFileScanner.next()
            qRelFileScanner.next() // skip the garbage input "0"
            val docID = qRelFileScanner.next()
            val isRelevant = qRelFileScanner.next()
            val newQRelDataEntry = qRelDataEntry(queryID, docID, isRelevant != "0")
            listOfQRelDataEntries.add(newQRelDataEntry)
        }
       return listOfQRelDataEntries
    }

    fun readResultsFile() : ArrayList<resultsDataEntry> {
        if(!fileName.endsWith(".results")) {
            throw InputMismatchException("Incorrect file format, expected .results!")
        }
        val listOfResultsDataEntries = ArrayList<resultsDataEntry>()
        val resultsScanner = Scanner(inputFile)
        while(resultsScanner.hasNext()){
            val queryID = resultsScanner.next()
            resultsScanner.next() // skip the garbage input "Q0"
            val docID = resultsScanner.next()
            val rank = resultsScanner.nextInt()
            val score = resultsScanner.nextDouble()
            val meta = resultsScanner.next()
            val newResultsDataEntry = resultsDataEntry(queryID, docID, rank, score, meta)
            listOfResultsDataEntries.add(newResultsDataEntry)
        }
        return listOfResultsDataEntries
    }
}
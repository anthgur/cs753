package edu.unh.cs.ir.tools

import java.io.FileInputStream
import java.util.*
import kotlin.collections.HashMap

data class qRelDataEntry(val docID: String, val isRelevant: Boolean)
data class resultsDataEntry(val docID: String, val rank: Int, val score: Double, val meta: String)

/**
 * Reads a specific file type, assumes that the file given
 * has the correct format, which is only checked via file extension.
 * @param fileName String the name of the data file to be read
 */
class DataReader(private val fileName: String) {

    private val inputFile = FileInputStream(fileName)

    /**
     * Only reads .grels files and gives back a map
     * @return HashMap<String, ArrayList<qRelDataEntry>>
     */
    fun readQRelFile(): HashMap<String, ArrayList<qRelDataEntry>> {
        if (!fileName.endsWith(".qrels")) {
            throw InputMismatchException("Incorrect file format, expected .qrels got $fileName!")
        }
        val mapOfRelevantFiles = HashMap<String, ArrayList<qRelDataEntry>>()
        val qRelFileScanner = Scanner(inputFile)
        while (qRelFileScanner.hasNext()) {
            val queryID = qRelFileScanner.next()
            qRelFileScanner.next() // skip the garbage input "0"
            val docID = qRelFileScanner.next()
            val isRelevant = qRelFileScanner.next()
            val newQRelDataEntry = qRelDataEntry(docID, isRelevant != "0")
            if (mapOfRelevantFiles[queryID] != null) {
                mapOfRelevantFiles[queryID]!!.add(newQRelDataEntry)
            } else {
                mapOfRelevantFiles[queryID] = ArrayList()
                mapOfRelevantFiles[queryID]!!.add(newQRelDataEntry)
            }
        }
        return mapOfRelevantFiles
    }

    /**
     * Only reads .results files and gives back a mapping
     * @return HashMap<String, ArrayList<resultsDataEntry>>
     */
    fun readResultsFile(): HashMap<String, ArrayList<resultsDataEntry>> {
        if (!fileName.endsWith(".results")) {
            throw InputMismatchException("Incorrect file format, expected .results got $fileName!")
        }
        val mapOfResultFiles = HashMap<String, ArrayList<resultsDataEntry>>()
        val resultsScanner = Scanner(inputFile)
        while (resultsScanner.hasNext()) {
            val queryID = resultsScanner.next()
            resultsScanner.next() // skip the garbage input "Q0"
            val docID = resultsScanner.next()
            val rank = resultsScanner.nextInt()
            val score = resultsScanner.nextDouble()
            val meta = resultsScanner.next()
            val newResultsDataEntry = resultsDataEntry(docID, rank, score, meta)
            if (mapOfResultFiles[queryID] != null) {
                mapOfResultFiles[queryID]!!.add(newResultsDataEntry)
            } else {
                mapOfResultFiles[queryID] = ArrayList()
                mapOfResultFiles[queryID]!!.add(newResultsDataEntry)
            }
        }
        return mapOfResultFiles
    }
}
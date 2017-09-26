package edu.unh.cs.ir.a3

import edu.unh.cs.ir.data.DocumentFrequency
import edu.unh.cs.ir.similarities.Lnc
import edu.unh.cs.ir.similarities.Ltn
import edu.unh.cs.ir.tools.*
import edu.unh.cs.tools.TokenizerAnalyzer
import edu.unh.cs.treccar.read_data.DeserializeData
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.standard.StandardAnalyzer
import java.io.FileInputStream
import java.io.FileWriter

fun main(args: Array<String>) {
    println("edu.unh.cs.ir.a3 main running...")

    val luceneDefaultResults: FileWriter
    val lncLtnResults: FileWriter
    val resultsFile: String
    val qRelFile: String

    try {
        when {
            args[0] == "-init" -> {
                println("expecting first argument to be paragraph data file path...")
                println("expecting second argument to be outline data file path...")
                println("Initializing the results files from paragraph file ${args[1]} and outline file ${args[2]}")
                println("Saving results at ${System.getProperty("user.dir")}")
                luceneDefaultResults = FileWriter(System.getProperty("user.dir") + "luceneDefault.results")
                lncLtnResults = FileWriter(System.getProperty("user.dir") + "lncLtnResults.results")
                generateResults(luceneDefaultResults, lncLtnResults, args)
            }
            args[0] == "-eval" -> {
                println("expecting first argument to be the qrels file path...")
                println("expecting the second argument to be the results file path...")
                println("Evaluating the results file from ${args[2]} with qrels file ${args[1]}")
                resultsFile = args[1]
                qRelFile = args[2]
                performEvaluation(resultsFile, qRelFile)
            }
            else -> {
                println("expecting first argument to be paragraph data file path...")
                println("expecting second argument to be outline data file path...")
                println("expecting third argument to be hierarchical data file path...")
                TODO("Clarify what needs to be done.")
            }
        }
    } catch (e: NoSuchFieldError) {
        println("Requires all arguments to be used!")
        println("usage:")
        println("-init [paragraphFilePath] [outlinesFilePath] | to generate results")
        println("-eval [qRelFilePath] [resultsFileFromInitPath] to evaluate the results")
        println(e.message)

    }

}

fun generateResults(luceneDefaultResults: FileWriter, lncLtnResults: FileWriter, args: Array<String>) {
    val lncSimilarity = Lnc()
    val ltnSimilarity = Ltn()

    // Create an indexer for Lucene default
    val indexer = Indexer()

    // Create an indexer for LNC documents
    val lncIndexer = Indexer(lncSimilarity)

    // Get paragraphs from the CBOR file
    val paragraphStream = FileInputStream(args[1])

    // Get pages from the CBOR file
    val pageStream = FileInputStream(args[2])

    // Holds our frequencies in a table representation
    val invertedIndex = HashMap<String, ArrayList<DocumentFrequency>>()

    // Standard token and stemming rules
    val analyzer = StandardAnalyzer()

    // Document ID for the current document being indexed
    var currentIndexDocID = 0

    // Add the paragraphs to each index
    DeserializeData.iterableParagraphs(paragraphStream).forEach {
        TokenizerAnalyzer.tokenizeString(analyzer, it.textOnly).forEach { token ->
            if (invertedIndex[token] != null) {
                if (invertedIndex[token]!![invertedIndex[token]!!.size -1].id == currentIndexDocID.toString()) {
                    invertedIndex[token]!![invertedIndex[token]!!.size -1].frequency += 1
                } else {
                    val newDocFrequency = DocumentFrequency(currentIndexDocID.toString(), 1)
                    invertedIndex[token]?.add(newDocFrequency)
                }
            } else if (invertedIndex[token] == null) {
                val newDocFrequencyList = ArrayList<DocumentFrequency>()
                val newDocFrequency = DocumentFrequency(currentIndexDocID.toString(), 1)
                newDocFrequencyList.add(newDocFrequency)
                invertedIndex[token] = newDocFrequencyList
            }
        }
        indexer.indexParagraph(it)
        lncIndexer.indexParagraph(it)
        currentIndexDocID++
    }

    invertedIndex.forEach { token, list ->
        if (token == "puzzle") {
            print("$token : ")
            list.forEach { (id, freq) ->
                print("($id|$freq)->")
            }
            print("END")
            println()
        }
    }

    // Close after we load the entries
    indexer.closeIndex()
    lncIndexer.closeIndex()

    // Create the search engines
    val directory = indexer.indexDir
    val searchEngine = SearchEngine(directory)

    // Create the LTN search engine for queries
    val lncDirectory = lncIndexer.indexDir
    val lncLtnSearchEngine = SearchEngine(lncDirectory, ltnSimilarity)

    // Page title queries
    DeserializeData.iterableAnnotations(pageStream).forEach { page ->
        val pageId = page.pageId.toString()
        searchEngine.performQuery(page.pageName, 100).scoreDocs.forEachIndexed { rank, scoreDoc ->
            val doc = searchEngine.getDoc(scoreDoc.doc)
            val docId = doc?.get(IndexerFields.ID.toString().toLowerCase())
            luceneDefaultResults.write("$pageId\tQ0\t$docId\t$rank\t${scoreDoc.score}\tteam7-luceneDefault\n")
        }
        lncLtnSearchEngine.performQuery(page.pageName, 100).scoreDocs.forEachIndexed { rank, scoreDoc ->
            val doc = lncLtnSearchEngine.getDoc(scoreDoc.doc)
            val docId = doc?.get(IndexerFields.ID.toString().toLowerCase())
            lncLtnResults.write("$pageId\tQ0\t$docId\t$rank\t${scoreDoc.score}\tteam7-lncltn\n")
        }
    }

    searchEngine.close()
    lncLtnSearchEngine.close()

    luceneDefaultResults.close()
    lncLtnResults.close()

}

fun performEvaluation(resultsFile: String, qRelFile: String) {
    val evaluator = Evaluator(DataReader(resultsFile), DataReader(qRelFile))
    val rPrecisionMean = evaluator.calculateRPrecision()
    val rPrecisionError = evaluator.calculateRPrecisionError(rPrecisionMean)
    val mapMean = evaluator.calculateMeanAveragePrecision()
    val mapError = evaluator.calculateMeanAveragePrecisionError(mapMean)
    val nDCGMean = evaluator.calculateNormalizedDiscountCumulativeGain()
    val nDCGError = evaluator.calculateNormalizedDiscountCumulativeGainError(nDCGMean)
    println("RPrecision: $rPrecisionMean Error $rPrecisionError")
    println("MAP: $mapMean Error $mapError")
    println("nDCG: $nDCGMean Error $nDCGError")
}


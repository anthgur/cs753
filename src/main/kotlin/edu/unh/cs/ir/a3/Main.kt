package edu.unh.cs.ir.a3

import tools.TokenAnalyzer
import edu.unh.cs.ir.tools.*
import edu.unh.cs.treccar.read_data.DeserializeData
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.standard.StandardAnalyzer
import java.io.FileInputStream
import java.io.FileWriter

fun main(args: Array<String>) {
    println("edu.unh.cs.ir.a3 main running...")
    // Create an indexer
    val indexer = Indexer()

    // Get paragraphs from the CBOR file
    val paragraphStream = FileInputStream(args[0])
    //val pageStream = FileInputStream( args[2])

    val invertedIndex = InvertedIndex()

    // docId for the current document being indexed
    var docId = 0
    // Add the paragraphs to the index
    DeserializeData.iterableParagraphs(paragraphStream).forEach{
        indexer.indexParagraph(it)
        TokenAnalyzer.tokenizeString(StandardAnalyzer(), it.textOnly).forEach { token ->
            invertedIndex.addToIndex(token, docId)
        }
        docId++
    }
    invertedIndex.printEntireIndex()
    // Close after we load the entries
    indexer.closeIndex()
}


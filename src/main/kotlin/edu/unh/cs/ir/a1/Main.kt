package edu.unh.cs.ir.a1

import edu.unh.cs.treccar.read_data.DeserializeData
import java.io.FileInputStream

fun main(args: Array<String>) {
    println("edu.unh.cs.ir.a1 main running...")

    // Create an indexer
    val indexer = Indexer()

    // Get paragraphs from the cbor file
    val stream = FileInputStream(System.getProperty("user.dir") +
            "/src/main/resources/input/test200/train.test200.cbor.paragraphs")
    val paragraphs = DeserializeData.iterableParagraphs(stream)
    for (paragraph in paragraphs)
    {
        // Index every paragraph into Lucene
        indexer.indexParagraph(paragraph)
    }

    // Close indexer
    indexer.closeIndexer()

    // Create search engine
    val se = SearchEngine()

    println("\nQuery 1: power nap benefits")

    var results = se.performSearch("power nap benefits", 10)
    println("Query 1 Results:")
    for (res in results.scoreDocs)
    {
        val doc = se.getDocument(res.doc)
        println("Document " + res.doc + " content: " + doc.getField("content").stringValue())
    }

    println("\nQuery 2: whale vocalization production of sound")

    results = se.performSearch("whale vocalization production of sound", 10)

    println("Query 2 Results:")
    for (res in results.scoreDocs)
    {
        val doc = se.getDocument(res.doc)
        println("Document " + res.doc + " content: " + doc.getField("content").stringValue())
    }

    println("\nQuery 3: pokemon puzzle league")

    results = se.performSearch("pokemon puzzle league", 10)

    println("Query 3 Results:")
    for (res in results.scoreDocs)
    {
        val doc = se.getDocument(res.doc)
        println("Document " + res.doc + " content: " + doc.getField("content").stringValue())
    }
}


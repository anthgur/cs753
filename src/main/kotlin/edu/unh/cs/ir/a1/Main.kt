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
    val dir = indexer.indexDir
    val se = SearchEngine(dir)

    println("\nQuery 1: power nap benefits")

    // perform query 1 and print results
    var results = se.performSearch("power nap benefits", 10)
    println("Query 1 Results:")
    for (res in results.scoreDocs)
    {
        val doc = se.getDocument(res.doc)
        println("Document " + res.doc + " content: " + doc.getField("content").stringValue())
    }

    println("\nQuery 2: whale vocalization production of sound")

    // perform query 2 and get results
    results = se.performSearch("whale vocalization production of sound", 10)
    println("Query 2 Results:")
    for (res in results.scoreDocs)
    {
        val doc = se.getDocument(res.doc)
        println("Document " + res.doc + " content: " + doc.getField("content").stringValue())
    }

    println("\nQuery 3: pokemon puzzle league")

    // perform query 3 and get results
    results = se.performSearch("pokemon puzzle league", 10)
    println("Query 3 Results:")
    for (res in results.scoreDocs)
    {
        val doc = se.getDocument(res.doc)
        println("Document " + res.doc + " content: " + doc.getField("content").stringValue())
    }

    // The second portion for grad students, implementing scoring function from class
    println("\nQuery 1: power nap benefits")

    // perform query 1 and print results
    results = se.performSearchWithCustomScoring("power nap benefits", 10)
    println("Query 1 Results:")
    for (res in results.scoreDocs)
    {
        val doc = se.getDocument(res.doc)
        println("Document " + res.doc + " content: " + doc.getField("content").stringValue())
    }

    println("\nQuery 2: whale vocalization production of sound")

    // perform query 2 and get results
    results = se.performSearchWithCustomScoring("whale vocalization production of sound", 10)
    println("Query 2 Results:")
    for (res in results.scoreDocs)
    {
        val doc = se.getDocument(res.doc)
        println("Document " + res.doc + " content: " + doc.getField("content").stringValue())
    }

    println("\nQuery 3: pokemon puzzle league")

    // perform query 3 and get results
    results = se.performSearchWithCustomScoring("pokemon puzzle league", 10)
    println("Query 3 Results:")
    for (res in results.scoreDocs)
    {
        val doc = se.getDocument(res.doc)
        println("Document " + res.doc + " content: " + doc.getField("content").stringValue())
    }
}


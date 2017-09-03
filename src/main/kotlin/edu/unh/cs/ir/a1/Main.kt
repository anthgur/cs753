package edu.unh.cs.ir.a1

import edu.unh.cs.treccar.read_data.DeserializeData
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.util.QueryBuilder
import java.io.FileInputStream

fun main(args: Array<String>) {
    println("edu.unh.cs.ir.a1 main running...")

    // Create an indexer
    val indexer = Indexer()

    // Get paragraphs from the CBOR file
    val stream = FileInputStream(System.getProperty("user.dir") +
            "/src/main/resources/input/test200/train.test200.cbor.paragraphs")
    DeserializeData.iterableParagraphs(stream).forEach { paragraph ->
        indexer.indexParagraph(paragraph)
    }

    // Close after we load the entries
    indexer.closeIndex()

    // Create the analyzer for the search engine
    val analyzer = StandardAnalyzer()
    val directory = indexer.indexDir
    val searchEngine = SearchEngine(directory)

    // Make the query build tool
    val parser = QueryBuilder(analyzer)

    val queries = listOf("power nap benefits", "whale vocalization production of sound", "pokemon puzzle league")
    queries.forEach{
        println("\"$it\" search results")
        performQuery(searchEngine, parser, it, 10)
    }
    searchEngine.closeSearchEngine()
}

fun performQuery(searchEngine: SearchEngine, parser: QueryBuilder, query: String, numResults: Int) {
   searchEngine.performQuery(parser.createBooleanQuery(
           IndexerFields.CONTENT.toString().toLowerCase(), query), numResults)
}


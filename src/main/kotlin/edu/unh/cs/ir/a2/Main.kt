package edu.unh.cs.ir.a2

import edu.unh.cs.treccar.read_data.DeserializeData
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.similarities.BasicStats
import org.apache.lucene.search.similarities.SimilarityBase
import org.apache.lucene.util.QueryBuilder
import java.io.FileInputStream
import edu.unh.cs.ir.tools.*
import java.io.FileWriter

fun main(args: Array<String>) {
    println("edu.unh.cs.ir.a2 main running...")
    println("expecting first argument to be paragraph data file path...")

    // Create files to be written to for the search engines
    val luceneDefaultResults = FileWriter(System.getProperty("user.dir") + "luceneDefault.results")
    val termFrequencyResults = FileWriter(System.getProperty("user.dir") + "termFrequency.results")

    println("Saving results at ${System.getProperty("user.dir")}")

    try { println("using Paragraphs: ${args[0]} Pages: ${args[1]}")} catch(e: Exception) {
        System.err.println("Did not give data file path...")
        System.exit(-1)
    }

    class freqSimilarity : SimilarityBase() {
        override fun score(stats: BasicStats?, freq: Float, docLen: Float): Float {
            return stats!!.totalTermFreq.toFloat()
        }

        override fun toString(): String {
            return "Frequency Similarity based on sum #{q_i}"
        }
    }

    // Instance of the term frequency similarity
    val termFrequencySimilarity = freqSimilarity()

    // Create an indexer
    val indexer = Indexer()

    // Create term frequency indexer
    val termFrequencyIndexer = Indexer(termFrequencySimilarity)

    // Get paragraphs from the CBOR file
    val paragraphStream = FileInputStream(args[0])
    val pageStream = FileInputStream( args[1])

    // Add the paragraphs to the index
    DeserializeData.iterableParagraphs(paragraphStream).forEach{
        indexer.indexParagraph(it)
        termFrequencyIndexer.indexParagraph(it)
    }

    // Close after we load the entries
    indexer.closeIndex()
    termFrequencyIndexer.closeIndex()

    // Create the analyzer for the search engine
    val analyzer = StandardAnalyzer()

    // Create the search engine
    val directory = indexer.indexDir
    val searchEngine = SearchEngine(directory)

    // Create the term frequency search engine
    val termFrequencyDirectory = termFrequencyIndexer.indexDir
    val termFrequencySearchEngine = SearchEngine(termFrequencyDirectory, termFrequencySimilarity)

    // Make the query build tool
    val parser = QueryParser(IndexerFields.CONTENT.toString().toLowerCase(), analyzer)

    // Use the pages as the query
    DeserializeData.iterableAnnotations(pageStream).forEachIndexed { query, page ->
        performQuery(searchEngine, parser, page.pageName, 100,
                listOf(page.pageId.toString(), query.toString(), "team7-luceneDefault"), luceneDefaultResults)
        performQuery(termFrequencySearchEngine, parser, page.pageName, 100,
                listOf(page.pageId.toString(), query.toString(), "team7-termFrequency"), termFrequencyResults)
    }

    searchEngine.closeSearchEngine()
    termFrequencySearchEngine.closeSearchEngine()

    termFrequencyResults.close()
    luceneDefaultResults.close()

}

fun performQuery(searchEngine: SearchEngine, parser: QueryBuilder, query: String, numResults: Int,
                 metaData: List<String>, resultsFile: FileWriter) {
    searchEngine.performPageQuery(parser.createBooleanQuery(
            IndexerFields.CONTENT.toString().toLowerCase(), query), numResults, metaData, resultsFile)
}


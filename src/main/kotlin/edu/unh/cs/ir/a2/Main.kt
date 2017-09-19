package edu.unh.cs.ir.a2

import edu.unh.cs.ir.a1.Indexer
import edu.unh.cs.ir.a1.SearchEngine
import edu.unh.cs.ir.a1.IndexerFields
import edu.unh.cs.treccar.read_data.DeserializeData
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.similarities.BasicStats
import org.apache.lucene.search.similarities.SimilarityBase
import org.apache.lucene.util.QueryBuilder
import java.io.FileInputStream

fun main(args: Array<String>) {
    println("edu.unh.cs.ir.a1 main running...")
    println("expecting first argument to be paragraph data file path...")

    try { println("using ${args[0]}")} catch(e: Exception) {
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
    var stream = FileInputStream(args[0])
    // val stream = FileInputStream(System.getProperty("user.dir") +
    // "/src/main/resources/input/test200/train.test200.cbor.paragraphs")

    // Add the paragraphs to the index
    DeserializeData.iterableParagraphs(stream).forEach{
        indexer.indexParagraph(it)
        termFrequencyIndexer.indexParagraph(it)
    }

    // Close after we load the entries
    stream.close()
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

    stream = FileInputStream(args[1])
    DeserializeData.iterAnnotations(stream).forEach {
        // default lucene rank
        performQuery(searchEngine, parser, it.pageName.toString(), it.pageId, 100,
                "team7-LuceneDefault")
        // term freq rank
        performQuery(termFrequencySearchEngine, parser, it.pageName.toString(), it.pageId, 100,
                "team7-TermFrequency")
    }

    searchEngine.closeSearchEngine()
    termFrequencySearchEngine.closeSearchEngine()
}

fun performQuery(searchEngine: SearchEngine, parser: QueryBuilder, query: String, queryId: String, numResults: Int,
                 method: String) {
    searchEngine.performQuery(parser.createBooleanQuery(
            IndexerFields.CONTENT.toString().toLowerCase(), query), numResults, queryId, method)
}


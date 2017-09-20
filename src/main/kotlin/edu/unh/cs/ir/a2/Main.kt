package edu.unh.cs.ir.a2

import edu.unh.cs.ir.utils.*
import edu.unh.cs.treccar.read_data.DeserializeData
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.similarities.BasicStats
import org.apache.lucene.search.similarities.SimilarityBase
import org.apache.lucene.util.QueryBuilder
import java.io.FileInputStream
import java.io.FileWriter

fun main(args: Array<String>) {
    println("edu.unh.cs.ir.a1 main running...")

    try { println("using ${args[1]}")} catch(e: Exception) {
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


    if (args[0] == "--init") {
        // Instance of the term frequency similarity
        val termFrequencySimilarity = freqSimilarity()

        // Create an indexer
        val indexer = Indexer()

        // Create term frequency indexer
        val termFrequencyIndexer = Indexer(termFrequencySimilarity)


        // Get paragraphs from the CBOR file
        var stream = FileInputStream(args[1])
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

        val luceneDefaultResults = FileWriter(System.getProperty("user.dir") + "luceneDefault.results")
        val termFrequencyResults = FileWriter(System.getProperty("user.dir") + "termFrequency.results")
        val pageStream = FileInputStream(args[2])
        // Use the pages as the query
        DeserializeData.iterableAnnotations(pageStream).forEachIndexed { query, page ->
            performQuery(searchEngine, parser, page.pageName, 100,
                    listOf(page.pageId.toString(), query.toString(), "team7-luceneDefault"), luceneDefaultResults)
            performQuery(termFrequencySearchEngine, parser, page.pageName, 100,
                    listOf(page.pageId.toString(), query.toString(), "team7-termFrequency"), termFrequencyResults)
        }

        luceneDefaultResults.close()
        termFrequencyResults.close()

        searchEngine.closeSearchEngine()
        termFrequencySearchEngine.closeSearchEngine()
    }

    if (args[0] == "--eval")
    {
        val qrelReader = Reader(args[1])
        val resReader = Reader(args[2])

        val eval = Evaluator(qrelReader, resReader)

        println(eval.PrecisionAtR())
        println(eval.MAP())
        println(eval.NDCG20())
    }
}

fun performQuery(searchEngine: SearchEngine, parser: QueryBuilder, query: String, numResults: Int,
                 metaData: List<String>, resultsFile: FileWriter) {
    searchEngine.performPageQuery(parser.createBooleanQuery(
            IndexerFields.CONTENT.toString().toLowerCase(), query), numResults, metaData, resultsFile)
}


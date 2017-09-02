package edu.unh.cs.ir.a1

import com.sun.xml.internal.bind.api.impl.NameConverter
import edu.unh.cs.treccar.read_data.DeserializeData
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.util.QueryBuilder
import java.io.FileInputStream

fun main(args: Array<String>) {
    println("edu.unh.cs.ir.a1 main running...")

    // Create an indexer
    val indexer = Indexer()

    // Get paragraphs from the CBOR file
    val stream = FileInputStream(System.getProperty("user.dir") +
            "/src/main/resources/input/test200/train.test200.cbor.paragraphs")
    DeserializeData.iterableParagraphs(stream).forEach{ paragraph ->
        indexer.indexParagraph(paragraph)
    }

    // Create the analyzer for the search engine
    val analyzer = StandardAnalyzer()
    val directory = indexer.indexDir
    val searchEngine = SearchEngine(directory)

    // Make the query build tool
    val parser = QueryBuilder(analyzer)
    searchEngine.performQuery(parser.
            createBooleanQuery(IndexerFields.CONTENT.toString().toLowerCase(),"power nap benefits"))


}


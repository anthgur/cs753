package edu.unh.cs.ir.a1

import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.similarities.SimilarityBase
import org.apache.lucene.store.RAMDirectory
import java.io.FileWriter

class SearchEngine(directory: RAMDirectory, similarity: SimilarityBase? = null) {
    private val curDirectory = directory
    private val directoryReader = DirectoryReader.open(curDirectory)
    private val indexSearcher = IndexSearcher(directoryReader)

    init{
        if(similarity != null) indexSearcher.setSimilarity(similarity)
    }

    fun performQuery(query: Query, numResults: Int) {
        indexSearcher.search(query, numResults).scoreDocs.forEach {
            val doc = indexSearcher.doc(it.doc)
            print("paragraph ID: ${it.doc} - ")
            print("paragraph ID: ${doc.get(IndexerFields.ID.toString().toLowerCase())}")
            println(" - content: ${doc.get(IndexerFields.CONTENT.toString().toLowerCase())}")
        }
    }

    fun performQuery(query: Query, numResults: Int, queryId: String, method: String) {
        val theWritingWriter:FileWriter
        if (method == "team7-LuceneDefault")
            theWritingWriter = FileWriter(System.getProperty("user.dir") + "luceneDefault.result")
        else
            theWritingWriter = FileWriter(System.getProperty("user.dir") + "termFrequency.result")
        var rank = 1
        indexSearcher.search(query, numResults).scoreDocs.forEach {
            val doc = indexSearcher.doc(it.doc)
            theWritingWriter.write(queryId + " Q0 " + doc.get("id").toLowerCase() + " " +
                rank++ + " " + it.score + " " + method + "\n")
        }
        theWritingWriter.close()
    }

    fun closeSearchEngine() {
        directoryReader.close()
        curDirectory.close()
    }
}
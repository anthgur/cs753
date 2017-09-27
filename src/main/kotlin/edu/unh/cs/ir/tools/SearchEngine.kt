package edu.unh.cs.ir.tools

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.TokenStream
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.index.*
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.similarities.SimilarityBase
import org.apache.lucene.store.RAMDirectory
import java.io.FileWriter

class SearchEngine(directory: RAMDirectory, similarity: SimilarityBase? = null) {
    private val curDirectory = directory
    private val directoryReader = DirectoryReader.open(curDirectory)
    private val indexSearcher = IndexSearcher(directoryReader)
    var maxDocId = 0

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

    fun performPageQuery(query: Query, numResults: Int, metaData: List<String>, resultsFile: FileWriter) {
        indexSearcher.search(query, numResults).scoreDocs.forEachIndexed { rank, it ->
            if (it.doc < maxDocId) {
                val doc = indexSearcher.doc(it.doc)
                resultsFile.write("${metaData[0]}\tQ0\t${doc.get(IndexerFields.ID.toString().toLowerCase())}\t$rank\t${it.score}\t${metaData[2]}\n")
            }
        }
    }

    fun closeSearchEngine() {
        directoryReader.close()
        curDirectory.close()
    }
}
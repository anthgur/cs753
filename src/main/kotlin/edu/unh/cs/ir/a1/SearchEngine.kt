package edu.unh.cs.ir.a1

import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.store.RAMDirectory

class SearchEngine(directory: RAMDirectory){

    private val curDirectory = directory
    private val directoryReader = DirectoryReader.open(curDirectory)
    private val indexSearcher = IndexSearcher(directoryReader)

    fun performQuery(query: Query, numResults: Int) {
        println("Searching $query")
        val scoreDocs = indexSearcher.search(query,numResults).scoreDocs.forEach {
            val doc = indexSearcher.doc(it.doc)
            println("Hit! paragraph ID: ${doc.get(IndexerFields.ID.toString().toLowerCase())}")
            println("content: ${doc.get(IndexerFields.CONTENT.toString().toLowerCase())}")
        }
        println(scoreDocs)
    }

    fun closeSearchEngine() {
        directoryReader.close()
        curDirectory.close()
    }
}
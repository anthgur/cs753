package edu.unh.cs.ir.a1

import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.similarities.SimilarityBase
import org.apache.lucene.store.RAMDirectory

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

    fun closeSearchEngine() {
        directoryReader.close()
        curDirectory.close()
    }
}
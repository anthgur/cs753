package edu.unh.cs.ir.tools

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.TopDocs
import org.apache.lucene.search.similarities.Similarity
import org.apache.lucene.store.Directory

interface CustomQueryBuilder {
    fun build(query: String): Query
}

class DefaultQueryBuilder : CustomQueryBuilder {
    private val analyzer = StandardAnalyzer()
    private val parser = QueryParser(IndexerFields.CONTENT.toString().toLowerCase(), analyzer)

    override fun build(query: String): Query {
        return parser.createBooleanQuery(IndexerFields.CONTENT.toString().toLowerCase(), query)
    }
}

class SearchEngine(private val directory: Directory,
                   similarity: Similarity? = null,
                   private val queryBuilder: CustomQueryBuilder = DefaultQueryBuilder()) {

    private val directoryReader = DirectoryReader.open(directory)
    private val indexSearcher = IndexSearcher(directoryReader)

    init {
        if (similarity != null) {
            indexSearcher.setSimilarity(similarity)
        }
    }

    fun performQuery(rawQuery: String, numResults: Int): TopDocs {
        val query = queryBuilder.build(rawQuery)
        return indexSearcher.search(query, numResults)
    }

    fun getDoc(id: Int): Document? {
        return indexSearcher.doc(id)
    }

    fun close() {
        directoryReader.close()
        directory.close()
    }
}

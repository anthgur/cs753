package edu.unh.cs.ir.a1

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.TopDocs
import org.apache.lucene.search.similarities.SimilarityBase
import org.apache.lucene.store.FSDirectory
import java.nio.file.Paths

class SearchEngine()
{
    private val indexSearcher = IndexSearcher(DirectoryReader.open(FSDirectory.open(Paths.get("index-directory"))))
    private val queryParser = QueryParser("content", StandardAnalyzer())

    fun performSearch(queryString: String, n: Int): TopDocs
    {
        val query = queryParser.parse(queryString)
        return indexSearcher.search(query, n)
    }

    fun performSearchWithCustomScoring(queryString: String, n: Int): TopDocs
    {
        // Make a new indexsearcher because we're gonna mess around with its scoring system
        val searcher = IndexSearcher(DirectoryReader.open(FSDirectory.open(Paths.get("index-directory"))))
        val sim = CustomScore()
        searcher.setSimilarity(sim)
        val query = queryParser.parse(queryString)
        return searcher.search(query, n)
    }

    fun getDocument(id: Int): Document
    {
        return indexSearcher.doc(id)
    }
}
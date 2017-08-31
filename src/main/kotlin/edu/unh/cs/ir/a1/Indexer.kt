package edu.unh.cs.ir.a1

import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.index.IndexableField
import org.apache.lucene.store.FSDirectory
import java.io.IOException
import java.nio.file.FileSystems

class Indexer() {

    private val path = FileSystems.getDefault().getPath("index-directory")
    private val indexDir = FSDirectory.open(path)!!
    private val config = IndexWriterConfig(StandardAnalyzer())

    private val indexWriter = IndexWriter(indexDir, config)

    fun getIndexWriter(){
        indexWriter
    }

    fun closeIndexWriter() {
        indexWriter.close()
    }

    fun indexEntry(entry: Any?) {
        println("adding $entry to index!")
        val doc = Document()
        doc.add(StringField("id", "1", Field.Store.YES))
        indexWriter.addDocument(doc)
    }

    fun rebuildIndexes() {
        closeIndexWriter()
    }


}
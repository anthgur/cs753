package edu.unh.cs.ir.a1

import edu.unh.cs.treccar.Data
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.RAMDirectory

class Indexer
{
    val indexDir = RAMDirectory()
    private val config = IndexWriterConfig(StandardAnalyzer())
    private val indexWriter = IndexWriter(indexDir, config)

    fun indexParagraph(paragraph: Data.Paragraph)
    {
        // Create the document for the paragraph object
        val doc = Document()
        doc.add(StringField(IndexerFields.ID.toString().toLowerCase(), paragraph.paraId, Field.Store.YES))
        doc.add(TextField(IndexerFields.CONTENT.toString().toLowerCase(), paragraph.textOnly, Field.Store.YES))
        indexWriter.addDocument(doc)
    }

    fun closeIndex() {
        indexWriter.close()
    }
}

enum class IndexerFields {
    ID, CONTENT
}
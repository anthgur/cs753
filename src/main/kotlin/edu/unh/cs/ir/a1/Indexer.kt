package edu.unh.cs.ir.a1

import edu.unh.cs.treccar.Data
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.StringField
import org.apache.lucene.document.TextField
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.store.FSDirectory
import java.nio.file.Paths

class Indexer()
{
    private val indexDir = FSDirectory.open(Paths.get("index-directory"))
    private val config = IndexWriterConfig(StandardAnalyzer())
    private val indexWriter = IndexWriter(indexDir, config)

    fun indexParagraph(paragraph: Data.Paragraph)
    {
        // Create the document for the paragraph object
        var doc = Document()
        doc.add(StringField("id", paragraph.paraId, Field.Store.YES))
        doc.add(StringField("content", paragraph.textOnly, Field.Store.YES))
        doc.add(TextField("content", paragraph.textOnly, Field.Store.NO))
        indexWriter.addDocument(doc)
    }

    fun closeIndexer()
    {
        if (indexWriter != null)
            indexWriter.close()
    }
}
package edu.unh.cs.ir.a1

import edu.unh.cs.treccar.read_data.DeserializeData
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.document.Document
import org.apache.lucene.document.Field
import org.apache.lucene.document.TextField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.store.RAMDirectory
import org.apache.lucene.util.QueryBuilder
import java.io.File
import java.io.FileInputStream

val ID_FIELD = "id"
val PARAGRAPH_FIELD = "paragraph"

class FileLoader(val fileName: String) {
    fun load(): File {
        val uri = this.javaClass.classLoader.getResource(fileName).toURI()
        return File(uri)
    }
}

fun main(args: Array<String>) {
    val analyzer = StandardAnalyzer()
    val directory = RAMDirectory()
    val config = IndexWriterConfig(analyzer)
    val writer = IndexWriter(directory, config)

    val file = FileLoader("input/test200/train.test200.cbor.paragraphs").load()
    val stream = FileInputStream(file)
    DeserializeData.iterParagraphs(stream).forEach { paragraph ->
        val document = Document()
        document.add(Field(ID_FIELD, paragraph.paraId, TextField.TYPE_STORED))
        document.add(Field(PARAGRAPH_FIELD, paragraph.textOnly, TextField.TYPE_STORED))
        writer.addDocument(document)
    }
    writer.close()

    val reader = DirectoryReader.open(directory)
    val searcher = IndexSearcher(reader)
    val builder = QueryBuilder(analyzer)
    val query = builder.createBooleanQuery(PARAGRAPH_FIELD, "power nap benefits")
    val hits = searcher.search(query, 1000)
    hits.scoreDocs.forEach { hit ->
        val doc = searcher.doc(hit.doc)
        println(doc.get(ID_FIELD) + ": " + doc.get(PARAGRAPH_FIELD))
    }
}

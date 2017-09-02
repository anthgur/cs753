package edu.unh.cs.ir.a1

import edu.unh.cs.treccar.read_data.DeserializeData
import java.io.FileInputStream

fun main(args: Array<String>) {
    println("edu.unh.cs.ir.a1 main running...")

    // Create an indexer
    val indexer = Indexer()

    // Get paragraphs from the cbor file
    val stream = FileInputStream(System.getProperty("user.dir") +
            "/src/main/resources/input/test200/train.test200.cbor.paragraphs")
    val paragraphIter = DeserializeData.iterableParagraphs(stream)
    for (paragraph in paragraphIter)
    {
        // Index every paragraph in Lucene
        indexer.indexParagraph(paragraph)
    }
}


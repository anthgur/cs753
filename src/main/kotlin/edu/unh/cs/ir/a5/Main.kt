package edu.unh.cs.ir.a5

import edu.unh.cs.ir.tools.*
import edu.unh.cs.tools.TokenizerAnalyzer
import edu.unh.cs.treccar.read_data.DeserializeData
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.search.similarities.BasicStats
import org.apache.lucene.search.similarities.SimilarityBase
import java.io.FileInputStream
import java.io.FileWriter

fun main(args: Array<String>) {
    println("edu.unh.cs.ir.a4 main running...")

    val luceneDefaultResults: FileWriter
    val customResults: FileWriter
    val resultsFile: String
    val qRelFile: String

    try {
        when {
            args[0] == "-init" -> {
                println("expecting first argument to be paragraph data file path...")
                println("expecting second argument to be outline data file path...")
                println("expecting third argument to be the type of TF-IDF to run...")
                println("Initializing the results files from paragraph file ${args[1]} and outline file ${args[2]}")
                println("Saving results at ${System.getProperty("user.dir")}")
                println("Running the ${args[3]} model.")
                luceneDefaultResults = FileWriter(System.getProperty("user.dir") + "luceneDefault.results")
                customResults = when {
                    args[3] == "lncltn" -> FileWriter(System.getProperty("user.dir") + "lncLtn.results")
                    args[3] == "bnnbnn" -> FileWriter(System.getProperty("user.dir") + "bnnBnn.results")
                    args[3] == "ancapc" -> FileWriter(System.getProperty("user.dir") + "ancApc.results")
                    args[3] == "ul" -> FileWriter(System.getProperty("user.dir") + "ul.results")
                    args[3] == "ujm" -> FileWriter(System.getProperty("user.dir") + "ujm.results")
                    args[3] == "uds" -> FileWriter(System.getProperty("user.dir") + "uds.results")
                    args[3] == "bl" -> FileWriter(System.getProperty("user.dir") + "bl.results")
                    else -> FileWriter(System.getProperty("user.dir") + "custom.results")
                }
                generateResults(luceneDefaultResults, customResults, args)
            }
            args[0] == "-eval" -> {
                println("expecting first argument to be the qrels file path...")
                println("expecting the second argument to be the results file path...")
                println("Evaluating the results file from ${args[2]} with qrels file ${args[1]}")
                resultsFile = args[1]
                qRelFile = args[2]
                performEvaluation(resultsFile, qRelFile)
            }
            args[0] == "-rank" -> {
                println("expecting first argument to be the non-Lucene results file")
                println("expecting second argument to be the Lucene results file")
                println("Calculating rank of ${args[1]} and Lucene file ${args[2]}")
                performRankEvaluation(args[1], args[2])
            }
            args[0] == "-learnAllRankings" -> {
                println("expecting first argument to be the qrels file path...")
                println("expecting the second argument to be the results file path...")
                generateResults(args)
            }
            args[0] == "-learnSingleQuery" -> {
                println("expecting first argument to be query")
                println("expecting second argument to be the qrels file path...")
                generateResults(args[1], args[2])
            }
            else -> {
                println("expecting first argument to be paragraph data file path...")
                println("expecting second argument to be outline data file path...")
                println("expecting third argument to be the type of TF-IDF to run...")
                println("Initializing the results files from paragraph file ${args[0]} and outline file ${args[1]}")
                println("Saving results at ${System.getProperty("user.dir")}")
                println("Running the ${args[2]} model.")
                luceneDefaultResults = FileWriter(System.getProperty("user.dir") + "luceneDefaultSections.results")
                customResults = when {
                    args[2] == "lncltn" -> FileWriter(System.getProperty("user.dir") + "lncLtnSection.results")
                    args[2] == "bnnbnn" -> FileWriter(System.getProperty("user.dir") + "bnnBnnSection.results")
                    args[2] == "ancapc" -> FileWriter(System.getProperty("user.dir") + "ancApcSection.results")
                    args[2] == "ul" -> FileWriter(System.getProperty("user.dir") + "ul.results")
                    args[3] == "ujm" -> FileWriter(System.getProperty("user.dir") + "ujm.results")
                    args[3] == "bl" -> FileWriter(System.getProperty("user.dir") + "bl.results")
                    else -> FileWriter(System.getProperty("user.dir") + "custom.results")
                }
                generateResults(luceneDefaultResults, customResults, args)
            }
        }
    } catch (e: Exception) {
        println("Requires all arguments to be used!")
        println("usage:")
        println("-init [paragraphFilePath] [outlinesFilePath] [model] | to generate results")
        println("-eval [qRelFilePath] [resultsFileFromInitPath] | to evaluate the results")
        println("[paragraphFilePath] [outlinesFilePath] [model] | section queries")
        println("\t\t\t[model = lncltn | bnnbnn | ancapc]")
        println(e.message)

    }

}

fun generateResults(luceneDefaultResults: FileWriter, customResults: FileWriter, args: Array<String>) {
    // Create an indexer for Lucene default
    val indexer = Indexer()

    // Create an index for LNC.LTN
    val lncLtnIndexer = Indexer()

    // Create an index for BNN.BNN
    val bnnBnnIndexer = Indexer()

    // Create an index for ANC.APC
    val ancApcIndexer = Indexer()

    // Create an index for U-L
    val uLIndexer = Indexer()

    // Create an index for U-JM
    val uJMIndexer = Indexer()

    // Create an index for U-DS
    val uDSIndexer = Indexer()

    // Create an index for B-L
    val bLIndexer = Indexer()

    // Get paragraphs from the CBOR file
    val paragraphStream : FileInputStream = if (args.size == 3) {
        FileInputStream(args[0])
    } else {
        FileInputStream(args[1])
    }

    // Get pages from the CBOR file
    val pageStream : FileInputStream = if (args.size == 3) {
        FileInputStream(args[1])
    } else {
        FileInputStream(args[2])
    }

    // Holds our frequencies in a table representation
    val invertedIndex = InvertedIndex()

    // Holds bigram info
    val bigramIndex = BigramIndex()

    // Standard token and stemming rules
    val analyzer = StandardAnalyzer()

    // Document ID for the current document being indexed
    var currentIndexDocID = 0

    // Document vectors
    val documentVectorsLnc = ArrayList<ArrayList<Double>>()
    val documentVectorsBnn = ArrayList<ArrayList<Double>>()
    val documentVectorsAnc = ArrayList<ArrayList<Double>>()
    val documentVectorsUL = ArrayList<ArrayList<Double>>()
    val documentVectorsUJM = ArrayList<ArrayList<Double>>()
    val documentVectorsUDS = ArrayList<ArrayList<Double>>()
    val documentVectorsBL = ArrayList<ArrayList<Double>>()

    val model : String = if (args.size == 3) {
        args[2]
    } else {
        args[3]
    }

    println("Using $model model...")

    // Add the paragraphs to each index
    DeserializeData.iterableParagraphs(paragraphStream).forEach {
        var lastToken = ""
        TokenizerAnalyzer.tokenizeString(analyzer, it.textOnly).forEach { token ->
            invertedIndex.addToIndex(token, currentIndexDocID)
            if (lastToken != "")
                bigramIndex.addToIndex(currentIndexDocID, token, lastToken)
            lastToken = token
        }

        when (model) {
            "lncltn" -> {
                documentVectorsLnc.add(invertedIndex.generateDocumentVector(TFIDF_DOC_TYPE.LNC, currentIndexDocID))
                lncLtnIndexer.indexParagraph(it)
            }
            "bnnbnn" -> {
                documentVectorsBnn.add(invertedIndex.generateDocumentVector(TFIDF_DOC_TYPE.BNN, currentIndexDocID))
                bnnBnnIndexer.indexParagraph(it)
            }
            "ancapc" -> {
                documentVectorsAnc.add(invertedIndex.generateDocumentVector(TFIDF_DOC_TYPE.ANC, currentIndexDocID))
                ancApcIndexer.indexParagraph(it)
            }
            "ul" -> {

                documentVectorsUL.add(invertedIndex.generateDocumentVector(TFIDF_DOC_TYPE.OTHER, currentIndexDocID))
                uLIndexer.indexParagraph(it)
            }
            "ujm" -> {
                documentVectorsUJM.add(invertedIndex.generateDocumentVector(TFIDF_DOC_TYPE.OTHER, currentIndexDocID))
                uJMIndexer.indexParagraph(it)
            }
            "uds" -> {
                documentVectorsUDS.add(invertedIndex.generateDocumentVector(TFIDF_DOC_TYPE.OTHER, currentIndexDocID))
                uDSIndexer.indexParagraph(it)
            }
            "bl" -> {
                documentVectorsBL.add(invertedIndex.generateDocumentVector(TFIDF_DOC_TYPE.OTHER, currentIndexDocID))
                bLIndexer.indexParagraph(it)
            }
        }
        indexer.indexParagraph(it)
        currentIndexDocID++
    }

    println()
    println("Indexing complete.")


    // Close after we load the entries
    indexer.closeIndex()
    lncLtnIndexer.closeIndex()
    bnnBnnIndexer.closeIndex()
    ancApcIndexer.closeIndex()
    uLIndexer.closeIndex()
    uJMIndexer.closeIndex()
    uDSIndexer.closeIndex()
    bLIndexer.closeIndex()

    // Create the search engines
    val directory = indexer.indexDir

    val lncLtnDirectory = lncLtnIndexer.indexDir
    val bnnBnnDirectory = bnnBnnIndexer.indexDir
    val ancApcDirectory = ancApcIndexer.indexDir
    val uLDirectory = uLIndexer.indexDir
    val uJMDirectory = uJMIndexer.indexDir
    val uDSDirectory = uDSIndexer.indexDir
    val bLDirectory = bLIndexer.indexDir

//    println(currentIndexDocID)

    val maxDocID = currentIndexDocID
    println("maxDocID $maxDocID")

    // Page title queries
    DeserializeData.iterableAnnotations(pageStream).forEach { page ->
        // Normal query is the page title
        var query = page.pageName

        // Check if we're doing section headings make that the query
        if (args.size == 3) {
            page.childSections.forEach { query += it.heading }
        }

        var pageId = page.pageId.toString()

        if (args.size == 3) {
            page.childSections.forEach { pageId += "/" + it.headingId.toString() }
        }

        val tokenizedQuery = tokenizeQuery(query, analyzer)

        val queryVectorLtn = invertedIndex.generateQueryVector(tokenizedQuery, TFIDF_QUERY_TYPE.LTN)
        val queryVectorBnn = invertedIndex.generateQueryVector(tokenizedQuery, TFIDF_QUERY_TYPE.BNN)
        val queryVectorApc = invertedIndex.generateQueryVector(tokenizedQuery, TFIDF_QUERY_TYPE.APC)

        val lncLtnSim = lncLtnSimilarity(invertedIndex, documentVectorsLnc, queryVectorLtn)
        val bnnBnnSim = bnnBnnSimilarity(invertedIndex, documentVectorsBnn, queryVectorBnn)
        val ancApcSim = ancApcSimilarity(invertedIndex, documentVectorsAnc, queryVectorApc)
        val uLSim = unigramLaplaceSimilarity(invertedIndex, tokenizedQuery, documentVectorsUL)
        val uJMSim = unigramJelinekMercerSimilarity(invertedIndex, tokenizedQuery, documentVectorsUJM, totalDocs = currentIndexDocID.toDouble())
        val uDSSim = unigramDirichletSimilarity(invertedIndex, tokenizedQuery, documentVectorsUDS)
        val bLSim = bigramLaplaceSimilarity(invertedIndex, tokenizedQuery, documentVectorsBL, bigramIndex)

        val searchEngine = SearchEngine(directory)
        val ltnLncEngine = SearchEngine(lncLtnDirectory, lncLtnSim)
        val bnnBnnEngine = SearchEngine(bnnBnnDirectory, bnnBnnSim)
        val ancApcEngine = SearchEngine(ancApcDirectory, ancApcSim)
        val uLEngine = SearchEngine(uLDirectory, uLSim)
        val uJMEngine = SearchEngine(uJMDirectory, uJMSim)
        val uDSEngine = SearchEngine(uDSDirectory, uDSSim)
        val bLEngine = SearchEngine(bLDirectory, bLSim)

        print("Starting Lucene default results...")
        val topDefaultScoredDocuments = searchEngine.performQuery(query, 100)

        topDefaultScoredDocuments.scoreDocs.forEachIndexed { rank, scoreDoc ->
            if (scoreDoc.doc <= maxDocID) {
                val doc = searchEngine.getDoc(scoreDoc.doc)
                val docId = doc?.get(IndexerFields.ID.toString().toLowerCase())
                luceneDefaultResults.write("$pageId\tQ0\t$docId\t$rank\t${scoreDoc.score}\tteam7-luceneDefault\n")
            }
        }
        println("Lucene default results done.")

        when (model) {
            "lncltn" -> {
                print("Starting LNC.LTN results...")

                val topLncLtnScoredDocuments = ltnLncEngine.performQuery(query, 100)

                topLncLtnScoredDocuments.scoreDocs.forEachIndexed { rank, scoreDoc ->
                    if (scoreDoc.doc <= maxDocID) {
                        val doc = ltnLncEngine.getDoc(scoreDoc.doc)
                        val docId = doc?.get(IndexerFields.ID.toString().toLowerCase())
                        customResults.write("$pageId\tQ0\t$docId\t$rank\t${scoreDoc.score}\tteam7-lncltn\n")
                    }
                }
                println("LNC.LTN results done.")
            }
            "bnnbnn" -> {
                print("Starting BNN.BNN results...")

                val topBnnBnnScoredDocuments = bnnBnnEngine.performQuery(query, 100)

                topBnnBnnScoredDocuments.scoreDocs.forEachIndexed { rank, scoreDoc ->
                    if (scoreDoc.doc <= maxDocID) {
                        val doc = bnnBnnEngine.getDoc(scoreDoc.doc)
                        val docId = doc?.get(IndexerFields.ID.toString().toLowerCase())
                        customResults.write("$pageId\tQ0\t$docId\t$rank\t${scoreDoc.score}\tteam7-bnnbnn\n")
                    }
                }
                println("BNN.BNN results done.")
            }
            "ancapc" -> {
                print("Starting ANC.APC results...")

                val topAncApcScoredDocuments = ancApcEngine.performQuery(query, 100)

                topAncApcScoredDocuments.scoreDocs.forEachIndexed { rank, scoreDoc ->
                    if (scoreDoc.doc <= maxDocID) {
                        val doc = ancApcEngine.getDoc(scoreDoc.doc)
                        val docId = doc?.get(IndexerFields.ID.toString().toLowerCase())
                        customResults.write("$pageId\tQ0\t$docId\t$rank\t${scoreDoc.score}\tteam7-ancapc\n")
                    }
                }

                println("ANC.APC results done.")
            }
            "ul" -> {
                print("Starting Unigram Laplace results...")

                val topULScoredDocuments = uLEngine.performQuery(query, 100)

                topULScoredDocuments.scoreDocs.forEachIndexed { rank, scoreDoc ->
                    if (scoreDoc.doc <= maxDocID) {
                        val doc = uLEngine.getDoc(scoreDoc.doc)
                        val docId = doc?.get(IndexerFields.ID.toString().toLowerCase())
                        customResults.write("$pageId\tQ0\t$docId\t$rank\t${scoreDoc.score}\tteam7-ul\n")
                    }
                }

                println("Unigram Laplace results done.")
            }
            "ujm" -> {
                print("Starting Unigram Jelinek Mercer results...")

                val topUJMScoredDocuments = uJMEngine.performQuery(query, 100)

                topUJMScoredDocuments.scoreDocs.forEachIndexed { rank, scoreDoc ->
                    if (scoreDoc.doc <= maxDocID) {
                        val doc = uJMEngine.getDoc(scoreDoc.doc)
                        val docId = doc?.get(IndexerFields.ID.toString().toLowerCase())
                        customResults.write("$pageId\tQ0\t$docId\t$rank\t${scoreDoc.score}\tteam7-ujm\n")
                    }
                }

                println("Unigram Jelinek Mercer results done.")
            }
            "uds" -> {
                print("Starting Unigram Dirichlet results...")

                val topUDSScoredDocuments = uDSEngine.performQuery(query, 100)

                topUDSScoredDocuments.scoreDocs.forEachIndexed { rank, scoreDoc ->
                    if (scoreDoc.doc <= maxDocID) {
                        val doc = uDSEngine.getDoc(scoreDoc.doc)
                        val docId = doc?.get(IndexerFields.ID.toString().toLowerCase())
                        customResults.write("$pageId\tQ0\t$docId\t$rank\t${scoreDoc.score}\tteam7-uds\n")
                    }
                }

                println("Unigram Dirichlet results done.")
            }
            "bl" -> {
                print("Starting Bigram Laplace results...")

                val topBLScoredDocuments = bLEngine.performQuery(query, 100)

                topBLScoredDocuments.scoreDocs.forEachIndexed { rank, scoreDoc ->
                    if (scoreDoc.doc <= maxDocID) {
                        val doc = bLEngine.getDoc(scoreDoc.doc)
                        val docId = doc?.get(IndexerFields.ID.toString().toLowerCase())
                        customResults.write("$pageId\tQ0\t$docId\t$rank\t${scoreDoc.score}\tteam7-bl\n")
                    }
                }

                println("Bigram Laplace results done.")
            }
        }

    }

    luceneDefaultResults.close()
    customResults.close()

}

fun generateResults(query: String, paragraphFile: String) {

    val rankMethods = ArrayList<String>()
    rankMethods.add("lncltn")
    rankMethods.add("bnnbnn")
    rankMethods.add("ancapc")
    rankMethods.add("ul")
    rankMethods.add("ujm")
    rankMethods.add("uds")
    rankMethods.add("bl")

    // Get paragraphs from the CBOR file
    var paragraphStream = FileInputStream(paragraphFile)

    // Holds our frequencies in a table representation
    val invertedIndex = InvertedIndex()

    // Holds bigram info
    val bigramIndex = BigramIndex()

    // Standard token and stemming rules
    val analyzer = StandardAnalyzer()

    val featureVectors = HashMap<Int, HashMap<Int, FeatureVector>>()

    // Add the paragraphs to each index
    rankMethods.forEachIndexed { _, model ->
        val index = Indexer()
        println("start index")
        // Document vectors
        val documentVectors = ArrayList<ArrayList<Double>>()
        // Document ID for the current document being indexed
        var currentIndexDocID = 0
        DeserializeData.iterableParagraphs(paragraphStream).forEach {
            var lastToken = ""
            TokenizerAnalyzer.tokenizeString(analyzer, it.textOnly).forEach { token ->
                invertedIndex.addToIndex(token, currentIndexDocID)
                if (lastToken != "")
                    bigramIndex.addToIndex(currentIndexDocID, token, lastToken)
                lastToken = token
            }

            var docType = TFIDF_DOC_TYPE.OTHER

            when (model) {
                "lncltn" -> {docType = TFIDF_DOC_TYPE.LNC}
                "bnnbnn" -> {docType = TFIDF_DOC_TYPE.BNN}
                "ancapc" -> {docType = TFIDF_DOC_TYPE.ANC}
            }

            documentVectors.add(invertedIndex.generateDocumentVector(docType, currentIndexDocID))
            index.indexParagraph(it)
            currentIndexDocID++
        }

        println()
        println("Indexing complete.")


        // Close after we load the entries
        index.closeIndex()

        // Create the search engines
        val directory = index.indexDir

        val maxDocID = currentIndexDocID

        var currentqID = 0

        val tokenizedQuery = tokenizeQuery(query, analyzer)

        var queryType = TFIDF_QUERY_TYPE.LTN

        when (model) {
            "lncltn" -> {queryType = TFIDF_QUERY_TYPE.LTN}
            "bnnbnn" -> {queryType = TFIDF_QUERY_TYPE.BNN}
            "ancapc" -> {queryType = TFIDF_QUERY_TYPE.APC}
        }

        val queryVector = invertedIndex.generateQueryVector(tokenizedQuery, queryType)

        var sim: SimilarityBase
        var searchEngine: SearchEngine

        when (model) {
            "lncltn" -> {sim = lncLtnSimilarity(invertedIndex, documentVectors, queryVector)
            }
            "bnnbnn" -> {sim = bnnBnnSimilarity(invertedIndex, documentVectors, queryVector)}
            "ancapc" -> {sim = ancApcSimilarity(invertedIndex, documentVectors, queryVector)}
            "ul" -> {sim = unigramLaplaceSimilarity(invertedIndex, tokenizedQuery, documentVectors)}
            "ujm" -> {sim = unigramJelinekMercerSimilarity(invertedIndex, tokenizedQuery, documentVectors, totalDocs = currentIndexDocID.toDouble())}
            "uds" -> {sim = unigramDirichletSimilarity(invertedIndex, tokenizedQuery, documentVectors)}
            "bl" -> {sim = bigramLaplaceSimilarity(invertedIndex, tokenizedQuery, documentVectors, bigramIndex)}
            else -> {sim = unigramDirichletSimilarity(invertedIndex, tokenizedQuery, documentVectors)}
        }

        searchEngine = SearchEngine(directory, sim)

        print("Starting search results...")
        val topScoredDocuments = searchEngine.performQuery(query, 10)

        topScoredDocuments.scoreDocs.forEachIndexed { rank, scoreDoc ->
            if (scoreDoc.doc <= maxDocID) {
                val doc = searchEngine.getDoc(scoreDoc.doc)
                val docId = doc?.get(IndexerFields.ID.toString().toLowerCase())
                when (model) {
                    "lncltn" -> {
                        if (featureVectors[scoreDoc.doc] == null)
                            featureVectors[scoreDoc.doc] = HashMap<Int, FeatureVector>()
                        if (featureVectors[scoreDoc.doc]!![currentqID] == null)
                            featureVectors[scoreDoc.doc]!![currentqID] = FeatureVector()
                        featureVectors[scoreDoc.doc]!![currentqID]!!.lncltnScore = 1.0/(rank.toDouble() + 1.0)
                    }
                    "bnnbnn" -> {
                        if (featureVectors[scoreDoc.doc] == null)
                            featureVectors[scoreDoc.doc] = HashMap<Int, FeatureVector>()
                        if (featureVectors[scoreDoc.doc]!![currentqID] == null)
                            featureVectors[scoreDoc.doc]!![currentqID] = FeatureVector()
                        featureVectors[scoreDoc.doc]!![currentqID]!!.bnnbnnScore = 1.0/(rank.toDouble() + 1.0)
                    }
                    "ancapc" -> {
                        if (featureVectors[scoreDoc.doc] == null)
                            featureVectors[scoreDoc.doc] = HashMap<Int, FeatureVector>()
                        if (featureVectors[scoreDoc.doc]!![currentqID] == null)
                            featureVectors[scoreDoc.doc]!![currentqID] = FeatureVector()
                        featureVectors[scoreDoc.doc]!![currentqID]!!.ancapcScore = 1.0/(rank.toDouble() + 1.0)
                    }
                    "ul" -> {
                        if (featureVectors[scoreDoc.doc] == null)
                            featureVectors[scoreDoc.doc] = HashMap<Int, FeatureVector>()
                        if (featureVectors[scoreDoc.doc]!![currentqID] == null)
                            featureVectors[scoreDoc.doc]!![currentqID] = FeatureVector()
                        featureVectors[scoreDoc.doc]!![currentqID]!!.ulScore = 1.0/(rank.toDouble() + 1.0)
                    }
                    "ujm" -> {
                        if (featureVectors[scoreDoc.doc] == null)
                            featureVectors[scoreDoc.doc] = HashMap<Int, FeatureVector>()
                        if (featureVectors[scoreDoc.doc]!![currentqID] == null)
                            featureVectors[scoreDoc.doc]!![currentqID] = FeatureVector()
                        featureVectors[scoreDoc.doc]!![currentqID]!!.ujmScore = 1.0/(rank.toDouble() + 1.0)
                    }
                    "uds" -> {
                        if (featureVectors[scoreDoc.doc] == null)
                            featureVectors[scoreDoc.doc] = HashMap<Int, FeatureVector>()
                        if (featureVectors[scoreDoc.doc]!![currentqID] == null)
                            featureVectors[scoreDoc.doc]!![currentqID] = FeatureVector()
                        featureVectors[scoreDoc.doc]!![currentqID]!!.udsScore = 1.0/(rank.toDouble() + 1.0)
                    }
                    "bl" -> {
                        if (featureVectors[scoreDoc.doc] == null)
                            featureVectors[scoreDoc.doc] = HashMap<Int, FeatureVector>()
                        if (featureVectors[scoreDoc.doc]!![currentqID] == null)
                            featureVectors[scoreDoc.doc]!![currentqID] = FeatureVector()
                        featureVectors[scoreDoc.doc]!![currentqID]!!.blScore = 1.0/(rank.toDouble() + 1.0)
                    }
                }
                featureVectors[scoreDoc.doc]!![currentqID]!!.documentID = docId!!
                featureVectors[scoreDoc.doc]!![currentqID]!!.queryID = currentqID
            }
        }


        // Flush RAM directory
        directory.close()
        invertedIndex.clear()
        bigramIndex.clear()

        // Get paragraphs from the CBOR file
        paragraphStream = FileInputStream(paragraphFile)
    }

    featureVectors.forEach { docid, queryList ->
        queryList.forEach { queryid, featureVec ->
            println("rel: " + featureVec.relevant + " qid: " + featureVec.queryID + " f0: " + featureVec.lncltnScore + " f1: " + featureVec.bnnbnnScore
                    + " f2: " + featureVec.ancapcScore + " f3: " + featureVec.ulScore + " f4: " + featureVec.ujmScore + " f5: " + featureVec.udsScore + " f6: " + featureVec.blScore +
                    " # " + featureVec.documentID)
        }
    }
}

fun generateResults(args: Array<String>) {

    val rankMethods = ArrayList<String>()
    rankMethods.add("lncltn")
    rankMethods.add("bnnbnn")
    rankMethods.add("ancapc")
    rankMethods.add("ul")
    rankMethods.add("ujm")
    rankMethods.add("uds")
    rankMethods.add("bl")

    // Get paragraphs from the CBOR file
    var paragraphStream : FileInputStream = if (args.size == 3) {
        FileInputStream(args[1])
    } else {
        FileInputStream(args[2])
    }

    // Get pages from the CBOR file
    var pageStream : FileInputStream = if (args.size == 3) {
        FileInputStream(args[2])
    } else {
        FileInputStream(args[3])
    }

    // Holds our frequencies in a table representation
    val invertedIndex = InvertedIndex()

    // Holds bigram info
    val bigramIndex = BigramIndex()

    // Standard token and stemming rules
    val analyzer = StandardAnalyzer()

    val featureVectors = HashMap<Int, HashMap<Int, FeatureVector>>()

    // Add the paragraphs to each index
    rankMethods.forEachIndexed { _, model ->
        val index = Indexer()
        println("start index")
        // Document vectors
        val documentVectors = ArrayList<ArrayList<Double>>()
        // Document ID for the current document being indexed
        var currentIndexDocID = 0
        DeserializeData.iterableParagraphs(paragraphStream).forEach {
            var lastToken = ""
            TokenizerAnalyzer.tokenizeString(analyzer, it.textOnly).forEach { token ->
                invertedIndex.addToIndex(token, currentIndexDocID)
                if (lastToken != "")
                    bigramIndex.addToIndex(currentIndexDocID, token, lastToken)
                lastToken = token
            }

            var docType = TFIDF_DOC_TYPE.OTHER

            when (model) {
                "lncltn" -> {docType = TFIDF_DOC_TYPE.LNC}
                "bnnbnn" -> {docType = TFIDF_DOC_TYPE.BNN}
                "ancapc" -> {docType = TFIDF_DOC_TYPE.ANC}
            }

            documentVectors.add(invertedIndex.generateDocumentVector(docType, currentIndexDocID))
            index.indexParagraph(it)
            currentIndexDocID++
        }

        println()
        println("Indexing complete.")


        // Close after we load the entries
        index.closeIndex()

        // Create the search engines
        val directory = index.indexDir

        val maxDocID = currentIndexDocID

        var currentqID = 0
        // Page title queries
        DeserializeData.iterableAnnotations(pageStream).forEach { page ->
            // Normal query is the page title
            var query = page.pageName

            // Check if we're doing section headings make that the query
            if (args.size == 4) {
                page.childSections.forEach { query += it.heading }
            }

            var pageId = page.pageId.toString()

            if (args.size == 4) {
                page.childSections.forEach { pageId += "/" + it.headingId.toString() }
            }

            val tokenizedQuery = tokenizeQuery(query, analyzer)

            var queryType = TFIDF_QUERY_TYPE.LTN

            when (model) {
                "lncltn" -> {queryType = TFIDF_QUERY_TYPE.LTN}
                "bnnbnn" -> {queryType = TFIDF_QUERY_TYPE.BNN}
                "ancapc" -> {queryType = TFIDF_QUERY_TYPE.APC}
            }

            val queryVector = invertedIndex.generateQueryVector(tokenizedQuery, queryType)

            var sim: SimilarityBase
            var searchEngine: SearchEngine

            when (model) {
                "lncltn" -> {sim = lncLtnSimilarity(invertedIndex, documentVectors, queryVector)
                }
                "bnnbnn" -> {sim = bnnBnnSimilarity(invertedIndex, documentVectors, queryVector)}
                "ancapc" -> {sim = ancApcSimilarity(invertedIndex, documentVectors, queryVector)}
                "ul" -> {sim = unigramLaplaceSimilarity(invertedIndex, tokenizedQuery, documentVectors)}
                "ujm" -> {sim = unigramJelinekMercerSimilarity(invertedIndex, tokenizedQuery, documentVectors, totalDocs = currentIndexDocID.toDouble())}
                "uds" -> {sim = unigramDirichletSimilarity(invertedIndex, tokenizedQuery, documentVectors)}
                "bl" -> {sim = bigramLaplaceSimilarity(invertedIndex, tokenizedQuery, documentVectors, bigramIndex)}
                else -> {sim = unigramDirichletSimilarity(invertedIndex, tokenizedQuery, documentVectors)}
            }

            searchEngine = SearchEngine(directory, sim)

            print("Starting search results...")
            val topScoredDocuments = searchEngine.performQuery(query, 10)

            topScoredDocuments.scoreDocs.forEachIndexed { rank, scoreDoc ->
                if (scoreDoc.doc <= maxDocID) {
                    val doc = searchEngine.getDoc(scoreDoc.doc)
                    val docId = doc?.get(IndexerFields.ID.toString().toLowerCase())
                    when (model) {
                        "lncltn" -> {
                            if (featureVectors[scoreDoc.doc] == null)
                                featureVectors[scoreDoc.doc] = HashMap<Int, FeatureVector>()
                            if (featureVectors[scoreDoc.doc]!![currentqID] == null)
                                featureVectors[scoreDoc.doc]!![currentqID] = FeatureVector()
                            featureVectors[scoreDoc.doc]!![currentqID]!!.lncltnScore = 1.0/(rank.toDouble() + 1.0)
                        }
                        "bnnbnn" -> {
                            if (featureVectors[scoreDoc.doc] == null)
                                featureVectors[scoreDoc.doc] = HashMap<Int, FeatureVector>()
                            if (featureVectors[scoreDoc.doc]!![currentqID] == null)
                                featureVectors[scoreDoc.doc]!![currentqID] = FeatureVector()
                            featureVectors[scoreDoc.doc]!![currentqID]!!.bnnbnnScore = 1.0/(rank.toDouble() + 1.0)
                        }
                        "ancapc" -> {
                            if (featureVectors[scoreDoc.doc] == null)
                                featureVectors[scoreDoc.doc] = HashMap<Int, FeatureVector>()
                            if (featureVectors[scoreDoc.doc]!![currentqID] == null)
                                featureVectors[scoreDoc.doc]!![currentqID] = FeatureVector()
                            featureVectors[scoreDoc.doc]!![currentqID]!!.ancapcScore = 1.0/(rank.toDouble() + 1.0)
                        }
                        "ul" -> {
                            if (featureVectors[scoreDoc.doc] == null)
                                featureVectors[scoreDoc.doc] = HashMap<Int, FeatureVector>()
                            if (featureVectors[scoreDoc.doc]!![currentqID] == null)
                                featureVectors[scoreDoc.doc]!![currentqID] = FeatureVector()
                            featureVectors[scoreDoc.doc]!![currentqID]!!.ulScore = 1.0/(rank.toDouble() + 1.0)
                        }
                        "ujm" -> {
                            if (featureVectors[scoreDoc.doc] == null)
                                featureVectors[scoreDoc.doc] = HashMap<Int, FeatureVector>()
                            if (featureVectors[scoreDoc.doc]!![currentqID] == null)
                                featureVectors[scoreDoc.doc]!![currentqID] = FeatureVector()
                            featureVectors[scoreDoc.doc]!![currentqID]!!.ujmScore = 1.0/(rank.toDouble() + 1.0)
                        }
                        "uds" -> {
                            if (featureVectors[scoreDoc.doc] == null)
                                featureVectors[scoreDoc.doc] = HashMap<Int, FeatureVector>()
                            if (featureVectors[scoreDoc.doc]!![currentqID] == null)
                                featureVectors[scoreDoc.doc]!![currentqID] = FeatureVector()
                            featureVectors[scoreDoc.doc]!![currentqID]!!.udsScore = 1.0/(rank.toDouble() + 1.0)
                        }
                        "bl" -> {
                            if (featureVectors[scoreDoc.doc] == null)
                                featureVectors[scoreDoc.doc] = HashMap<Int, FeatureVector>()
                            if (featureVectors[scoreDoc.doc]!![currentqID] == null)
                                featureVectors[scoreDoc.doc]!![currentqID] = FeatureVector()
                            featureVectors[scoreDoc.doc]!![currentqID]!!.blScore = 1.0/(rank.toDouble() + 1.0)
                        }
                    }
                    featureVectors[scoreDoc.doc]!![currentqID]!!.documentID = docId!!
                    featureVectors[scoreDoc.doc]!![currentqID]!!.queryID = currentqID
                }
            }
            currentqID++
        }

        // Flush RAM directory
        directory.close()
        invertedIndex.clear()
        bigramIndex.clear()

        // Get paragraphs from the CBOR file
        paragraphStream = if (args.size == 3) {
            FileInputStream(args[1])
        } else {
            FileInputStream(args[2])
        }

        // Get pages from the CBOR file
        pageStream = if (args.size == 3) {
            FileInputStream(args[2])
        } else {
            FileInputStream(args[3])
        }
    }

    featureVectors.forEach { docid, queryList ->
        queryList.forEach { queryid, featureVec ->
            println("rel: " + featureVec.relevant + " qid: " + featureVec.queryID + " f0: " + featureVec.lncltnScore + " f1: " + featureVec.bnnbnnScore
            + " f2: " + featureVec.ancapcScore + " f3: " + featureVec.ulScore + " f4: " + featureVec.ujmScore + " f5: " + featureVec.udsScore + " f6: " + featureVec.blScore +
                    " # " + featureVec.documentID)
        }
    }
}

class unigramDirichletSimilarity(private val invertedIndex: InvertedIndex, private val tokenizedQuery: ArrayList<String>,
                                 private val documentVector: ArrayList<ArrayList<Double>>) : SimilarityBase() {
    private var currentDocument = 0
    private var mu = 1000

    override fun toString(): String = "U-DS Similarity"

    override fun score(stats: BasicStats?, freq: Float, docLen: Float): Float {
        var score = 1.0

        tokenizedQuery.forEach { term ->
            var sum = 0.0
            documentVector[currentDocument].forEach { sum += it }
            val termFreq = invertedIndex.getTermFrequency(currentDocument, term)
            val pt = termFreq / invertedIndex.getDocSize(term).toDouble()
            val prob = (termFreq + mu * pt) / (sum + mu)
            if (prob != 0.0) {
                score *= prob
            }
        }

        currentDocument++
        return score.toFloat()
    }

}

class unigramLaplaceSimilarity(private val invertedIndex: InvertedIndex, private val tokenizedQuery: ArrayList<String>,
                               private val documentVector: ArrayList<ArrayList<Double>>) : SimilarityBase() {
    private var currentDocument = 0

    override fun toString(): String = "U-L Similarity"

    override fun score(stats: BasicStats?, freq: Float, docLen: Float): Float {
        var score = 1.0

        // Have to compute each query term P on a per document basis anyhow, so do that shit here
        tokenizedQuery.forEachIndexed { _, s ->
            // sum of query term freqs in d
            var sum = 0.0
            documentVector[currentDocument].forEachIndexed { _, frequency -> sum += frequency }
            val probTD = ((invertedIndex.getTermFrequency(currentDocument, s) + 1).toFloat() / (sum + invertedIndex.size).toFloat())

            score *= probTD
        }

        currentDocument++
        return score.toFloat()
    }

}

class unigramJelinekMercerSimilarity(private val invertedIndex: InvertedIndex, private val tokenizedQuery: ArrayList<String>,
                                     private val documentVector: ArrayList<ArrayList<Double>>, private val totalDocs: Double) : SimilarityBase() {
    private var lambda = 0.9
    private var currentDocument = 0

    override fun score(stats: BasicStats?, freq: Float, docLen: Float): Float {
        var productOfProbabilities = 1.0
        var documentTermFrequencySum = 1.0
        documentVector[currentDocument].forEach { documentTermFrequencySum += it }

        tokenizedQuery.forEach { term ->
            val firstTerm = lambda * invertedIndex.getTermFrequency(currentDocument, term) / documentTermFrequencySum
            val secondTerm = (1.0 - lambda) * (invertedIndex.getTermFrequency(currentDocument,term).toDouble() / invertedIndex.getDocSize(term).toDouble())
            if (firstTerm + secondTerm != 0.0) {
                productOfProbabilities *= firstTerm + secondTerm
            }
        }

        currentDocument++
        return productOfProbabilities.toFloat()
    }

    override fun toString(): String = "U-JM Similarity"

}

class bigramLaplaceSimilarity(private val invertedIndex: InvertedIndex, private val tokenizedQuery: ArrayList<String>,
                              private val documentVector: ArrayList<ArrayList<Double>>, private val bigramIndex: BigramIndex)
    : SimilarityBase() {
    private var currentDocument = 0

    override fun toString(): String = "B-L Similarity"

    override fun score(stats: BasicStats?, freq: Float, docLen: Float): Float {
        var score = 1.0

        // Have to compute each query term P on a per document basis anyhow, so do that shit here
        var lastTerm = ""
        tokenizedQuery.forEachIndexed { _, s ->
            // sum of query term freqs in d
            var sum = 0.0
            // calculate prob of bigram
            var bigramProb = 0.0
            if (lastTerm == "")
            {
                lastTerm = s
                bigramProb = invertedIndex.getTermFrequency(currentDocument, s).toDouble()
            }
            else
            {
                bigramProb = bigramIndex.getFrequency(currentDocument, s, lastTerm).toDouble()
                lastTerm = s
            }
            documentVector[currentDocument].forEachIndexed { _, frequency -> sum += frequency }
            val probTD = ((bigramProb + 1).toFloat() / (sum + invertedIndex.size).toFloat())

            score *= probTD
        }

        currentDocument++
        return score.toFloat()
    }

}

class lncLtnSimilarity(private val invertedIndex: InvertedIndex, private val documentVectors: ArrayList<ArrayList<Double>>,
                       private val queryVector: ArrayList<Double>) : SimilarityBase() {
    private var currentDocument = 0

    override fun toString(): String {
        return "LNC.LTN Similarity"
    }

    override fun score(stats: BasicStats?, freq: Float, docLen: Float): Float {
        val normalizedDocumentVector = invertedIndex.normalizeVector(documentVectors[currentDocument],
                TFIDF_DOC_TYPE.LNC)
        val normalizedQueryVector = invertedIndex.normalizeVector(queryVector,
                TFIDF_DOC_TYPE.BNN)
        val score = calculateInnerProduct(normalizedDocumentVector, normalizedQueryVector)
        currentDocument++
        return score.toFloat()
    }

}

class bnnBnnSimilarity(private val invertedIndex: InvertedIndex, private val documentVectors: ArrayList<ArrayList<Double>>,
                       private val queryVector: ArrayList<Double>) : SimilarityBase() {
    private var currentDocument = 0

    override fun toString(): String {
        return "LNC.LTN Similarity"
    }

    override fun score(stats: BasicStats?, freq: Float, docLen: Float): Float {
        val normalizedDocumentVector = invertedIndex.normalizeVector(documentVectors[currentDocument],
                TFIDF_DOC_TYPE.BNN)
        val normalizedQueryVector = invertedIndex.normalizeVector(queryVector,
                TFIDF_DOC_TYPE.BNN)
        val score = calculateInnerProduct(normalizedDocumentVector, normalizedQueryVector)
        currentDocument++
        return score.toFloat()
    }

}

class ancApcSimilarity(private val invertedIndex: InvertedIndex, private val documentVectors: ArrayList<ArrayList<Double>>,
                       private val queryVector: ArrayList<Double>) : SimilarityBase() {
    private var currentDocument = 0

    override fun toString(): String {
        return "LNC.LTN Similarity"
    }

    override fun score(stats: BasicStats?, freq: Float, docLen: Float): Float {
        val normalizedDocumentVector = invertedIndex.normalizeVector(documentVectors[currentDocument],
                TFIDF_DOC_TYPE.ANC)
        val normalizedQueryVector = invertedIndex.normalizeVector(queryVector,
                TFIDF_DOC_TYPE.ANC)
        val score = calculateInnerProduct(normalizedDocumentVector, normalizedQueryVector)
        currentDocument++
        return score.toFloat()
    }

}

fun calculateInnerProduct(documentVectors: ArrayList<Double>, queryVector: ArrayList<Double>): Double {
    var innerProduct = 0.0
    assert(documentVectors.size == queryVector.size)
    documentVectors.forEachIndexed { index, d ->
        innerProduct += d + queryVector[index]
    }
    return innerProduct
}

fun tokenizeQuery(query: String, analyzer: StandardAnalyzer): ArrayList<String> {
    val tokens = ArrayList<String>()
    TokenizerAnalyzer.tokenizeString(analyzer, query).forEach { token ->
        tokens.add(token)
    }
    return tokens
}

fun performRankEvaluation(resultsFile: String, luceneResultsFile: String) {
    val evaluator = SpearmanRank(DataReader(resultsFile), DataReader(luceneResultsFile))
    println("Spearman's Rank Correlation: ${evaluator.calculateRank()}")
}

fun performEvaluation(resultsFile: String, qRelFile: String) {
    val evaluator = Evaluator(DataReader(resultsFile), DataReader(qRelFile))
    val rPrecisionMean = evaluator.calculateRPrecision()
    val rPrecisionError = evaluator.calculateRPrecisionError(rPrecisionMean)
    val mapMean = evaluator.calculateMeanAveragePrecision()
    val mapError = evaluator.calculateMeanAveragePrecisionError(mapMean)
    val nDCGMean = evaluator.calculateNormalizedDiscountCumulativeGain()
    val nDCGError = evaluator.calculateNormalizedDiscountCumulativeGainError(nDCGMean)
    println("RPrecision: $rPrecisionMean Error $rPrecisionError")
    println("MAP: $mapMean Error $mapError")
    println("nDCG: $nDCGMean Error $nDCGError")
}


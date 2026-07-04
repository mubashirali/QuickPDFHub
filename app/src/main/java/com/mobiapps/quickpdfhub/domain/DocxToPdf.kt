package com.mobiapps.quickpdfhub.domain

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.pdmodel.PDPage
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream

object DocxToPdf {

    private data class TextRun(
        val text: String,
        val bold: Boolean = false,
        val italic: Boolean = false,
        val fontSize: Float = 11f,
    )

    private data class Paragraph(
        val runs: List<TextRun>,
        val isHeading: Boolean = false,
        val headingLevel: Int = 0,
    )

    suspend fun execute(context: Context, uri: Uri): WorkResult =
        withContext(Dispatchers.IO) {
            try {
                PDFBoxResourceLoader.init(context.applicationContext)

                val sizeBeforeBytes = context.contentResolver
                    .openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L

                val displayName = displayNameOf(context, uri)
                val baseName = displayName
                    .removeSuffix(".docx").removeSuffix(".DOCX")
                    .removeSuffix(".doc").removeSuffix(".DOC")
                val outputFileName = "$baseName.pdf"
                val outputFile = File(context.cacheDir, outputFileName)

                val paragraphs = parseDocx(context, uri)
                renderToPdf(paragraphs, outputFile)

                val outputUri = FileProvider.getUriForFile(
                    context, "${context.packageName}.provider", outputFile,
                )

                WorkResult.Success(
                    outputUri = outputUri,
                    outputFileName = outputFileName,
                    sizeBeforeBytes = sizeBeforeBytes,
                    sizeAfterBytes = outputFile.length(),
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                WorkResult.Error(e.message ?: "DOCX conversion failed.")
            }
        }

    private fun parseDocx(context: Context, uri: Uri): List<Paragraph> {
        context.contentResolver.openInputStream(uri)?.use { input ->
            ZipInputStream(input).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    if (entry.name == "word/document.xml") return parseDocumentXml(zip)
                    entry = zip.nextEntry
                }
            }
        }
        return emptyList()
    }

    private fun parseDocumentXml(stream: InputStream): List<Paragraph> {
        val paragraphs = mutableListOf<Paragraph>()
        val factory = XmlPullParserFactory.newInstance().apply { isNamespaceAware = false }
        val parser = factory.newPullParser()
        parser.setInput(stream, "UTF-8")

        var runs = mutableListOf<TextRun>()
        var styleId = ""
        var inPara = false
        var inRun = false
        var inText = false
        var inRunProps = false
        var inParaProps = false
        var bold = false
        var italic = false
        var fontSize = 11f

        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            val tag = parser.name ?: ""
            when (event) {
                XmlPullParser.START_TAG -> when (tag) {
                    "w:p" -> {
                        inPara = true
                        runs = mutableListOf()
                        styleId = ""
                    }
                    "w:pPr" -> inParaProps = true
                    "w:pStyle" -> if (inParaProps) {
                        styleId = parser.getAttributeValue(null, "w:val") ?: ""
                    }
                    "w:r" -> {
                        inRun = true
                        bold = false
                        italic = false
                        fontSize = 11f
                    }
                    "w:rPr" -> inRunProps = true
                    "w:b" -> if (inRunProps) bold = true
                    "w:i" -> if (inRunProps) italic = true
                    "w:sz" -> if (inRunProps) {
                        fontSize = ((parser.getAttributeValue(null, "w:val")
                            ?.toFloatOrNull() ?: 22f) / 2f).coerceIn(8f, 72f)
                    }
                    "w:t" -> inText = true
                    "w:br" -> if (inRun) runs.add(TextRun("\n"))
                }
                XmlPullParser.TEXT -> if (inText && inRun && inPara) {
                    val text = parser.text ?: ""
                    if (text.isNotEmpty()) runs.add(TextRun(text, bold, italic, fontSize))
                }
                XmlPullParser.END_TAG -> when (tag) {
                    "w:p" -> {
                        inPara = false
                        val isHeading = styleId.matches(Regex("(?i)heading\\d*|h[1-6]"))
                        val level = styleId.lastOrNull()?.digitToIntOrNull()?.coerceIn(1, 6) ?: 1
                        paragraphs.add(Paragraph(runs.toList(), isHeading, if (isHeading) level else 0))
                    }
                    "w:pPr" -> inParaProps = false
                    "w:r" -> inRun = false
                    "w:rPr" -> inRunProps = false
                    "w:t" -> inText = false
                }
            }
            event = parser.next()
        }
        return paragraphs
    }

    private fun renderToPdf(paragraphs: List<Paragraph>, outputFile: File) {
        val marginLeft = 60f
        val marginRight = 60f
        val marginTop = 60f
        val marginBottom = 60f
        val pageWidth = PDRectangle.A4.width
        val pageHeight = PDRectangle.A4.height

        val doc = PDDocument()
        var page = PDPage(PDRectangle.A4)
        doc.addPage(page)
        var cs = PDPageContentStream(doc, page)
        var y = pageHeight - marginTop

        fun newPage() {
            cs.close()
            page = PDPage(PDRectangle.A4)
            doc.addPage(page)
            cs = PDPageContentStream(doc, page)
            y = pageHeight - marginTop
        }

        fun font(bold: Boolean, italic: Boolean): PDType1Font = when {
            bold && italic -> PDType1Font.HELVETICA_BOLD_OBLIQUE
            bold -> PDType1Font.HELVETICA_BOLD
            italic -> PDType1Font.HELVETICA_OBLIQUE
            else -> PDType1Font.HELVETICA
        }

        fun safeText(s: String) = s.map { if (it.code in 32..255) it else '?' }.joinToString("")

        fun strWidth(s: String, f: PDType1Font, size: Float) =
            try { f.getStringWidth(s) / 1000f * size } catch (_: Exception) { s.length * size * 0.5f }

        fun drawLine(tokens: List<Triple<String, PDType1Font, Float>>) {
            val lineH = tokens.maxOfOrNull { it.third } ?: 11f
            if (y - lineH * 1.4f < marginBottom) newPage()
            var x = marginLeft
            for ((word, f, size) in tokens) {
                cs.beginText()
                cs.setFont(f, size)
                cs.newLineAtOffset(x, y)
                cs.showText(safeText(word))
                cs.endText()
                x += strWidth(word, f, size)
            }
            y -= lineH * 1.4f
        }

        for (para in paragraphs) {
            if (para.runs.isEmpty()) { y -= 8f; if (y < marginBottom) newPage(); continue }

            val baseSize = when {
                para.isHeading && para.headingLevel == 1 -> 20f
                para.isHeading && para.headingLevel == 2 -> 16f
                para.isHeading -> 14f
                else -> 11f
            }
            if (para.isHeading) { y -= 6f; if (y < marginBottom) newPage() }

            data class Token(val word: String, val bold: Boolean, val italic: Boolean, val size: Float)
            val tokens = mutableListOf<Token>()
            for (run in para.runs) {
                val size = if (run.fontSize > 0) run.fontSize else baseSize
                val parts = run.text.split(" ")
                parts.forEachIndexed { i, w ->
                    if (w.isNotEmpty()) tokens.add(Token(w, run.bold, run.italic, size))
                    if (i < parts.size - 1) tokens.add(Token(" ", run.bold, run.italic, size))
                }
            }

            var lineTokens = mutableListOf<Triple<String, PDType1Font, Float>>()
            var lineWidth = 0f
            for (token in tokens) {
                if (token.word == "\n") {
                    drawLine(lineTokens)
                    lineTokens = mutableListOf()
                    lineWidth = 0f
                    continue
                }
                val f = font(token.bold, token.italic)
                val w = strWidth(token.word, f, token.size)
                if (lineWidth + w > pageWidth - marginRight && lineTokens.isNotEmpty()) {
                    drawLine(lineTokens)
                    lineTokens = mutableListOf()
                    lineWidth = 0f
                }
                lineTokens.add(Triple(token.word, f, token.size))
                lineWidth += w
            }
            if (lineTokens.isNotEmpty()) drawLine(lineTokens)
            y -= if (para.isHeading) 4f else 2f
        }

        cs.close()
        doc.save(outputFile)
        doc.close()
    }
}

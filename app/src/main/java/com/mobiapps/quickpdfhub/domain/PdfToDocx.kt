package com.mobiapps.quickpdfhub.domain

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object PdfToDocx {

    suspend fun execute(context: Context, uri: Uri): WorkResult =
        withContext(Dispatchers.IO) {
            var outputFile: File? = null
            try {
                PDFBoxResourceLoader.init(context.applicationContext)

                val sizeBeforeBytes = context.contentResolver
                    .openFileDescriptor(uri, "r")?.use { it.statSize } ?: 0L

                val displayName = displayNameOf(context, uri)
                val baseName = displayName.removeSuffix(".pdf").removeSuffix(".PDF")
                val outputFileName = "$baseName.docx"
                outputFile = File(context.cacheDir, outputFileName)

                val text = context.contentResolver.openInputStream(uri)?.use { stream ->
                    PDDocument.load(stream).use { doc ->
                        PDFTextStripper().apply { sortByPosition = true }.getText(doc)
                    }
                } ?: throw IllegalStateException("Cannot read PDF")

                writeDocx(text, outputFile)

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
                runCatching { outputFile?.delete() }
                WorkResult.Error(e.message ?: "PDF to DOCX conversion failed.")
            }
        }

    private fun writeDocx(text: String, outputFile: File) {
        ZipOutputStream(outputFile.outputStream().buffered()).use { zip ->
            zip.entry("[Content_Types].xml", CONTENT_TYPES)
            zip.entry("_rels/.rels", RELS)
            zip.entry("word/_rels/document.xml.rels", WORD_RELS)
            zip.entry("word/document.xml", buildDocumentXml(text))
        }
    }

    private fun ZipOutputStream.entry(name: String, content: String) {
        putNextEntry(ZipEntry(name))
        write(content.toByteArray(Charsets.UTF_8))
        closeEntry()
    }

    private fun buildDocumentXml(text: String): String {
        val paras = text
            .split(Regex("\r?\n"))
            .joinToString("") { line ->
                val safe = line
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;")
                    .replace("'", "&apos;")
                "<w:p><w:r><w:t xml:space=\"preserve\">$safe</w:t></w:r></w:p>"
            }
        return """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
            xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <w:body>$paras<w:sectPr/></w:body>
</w:document>"""
    }

    private val CONTENT_TYPES = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml"
    ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
</Types>"""

    private val WORD_RELS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships"/>"""

    private val RELS = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1"
    Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"
    Target="word/document.xml"/>
</Relationships>"""
}

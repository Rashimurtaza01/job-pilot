package com.example.jobpilot.export

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.example.jobpilot.data.model.JobRecord
import com.example.jobpilot.data.model.ResearchTaskState
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class GeneratedExportFile(
    val file: File,
    val format: String,
    val mimeType: String,
    val fileSizeFormatted: String,
    val generatedTimestampUtc: String
)

object CsvReportGenerator {
    fun generateCsv(context: Context, jobs: List<JobRecord>, taskQuery: String): GeneratedExportFile {
        val dir = File(context.cacheDir, "reports").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(dir, "JobPilot_Dossier_${timeStamp}.csv")

        val sb = StringBuilder()
        // Headers
        sb.append("Job ID,Job Title,Company,Sector,Seniority,Work Arrangement,Location,Salary Min,Salary Max,Currency,Pay Period,Salary Disclosed,Published UTC,Relative Age,Discovered UTC,Last Verified UTC,Recruiter Name,Recruiter Title,Recruiter Verification,Recruiter Relationship,Referral Status,Referral URL,Match Score,Missing Fields,Source Provider,Original URL\n")

        jobs.forEach { j ->
            val missing = mutableListOf<String>()
            if (!j.isSalaryDisclosed) missing.add("Salary")
            if (j.recruiter == null) missing.add("Recruiter")
            if (j.publishedAtUtc == null) missing.add("Exact Post Time")

            val row = listOf(
                escapeCsv(j.id),
                escapeCsv(j.title),
                escapeCsv(j.companyName),
                escapeCsv(j.sector),
                escapeCsv(j.seniority.label),
                escapeCsv(j.workArrangement.label),
                escapeCsv(j.locationString),
                j.salaryMin?.toString() ?: "",
                j.salaryMax?.toString() ?: "",
                escapeCsv(j.salaryCurrency),
                escapeCsv(j.salaryPeriod),
                if (j.isSalaryDisclosed) "YES" else "NO",
                escapeCsv(j.publishedAtUtc ?: "Not Disclosed"),
                escapeCsv(j.sourceReportedRelativeAge),
                escapeCsv(j.discoveredAtUtc),
                escapeCsv(j.lastVerifiedAtUtc ?: "Pending"),
                escapeCsv(j.recruiter?.name ?: "None Disclosed"),
                escapeCsv(j.recruiter?.professionalTitle ?: "N/A"),
                escapeCsv(j.recruiter?.verificationState?.badgeText ?: "N/A"),
                escapeCsv(j.recruiter?.relationship?.label ?: "N/A"),
                escapeCsv(j.referralProgram.label),
                escapeCsv(j.referralProgramUrl),
                j.matchScore.toString(),
                escapeCsv(missing.joinToString("; ").ifEmpty { "None" }),
                escapeCsv(j.sourceProvider),
                escapeCsv(j.originalJobUrl)
            )
            sb.append(row.joinToString(",")).append("\n")
        }

        FileOutputStream(file).use { it.write(sb.toString().toByteArray(Charsets.UTF_8)) }

        val sizeFormatted = "${file.length() / 1024 + 1} KB"
        val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return GeneratedExportFile(file, "CSV", "text/csv", sizeFormatted, utcFormat.format(Date()))
    }

    private fun escapeCsv(value: String): String {
        var str = value.replace("\"", "\"\"")
        if (str.contains(",") || str.contains("\n") || str.contains("\"")) {
            str = "\"$str\""
        }
        return str
    }
}

object ExcelXmlReportGenerator {
    fun generateWorkbook(context: Context, jobs: List<JobRecord>, taskState: ResearchTaskState): GeneratedExportFile {
        val dir = File(context.cacheDir, "reports").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(dir, "JobPilot_Executive_Workbook_${timeStamp}.xml")

        val sb = StringBuilder()
        sb.append("""<?xml version="1.0"?>
<?mso-application progid="Excel.Sheet"?>
<Workbook xmlns="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:o="urn:schemas-microsoft-com:office:office"
 xmlns:x="urn:schemas-microsoft-com:office:excel"
 xmlns:ss="urn:schemas-microsoft-com:office:spreadsheet"
 xmlns:html="http://www.w3.org/TR/REC-html40">
 <Styles>
  <Style ss:ID="Default" ss:Name="Normal">
   <Alignment ss:Vertical="Bottom"/>
   <Borders/>
   <Font ss:FontName="Calibri" x:Family="Swiss" ss:Size="11" ss:Color="#000000"/>
  </Style>
  <Style ss:ID="Header">
   <Font ss:FontName="Calibri" x:Family="Swiss" ss:Size="11" ss:Color="#FFFFFF" ss:Bold="1"/>
   <Interior ss:Color="#171717" ss:Pattern="Solid"/>
  </Style>
  <Style ss:ID="Title">
   <Font ss:FontName="Calibri" x:Family="Swiss" ss:Size="16" ss:Color="#000000" ss:Bold="1"/>
  </Style>
  <Style ss:ID="SubTitle">
   <Font ss:FontName="Calibri" x:Family="Swiss" ss:Size="12" ss:Color="#666666" ss:Italic="1"/>
  </Style>
 </Styles>
""").append("\n")

        // Worksheet 1: Executive Summary
        sb.append(""" <Worksheet ss:Name="Executive Summary">
  <Table>
   <Row><Cell ss:StyleID="Title"><Data ss:Type="String">JobPilot Intelligence - Executive Research Dossier</Data></Cell></Row>
   <Row><Cell ss:StyleID="SubTitle"><Data ss:Type="String">Query: ${escapeXml(taskState.query.ifEmpty { "General Technical Vacancies" })}</Data></Cell></Row>
   <Row><Cell><Data ss:Type="String">Generated UTC: ${taskState.timestampUtc}</Data></Cell></Row>
   <Row><Cell><Data ss:Type="String">Total Opportunities Analyzed: ${jobs.size}</Data></Cell></Row>
   <Row><Cell><Data ss:Type="String">Verified Record Count: ${taskState.verifiedCount}</Data></Cell></Row>
   <Row><Cell><Data ss:Type="String">Duplicates Removed: ${taskState.duplicatesRemoved}</Data></Cell></Row>
   <Row><Cell><Data ss:Type="String">Undisclosed Fields Identified: ${taskState.missingFieldsCount}</Data></Cell></Row>
   <Row><Cell><Data ss:Type="String">Sources Queried: ${escapeXml(taskState.sourcesSearched.joinToString(", "))}</Data></Cell></Row>
  </Table>
 </Worksheet>
""")

        // Worksheet 2: Jobs
        sb.append(""" <Worksheet ss:Name="Jobs">
  <Table>
   <Row ss:StyleID="Header">
    <Cell><Data ss:Type="String">Job ID</Data></Cell>
    <Cell><Data ss:Type="String">Title</Data></Cell>
    <Cell><Data ss:Type="String">Company</Data></Cell>
    <Cell><Data ss:Type="String">Seniority</Data></Cell>
    <Cell><Data ss:Type="String">Arrangement</Data></Cell>
    <Cell><Data ss:Type="String">Location</Data></Cell>
    <Cell><Data ss:Type="String">Salary</Data></Cell>
    <Cell><Data ss:Type="String">Published UTC</Data></Cell>
    <Cell><Data ss:Type="String">Match Score</Data></Cell>
    <Cell><Data ss:Type="String">Source</Data></Cell>
    <Cell><Data ss:Type="String">Job URL</Data></Cell>
   </Row>
""")
        jobs.forEach { j ->
            val sal = if (j.isSalaryDisclosed) "${j.salaryCurrency} ${j.salaryMin?.toInt()} - ${j.salaryMax?.toInt()}" else "Not Disclosed"
            sb.append("""   <Row>
    <Cell><Data ss:Type="String">${escapeXml(j.id)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(j.title)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(j.companyName)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(j.seniority.label)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(j.workArrangement.label)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(j.locationString)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(sal)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(j.publishedAtUtc ?: "N/A")}</Data></Cell>
    <Cell><Data ss:Type="Number">${j.matchScore}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(j.sourceProvider)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(j.originalJobUrl)}</Data></Cell>
   </Row>
""")
        }
        sb.append("  </Table>\n </Worksheet>\n")

        // Worksheet 3: Recruiters
        sb.append(""" <Worksheet ss:Name="Recruiters">
  <Table>
   <Row ss:StyleID="Header">
    <Cell><Data ss:Type="String">Job ID</Data></Cell>
    <Cell><Data ss:Type="String">Company</Data></Cell>
    <Cell><Data ss:Type="String">Recruiter Name</Data></Cell>
    <Cell><Data ss:Type="String">Professional Title</Data></Cell>
    <Cell><Data ss:Type="String">Relationship</Data></Cell>
    <Cell><Data ss:Type="String">Verification State</Data></Cell>
    <Cell><Data ss:Type="String">Public Profile URL</Data></Cell>
    <Cell><Data ss:Type="String">Evidence Snippet</Data></Cell>
   </Row>
""")
        jobs.filter { it.recruiter != null }.forEach { j ->
            val r = j.recruiter!!
            sb.append("""   <Row>
    <Cell><Data ss:Type="String">${escapeXml(j.id)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(j.companyName)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(r.name)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(r.professionalTitle)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(r.relationship.label)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(r.verificationState.label)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(r.publicProfileUrl)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(r.evidenceSnippet)}</Data></Cell>
   </Row>
""")
        }
        sb.append("  </Table>\n </Worksheet>\n")

        // Worksheet 4: Companies
        sb.append(""" <Worksheet ss:Name="Companies">
  <Table>
   <Row ss:StyleID="Header">
    <Cell><Data ss:Type="String">Company Name</Data></Cell>
    <Cell><Data ss:Type="String">Official Website</Data></Cell>
    <Cell><Data ss:Type="String">Sector</Data></Cell>
    <Cell><Data ss:Type="String">Headquarters</Data></Cell>
    <Cell><Data ss:Type="String">Referral Status</Data></Cell>
    <Cell><Data ss:Type="String">Careers URL</Data></Cell>
   </Row>
""")
        jobs.mapNotNull { it.company }.distinctBy { it.name }.forEach { c ->
            sb.append("""   <Row>
    <Cell><Data ss:Type="String">${escapeXml(c.name)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(c.officialWebsite)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(c.sector)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(c.headquarters)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(c.referralStatus.label)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(c.careersPageUrl)}</Data></Cell>
   </Row>
""")
        }
        sb.append("  </Table>\n </Worksheet>\n")

        // Worksheet 5: Referral Programmes
        sb.append(""" <Worksheet ss:Name="Referral Programmes">
  <Table>
   <Row ss:StyleID="Header">
    <Cell><Data ss:Type="String">Company</Data></Cell>
    <Cell><Data ss:Type="String">Referral Program Status</Data></Cell>
    <Cell><Data ss:Type="String">Program URL</Data></Cell>
    <Cell><Data ss:Type="String">Policy Instructions</Data></Cell>
   </Row>
""")
        jobs.distinctBy { it.companyName }.forEach { j ->
            sb.append("""   <Row>
    <Cell><Data ss:Type="String">${escapeXml(j.companyName)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(j.referralProgram.label)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(j.referralProgramUrl.ifEmpty { "N/A" })}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(j.company?.referralInstructions ?: "No public instructions disclosed")}</Data></Cell>
   </Row>
""")
        }
        sb.append("  </Table>\n </Worksheet>\n")

        // Worksheet 6: Source Evidence
        sb.append(""" <Worksheet ss:Name="Source Evidence">
  <Table>
   <Row ss:StyleID="Header">
    <Cell><Data ss:Type="String">Job ID</Data></Cell>
    <Cell><Data ss:Type="String">Field Name</Data></Cell>
    <Cell><Data ss:Type="String">Extracted Value</Data></Cell>
    <Cell><Data ss:Type="String">Verification State</Data></Cell>
    <Cell><Data ss:Type="String">Source URL</Data></Cell>
   </Row>
""")
        jobs.forEach { j ->
            j.fieldEvidence.values.forEach { ev ->
                sb.append("""   <Row>
    <Cell><Data ss:Type="String">${escapeXml(j.id)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(ev.fieldName)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(ev.extractedValue)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(ev.verificationState.label)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(ev.sourceUrl)}</Data></Cell>
   </Row>
""")
            }
        }
        sb.append("  </Table>\n </Worksheet>\n")

        // Worksheet 7: Search Parameters
        sb.append(""" <Worksheet ss:Name="Search Parameters">
  <Table>
   <Row ss:StyleID="Header"><Cell><Data ss:Type="String">Parameter</Data></Cell><Cell><Data ss:Type="String">Configured Value</Data></Cell></Row>
   <Row><Cell><Data ss:Type="String">Query String</Data></Cell><Cell><Data ss:Type="String">${escapeXml(taskState.query)}</Data></Cell></Row>
   <Row><Cell><Data ss:Type="String">Run Timestamp UTC</Data></Cell><Cell><Data ss:Type="String">${taskState.timestampUtc}</Data></Cell></Row>
   <Row><Cell><Data ss:Type="String">Execution Stage</Data></Cell><Cell><Data ss:Type="String">${taskState.stage.title}</Data></Cell></Row>
  </Table>
 </Worksheet>
""")

        // Worksheet 8: Data Quality and Missing Fields
        sb.append(""" <Worksheet ss:Name="Data Quality">
  <Table>
   <Row ss:StyleID="Header">
    <Cell><Data ss:Type="String">Job ID</Data></Cell>
    <Cell><Data ss:Type="String">Company</Data></Cell>
    <Cell><Data ss:Type="String">Salary Status</Data></Cell>
    <Cell><Data ss:Type="String">Recruiter Status</Data></Cell>
    <Cell><Data ss:Type="String">Timestamp Precision</Data></Cell>
   </Row>
""")
        jobs.forEach { j ->
            sb.append("""   <Row>
    <Cell><Data ss:Type="String">${escapeXml(j.id)}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(j.companyName)}</Data></Cell>
    <Cell><Data ss:Type="String">${if (j.isSalaryDisclosed) "Disclosed" else "UNDISCLOSED"}</Data></Cell>
    <Cell><Data ss:Type="String">${if (j.recruiter != null) "Assigned" else "UNDISCLOSED"}</Data></Cell>
    <Cell><Data ss:Type="String">${escapeXml(j.timestampPrecision)}</Data></Cell>
   </Row>
""")
        }
        sb.append("  </Table>\n </Worksheet>\n")

        sb.append("</Workbook>\n")

        FileOutputStream(file).use { it.write(sb.toString().toByteArray(Charsets.UTF_8)) }

        val sizeFormatted = "${file.length() / 1024 + 1} KB"
        val utcFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return GeneratedExportFile(file, "Excel XML", "application/vnd.ms-excel", sizeFormatted, utcFormat.format(Date()))
    }

    private fun escapeXml(str: String): String {
        return str.replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }
}

object PdfReportGenerator {
    fun generatePdf(context: Context, jobs: List<JobRecord>, taskQuery: String): GeneratedExportFile {
        val dir = File(context.cacheDir, "reports").apply { mkdirs() }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val file = File(dir, "JobPilot_Executive_Report_${timeStamp}.pdf")

        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val paintTitle = Paint().apply {
            color = Color.BLACK
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintSubtitle = Paint().apply {
            color = Color.DKGRAY
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paintHeader = Paint().apply {
            color = Color.BLACK
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintBody = Paint().apply {
            color = Color.BLACK
            textSize = 9f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paintMuted = Paint().apply {
            color = Color.GRAY
            textSize = 8f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paintLine = Paint().apply {
            color = Color.LTGRAY
            strokeWidth = 1f
        }

        var y = 40f

        // Document Header
        canvas.drawText("JOBPILOT INTELLIGENCE", 40f, y, paintTitle)
        y += 18f
        canvas.drawText("EXECUTIVE RESEARCH DOSSIER & VERIFIED OPPORTUNITY AUDIT", 40f, y, paintSubtitle)
        y += 10f
        canvas.drawLine(40f, y, 555f, y, paintLine)
        y += 20f

        // Metadata box
        val utcFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss 'UTC'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        canvas.drawText("Target Query: ${taskQuery.ifEmpty { "Global Technical Vacancies" }.take(60)}", 40f, y, paintBody)
        y += 14f
        canvas.drawText("Generated: ${utcFormat.format(Date())} | Analyzed: ${jobs.size} vacancies", 40f, y, paintBody)
        y += 14f
        canvas.drawText("Audited Sources: RemoteOK Public API, Global Corporate Intelligence Feed", 40f, y, paintBody)
        y += 18f
        canvas.drawLine(40f, y, 555f, y, paintLine)
        y += 20f

        canvas.drawText("TOP VERIFIED REQUISITIONS", 40f, y, paintHeader)
        y += 16f

        // Render first 5 job entries cleanly
        for ((idx, job) in jobs.take(5).withIndex()) {
            canvas.drawText("${idx + 1}. ${job.title} — ${job.companyName}", 40f, y, paintHeader)
            y += 12f
            val sal = if (job.isSalaryDisclosed) "${job.salaryCurrency} ${job.salaryMin?.toInt()} - ${job.salaryMax?.toInt()}" else "Salary: Not Disclosed"
            canvas.drawText("Arrangement: ${job.workArrangement.label} | ${job.locationString} | $sal", 50f, y, paintBody)
            y += 11f
            val rec = job.recruiter?.let { "${it.name} (${it.professionalTitle}) - [${it.verificationState.badgeText}]" } ?: "Recruiter: No public contact disclosed"
            canvas.drawText("Recruiter: $rec", 50f, y, paintBody)
            y += 11f
            val ref = "Referral: ${job.referralProgram.label}"
            canvas.drawText(ref, 50f, y, paintBody)
            y += 11f
            canvas.drawText("Source: ${job.sourceProvider} | URL: ${job.originalJobUrl.take(65)}", 50f, y, paintMuted)
            y += 16f
            canvas.drawLine(40f, y, 555f, y, paintLine)
            y += 16f
        }

        // Footer
        canvas.drawText("JobPilot Intelligence • Cryptographic Provenance & Transparent Research • Confidential", 40f, 800f, paintMuted)

        pdfDocument.finishPage(page)

        FileOutputStream(file).use { pdfDocument.writeTo(it) }
        pdfDocument.close()

        val sizeFormatted = "${file.length() / 1024 + 1} KB"
        return GeneratedExportFile(file, "PDF", "application/pdf", sizeFormatted, utcFormat.format(Date()))
    }
}

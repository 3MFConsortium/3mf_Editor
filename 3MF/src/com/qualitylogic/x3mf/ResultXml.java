package com.qualitylogic.x3mf;

import org.apache.commons.lang3.StringEscapeUtils;

/** Last-resort report when an internal diagnostic fragment is malformed. */
final class ResultXml {
    private ResultXml() { }

    static String fallbackAggregate(String dateTime, String original) {
        String report = fallback("", dateTime, original);
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><TestResults dateTime=\""
                + StringEscapeUtils.escapeXml10(dateTime) + "\">"
                + report.substring(report.indexOf("<TestResult ")) + "</TestResults>";
    }

    static String fallback(String file, String dateTime, String original) {
        // Fail explicitly: a serialization error must never look like a valid model.
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<TestResult file=\"" + StringEscapeUtils.escapeXml10(file)
                + "\" dateTime=\"" + StringEscapeUtils.escapeXml10(dateTime) + "\" result=\"Fail\">"
                + "<PrimaryIssue issueID=\"ER_REPORT_XML\" severity=\"critical\" class=\"report\" type=\"serialization error\">"
                + "Could not serialize validation diagnostics. Original report: "
                + StringEscapeUtils.escapeXml10(original)
                + "</PrimaryIssue></TestResult>";
    }
}

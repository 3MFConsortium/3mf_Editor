package com.qualitylogic.x3mf;

import java.io.StringReader;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public final class ResultXmlTest {
    public static void main(String[] args) throws Exception {
        String file = "a & b ' \".3mf";
        String original = "<Issue><broken & diagnostic\u0001";
        Document doc = parse(ResultXml.fallback(file, "date & time", original));
        require(file.equals(doc.getDocumentElement().getAttribute("file")), "filename round trip");
        require("Fail".equals(doc.getDocumentElement().getAttribute("result")), "explicit failure");
        require("ER_REPORT_XML".equals(doc.getElementsByTagName("PrimaryIssue").item(0)
                .getAttributes().getNamedItem("issueID").getNodeValue()), "fallback diagnostic");
        require(doc.getElementsByTagName("PrimaryIssue").item(0).getTextContent()
                .endsWith(original.replace("\u0001", "")), "escaped original report");
        doc = parse(ResultXml.fallbackAggregate("date & time", original));
        require("TestResults".equals(doc.getDocumentElement().getNodeName()), "aggregate root");
        require(doc.getElementsByTagName("TestResult").getLength() == 1, "aggregate failure child");
        System.out.println("6 report fallback checks passed");
    }

    private static Document parse(String xml) throws Exception {
        return SafeXml.factory().newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}

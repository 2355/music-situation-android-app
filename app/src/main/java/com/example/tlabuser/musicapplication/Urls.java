package com.example.tlabuser.musicapplication;

/**
 * Created by tlabuser on 2017/12/27.
 */

public class Urls {
    public static final String HEAD = "https://musicmetadata.herokuapp.com/ds/query?query=";
    public static final String TAIL = "&output=json&stylesheet=/xml-to-html.xsl";
    public static final String SELECT_SITUATIONS =
            "prefix situation: <http://music.metadata.database.situation/> \n" +
                    "\n" +
                    "SELECT distinct ?tag\n" +
                    "WHERE {\n" +
                    "  ?b situation:tag ?tag;\n" +
                    "      situation:weight ?weight.\n" +
                    "}\n" +
                    "order by (?tag)";

    public static final String SELECT_TRACKS =
            "prefix dc: <http://purl.org/dc/elements/1.1/> \n" +
                    "prefix foaf: <http://xmlns.com/foaf/0.1/> \n" +
                    "prefix situation: <http://music.metadata.database.situation/> \n" +
                    "prefix tag: <http://music.metadata.database.tag/>\n" +
                    "\n" +
                    "SELECT ?artist ?title ?tag ?weight\n" +
                    "WHERE {\n" +
                    "  ?s foaf:maker ?artist;\n" +
                    "     dc:title ?title;\n" +
                    "     situation:blank ?b.\n" +
                    "  \n" +
                    "  ?b situation:tag ?tag;\n" +
                    "      situation:weight ?weight.\n" +
                    "\n" +
                    "FILTER( ?tag = tag:%s ) \n" +
                    "}\n" +
                    "order by desc(?weight)";
}

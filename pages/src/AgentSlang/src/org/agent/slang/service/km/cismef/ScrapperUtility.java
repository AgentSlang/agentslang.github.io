/*
 * Copyright (c) Ovidiu Serban, ovidiu@roboslang.org
 *               web:http://ovidiu.roboslang.org/
 * All Rights Reserved. Use is subject to license terms.
 *
 * This file is part of AgentSlang Project (http://agent.roboslang.org/).
 *
 * AgentSlang is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 of the License and CECILL-B.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * The CECILL-B license file should be a part of this project. If not,
 * it could be obtained at  <http://www.cecill.info/>.
 *
 * The usage of this project makes mandatory the authors citation in
 * any scientific publication or technical reports. For websites or
 * research projects the AgentSlang website and logo needs to be linked
 * in a visible area.
 */

package org.agent.slang.service.km.cismef;

import org.agent.slang.service.km.cismef.data.Concept;
import org.agent.slang.service.km.cismef.data.RelatedConcept;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Ovidiu Serban, ovidiu@roboslang.org
 * @version 1, 2/19/13
 */
public class ScrapperUtility {
    private final static HttpClient client = HttpClientBuilder.create().build();

    private final List<Concept> conceptList = new LinkedList<Concept>();

    private File cachePath;

    public ScrapperUtility(File cachePath) throws IOException {
        this.cachePath = cachePath;
        setupCache();
    }

    private void setupCache() throws IOException {
        if (!cachePath.exists()) {
            if (!cachePath.mkdirs()) {
                throw new IOException("Unable to setup the cache path:" + cachePath.getAbsolutePath());
            }
        }
    }

    public List<Concept> scrapConcept(String concept) throws IOException {
        conceptList.clear();

        File conceptFile = new File(cachePath, concept + ".xml");
        if (!conceptFile.exists()) {
            retrieveFile(conceptFile, concept);
        }

        try {
            InputStream inputStream = new FileInputStream(conceptFile);
            parseModel(inputStream);
        } catch (FileNotFoundException e) {
            throw new IOException(e);
        }

        return conceptList;
    }

    private void retrieveFile(File conceptFile, String concept) {
        HttpGet httpGet = new HttpGet("http://pts.chu-rouen.fr/recherche.html?motRecherche=" + concept);
        try {
            HttpResponse response = client.execute(httpGet);
            if (response.getStatusLine().getStatusCode() == 200) {
                FileOutputStream fileOutputStream = new FileOutputStream(conceptFile);
                fileOutputStream.write(EntityUtils.toByteArray(response.getEntity()));
                fileOutputStream.close();
            }
        } catch (IOException exception) {
            //-- ignore
        } finally {
            httpGet.releaseConnection();
        }
    }

    private static final String TAG_RDF = "rdf:RDF";
    private static final String TAG_CONCEPT = "skos:Concept";

    private void parseModel(InputStream inputStream) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputStream);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName(TAG_RDF);
            for (int i = 0; i < nList.getLength(); i++) {
                if (nList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                    Element rdfElement = (Element) nList.item(i);

                    NodeList concepts = rdfElement.getElementsByTagName(TAG_CONCEPT);
                    for (int p = 0; p < concepts.getLength(); p++) {
                        if (concepts.item(p).getNodeType() == Node.ELEMENT_NODE && concepts.item(p).getParentNode().equals(rdfElement)) {
                            Element conceptElement = (Element) concepts.item(p);
                            parseConcept(conceptElement);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    <skos:Concept rdf:about="http://www.chu-rouen.fr/cismef#MSH_D_056151">
        <cismef:motCle>true</cismef:motCle>
        <cismef:conceptType rdf:parseType="Resource">
            <cismef:label>MeSHdescripteur</cismef:label>
            <cismef:nom>Descripteur MeSH</cismef:nom>
        </cismef:conceptType>
        <skos:prefLabel xml:lang="fr">remodelage des voies aériennes</skos:prefLabel>
        <skos:prefLabel xml:lang="en">airway remodeling</skos:prefLabel>
    </skos:Concept>
     */

    private void parseConcept(Element conceptElement) {
        Concept concept = new Concept();
        concept.setPrincipal(isPrincipal(conceptElement));
        concept.setKeyWord(isKeyWord(conceptElement));
        concept.setConceptType(getConceptType(conceptElement));
        concept.setConceptLabels(extractLabels(conceptElement));

        extractRelatedConcept(concept, conceptElement);

        conceptList.add(concept);
    }


    private static final String TAG_PRINCIPAL = "cismef:conceptPrincipal";

    private static boolean isPrincipal(Element conceptElement) {
        NodeList principalList = conceptElement.getElementsByTagName(TAG_PRINCIPAL);
        for (int i = 0; i < principalList.getLength(); i++) {
            if (principalList.item(i).getNodeType() == Node.ELEMENT_NODE && principalList.item(i).getParentNode().equals(conceptElement)) {
                if (principalList.item(i).getTextContent().trim().equals("true")) {
                    return true;
                }
            }
        }
        return false;
    }


    /*
        <cismef:conceptType rdf:parseType="Resource">
            <cismef:label>MeSHdescripteur</cismef:label>
            <cismef:nom>Descripteur MeSH</cismef:nom>
        </cismef:conceptType>
     */

    private static final String TAG_CONCEPT_TYPE = "cismef:conceptType";
    private static final String TAG_CONCEPT_TYPE_LABEL = "cismef:label";

    private static String getConceptType(Element conceptElement) {
        NodeList conceptTypeList = conceptElement.getElementsByTagName(TAG_CONCEPT_TYPE);
        for (int i = 0; i < conceptTypeList.getLength(); i++) {
            if (conceptTypeList.item(i).getNodeType() == Node.ELEMENT_NODE && conceptTypeList.item(i).getParentNode().equals(conceptElement)) {
                NodeList conceptTypeLabelList = ((Element) conceptTypeList.item(i)).getElementsByTagName(TAG_CONCEPT_TYPE_LABEL);
                for (int j = 0; j < conceptTypeLabelList.getLength(); j++) {
                    if (conceptTypeLabelList.item(j).getNodeType() == Node.ELEMENT_NODE && conceptTypeLabelList.item(j).getParentNode().equals(conceptTypeList.item(i))) {
                        return conceptTypeLabelList.item(j).getTextContent().trim();
                    }
                }
            }
        }
        return "";
    }

    /*
        <cismef:motCle>true</cismef:motCle>
     */

    private static final String TAG_KEYWORD = "cismef:motCle";

    private static boolean isKeyWord(Element conceptElement) {
        NodeList keywordList = conceptElement.getElementsByTagName(TAG_KEYWORD);
        for (int i = 0; i < keywordList.getLength(); i++) {
            if (keywordList.item(i).getNodeType() == Node.ELEMENT_NODE && keywordList.item(i).getParentNode().equals(conceptElement)) {
                if (keywordList.item(i).getTextContent().trim().equals("true")) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
        <skos:prefLabel xml:lang="fr">remodelage des voies aériennes</skos:prefLabel>
        <skos:prefLabel xml:lang="en">airway remodeling</skos:prefLabel>
     */
    private static final String TAG_LABEL_PREF = "skos:prefLabel";
    private static final String ATT_LANG = "xml:lang";

    private static Map<String, String> extractLabels(Element conceptElement) {
        Map<String, String> result = new HashMap<String, String>();

        NodeList labelList = conceptElement.getElementsByTagName(TAG_LABEL_PREF);
        for (int i = 0; i < labelList.getLength(); i++) {
            if (labelList.item(i).getNodeType() == Node.ELEMENT_NODE && labelList.item(i).getParentNode().equals(conceptElement)) {
                String lang = ((Element) labelList.item(i)).getAttribute(ATT_LANG);
                String label = labelList.item(i).getTextContent().trim();
                result.put(lang, label);
            }
        }

        return result;
    }

    /*
        <skos:altLabel rdf:parseType="Resource">
            <cismef:type>MeSHsynonyme</cismef:type>
            <cismef:typeLabel>Synonyme MeSH</cismef:typeLabel>
            <cismef:value xml:lang="en">bronchial asthma</cismef:value>
        </skos:altLabel>

        <skos:definition rdf:parseType="Resource">
            <cismef:value xml:lang="en">A form of bronchial disorder with three distinct components: airway
                hyper-responsiveness (RESPIRATORY HYPERSENSITIVITY), airway INFLAMMATION, and intermittent AIRWAY
                OBSTRUCTION. It is characterized by spasmodic contraction of airway smooth muscle, WHEEZING, and dyspnea
                (DYSPNEA, PAROXYSMAL).
            </cismef:value>
            <cismef:type>Définition MeSH</cismef:type>
        </skos:definition>
     */

    private static final String TAG_ALT_LABEL = "skos:altLabel";
    private static final String TAG_DEFINITION = "skos:definition";

    private static void extractRelatedConcept(Concept concept, Element conceptElement) {
        NodeList labelList = conceptElement.getElementsByTagName(TAG_ALT_LABEL);
        for (int i = 0; i < labelList.getLength(); i++) {
            if (labelList.item(i).getNodeType() == Node.ELEMENT_NODE && labelList.item(i).getParentNode().equals(conceptElement)) {
                concept.addRelatedConcepts(extractRelatedConceptItem((Element) labelList.item(i), "synonym"));
            }
        }

        labelList = conceptElement.getElementsByTagName(TAG_DEFINITION);
        for (int i = 0; i < labelList.getLength(); i++) {
            if (labelList.item(i).getNodeType() == Node.ELEMENT_NODE && labelList.item(i).getParentNode().equals(conceptElement)) {
                concept.addRelatedConcepts(extractRelatedConceptItem((Element) labelList.item(i), "definition"));
            }
        }
    }

    private static final String TAG_CIS_TYPE = "cismef:type";
    private static final String TAG_CIS_VALUE = "cismef:value";

    private static RelatedConcept extractRelatedConceptItem(Element altElement, String type) {
        RelatedConcept relatedConcept = new RelatedConcept(type);

        NodeList itemElement = altElement.getElementsByTagName(TAG_CIS_TYPE);
        for (int i = 0; i < itemElement.getLength(); i++) {
            if (itemElement.item(i).getNodeType() == Node.ELEMENT_NODE && itemElement.item(i).getParentNode().equals(altElement)) {
                relatedConcept.setSource(itemElement.item(i).getTextContent().trim());
            }
        }

        itemElement = altElement.getElementsByTagName(TAG_CIS_VALUE);
        for (int i = 0; i < itemElement.getLength(); i++) {
            if (itemElement.item(i).getNodeType() == Node.ELEMENT_NODE && itemElement.item(i).getParentNode().equals(altElement)) {
                relatedConcept.setValue(((Element) itemElement.item(i)).getAttribute(ATT_LANG), itemElement.item(i).getTextContent().trim());
            }
        }

        return relatedConcept;
    }

    private static void printScrappedConcept(ScrapperUtility scrapperUtility, String concept) throws IOException {
        System.out.println();
        System.out.println("Scrap concept: " + concept);
        for (Concept item : scrapperUtility.scrapConcept(concept)) {
            System.out.println(item.toString());
        }
    }

    public static void main(String[] args) {
        try {
            ScrapperUtility scrapperUtility = new ScrapperUtility(new File("cismef-cache"));
            printScrappedConcept(scrapperUtility, "asthme");
            printScrappedConcept(scrapperUtility, "paludisme");
            printScrappedConcept(scrapperUtility, "parasite");
            printScrappedConcept(scrapperUtility, "fever");
            printScrappedConcept(scrapperUtility, "xkcd");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

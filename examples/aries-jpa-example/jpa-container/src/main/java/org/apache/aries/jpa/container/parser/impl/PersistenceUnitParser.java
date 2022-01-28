/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.aries.jpa.container.parser.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This helper can be used to locate persistence.xml files in a bundle
 */
public class PersistenceUnitParser {
    private static final String DEFAULT_PERSISTENCE_LOCATION = "META-INF/persistence.xml";
    private static final Logger LOG = LoggerFactory.getLogger("org.apache.aries.jpa.container");
    public static final String PERSISTENCE_UNIT_HEADER = "Meta-Persistence";
    
    private PersistenceUnitParser() {
    }

    /**
     * This method locates persistence descriptor files based on a combination of the default location
     * "META-INF/persistence.xml" and the Meta-Persistence header. Note that getEntry is used to ensure we do
     * not alter the state of the bundle Note also that web application bundles will never return persistence
     * descriptors
     * @param context 
     * 
     * @param bundle The bundle to search
     * @param packageAdmin 
     * @return
     */
    public static Collection<PersistenceUnit> getPersistenceUnits(Bundle bundle) {
        Collection<PersistenceUnit> punits = new ArrayList<PersistenceUnit>();
        Dictionary<String, String> headers = bundle.getHeaders();
        String metaPersistence = headers.get(PERSISTENCE_UNIT_HEADER);

        Set<String> locations = new HashSet<String>();
        if (metaPersistence == null) {
            return punits;
        }

        if (!metaPersistence.isEmpty()) {
            // Split apart the header to get the individual entries
            for (String s : metaPersistence.split(",")) {
                locations.add(s.trim());
            }
        }
        
        if (!locations.contains(DEFAULT_PERSISTENCE_LOCATION)) {
            locations.add(DEFAULT_PERSISTENCE_LOCATION);
        }

        // Find the file and add it to our list
        for (String location : locations) {
            try {
                InputStream is = locateFile(bundle, location);
                if (is != null) {
                    parse(bundle, is, punits);
                }
            } catch (Exception e) {
                LOG.error("exception.while.locating.descriptor", e);
                return Collections.emptySet();
            }
        }
        
        return punits;
    }

    private static void parse(Bundle bundle, InputStream is, Collection<PersistenceUnit> punits) {
        SAXParserFactory parserFactory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = parserFactory.newSAXParser();
            JPAHandler handler = new JPAHandler(bundle);
            parser.parse(is, handler);
            punits.addAll(handler.getPersistenceUnits());
        } catch (Exception e) {
            throw new RuntimeException("Error parsing persistence unit in bundle " + bundle.getSymbolicName(), e); // NOSONAR
        } finally {
            safeClose(is);
        }
    }

    private static void safeClose(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                LOG.debug("Exception while closing", e);
            }
        }
    }

    /**
     * Locate a persistence descriptor file in a bundle based on a String name.
     * 
     * @param bundle
     * @param persistenceXmlFiles
     * @param jarLocation
     * @throws IOException
     */
    private static InputStream locateFile(Bundle bundle, String location) throws IOException {
        // There is nothing for an empty location
        if ("".equals(location)) {
            return null;
        }

        // If there is a '!' then we have to look in a jar
        int bangIndex = location.indexOf('!');
        if (bangIndex == -1) {
            URL url = bundle.getEntry(location);

            if (url != null) {
                return url.openStream();
            }
        } else {
            URL url = bundle.getEntry(location.substring(0, bangIndex));
            if (url != null) {
                String toLocate = location.substring(bangIndex + 2);
                return locateInJar(url, toLocate);
            }
        }
        return null;
    }

    private static InputStream locateInJar(URL url, String toLocate) throws IOException {
        JarInputStream jis = new JarInputStream(url.openStream());
        JarEntry entry = jis.getNextJarEntry();

        while (entry != null) {
            if (entry.getName().equals(toLocate)) {
                return jis;
            }
            entry = jis.getNextJarEntry();
        }
        return null;
    }
}

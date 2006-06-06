/*
 * Copyright (c)2004-2006 Mark Logic Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * The use of the Apache License does not indicate that this project is
 * affiliated with the Apache Software Foundation.
 */
package com.marklogic.ps.xqsync;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.marklogic.ps.Connection;
import com.marklogic.ps.SimpleLogger;
import com.marklogic.ps.Utilities;
import com.marklogic.xdbc.XDBCException;
import com.marklogic.xdbc.XDBCResultSequence;

/**
 * @author Michael Blakeley <michael.blakeley@marklogic.com>
 *
 */
public class XQSyncXMLSizer {

    private static SimpleLogger logger = SimpleLogger.getSimpleLogger();

    public static void main(String[] args) throws XDBCException, IOException {
        final String NAME = XQSyncXMLSizer.class.getName();

        Properties props = new java.util.Properties();

        logger.configureLogger(props);
        logger.info("default encoding is "
                + System.getProperty("file.encoding"));
        System.setProperty("file.encoding", props.getProperty(
                "DEFAULT_CHARSET", "UTF-8"));
        logger.info("default encoding is now "
                + System.getProperty("file.encoding"));

        long start = System.currentTimeMillis();

        String collectionName = "BOB2Test";
        long size = getCollectionSize("ovid:ovid@cheddar.ut.ovid.com:9002",
                collectionName);
        System.out.println("collection " + collectionName + ": " + size + " Bytes");

        logger.info("finished in " + (System.currentTimeMillis() - start)
                + " ms");
    }

    private static long getCollectionSize(String _connectionString,
            String _collectionName) throws XDBCException, IOException {
        long size = 0;
        long count = 0;
        logger.info("querying " + _collectionName + " on " + _connectionString);
        Connection conn = new Connection(_connectionString);
        String query = "define variable $uri as xs:string external\n"
                + "collection($uri)";
        Map externs = new HashMap(1);
        externs.put("uri", _collectionName);

        XDBCResultSequence rs = conn.executeQuery(query, externs);
        InputStream is;
        Reader r;

        while (rs.hasNext()) {
            rs.next();
            logger.info("count = " + count + ", size = " + size + ", avg = " + (count > 0 ? size / count : 0));
            if (rs.getItemType() == XDBCResultSequence.XDBC_Binary) {
                is = rs.getInputStream();
                size += Utilities.getSize(is);
                is.close();
            } else {
                r = rs.getReader();
                size += Utilities.getSize(r);
                r.close();
            }
            count++;
        }

        rs.close();
        return size;
    }
}
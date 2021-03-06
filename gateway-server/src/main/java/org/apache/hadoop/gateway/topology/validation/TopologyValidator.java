/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.hadoop.gateway.topology.validation;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.apache.hadoop.gateway.topology.Topology;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class TopologyValidator {

  private Collection<String> errors = null;
  private final String filePath;

  public TopologyValidator(Topology t){
    filePath = t.getUri().getPath();
  }

  public TopologyValidator(String path){
    this.filePath = path;
  }

  public TopologyValidator(URL file){
    filePath = file.getPath();
  }

  public boolean validateTopology() {
    errors = new LinkedList<String>();
    try {
      SchemaFactory fact = SchemaFactory
          .newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
      URL schemaUrl = ClassLoader.getSystemResource( "conf/topology-v1.xsd" );
      Schema s = fact.newSchema( schemaUrl );
      Validator validator = s.newValidator();
      final List<SAXParseException> exceptions = new LinkedList<>();
      validator.setErrorHandler(new ErrorHandler() {
        public void warning(SAXParseException exception) throws SAXException {
          exceptions.add(exception);
        }

        public void fatalError(SAXParseException exception) throws SAXException {
          exceptions.add(exception);
        }

        public void error(SAXParseException exception) throws SAXException {
          exceptions.add(exception);
        }
      });

      File xml = new File(filePath);
      validator.validate(new StreamSource(xml));
      if(exceptions.size() > 0) {
        for (SAXParseException e : exceptions) {
          errors.add("Line: " + e.getLineNumber() + " -- " + e.getMessage());
        }
        return false;
      } else {
        return true;
      }

    } catch (IOException e) {
      errors.add("Error reading topology file");
      errors.add(e.getMessage());
      return false;
    } catch (SAXException e) {
      errors.add("There was a fatal error in parsing the xml file.");
      errors.add(e.getMessage());
      return false;
    } catch (NullPointerException n) {
      errors.add("Error retrieving schema from ClassLoader");
      return false;
    }
  }

  public Collection<String> getTopologyErrors(){

    if(errors != null){
      return errors;
    }else{
      validateTopology();
      return errors;
    }
  }

  public String getErrorString(){
    StringBuilder out = new StringBuilder();
    out.append("");
    for(String s : getTopologyErrors()){
      out.append(s + "\n");
    }
    return out.toString();
  }

}

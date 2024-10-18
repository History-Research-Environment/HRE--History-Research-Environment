package hre.dbla;
/*********************************************************************
 * RequestListener processes connects to RequestManager over network.
 * Receives requests from RequestManager over data network
 * Transfer requests for data over Database Layer API
 * ********************************************************************
 * v0.00.0016 2019-12-20 - First version (N. Tolleshaug)
 * ********************************************************************
 * NOTE under construction
 * ********************************************************************
 */

import java.io.StringWriter;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import hre.gui.HGlobal;
/**
 * RequestListener is connected to RequestManager over a data network
 * and handles communication between BusinessLayer and DatabaseLayer
 * for HRE clients that need to communicate with a HRE database on a remote server.
 * @see document
 * @author Nils Tolleshaug
 * @version 0.00.0016 2019-12-20
 */
public class HDRequestListener {

	public HDRequestListener() {
		if (HGlobal.DEBUG) System.out.println("Initiate RequestListener");
	}

/**
 * ResSetToXML Convert ResultSet to DOM and generate XML stream
 * @param resSet pointer to ResultSet
 * @return StringWriter - Serial data stream for transfer over data network
 * @throws HDException
 */
       public static StringWriter ResSetToXML(ResultSet resSet) throws HDException {

           DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
           DocumentBuilder builder;

		try {
			builder = factory.newDocumentBuilder();

           Document doc = builder.newDocument();
           Element results = doc.createElement("Results");
           doc.appendChild(results);

           ResultSetMetaData rsmd;
           rsmd = resSet.getMetaData();

           int colCount;
           colCount = rsmd.getColumnCount();
           resSet.beforeFirst(); // Reset to first index

           while (resSet.next()) {
        	   Element row = doc.createElement("Row");
        	   results.appendChild(row);
        	   for (int i = 1; i <= colCount; i++) {
        		   String columnName = rsmd.getColumnName(i);
        		   Object value = resSet.getObject(i);
        		   Element node = doc.createElement(columnName);
        		   node.appendChild(doc.createTextNode(value.toString()));
        		   row.appendChild(node);
        	   }
           }

           DOMSource domSource = new DOMSource(doc);
           TransformerFactory tf = TransformerFactory.newInstance();
           Transformer transformer;
           transformer = tf.newTransformer();
           transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
           transformer.setOutputProperty(OutputKeys.METHOD, "xml");
           transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
           StringWriter sw = new StringWriter();
           StreamResult sr = new StreamResult(sw);
           transformer.transform(domSource, sr);

           if (HGlobal.DEBUG) System.out.println(sw.toString());
           return sw;

		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			throw new HDException();
		}  catch (SQLException e) {
			// TODO Auto-generated catch block
			throw new HDException();
		} catch (TransformerConfigurationException e) {
			// TODO Auto-generated catch block
			throw new HDException();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			throw new HDException();
		}

    }

} // End class RequestListener

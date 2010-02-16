package pt.webdetails.cda.dataaccess;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.AbstractNamedMDXDataFactory;
import org.pentaho.reporting.engine.classic.extensions.datasources.olap4j.BandedMDXDataFactory;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.connections.olap4j.Olap4JConnection;
import pt.webdetails.cda.settings.UnknownConnectionException;

/**
 * Implementation of a DataAccess that will get data from a SQL database
 * <p/>
 * User: pedro
 * Date: Feb 3, 2010
 * Time: 12:18:05 PM
 */
public class Olap4JDataAccess extends PREDataAccess
{

  private static final Log logger = LogFactory.getLog(Olap4JDataAccess.class);

  public Olap4JDataAccess(final Element element)
  {
    super(element);
  }

  protected AbstractNamedMDXDataFactory createDataFactory() throws UnknownConnectionException, InvalidConnectionException
  {
    final Olap4JConnection connection = (Olap4JConnection) getCdaSettings().getConnection(getConnectionId());
    return new BandedMDXDataFactory(connection.getInitializedConnectionProvider());
  }

  @Override
  public DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException
  {

    logger.debug("Creating BandedMDXDataFactory");


    final AbstractNamedMDXDataFactory mdxDataFactory = createDataFactory();
    mdxDataFactory.setQuery("query", getQuery());

    return mdxDataFactory;


  }

  public String getType()
  {
    return "olap4J";
  }
}

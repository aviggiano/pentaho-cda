package pt.webdetails.cda.dataaccess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.DataFactory;
import org.pentaho.reporting.engine.classic.core.ParameterDataRow;
import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.states.CachingDataFactory;
import org.pentaho.reporting.engine.classic.core.util.CloseableTableModel;
import org.pentaho.reporting.engine.classic.core.util.LibLoaderResourceBundleFactory;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;
import org.pentaho.reporting.libraries.resourceloader.ResourceManager;
import pt.webdetails.cda.connections.InvalidConnectionException;
import pt.webdetails.cda.settings.UnknownConnectionException;

/**
 * This is the DataAccess implementation for PentahoReportingEngine based queries.
 * <p/>
 * User: pedro
 * Date: Feb 3, 2010
 * Time: 2:18:19 PM
 */
public abstract class PREDataAccess extends SimpleDataAccess
{

  private static final Log logger = LogFactory.getLog(PREDataAccess.class);

  private TableModel tableModel;
  private CachingDataFactory localDataFactory;

  public PREDataAccess(final Element element)
  {

    super(element);

  }


  public abstract DataFactory getDataFactory() throws UnknownConnectionException, InvalidConnectionException;

  @Override
  protected TableModel performRawQuery(final ParameterDataRow parameterDataRow) throws QueryException
  {
    try
    {

      final CachingDataFactory dataFactory = new CachingDataFactory(getDataFactory());

      final ResourceManager resourceManager = new ResourceManager();
      resourceManager.registerDefaults();
      final ResourceKey contextKey = getCdaSettings().getContextKey();


      dataFactory.initialize(ClassicEngineBoot.getInstance().getGlobalConfig(), resourceManager,
          contextKey,
          new LibLoaderResourceBundleFactory(resourceManager, contextKey, Locale.getDefault(), TimeZone.getDefault()));


      // fire the query. you always get a tablemodel or an exception.
      final TableModel tableModel = dataFactory.queryData("query", parameterDataRow);

      // Store this variable so that we can close it later
      setLocalDataFactory(dataFactory);

      return tableModel;

    }
    catch (UnknownConnectionException e)
    {
      throw new QueryException("Unknown connection", e);
    }
    catch (InvalidConnectionException e)
    {
      throw new QueryException("Unknown connection", e);
    }
    catch (ReportDataFactoryException e)
    {
      throw new QueryException("ReportDataFactoryException", e);
    }


  }

  public void closeDataSource() throws QueryException
  {

    if (localDataFactory == null)
    {
      return;
    }

    // and at the end, close your tablemodel if it holds on to resources like a resultset
    if (getTableModel() instanceof CloseableTableModel)
    {
      final CloseableTableModel ctm = (CloseableTableModel) getTableModel();
      ctm.close();
    }

    // and finally shut down the datafactory to free any connection that may be open.
    getLocalDataFactory().close();

    localDataFactory = null;
  }


  public TableModel getTableModel()
  {
    return tableModel;
  }

  public void setTableModel(final TableModel tableModel)
  {
    this.tableModel = tableModel;
  }

  public CachingDataFactory getLocalDataFactory()
  {
    return localDataFactory;
  }

  public void setLocalDataFactory(final CachingDataFactory localDataFactory)
  {
    this.localDataFactory = localDataFactory;
  }

  public ArrayList<DataAccessConnectionDescriptor> getDataAccessConnectionDescriptors() {
    ArrayList<DataAccessConnectionDescriptor> descriptor = new ArrayList<DataAccessConnectionDescriptor>();
    DataAccessConnectionDescriptor proto = new DataAccessConnectionDescriptor();
    proto.addDataAccessProperty(new PropertyDescriptor("Query",PropertyDescriptor.TYPE.STRING,PropertyDescriptor.SOURCE.DATAACCESS));
    descriptor.add(proto);
    return descriptor;
   }

  protected HashMap<String, String> getInterface() {
    HashMap<String, String> properties = super.getInterface();
    if (properties == null) {
      properties = new HashMap<String, String>();
    }
    properties.put("query", "");
    return properties;
  }
}

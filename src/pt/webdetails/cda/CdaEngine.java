package pt.webdetails.cda;

import java.io.OutputStream;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import pt.webdetails.cda.dataaccess.QueryException;
import pt.webdetails.cda.discovery.DiscoveryOptions;
import pt.webdetails.cda.exporter.ExporterEngine;
import pt.webdetails.cda.exporter.ExporterException;
import pt.webdetails.cda.exporter.UnsupportedExporterException;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.UnknownDataAccessException;
import pt.webdetails.cda.utils.SolutionRepositoryUtils;

/**
 * Main engine class that will answer to calls
 * <p/>
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 2:24:16 PM
 */
public class CdaEngine
{


  private static final Log logger = LogFactory.getLog(CdaEngine.class);
  private static CdaEngine _instance;


  public CdaEngine()
  {
    logger.info("Initializing CdaEngine");
    init();

  }


  public void doQuery(final OutputStream out,
                      final CdaSettings cdaSettings,
                      final QueryOptions queryOptions) throws UnknownDataAccessException, QueryException, UnsupportedExporterException, ExporterException
  {

    logger.debug("Doing query on CdaSettings [ " + cdaSettings.getId() + " (" + queryOptions.getDataAccessId() + ")]");

    TableModel tableModel = cdaSettings.getDataAccess(queryOptions.getDataAccessId()).doQuery(queryOptions);

    // Handle the exports

    ExporterEngine.getInstance().getExporter(queryOptions.getOutputType()).export(out, tableModel);

  }


  public void listQueries(final OutputStream out,
                          final CdaSettings cdaSettings,
                          final DiscoveryOptions discoveryOptions) throws UnsupportedExporterException, ExporterException
  {

    logger.debug("Getting list of queries on CdaSettings [ " + cdaSettings.getId() + ")]");


    final TableModel tableModel = cdaSettings.listQueries(discoveryOptions);

    // Handle the exports

    ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).export(out, tableModel);

  }


  public void listParameters(final OutputStream out,
                             final CdaSettings cdaSettings,
                             final DiscoveryOptions discoveryOptions) throws UnknownDataAccessException, UnsupportedExporterException, ExporterException
  {

    logger.debug("Getting list of queries on CdaSettings [ " + cdaSettings.getId() + ")]");


    final TableModel tableModel = cdaSettings.getDataAccess(discoveryOptions.getDataAccessId()).listParameters(discoveryOptions);

    // Handle the exports

    ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).export(out, tableModel);


  }

  public void getCdaList(final OutputStream out, final IPentahoSession userSession)
  {

    final TableModel tableModel = SolutionRepositoryUtils.getInstance().getCdaList(userSession);



  }


  private void init()
  {

    // Start ClassicEngineBoot
    CdaBoot.getInstance().start();
    ClassicEngineBoot.getInstance().start();

  }


  public static synchronized CdaEngine getInstance()
  {

    if (_instance == null)
    {
      _instance = new CdaEngine();
    }

    return _instance;
  }


}

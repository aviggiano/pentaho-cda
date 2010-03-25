package pt.webdetails.cda.dataaccess;

import java.util.ArrayList;
import javax.swing.table.TableModel;

import pt.webdetails.cda.discovery.DiscoveryOptions;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;

/**
 * DataAccess interface
 * <p/>
 * Created by IntelliJ IDEA.
 * User: pedro
 * Date: Feb 2, 2010
 * Time: 2:44:01 PM
 */
public interface DataAccess
{

  public String getId();

  public String getName();

  public String getType();

  public DataAccessEnums.ACCESS_TYPE getAccess();

  public boolean isCache();

  public int getCacheDuration();

  public CdaSettings getCdaSettings();

  public void setCdaSettings(CdaSettings cdaSettings);

  public TableModel doQuery(QueryOptions queryOptions) throws QueryException;

  public ColumnDefinition getColumnDefinition(int idx);

  public ArrayList<ColumnDefinition> getCalculatedColumns();

  public ArrayList<Integer> getOutputs();

  public TableModel listParameters(DiscoveryOptions discoveryOptions);

  public ArrayList<DataAccessConnectionDescriptor> getDataAccessConnectionDescriptors();

  public void storeDescriptor(DataAccessConnectionDescriptor descriptor);
}
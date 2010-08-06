/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.webdetails.cda.cache;

import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.cfg.Configuration;
import org.pentaho.platform.repository.hibernate.HibernateUtil;

import org.hibernate.Session;
import org.pentaho.platform.api.engine.IParameterProvider;
import org.pentaho.platform.api.engine.IPluginResourceLoader;
import org.pentaho.platform.engine.core.system.PentahoSystem;
import pt.webdetails.cda.CdaContentGenerator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.StandaloneSession;
import org.pentaho.platform.scheduler.SchedulerHelper;
import org.quartz.CronExpression;

/**
 *
 * @author pdpi
 */
public class CacheManager
{

  static Log logger = LogFactory.getLog(CacheManager.class);
  final String PLUGIN_PATH = PentahoSystem.getApplicationContext().getSolutionPath("system/" + CdaContentGenerator.PLUGIN_NAME);
  public static int DEFAULT_MAX_AGE = 3600;  // 1 hour
  PriorityQueue<CachedQuery> queue;

  enum functions
  {

    LIST, CHANGE, RELOAD, DELETE, PERSIST, MONITOR, DETAILS, TEST, EXECUTE, IMPORT
  }
  private static CacheManager _instance;


  public static synchronized CacheManager getInstance()
  {
    if (_instance == null)
    {
      _instance = new CacheManager();
    }
    return _instance;
  }


  public CacheManager()
  {
    initialize();
  }


  public void handleCall(IParameterProvider requestParams, OutputStream out)
  {
    String method = requestParams.getParameter("method").toString();
    try
    {
      switch (functions.valueOf(method.toUpperCase()))
      {
        case CHANGE:
          change(requestParams, out);
          break;
        case TEST:
          test(requestParams, out);
          break;
        case RELOAD:
          load(requestParams, out);
          break;
        case LIST:
          list(requestParams, out);
          break;
        case EXECUTE:
          execute(requestParams, out);
          break;
        case DELETE:
          delete(requestParams, out);
        case IMPORT:
          importQueries(requestParams, out);
      }
    }
    catch (Exception e)
    {
      logger.error(e);
    }
  }


  public void register(Query query)
  {
  }


  private void initialize()
  {
    try
    {
      initHibernate();
    }
    catch (Exception e)
    {
      // this stuff throws exceptions because of duplicate mappings. Moving swiftly on!
    }
    initQueue();
  }


  public void render(IParameterProvider requestParams, OutputStream out)
  {
  }


  public void test(IParameterProvider requestParams, OutputStream out)
  {
    CachedQuery cq = new CachedQuery();

    cq.setCdaFile("bi-developers/cda/cdafiles/xpath.cda");
    cq.setDataAccessId("1");
    cq.setCronString("10/15 0/2 * * * ?");
    cq.setExecuteAtStart(true);
    cq.save();
    queue.add(cq);
    CacheActivator.reschedule(queue);
    //initHibernate();
    //initQueue();
    //coldInit();
    //SchedulerHelper.createSimpleTriggerJob(PentahoSessionHolder.getSession(), "system", "cda/actions", "scheduler.xaction", "trigger", "cda", "", new Date(), null, 2, 10000);
  }


  private void load(IParameterProvider requestParams, OutputStream out) throws Exception
  {
    Long id = Long.decode(requestParams.getParameter("id").toString());
    Query q = loadQuery(id);
    if (q == null)
    {
      out.write("{}".getBytes("UTF-8"));
      logger.error("Couldn' get Query with id=" + id.toString());
    }
    try
    {
      JSONObject json = q.toJSON();
      out.write(json.toString(2).getBytes("UTF-8"));
    }
    catch (Exception e)
    {
      logger.error(e);
    }
  }


  public Query loadQuery(Long id)
  {
    Session s = getHibernateSession();

    return (Query) s.load(Query.class, id);
  }


  private void monitor(IParameterProvider requestParams, OutputStream out)
  {
    return; //NYI!
  }


  private void persist(IParameterProvider requestParams, OutputStream out) throws Exception
  {
    Long id = Long.decode(requestParams.getParameter("id").toString());
    Session s = getHibernateSession();
    UncachedQuery uq = (UncachedQuery) s.load(UncachedQuery.class, id);
    CachedQuery cq = uq.cacheMe();
    if (uq != null)
    {
      s.delete(s);
    }
    JSONObject json = uq.toJSON();
    out.write(json.toString(2).getBytes("UTF-8"));
  }


  public void called(String file, String id, Boolean hit)
  {
    return; //not implemented yet!
    /*
    Session s = getSession();
    Query q;
    List l = s.createQuery("from CachedQuery where cdaFile=? and dataAccessId=?") //
    .setString(0, file) //
    .setString(1, id) //
    .list();

    if (l.size() == 0)
    {
    // No results, create a new (uncached) query object.
    q = new UncachedQuery();
    }
    else if (l.size() == 1)
    {
    q = (Query) l.get(0);
    }
    else
    {
    q = (Query) l.get(0);
    // Find correct params set
    //
    }
    q.registerRequest(hit);
    s.save(q);
     */
  }


  private void change(IParameterProvider requestParams, OutputStream out) throws Exception
  {
    String jsonString = requestParams.getParameter("object").toString();
    JSONTokener jsonTokener = new JSONTokener(jsonString);
    try
    {
      Query q;
      JSONObject json = new JSONObject(jsonTokener);
      if (json.has("cronString"))
      {
        q = new CachedQuery(json);
        queue.add((CachedQuery) q);
        CacheActivator.reschedule(queue);
      }
      else
      {
        q = new UncachedQuery(json);
      }
      Session s = getHibernateSession();
      s.save(q);
      s.flush();
    }
    catch (JSONException jse)
    {
      out.write("".getBytes("UTF-8"));
    }


    out.write("".getBytes("UTF-8"));
  }


  private void list(IParameterProvider requestParams, OutputStream out)
  {
    JSONArray queries = new JSONArray();
    Session s = getHibernateSession();
    List l = s.createQuery("from CachedQuery").list();
    for (Object o : l)
    {
      queries.put(((Query) o).toJSON());
    }
    try
    {
      out.write(queries.toString(2).getBytes("UTF-8"));
    }
    catch (Exception e)
    {
      logger.error(e);
    }
  }


  private void importQueries(IParameterProvider requestParams, OutputStream out) throws Exception
  {
    String jsonString = requestParams.getParameter("object").toString();
    JSONTokener jsonTokener = new JSONTokener(jsonString);
    try
    {
      Query q;
      JSONObject json;
      Session s = getHibernateSession();
      JSONArray ja = new JSONArray(jsonTokener);
      for (int i = 0; i < ja.length(); i++)
      {
        json = ja.getJSONObject(i);
        if (json.has("cronString"))
        {
          q = new CachedQuery(json);
          queue.add((CachedQuery) q);
          CacheActivator.reschedule(queue);
        }
        else
        {
          q = new UncachedQuery(json);
        }
        s.save(q);
      }


      s.flush();
    }
    catch (JSONException jse)
    {
      out.write("".getBytes("UTF-8"));
    }
  }


  private void execute(IParameterProvider requestParams, OutputStream out)
  {
    Long id = Long.decode(requestParams.getParameter("id").toString());
    Session s = getHibernateSession();
    CachedQuery q = (CachedQuery) s.load(CachedQuery.class, id);

    if (q == null)
    {
      // Query doesn't exist or is not set for auto-caching
      return;
    }
    try
    {
      q.execute();
    }
    catch (Exception ex)
    {
      logger.error(ex);
    }
  }


  private void delete(IParameterProvider requestParams, OutputStream out)
  {
    Long id = Long.decode(requestParams.getParameter("id").toString());
    Session s = getHibernateSession();
    s.delete(s.load(Query.class, id));
    s.flush();
  }


  private Session getHibernateSession()
  {

    return HibernateUtil.getSession();

  }

  class SortByTimeDue implements Comparator<CachedQuery>
  {

    public int compare(CachedQuery o1, CachedQuery o2)
    {
      return (int) (o1.getNextExecution().getTime() - o2.getNextExecution().getTime());
    }
  }


  private void initQueue()
  {
    Session s = getHibernateSession();
    List l = s.createQuery("from CachedQuery").list();
    this.queue = new PriorityQueue<CachedQuery>(20, new SortByTimeDue());
    for (Object o : l)
    {
      CachedQuery cq = (CachedQuery) o;
      if (cq.getLastExecuted() == null)
      {
        cq.setLastExecuted(new Date(0L));
      }
      Date nextExecution;
      try
      {
        nextExecution = new CronExpression(cq.getCronString()).getNextValidTimeAfter(new Date());
      }
      catch (ParseException ex)
      {
        nextExecution = new Date(0);
        logger.error("Failed to schedule " + cq.toString());
      }
      cq.setNextExecution(nextExecution);
      this.queue.add(cq);
    }
  }


  public static void initHibernate()
  {

    // Get hbm file
    IPluginResourceLoader resLoader = PentahoSystem.get(IPluginResourceLoader.class, null);
    InputStream in = resLoader.getResourceAsStream(CdaContentGenerator.class, "cachemanager.hbm.xml");

    // Close session and rebuild
    HibernateUtil.closeSession();
    Configuration configuration = HibernateUtil.getConfiguration();
    //if (configuration.getClassMapping(CachedQuery.class.getCanonicalName()) == null)
    //{
    configuration.addInputStream(in);
    try
    {
      HibernateUtil.rebuildSessionFactory();
    }
    catch (Exception e)
    {
      return;
    }
    //}
  }


  /**
   * Initializes the CacheManager from a cold boot. Ensures all essential cached queries
   * are populated at boot time, and sets up the first query timer.
   */
  public void coldInit()
  {
    // run all queries
    Session s = getHibernateSession();
    List l = s.createQuery("from CachedQuery where executeAtStart = true").list();
    for (Object o : l)
    {
      CachedQuery cq = (CachedQuery) o;
      try
      {
        cq.execute();
      }
      catch (Exception ex)
      {
        logger.error("Error executing " + cq.toString() + ":" + ex.toString());
      }
    }

    CacheActivator.reschedule(queue);
  }


  /**
   * Re-initializes the CacheManager after. Should be called after a plug-in installation
   * at runtime, to ensure the query queue is kept consistent
   */
  public void hotInit()
  {
    return; //NYI
  }


  public PriorityQueue<CachedQuery> getQueue()
  {
    return queue;
  }
}

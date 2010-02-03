package pt.webdetails.cda.connections;

import org.dom4j.Element;

public class JdbcConnectionInfo {

  private String driver;
  private String url;
  private String user;
  private String pass;


  public JdbcConnectionInfo(final Element connection) {

    setDriver((String) connection.selectObject("string(./Driver)"));
    setUrl((String) connection.selectObject("string(./Url)"));
    setUser((String) connection.selectObject("string(./User)"));
    setPass((String) connection.selectObject("string(./Pass)"));

  }


  public String getDriver() {
    return driver;
  }

  public void setDriver(final String driver) {
    this.driver = driver;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(final String url) {
    this.url = url;
  }

  public String getUser() {
    return user;
  }

  public void setUser(final String user) {
    this.user = user;
  }

  public String getPass() {
    return pass;
  }

  public void setPass(final String pass) {
    this.pass = pass;
  }


}
Getting the list of CDA files on the server

http://127.0.0.1:8080/pentaho/content/cda/getCdaList?outputType=xml

Returns:

<?xml version="1.0" encoding="UTF-8"?>
<CdaExport>
  <MetaData>
    <ColumnMetaData index="0" type="String" name="name"/>
    <ColumnMetaData index="1" type="String" name="path"/>
  </MetaData>
  <ResultSet/>
</CdaExport>

<?xml version="1.0" encoding="UTF-8"?>
<CdaExport>
  <MetaData>
    <ColumnMetaData index="0" type="String" name="name"/>
    <ColumnMetaData index="1" type="String" name="path"/>
  </MetaData>
  <ResultSet>
    <Row>
      <Col>sample.cda</Col>
      <Col>/pentaho-solutions/steel-wheels/sample.cda</Col>
    </Row>
  </ResultSet>
</CdaExport>


http://127.0.0.1:8080/pentaho/content/cda/listQueries?solution=steel-wheels&path=&file=sample.cda&outputType=xml

<?xml version="1.0" encoding="UTF-8"?>
<CdaExport>
  <MetaData>
    <ColumnMetaData index="0" type="String" name="id"/>
    <ColumnMetaData index="1" type="String" name="name"/>
    <ColumnMetaData index="2" type="String" name="type"/>
  </MetaData>
  <ResultSet>
    <Row>
      <Col>2</Col>
      <Col></Col>
      <Col>mdx</Col>
    </Row>
    <Row>
      <Col>1</Col>
      <Col>Sql Query on SampleData</Col>
      <Col>sql</Col>
    </Row>
  </ResultSet>
</CdaExport>


http://127.0.0.1:8080/pentaho/content/cda/listParameters?solution=steel-wheels&&path=&file=sample.cda&outputType=xml&dataAccessId=2

<?xml version="1.0" encoding="UTF-8"?>
<CdaExport>
  <MetaData>
    <ColumnMetaData index="0" type="String" name="name"/>
    <ColumnMetaData index="1" type="String" name="type"/>
    <ColumnMetaData index="2" type="String" name="defaultValue"/>
    <ColumnMetaData index="3" type="String" name="pattern"/>
  </MetaData>
  <ResultSet>
    <Row>
      <Col>status</Col>
      <Col>String</Col>
      <Col>In Process</Col>
      <Col isNull="true"/>
    </Row>
  </ResultSet>
</CdaExport>



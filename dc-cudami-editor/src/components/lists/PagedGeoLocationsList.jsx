import React, {useEffect, useState} from 'react'
import {Card, CardBody, Col, Nav, Row, Table} from 'reactstrap'
import {useTranslation} from 'react-i18next'

import ActionButtons from './ActionButtons'
import LanguageTab from '../LanguageTab'
import Pagination from '../Pagination'
import {loadDefaultLanguage, typeToEndpointMapping} from '../../api'
import usePagination from '../../hooks/usePagination'

const PagedGeoLocationsList = ({apiContextPath = '/', mockApi = false}) => {
  const type = 'geoLocation'
  const {
    content: geoLocations,
    numberOfPages,
    pageNumber,
    setPageNumber,
    totalElements,
  } = usePagination(apiContextPath, mockApi, type)
  const [defaultLanguage, setDefaultLanguage] = useState('')
  useEffect(() => {
    loadDefaultLanguage(apiContextPath, mockApi).then((defaultLanguage) =>
      setDefaultLanguage(defaultLanguage)
    )
  }, [])
  const {t} = useTranslation()
  return (
    <>
      <Row>
        <Col>
          <h1>{t('geolocations')}</h1>
        </Col>
        <Col className="text-right"></Col>
      </Row>
      <Row>
        <Col>
          <hr />
        </Col>
      </Row>
      <Nav tabs>
        <LanguageTab
          activeLanguage={defaultLanguage}
          language={defaultLanguage}
          toggle={() => {}}
        />
      </Nav>
      <Card className="border-top-0">
        <CardBody>
          <Pagination
            changePage={({selected}) => setPageNumber(selected)}
            numberOfPages={numberOfPages}
            pageNumber={pageNumber}
            totalElements={totalElements}
            type={type}
          />
          <Table bordered className="mb-0" hover responsive size="sm" striped>
            <thead>
              <tr>
                <th className="text-center">{t('label')}</th>
                <th className="text-center">{t('description')}</th>
                <th className="text-center">{t('actions')}</th>
              </tr>
            </thead>
            <tbody>
              {geoLocations.map(({description, label, name, uuid}) => (
                <tr key={uuid}>
                  <td>{label?.[defaultLanguage]}</td>
                  <td>{description?.[defaultLanguage]}</td>
                  <td className="text-center">
                    <ActionButtons
                      editUrl={`${apiContextPath}${typeToEndpointMapping[type]}/${uuid}/edit`}
                      showEdit={false}
                      showView={false}
                    />
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
          <Pagination
            changePage={({selected}) => setPageNumber(selected)}
            numberOfPages={numberOfPages}
            pageNumber={pageNumber}
            position="under"
            showTotalElements={false}
            totalElements={totalElements}
            type={type}
          />
        </CardBody>
      </Card>
    </>
  )
}

export default PagedGeoLocationsList
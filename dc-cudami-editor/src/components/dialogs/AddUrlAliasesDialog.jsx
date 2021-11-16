import './AddUrlAliasesDialog.css'

import classNames from 'classnames'
import omit from 'lodash/omit'
import pick from 'lodash/pick'
import {subscribe, unsubscribe} from 'pubsub-js'
import {useContext, useEffect, useState} from 'react'
import {useTranslation} from 'react-i18next'
import {FaCheck, FaGlobe, FaLink, FaTrashAlt} from 'react-icons/fa'
import {
  Button,
  FormFeedback,
  Input,
  InputGroup,
  InputGroupAddon,
  ListGroup,
  ListGroupItem,
  ListGroupItemText,
  Modal,
  ModalBody,
  ModalFooter,
  ModalHeader,
} from 'reactstrap'

import {generateSlug, loadRootIdentifiables} from '../../api'
import AppContext from '../AppContext'
import Autocomplete from '../Autocomplete'
import CircleButton from '../CircleButton'
import FeedbackMessage from '../FeedbackMessage'
import {UrlAlias} from '../UrlAliases'

const AddUrlAliasesDialog = ({
  activeLanguage,
  existingUrlAliases = [],
  isOpen,
  onSubmit,
  parentWebsite,
  target,
  toggle,
}) => {
  const initialUrlAlias = {
    hasEmptySlug: true,
    isDuplicate: false,
    slug: '',
    targetLanguage: activeLanguage,
    website:
      parentWebsite &&
      pick(parentWebsite, ['entityType', 'type', 'url', 'uuid']),
    ...target,
  }
  const {apiContextPath} = useContext(AppContext)
  const [activeStep, setActiveStep] = useState(0)
  const [newUrlAlias, setNewUrlAlias] = useState(initialUrlAlias)
  const {t} = useTranslation()
  const destroy = () => {
    toggle()
    setActiveStep(0)
    setNewUrlAlias(initialUrlAlias)
  }
  const isInvalid = newUrlAlias.hasEmptySlug || newUrlAlias.isDuplicate
  const steps = [
    ...(parentWebsite
      ? []
      : [{Icon: FaGlobe, label: t('types:website'), name: 'website'}]),
    {Icon: FaLink, label: t('slug'), name: 'slug'},
    {Icon: FaCheck, name: 'confirm'},
  ]
  const stepName = steps[activeStep].name
  useEffect(() => {
    const token = subscribe('editor.show-add-urlaliases-dialog', () => {
      toggle()
    })
    return () => unsubscribe(token)
  }, [])
  useEffect(() => {
    setNewUrlAlias({...newUrlAlias, targetLanguage: activeLanguage})
  }, [activeLanguage])
  useEffect(() => {
    setNewUrlAlias({
      ...newUrlAlias,
      hasEmptySlug: !newUrlAlias.slug,
      isDuplicate: existingUrlAliases.some(
        ({slug, website}) =>
          slug === newUrlAlias.slug &&
          newUrlAlias.website?.uuid === website?.uuid,
      ),
    })
  }, [newUrlAlias.slug, newUrlAlias.website])
  return (
    <Modal isOpen={isOpen} size="lg" toggle={destroy}>
      <ModalHeader toggle={destroy}>{t('addUrlAlias')}</ModalHeader>
      <ModalBody>
        <ListGroup horizontal>
          {steps.map(({Icon, label, name}, idx) => {
            const enabled = idx <= activeStep
            return (
              <ListGroupItem
                className={classNames(
                  'align-items-center border-0 d-flex flex-column flex-fill pt-0 with-center-line',
                  {
                    enabled,
                  },
                )}
                key={name}
              >
                <CircleButton
                  disabled={!enabled}
                  onClick={() => setActiveStep(idx)}
                >
                  <Icon />
                </CircleButton>
                <ListGroupItemText className="mb-0">
                  {label ?? t(name)}
                </ListGroupItemText>
              </ListGroupItem>
            )
          })}
        </ListGroup>
        {stepName === 'website' && (
          <>
            <FeedbackMessage
              className="mb-2"
              message={{
                color: 'info',
                key: 'noWebsiteNeeded',
              }}
            />
            <Autocomplete
              activeLanguage={activeLanguage}
              maxElements={5}
              onSearch={(contextPath, searchTerm, pageNumber, pageSize) =>
                loadRootIdentifiables(
                  contextPath,
                  'website',
                  pageNumber,
                  pageSize,
                  searchTerm,
                )
              }
              onSelect={(website) => {
                setNewUrlAlias({
                  ...newUrlAlias,
                  website: pick(website, ['entityType', 'type', 'url', 'uuid']),
                })
                setActiveStep(activeStep + 1)
              }}
              placeholder={t('websiteSearchTerm')}
            />
          </>
        )}
        {stepName === 'slug' && (
          <>
            <InputGroup className="mb-1">
              <Input
                readOnly
                required
                type="url"
                value={newUrlAlias.website?.url ?? ''}
              />
              {newUrlAlias.website && !parentWebsite && (
                <InputGroupAddon addonType="append">
                  <Button
                    className="align-items-center d-flex px-1"
                    color="primary"
                    onClick={() => {
                      setNewUrlAlias({
                        ...newUrlAlias,
                        website: undefined,
                      })
                      setActiveStep(activeStep - 1)
                    }}
                    outline
                  >
                    <FaTrashAlt />
                  </Button>
                </InputGroupAddon>
              )}
            </InputGroup>
            <Input
              className="rounded"
              invalid={isInvalid}
              onChange={(evt) =>
                setNewUrlAlias({
                  ...newUrlAlias,
                  primary: !existingUrlAliases.some(
                    ({primary, website: ws}) =>
                      newUrlAlias.website?.uuid === ws?.uuid && primary,
                  ),
                  slug: evt.target.value,
                })
              }
              placeholder={t('slug')}
              value={newUrlAlias.slug}
            />
            {isInvalid && (
              <FormFeedback>
                {newUrlAlias.isDuplicate
                  ? t('feedback:noDuplicateSlugs')
                  : t('feedback:cannotBeEmpty')}
              </FormFeedback>
            )}
          </>
        )}
        {stepName === 'confirm' && (
          <ListGroup>
            <UrlAlias
              readOnly={true}
              primary={newUrlAlias.primary}
              slug={newUrlAlias.slug}
              url={newUrlAlias.website?.url}
            />
          </ListGroup>
        )}
      </ModalBody>
      <ModalFooter>
        <Button color="light" onClick={destroy}>
          {t('cancel')}
        </Button>
        {stepName === 'website' && (
          <Button color="primary" onClick={() => setActiveStep(activeStep + 1)}>
            {t('next')}
          </Button>
        )}
        {stepName === 'slug' && (
          <Button
            color="primary"
            disabled={isInvalid}
            onClick={async () => {
              const slug = await generateSlug(
                apiContextPath,
                activeLanguage,
                newUrlAlias.slug,
                newUrlAlias.website?.uuid,
              )
              setNewUrlAlias({...newUrlAlias, slug})
              setActiveStep(activeStep + 1)
            }}
          >
            {t('next')}
          </Button>
        )}
        {stepName === 'confirm' && (
          <Button
            color="primary"
            onClick={() => {
              onSubmit(omit(newUrlAlias, ['hasEmptySlug', 'isDuplicate']))
              destroy()
            }}
          >
            {t('add')}
          </Button>
        )}
      </ModalFooter>
    </Modal>
  )
}

export default AddUrlAliasesDialog
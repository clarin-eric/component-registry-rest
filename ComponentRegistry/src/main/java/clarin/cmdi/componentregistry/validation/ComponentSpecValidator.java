package clarin.cmdi.componentregistry.validation;

import clarin.cmdi.componentregistry.AuthenticationRequiredException;
import clarin.cmdi.componentregistry.ComponentRegistry;
import clarin.cmdi.componentregistry.ComponentRegistryException;
import clarin.cmdi.componentregistry.ItemNotFoundException;
import clarin.cmdi.componentregistry.MDMarshaller;
import clarin.cmdi.componentregistry.NullIdException;
import clarin.cmdi.componentregistry.RegistrySpace;
import clarin.cmdi.componentregistry.UserUnauthorizedException;
import clarin.cmdi.componentregistry.components.ComponentSpec;
import clarin.cmdi.componentregistry.components.ComponentType;
import clarin.cmdi.componentregistry.BaseDescription;
import clarin.cmdi.schema.cmd.ValidatorException;
import eu.clarin.cmdi.toolkit.CMDToolkit;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentSpecValidator extends BaseValidator {

    private final static Logger LOG = LoggerFactory.getLogger(ComponentSpecValidator.class);
    public static final String MISMATCH_ERROR = "Cannot register component as a profile or vica versa.";
    public static final String COMPONENT_NOT_REGISTERED_ERROR = "referenced component is not registered or does not have a correct componentId: ";
    public static final String PARSE_ERROR = "Error in validation input file: ";
    public static final String SCHEMA_ERROR = "Error in reading general component schema: ";
    public static final String VALIDATION_ERROR = "Error while validating file: ";
    public static final String IO_ERROR = "Error while reading specification or general component schema: ";
    public static final String INTERNAL_ERROR = "Internal error: ";
    public static final String COMPONENT_NOT_REGISTERED_IN_APPROPRIATE_SPACE_ERROR = "referenced component cannot be found in the appropriate registry components: ";
    public static final String COMPONENT_REGISTRY_EXCEPTION_ERROR = "An exception occurred while accessing the component registry: ";
    public static final String ILLEGAL_ATTRIBUTE_NAME_ERROR = "Illegal attribute name: ";
    public static final Collection<String> ILLEGAL_ATTRIBUTE_NAMES = Collections.unmodifiableCollection(Arrays.asList("ref", "ComponentId"));
    private ComponentSpec spec = null;
    private byte[] originalSpecBytes;
    private final InputStream input;
    private final BaseDescription description;
    private final ComponentRegistry registry;
    private final MDMarshaller marshaller;
    private boolean preRegistrationMode = true;

    /**
     *
     * @param input In order to validate the input is consumed. So use
     * @param marshaller
     * @see getComponentSpec to get the parsed ComponentSpec.
     * @param description
     * @param registry (registry you currently used)
     */
    public ComponentSpecValidator(InputStream input, BaseDescription description, ComponentRegistry registry, MDMarshaller marshaller) {
        this.input = input;
        this.description = description;
        this.registry = registry;
        this.marshaller = marshaller;
    }

    @Override
    public boolean validate() throws UserUnauthorizedException {
        try {
            // We may need to reuse the input stream, so save it to a byte array first
            originalSpecBytes = getBytesFromInputStream();
            final StreamSource source = new StreamSource(new ByteArrayInputStream(originalSpecBytes));
            final String schematronPhase = preRegistrationMode
                    ? CMDToolkit.SCHEMATRON_PHASE_CMD_COMPONENT_PRE_REGISTRATION
                    : CMDToolkit.SCHEMATRON_PHASE_CMD_COMPONENT_POST_REGISTRATION;

            final CMDValidateRunner validatorRunner = new CMDValidateRunner(source, schematronPhase) {
                @Override
                protected void handleError(String text) {
                    addErrorMessage(VALIDATION_ERROR + text);
                }
            };

            if (validatorRunner.validate()) {
                spec = unmarshalSpec(originalSpecBytes);
                if (spec.isIsProfile() != description.isProfile()) {
                    addErrorMessage(MISMATCH_ERROR);
                }
            }
        } catch (JAXBException e) {
            addErrorMessage(PARSE_ERROR + e.getMessage());
            LOG.error(PARSE_ERROR, e);
        } catch (ValidatorException e) {
            addErrorMessage(VALIDATION_ERROR + e.getMessage());
            LOG.error(VALIDATION_ERROR, e);
        } catch (IOException e) {
            addErrorMessage(IO_ERROR + e.getMessage());
            LOG.error(IO_ERROR, e);
        }
        if (!hasErrors()) {
            try {
                validateComponents(spec);
            } catch (ComponentRegistryException e) {
                addErrorMessage(COMPONENT_REGISTRY_EXCEPTION_ERROR + e);
            } catch (ItemNotFoundException | NullIdException e2) {
                addErrorMessage(COMPONENT_NOT_REGISTERED_ERROR + e2);
            } catch (AuthenticationRequiredException e) {
                addErrorMessage(INTERNAL_ERROR + e);
            }
        }
        return !hasErrors();
    }

    private byte[] getBytesFromInputStream() throws IOException {
        int len;
        byte[] b = new byte[4096];
        final ByteArrayOutputStream bOS = new ByteArrayOutputStream();

        while ((len = input.read(b)) > 0) {
            bOS.write(b, 0, len);
        }

        return bOS.toByteArray();
    }

    private void validateComponents(ComponentSpec componentSpec) throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException, NullIdException, AuthenticationRequiredException {
        validateComponents(Collections.singletonList(componentSpec.getComponent()));
    }

    private void validateComponents(List<ComponentType> cmdComponents) throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException, NullIdException, AuthenticationRequiredException {
        for (ComponentType cmdComponentType : cmdComponents) {
            this.validateDescribedComponents(cmdComponentType);
            this.validateComponents(cmdComponentType.getComponent());//Recursion
        }
    }

    private void validateDescribedComponents(ComponentType cmdComponentType) throws ComponentRegistryException, UserUnauthorizedException, ItemNotFoundException, NullIdException, AuthenticationRequiredException {
        if (isDefinedInSeparateFile(cmdComponentType)) {
            String id = cmdComponentType.getComponentRef();
            if (id == null) {
                String name = (cmdComponentType.getName() == null) ? "null" : cmdComponentType.getName();
                throw new NullIdException("The component with the name " + name + " has a null id. :(");
            }
            ComponentSpec registeredComponent = registry.getMDComponent(id);
            if (registeredComponent != null) {
                final String componentId = cmdComponentType.getComponentRef();
                if (registry.isItemPublic(id)) {  // if  a component is public, it is available for any registry
                    return;
                }
                // a private component for a private registry is available only if its owner is the owner of the resgitry
                if (RegistrySpace.PRIVATE.equals(registry.getRegistrySpace())) {
                    if (registry.canCurrentUserAccessDescription(componentId)) {
                        return;
                    }
                    addErrorMessage(COMPONENT_NOT_REGISTERED_IN_APPROPRIATE_SPACE_ERROR + componentId + " (private registry)");
                    return;
                } else { // a private component in a group registry is availabe only if it belongs to the group
                    if (RegistrySpace.GROUP.equals(registry.getRegistrySpace())) {
                        if (registry.getGroupId() == null) {
                            addErrorMessage(COMPONENT_REGISTRY_EXCEPTION_ERROR + "in the group space, the group id is null");
                            return;
                        }
                        List<Number> componentGroupIds = registry.getItemGroups(cmdComponentType.getComponentRef());
                        if (componentGroupIds.contains(registry.getGroupId())) {
                            return;
                        }
                        addErrorMessage(COMPONENT_NOT_REGISTERED_IN_APPROPRIATE_SPACE_ERROR + componentId + " (group registry) " + registry.getGroupId());
                        return;
                    }
                    addErrorMessage(COMPONENT_NOT_REGISTERED_IN_APPROPRIATE_SPACE_ERROR + componentId + " (private component in public registry).");
                    return;
                }
            }
            addErrorMessage(COMPONENT_NOT_REGISTERED_ERROR + cmdComponentType.getComponentRef());
        }

    }

    private boolean isDefinedInSeparateFile(ComponentType cmdComponentType) {
        return cmdComponentType.getComponentRef() != null && !cmdComponentType.getComponentRef().isBlank();
    }

    /**
     * Do not call before having called {@link #validate() }!
     *
     * @return the spec unmarshalled during {@link #validate() }. If this has
     * not been called, returns null.
     */
    public ComponentSpec getComponentSpec() {
        return spec;
    }

    /**
     * Creates a fresh (re-unmarshalled) copy of the specification this instance
     * has validated. If you are not going to alter this copy, you can re-use
     * and share the copy used during validation by getting it from {@link #getComponentSpec()
     * }. <em>Do not call before having called {@link #validate() }!</em>
     *
     * @return a freshly unmarshalled copy of the spec based on the bytes
     * collected from the input stream passed to {@link #validate() }. If this
     * has not been called, returns null.
     * @throws JAXBException exception occurred while marshalling from the input
     * bytes
     * @see #validate()
     * @see #getComponentSpec()
     */
    public ComponentSpec getCopyOfCMDComponentSpec() throws JAXBException {
        // Re-unmarshall original bytes
        return unmarshalSpec(originalSpecBytes);

    }

    private ComponentSpec unmarshalSpec(byte[] inputBytes) throws JAXBException {
        return marshaller.unmarshal(ComponentSpec.class, new ByteArrayInputStream(inputBytes), null);
    }

    /**
     *
     * @param preRegistrationMode whether to carry out validation in
     * pre-registration mode
     */
    public void setPreRegistrationMode(boolean preRegistrationMode) {
        this.preRegistrationMode = preRegistrationMode;
    }

    @Override
    public boolean runIfInvalid() {
        return true;
    }
}

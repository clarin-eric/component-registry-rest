package clarin.cmdi.componentregistry.rest;

import java.security.Principal;

import clarin.cmdi.componentregistry.ShhaaUserCredentials;

public final class DummyPrincipal implements Principal {

    public static final DummyPrincipal DUMMY_ADMIN_PRINCIPAL = new DummyPrincipal("JUnit.Admin@test.com");
    public static final DummyPrincipal DUMMY_PRINCIPAL = new DummyPrincipal("JUnit@test.com");
    public static final DummyPrincipal DUMMY_PRINCIPAL2 = new DummyPrincipal("M.Ock@test.com");
    public static final ShhaaUserCredentials DUMMY_CREDENTIALS = new ShhaaUserCredentials(DUMMY_PRINCIPAL) {

	@Override
	public String getDisplayName() {
	    return "J.Unit";
	}
    };
    public static final ShhaaUserCredentials DUMMY_ADMIN_CREDENTIALS = new ShhaaUserCredentials(DUMMY_ADMIN_PRINCIPAL);
    private final String username;

    public DummyPrincipal(String username) {
	this.username = username;
    }

    @Override
    public String getName() {
	return username;
    }

    public ShhaaUserCredentials getCredentials() {
	return new ShhaaUserCredentials(this);
    }
}

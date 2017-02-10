package ch.elexis.admin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.RoleConstants;
import ch.elexis.data.AbstractPersistentObjectTest;
import ch.elexis.data.Anwender;
import ch.elexis.data.Person;
import ch.elexis.data.Query;
import ch.elexis.data.Right;
import ch.elexis.data.Role;
import ch.elexis.data.User;
import ch.rgw.tools.JdbcLink;

public class RoleBasedAccessControlTest extends AbstractPersistentObjectTest {
	
	public RoleBasedAccessControlTest(JdbcLink link){
		super(link);
	}
	
	private static final String USERNAME = "user";
	private static final String PASSWORD = "password";
	
	private Anwender anwender;
	private User user;
	
	@Before
	public void before() throws Exception{
		User load = User.load(USERNAME);
		if (load != null && load.exists()) {
			anwender = load.getAssignedContact();
		} else {
			anwender = new Anwender(USERNAME, PASSWORD);
		}
		boolean succ = Anwender.login(USERNAME, PASSWORD);
		assertTrue(succ);
		user = (User) ElexisEventDispatcher.getSelected(User.class);
		assertNotNull(user);
		assertEquals(user.getAssignedContact().getId(), anwender.getId());
	}
	
	@After
	public void after(){
		anwender.delete();
	}
	
	@Test
	public void testInitialState(){
		User user = User.load(User.USERNAME_ADMINISTRATOR);
		assertNotNull("User Administrator is null", user);
		Person assignedContact = user.getAssignedContact();
		assertNotNull("No contact assoaciated to user administrator", assignedContact);
		boolean queryRightForUser = CoreHub.acl.request(user, AccessControlDefaults.AC_LOGIN);
		assertTrue("Administrator is denied login", queryRightForUser);
		
		List<Right> rights = new Query<Right>(Right.class).execute();
		assertEquals(40, rights.size());
		List<Role> roles = new Query<Role>(Role.class).execute();
		assertEquals(6, roles.size());
		Role ur = Role.load(RoleConstants.SYSTEMROLE_LITERAL_USER);
		ACE[] assignedUserRights = ur.getAssignedAccessRights();
		assertEquals(53, assignedUserRights.length);
	}
	
	@Test
	public void testUserAddWithOKRight(){
		Role ur = Role.load(RoleConstants.SYSTEMROLE_LITERAL_USER);
		assertNotNull(ur);
		List<Role> assignedRoles = user.getAssignedRoles();
		boolean userHasRole = false;
		for (Role role : assignedRoles) {
			if (ur.getId().equals(role.getId())) {
				userHasRole = true;
			}
		}
		assertTrue(userHasRole);
		
		CoreHub.acl.grant(ur, AccessControlDefaults.AC_EXIT);
		
		boolean roleHasRight =
			RoleBasedAccessControl.queryRightForRole(ur, AccessControlDefaults.AC_EXIT);
		assertTrue(roleHasRight);
		boolean userHasRight =
			RoleBasedAccessControl.queryRightForUser(user, AccessControlDefaults.AC_EXIT);
		assertTrue(userHasRight);
	}
	
	@Test
	public void testUserAddWithNonOKRight(){
		boolean rightFalse =
			RoleBasedAccessControl.queryRightForUser(user, AccessControlDefaults.ADMIN_ACE);
		assertFalse(rightFalse);
	}
	
	@Test
	public void testUserAddAndRevokeParentRightInvolvesChildRights(){
		Role userRole = Role.load(RoleConstants.SYSTEMROLE_LITERAL_USER);
		userRole.grantAccessRight(RoleBasedAccessControlTestACLContribution.parent);
		boolean rightTrue = RoleBasedAccessControl.queryRightForUser(user,
			RoleBasedAccessControlTestACLContribution.child1child1);
		assertTrue(rightTrue);
		userRole.revokeAccessRight(RoleBasedAccessControlTestACLContribution.parent);
		boolean rightFalse = RoleBasedAccessControl.queryRightForUser(user,
			RoleBasedAccessControlTestACLContribution.child1child1);
		assertFalse(rightFalse);
	}
	
	@Test
	public void testUserLock(){
		user.setActive(false);
		CoreHub.logoffAnwender();
		boolean rightFalse = Anwender.login(USERNAME, PASSWORD);
		assertFalse(rightFalse);
		
		// activate user again
		user.setActive(true);
	}
}

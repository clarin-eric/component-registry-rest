package clarin.cmdi.componentregistry.impl.database;

import clarin.cmdi.componentregistry.BaseUnitTest;
import clarin.cmdi.componentregistry.util.DatesHelper;
import clarin.cmdi.componentregistry.model.Comment;
import clarin.cmdi.componentregistry.persistence.jpa.CommentsDao;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 *
 * @author jean-charles Ferrières <jean-charles.ferrieres@mpi.nl>
 * @author George.Georgovassilis@mpi.nl
 */
public class CommentsDaoTest extends BaseUnitTest{

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private CommentsDao commentsDao;

    @Test
    public void testInjection() {
	assertNotNull(jdbcTemplate);
	assertNotNull(commentsDao);
    }

    @Test
    public void testInsertProfileComment() {
	Comment comment = createTestComment();
	comment.setComponentRef(TEST_COMMENT_PROFILE_ID);

	assertEquals(0, commentsDao.getAllComments().size());
	comment.setUserId(TEST_COMMENT_USER_ID);
	Number newId = Long.parseLong(commentsDao.saveAndFlush(comment).getId());
	assertNotNull(newId);

	List<Comment> comments = commentsDao.getAllComments();
	assertNotNull(comments);
	assertEquals(1, comments.size());

	assertEquals(TEST_COMMENT_NAME, comments.get(0).getComment());
	assertEquals(TEST_COMMENT_PROFILE_ID, comments.get(0).getComponentRef());
	assertEquals(TEST_COMMENT_DATE, comments.get(0).getCommentDate());
	assertEquals(TEST_COMMENT_USER_NAME, comments.get(0).getUserName());
	assertEquals(TEST_COMMENT_USER_ID, comments.get(0).getUserId());
    }

    @Test
    public void testInsertComponentComment() {
	Comment comment = createTestComment();
	comment.setComponentRef(TEST_COMMENT_COMPONENT_ID);

	assertEquals(0, commentsDao.getAllComments().size());
	
	comment.setUserId(TEST_COMMENT_USER_ID);
	Number newId = Long.parseLong(commentsDao.saveAndFlush(comment).getId());
	assertNotNull(newId);

	List<Comment> comments = commentsDao.getAllComments();
	assertNotNull(comments);
	assertEquals(1, comments.size());

	assertEquals(TEST_COMMENT_NAME, comments.get(0).getComment());
	assertEquals(TEST_COMMENT_COMPONENT_ID, comments.get(0).getComponentRef());
	assertEquals(TEST_COMMENT_DATE, comments.get(0).getCommentDate());
	assertEquals(TEST_COMMENT_USER_NAME, comments.get(0).getUserName());
	assertEquals(TEST_COMMENT_USER_ID, comments.get(0).getUserId());
    }

    @Test
    public void testGetById() {
	Comment comment1 = createTestComment();
	comment1.setComponentRef(TEST_COMMENT_PROFILE_ID);
	comment1.setUserId(TEST_COMMENT_USER_ID);
	Number commentId = Long.parseLong(commentsDao.saveAndFlush(comment1).getId());
	Comment commentById = commentsDao.findById(commentId.longValue()).orElseThrow();
	assertEquals(TEST_COMMENT_PROFILE_ID, commentById.getComponentRef());
	assertEquals(TEST_COMMENT_NAME, commentById.getComment());
    }

    @Test
    public void testGetCommentsFromProfile() {
	List<Comment> descriptions = commentsDao.getCommentsFromItem(TEST_COMMENT_PROFILE_ID);
	assertNotNull(descriptions);

	int size = descriptions.size();
	Comment comment1 = createTestComment();
	comment1.setComponentRef(TEST_COMMENT_PROFILE_ID);
	comment1.setUserId(TEST_COMMENT_USER_ID);
	commentsDao.save(comment1);

	descriptions = commentsDao.getCommentsFromItem(TEST_COMMENT_PROFILE_ID);
	assertEquals(size + 1, descriptions.size());

	Comment comment2 = createTestComment();
	comment2.setComponentRef(TEST_COMMENT_PROFILE_ID);
	comment2.setUserId(TEST_COMMENT_USER_ID);
	commentsDao.save(comment2);

	descriptions = commentsDao.getCommentsFromItem(TEST_COMMENT_PROFILE_ID);
	assertEquals(size + 2, descriptions.size());
    }

    @Test
    public void testGetCommentsFromComponent() {
	List<Comment> descriptions = commentsDao.getCommentsFromItem(TEST_COMMENT_COMPONENT_ID);
	assertNotNull(descriptions);

	int size = descriptions.size();
	Comment comment1 = createTestComment();
	comment1.setComponentRef(TEST_COMMENT_COMPONENT_ID);
	comment1.setUserId(TEST_COMMENT_USER_ID);
	commentsDao.saveAndFlush(comment1);

	descriptions = commentsDao.getCommentsFromItem(TEST_COMMENT_COMPONENT_ID);
	assertEquals(size + 1, descriptions.size());

	Comment comment2 = createTestComment();
	comment2.setComponentRef(TEST_COMMENT_COMPONENT_ID);
	comment2.setUserId(TEST_COMMENT_USER_ID);
	commentsDao.saveAndFlush(comment2);

	descriptions = commentsDao.getCommentsFromItem(TEST_COMMENT_COMPONENT_ID);
	assertEquals(size + 2, descriptions.size());
    }

    @Test
    public void testGetAllComments() {
	assertEquals(0, commentsDao.getAllComments().size());
    }

    @Test
    public void testGetComment() {
	Comment comment = createTestComment();
	comment.setUserId(8);
	commentsDao.saveAndFlush(comment);

	assertNotNull(commentsDao.getByComment(TEST_COMMENT_NAME));
	assertNull(commentsDao.getByComment("NON_EXISTENT_COMMENT_NAME"));
    }

    @Test
    public void testSetDelete() {
	Comment comment = createTestComment();
	int count = commentsDao.getAllComments().size();

	comment.setUserId(8);
	Number id = Long.parseLong(commentsDao.saveAndFlush(comment).getId());
	assertNotNull(id);
	assertEquals(count + 1, commentsDao.getAllComments().size());
	assertNotNull(commentsDao.getByComment(comment.getComment()));

	comment.setId(id.toString());
	commentsDao.delete(comment);
	assertEquals(count, commentsDao.getAllComments().size());

	assertNull(commentsDao.getByComment(comment.getComment()));
    }

    public static Comment createTestComment() {
	Comment testComment = new Comment();
	testComment.setComment(TEST_COMMENT_NAME);
	testComment.setCommentDate(TEST_COMMENT_DATE);
	testComment.setUserId(TEST_COMMENT_USER_ID);
	testComment.setUserName(TEST_COMMENT_USER_NAME);
	return testComment;
    }
    public final static String TEST_COMMENT_ID = "1";
    public final static String TEST_COMMENT_PROFILE_ID = "clarin.eu:cr1:p_1297242111880";
    public final static String TEST_COMMENT_COMPONENT_ID = "clarin.eu:cr1:c_1290431694600";
    public final static String TEST_COMMENT_NAME = "test";
    public final static int TEST_COMMENT_USER_ID = 8;
    public final static String TEST_COMMENT_USER_NAME = "J. Unit";
    public final static Date TEST_COMMENT_DATE = new Date();
}
